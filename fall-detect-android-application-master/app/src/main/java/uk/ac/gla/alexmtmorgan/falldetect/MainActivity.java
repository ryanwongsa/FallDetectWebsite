package uk.ac.gla.alexmtmorgan.falldetect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnTouchListener {

    private static final String TAG = ".MainActivity";

    private static final String ANDROID_TYPE = "MainActivity";

    private SharedPreferences sharedPreferences;

    private boolean agreedToEthics;

    private Context mContext;
    private RequestQueueContainer mRequestQueueContainer;

    private long startTime;

    private boolean serviceStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.agreedToEthics =
                this.sharedPreferences
                        .getBoolean(getString(R.string.pref_previously_started), false);

        if(!this.agreedToEthics) {
            this.showEthicsApproval();
        } else {
            Intent mServiceIntent = new Intent(this, AccelerometerService.class);
            this.startService(mServiceIntent);
        }

        final RelativeLayout footer = (RelativeLayout)findViewById(R.id.footer);
        footer.setOnTouchListener(this);

        this.mContext = this.getApplicationContext();
        this.mRequestQueueContainer = RequestQueueContainer.getInstance(this);

        this.startTime = System.currentTimeMillis();
    }

    private void showEthicsApproval() {
        startActivity(new Intent(MainActivity.this, EthicsApprovalActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();

        final double duration = (System.currentTimeMillis() - this.startTime) / 1000.0;

        if(this.agreedToEthics) {
            new Thread(new LifetimeLogger(this.mRequestQueueContainer, this.mContext,
                    ANDROID_TYPE, duration)).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isFinishing()) {
            Intent intent = new Intent(AccelerometerService.SERVICE);
            intent.setPackage(AccelerometerService.PACKAGE);

            stopService(intent);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Toast.makeText(this, "The app may be minimised", Toast.LENGTH_SHORT).show();

        return true;
    }


}
