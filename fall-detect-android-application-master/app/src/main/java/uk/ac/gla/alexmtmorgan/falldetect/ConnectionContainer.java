package uk.ac.gla.alexmtmorgan.falldetect;

/**
 * Created by alexmtmorgan on 23/01/2016.
 */
public class ConnectionContainer {

    private static final String BASE_URL =
            "https://ec2-52-48-157-145.eu-west-1.compute.amazonaws.com/api/log";
    public static final String LOG_EVENT_URL = BASE_URL + "/events";

    public static final String EVENT_TYPE_FALL = "fall";
    public static final String EVENT_TYPE_LIFETIME = "lifetime";

    public static final String DEVICE_ID_JSON =
            "{ \"device\": { " +
                    "\"uuid\": \"%s\", " +
                    "\"model\": \"%s\", " +
                    "\"market_name\": \"%s\" " +
                    "}" +
            "}";

    public static final String EVENT_JSON =
            "{ " +
                    "\"type\": \"%s\"" +
            "}";

    public static final String FALL_JSON =
            "{ " +
                    "\"landed\": %b, " +
                    "\"confirmed\": %b, " +
                    "\"time_out\": %b " +
            "}";

    public static final String LIFETIME_JSON =
            "{ " +
                    "\"duration\": %.2f, " +
                    "\"type\": %s " +
            "}";
}