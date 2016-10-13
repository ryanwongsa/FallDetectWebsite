package uk.ac.gla.alexmtmorgan.falldetect;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by alexmtmorgan on 27/01/2016.
 */
public class LifetimeLogger implements Runnable {

    private static final String TAG = ".LifetimeLogger";

    private RequestQueueContainer mRequestQueueContainer;
    private Context mContext;
    private String androidType;
    private double duration;

    public LifetimeLogger(RequestQueueContainer mRequestQueueContainer, Context mContext,
                          String androidType, double duration) {
        super();
        this.mRequestQueueContainer = mRequestQueueContainer;
        this.mContext = mContext;
        this.androidType = androidType;
        this.duration = duration;
    }

    @Override
    public void run() {
        try {
            JSONObject json = new JSONObject(
                    String.format(ConnectionContainer.DEVICE_ID_JSON,
                            Settings.Secure.getString(this.mContext.getContentResolver(),
                                    Settings.Secure.ANDROID_ID),
                            String.format("%s %s", Build.MANUFACTURER, Build.MODEL),
                            DeviceName.getDeviceName()
                    )
            );

            JSONObject eventJson = new JSONObject(
                    String.format(ConnectionContainer.EVENT_JSON,
                            ConnectionContainer.EVENT_TYPE_LIFETIME)
            );

            JSONObject lifetimeJson = new JSONObject(
                    String.format(Locale.UK, ConnectionContainer.LIFETIME_JSON,
                            duration, androidType)
            );

            eventJson.put(ConnectionContainer.EVENT_TYPE_LIFETIME, lifetimeJson);

            json.put("event", eventJson);

            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,
                    ConnectionContainer.LOG_EVENT_URL, json, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.wtf(TAG, "Response:\t:" + error.toString());
                        }
                    }
            );

            this.mRequestQueueContainer.addToRequestQueue(postRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
