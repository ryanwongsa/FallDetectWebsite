package uk.ac.gla.alexmtmorgan.falldetect;

import android.support.annotation.NonNull;

/**
 * Created by alexmtmorgan on 30/12/2015.
 */
public class AccelerometerNode implements Comparable<AccelerometerNode>{

    private float gForce;
    private Long timestamp;

    public AccelerometerNode(float gForce, Long timestamp) {
        super();

        this.gForce = gForce;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(@NonNull AccelerometerNode accelerometerNode) {
        return this.getTimestamp().compareTo(accelerometerNode.getTimestamp());
    }

    public float getgForce() {
        return this.gForce;
    }

    public void setgForce(float gForce) {
        this.gForce = gForce;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }
}
