package uk.ac.gla.alexmtmorgan.falldetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class EthicsApprovalActivity extends Activity {

    private static final String TAG = ".EthicsApprovalActivity";

    private static final String ANDROID_TYPE = "EthicsApprovalActivity";

    private Context mContext;
    private RequestQueueContainer mRequestQueueContainer;

    private SharedPreferences sharedPreferences;

    private boolean agreedToEthics;

    private Button agreeButton;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethics_approval);

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        this.mContext = this.getApplicationContext();
        this.mRequestQueueContainer = RequestQueueContainer.getInstance(this);

        this.agreeButton = (Button)findViewById(R.id.agree_button);

        this.startTime = System.currentTimeMillis();
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

    public void onAgree(View view) {

        this.agreedToEthics = true;

        this.sharedPreferences
                .edit()
                .putBoolean(this.getString(R.string.pref_previously_started), Boolean.TRUE)
                .apply();

        Intent mServiceIntent = new Intent(this, AccelerometerService.class);
        this.startService(mServiceIntent);

        finish();
    }
}
