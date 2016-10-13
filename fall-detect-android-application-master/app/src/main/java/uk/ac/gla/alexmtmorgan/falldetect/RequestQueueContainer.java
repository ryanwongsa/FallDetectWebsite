package uk.ac.gla.alexmtmorgan.falldetect;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;

import uk.ac.gla.alexmtmorgan.falldetect.toolbox.ExtHttpClientStack;
import uk.ac.gla.alexmtmorgan.falldetect.toolbox.SslHttpClient;

/**
 * Created by alexmtmorgan on 18/01/2016.
 */
public class RequestQueueContainer {
    private static RequestQueueContainer mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private RequestQueueContainer(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueContainer getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueContainer(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {

            // fetch keystore
            InputStream keyStore = mContext.getResources().openRawResource(R.raw.server);

            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext(),
                    new ExtHttpClientStack(new SslHttpClient(keyStore, "4ndr01d-f411-d3t3ct")));
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}