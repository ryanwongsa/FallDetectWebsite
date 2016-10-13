package uk.ac.gla.alexmtmorgan.falldetect;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * Created by alexmtmorgan on 20/02/2016.
 */
public class NotificationActivity extends Activity {

    private static final String TAG = ".NotificationActivity";

    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    private static ConcurrentLinkedQueue<Future> userNotOkayTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(getIntent().getIntExtra(NOTIFICATION_ID, -1));

        if(NotificationActivity.userNotOkayTasks != null) {
            Log.e(TAG, "entered cancellation");
            while(!NotificationActivity.userNotOkayTasks.isEmpty()) {
                Log.e(TAG, "there are tasks to be cancelled");

                Log.e(TAG, "cancel worked: " + NotificationActivity.userNotOkayTasks.poll().cancel(true));
            }
        }

        finish(); // since finish() is called in onCreate(), onDestroy() will be called immediately
    }

    public static PendingIntent getUserOkayIntent(int notificationId, Context context, ConcurrentLinkedQueue<Future> userNotOkayTasks) {

        NotificationActivity.userNotOkayTasks = userNotOkayTasks;

        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // add the notification id
        intent.putExtra(NOTIFICATION_ID, notificationId);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}