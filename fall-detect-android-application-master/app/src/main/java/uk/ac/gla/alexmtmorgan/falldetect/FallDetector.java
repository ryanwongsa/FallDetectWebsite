package uk.ac.gla.alexmtmorgan.falldetect;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by alexmtmorgan on 30/12/2015.
 */
public class FallDetector implements Runnable {

    private static final String TAG = ".FallDetector";

    private static final float FALLING_G_FORCE = 0.6F;
    private static final float LANDED_G_FORCE = 1.5F;
    private static final float RESTING_G_FORCE = 1F;
    private static final float MEAN_G_FORCE_DEVIATION = 0.2F;

    private static final long ONE_SECOND = 1000L;

    private Context mContext;
    private RequestQueueContainer mRequestQueueContainer;

    private final PriorityBlockingQueue<AccelerometerNode> inputQueue;
    private final ConcurrentLinkedQueue<String> messageQueue;

    public FallDetector(PriorityBlockingQueue<AccelerometerNode> inputQueue,
                        ConcurrentLinkedQueue<String> messageQueue,
                        Context mContext) {
        this.inputQueue = inputQueue;
        this.messageQueue = messageQueue;
        this.mContext = mContext;
        this.mRequestQueueContainer = RequestQueueContainer.getInstance(this.mContext);
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //noinspection InfiniteLoopStatement
        for(;;) {
            try {
                // get most recent node
                AccelerometerNode node = this.inputQueue.take();

                /*
                 * if gForce below 0.6, timer for 1 second to check gforce > 1.5
                 */
                float gForce = node.getgForce();

                boolean landed = false;

                if(gForce < FALLING_G_FORCE) {

                    long startTimer = System.currentTimeMillis();

                    while(System.currentTimeMillis() - startTimer < ONE_SECOND && !landed) {
                        node = this.inputQueue.take();

                        if(node.getgForce() > LANDED_G_FORCE) {
                            landed = true;
                        }
                    }

                    if(landed) {

                        int maybeGotUp = 0;
                        boolean gotUp = false;

                        startTimer = System.currentTimeMillis();
                        double runningTotal = 0, numOfNodes = 0, averageGForce = 0;

                        ArrayList<Float> allValues = new ArrayList<>();

                        while(System.currentTimeMillis() - startTimer < 3 * ONE_SECOND) {
                            node = this.inputQueue.take();
                            numOfNodes++;

                            allValues.add(node.getgForce());

                            runningTotal += node.getgForce();
                            averageGForce = runningTotal/numOfNodes;
                        }

                        for(Float force : allValues) {
                            if(force < FALLING_G_FORCE || force > LANDED_G_FORCE) {
                                if(++maybeGotUp > 3) {
                                    gotUp = true;
                                    break;
                                };
                            }
                        }

                        if(!gotUp && averageGForce > RESTING_G_FORCE - MEAN_G_FORCE_DEVIATION &&
                                averageGForce < RESTING_G_FORCE + MEAN_G_FORCE_DEVIATION) {

                            this.messageQueue.add("Fall has occured");
                        }
                    }

                    new Thread(new FallLogger(this.mRequestQueueContainer, this.mContext,
                            landed, false, false)).start();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
