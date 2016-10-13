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

/**
 * Created by alexmtmorgan on 27/01/2016.
 */
public class FallLogger implements Runnable {

    private static final String TAG = ".FallLogger";

    private RequestQueueContainer mRequestQueueContainer;
    private Context mContext;
    private boolean landed, confirmed, timeOut;

    public FallLogger(RequestQueueContainer mRequestQueueContainer, Context mContext,
                          boolean landed, boolean confirmed, boolean timeOut) {
        super();
        this.mRequestQueueContainer = mRequestQueueContainer;
        this.mContext = mContext;
        this.landed = landed;
        this.confirmed = confirmed;
        this.timeOut = timeOut;
    }

    @Override
    public void run() {
        try {

            Log.e(TAG, "Logging a fall...");

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
                            ConnectionContainer.EVENT_TYPE_FALL)
            );

            JSONObject fallJson = new JSONObject(
                    String.format(ConnectionContainer.FALL_JSON,
                            this.landed, this.confirmed, this.timeOut)
            );

            eventJson.put(ConnectionContainer.EVENT_TYPE_FALL, fallJson);

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
