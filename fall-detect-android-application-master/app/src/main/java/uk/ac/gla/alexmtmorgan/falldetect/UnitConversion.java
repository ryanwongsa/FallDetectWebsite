package uk.ac.gla.alexmtmorgan.falldetect;

/**
 * Created by alexmtmorgan on 03/01/2016.
 */
public class UnitConversion {

    private static final float G_FORCE_CONVERSION = 0.101971621f;

    public static float accelerationToGForce(float acceleration) {
        return acceleration * G_FORCE_CONVERSION;
    }

    public static float gForceToAcceleration(float gForce) {
        return gForce / G_FORCE_CONVERSION;
    }
}
