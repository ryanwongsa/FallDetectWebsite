package uk.ac.gla.alexmtmorgan.falldetect;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alexmtmorgan on 10/12/2015.
 */
public class AccelerometerService extends Service implements SensorEventListener {

    private static final String TAG = ".AccelerometerService";

    public static final String PACKAGE =
            "uk.ac.gla.alexmtmorgan.falldetect";
    public static final String SERVICE =
            PACKAGE + TAG;

    private static final String ANDROID_TYPE = "Service";

    private static final int SERVICE_NOTIF_ID = 1;
    private static final int FALL_DETECTED_NOTIF_ID = 2;

    private static final long THIRTY_SECONDS = 30L;

    private ScheduledExecutorService scheduledExecutorForTimedLogger;
    private ConcurrentLinkedQueue<Future> waitingTasks;

    private Context mContext;
    private RequestQueueContainer mRequestQueueContainer;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private final LinkedBlockingQueue<SensorEvent> inputQueue;
    private final PriorityBlockingQueue<AccelerometerNode> outputQueue;
    private final ConcurrentLinkedQueue<String> messageQueue;

    private ExecutorService threadPool;
    private AccelerometerConsumer accelerometerConsumer;
    private FallDetector fallDetector;

    private NotificationManager mNotificationManager;
    private StringBuilder mNotificationSoundFileLocation;
    private Uri mSoundUri;
    private Notification mFallNotification;

    private Notification mServiceNotification;

    private long startTime = System.currentTimeMillis();

    public AccelerometerService() {
        super();

        this.inputQueue = new LinkedBlockingQueue<>();
        this.outputQueue = new PriorityBlockingQueue<>();
        this.messageQueue = new ConcurrentLinkedQueue<>();

        this.threadPool = Executors.newCachedThreadPool();
        this.scheduledExecutorForTimedLogger = Executors.newScheduledThreadPool(4);
        this.accelerometerConsumer = new AccelerometerConsumer(this.inputQueue, this.outputQueue);

        this.waitingTasks = new ConcurrentLinkedQueue<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mContext = this.getApplicationContext();
        this.mRequestQueueContainer = RequestQueueContainer.getInstance(this);

        this.fallDetector = new FallDetector(this.outputQueue, this.messageQueue, this.mContext);

        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if((this.mSensor = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) != null) {
            this.mSensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.w(TAG, "No accelerometer detected");
            return START_NOT_STICKY;
        }

        //Define Notification Manager
        this.mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //Construct the sound file notification uri
        this.mNotificationSoundFileLocation = new StringBuilder("android.resource://");
        this.mNotificationSoundFileLocation.append(
                this.mContext.getResources().getResourceName(R.raw.screaming_goat).replace(":","/"));
        //Define sound URI
        this.mSoundUri = Uri.parse(this.mNotificationSoundFileLocation.toString());

        //Create mFallNotification
        this.mFallNotification = this.getmFallNotification();

        //Create mServiceNotification
        this.mServiceNotification = this.getmServiceNotification();

        //Launch service notification to inform user that service is running
        this.mNotificationManager.notify(SERVICE_NOTIF_ID, this.mServiceNotification);

        this.threadPool.submit(this.accelerometerConsumer);
        this.threadPool.submit(this.fallDetector);

        Log.i(TAG, "Service started!");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.scheduledExecutorForTimedLogger.shutdownNow();

        final double duration = (System.currentTimeMillis() - this.startTime) / 1000.0;
        new Thread(new LifetimeLogger(this.mRequestQueueContainer, this.mContext,
                        ANDROID_TYPE, duration)).start();

        this.mSensorManager.unregisterListener(this);

        this.mNotificationManager.cancelAll();

        this.threadPool.shutdown();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            this.inputQueue.put(event);

            if(this.messageQueue.poll() != null) {

                //Display mFallNotification
                this.mNotificationManager.notify(FALL_DETECTED_NOTIF_ID, this.mFallNotification);

                this.newFallLoggerTask();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void newFallLoggerTask() {
        this.waitingTasks.add(this.scheduledExecutorForTimedLogger.schedule(new FallLogger(
                RequestQueueContainer.getInstance(this.mContext), this.mContext, true, false, true
        ), THIRTY_SECONDS, TimeUnit.SECONDS));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }

    private Notification getmFallNotification() {

        //convert icon to Bitmap for largeicon
        Bitmap largeFallDetectedNotifIcon =
                BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_notif_fall_detected);

        PendingIntent userOkayIntent =
                NotificationActivity.getUserOkayIntent(FALL_DETECTED_NOTIF_ID, this.mContext,
                        this.waitingTasks);

        //Create mFallNotification
        Notification mFallNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_fall_detected)
                .setLargeIcon(largeFallDetectedNotifIcon)
                .setContentTitle(getString(R.string.fall_detected_notif_title))
                .setContentText(getString(R.string.fall_detected_notif_message))
                .setPriority(Notification.PRIORITY_MAX)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI) //This sets the sound to play
                .addAction(R.drawable.ic_notif_action_confirm, getString(R.string.fall_detected_notif_user_okay), userOkayIntent)
                .build();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mFallNotification.category = Notification.CATEGORY_STATUS;
        }

        mFallNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        return mFallNotification;
    }

    private Notification getmServiceNotification() {

        //convert icon to Bitmap for largeicon
        Bitmap largeServiceNotifIcon =
                BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_notif_acc_service);

        Intent activityIntent = new Intent(this.mContext, MainActivity.class);

        PendingIntent openActivity = PendingIntent.getActivity(this.mContext, 0, activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Create mServiceNotification
        Notification mServiceNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_acc_service)
                .setLargeIcon(largeServiceNotifIcon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_notification_text))
                .setContentIntent(openActivity)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build(); //This sets the sound to play

        mServiceNotification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        return mServiceNotification;
    }
}
