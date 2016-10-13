package uk.ac.gla.alexmtmorgan.falldetect;

import android.hardware.SensorEvent;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by alexmtmorgan on 11/12/2015.
 */
public class AccelerometerConsumer implements Runnable {

    private static final String TAG = ".AccelerometerConsumer";

    private final LinkedBlockingQueue<SensorEvent> inputQueue;
    private final PriorityBlockingQueue<AccelerometerNode> outputQueue;

    public AccelerometerConsumer(LinkedBlockingQueue<SensorEvent> inputQueue,
                                 PriorityBlockingQueue<AccelerometerNode> outputQueue) {
        super();

        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //noinspection InfiniteLoopStatement
        for(;;) {
            try {
                SensorEvent event = this.inputQueue.take();

                float x,y,z;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                float acceleration = (float)Math.sqrt(x*x + y*y + z*z);

                float gForce = UnitConversion.accelerationToGForce(acceleration);

                this.outputQueue.put(new AccelerometerNode(gForce, event.timestamp));

            } catch (InterruptedException e) {
                Log.e(TAG,e.toString());
            }
        }
    }
}