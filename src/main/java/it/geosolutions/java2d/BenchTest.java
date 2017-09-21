/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package it.geosolutions.java2d;

/**
 *
 * @author bourgesl
 */
public class BenchTest extends BaseTest {

    /* debug settings */
    final static boolean showImage = false;
    final static boolean showImageIntermediate = false;

    /* profile settings */
    /* use shared image for all iterations or create 1 image per iteration */
    final static boolean useSharedImage = Profile.getBoolean(Profile.KEY_USE_SHARED_IMAGE);

    // constants:
    static final int PASS = Profile.getInteger(Profile.KEY_PASS);
    static final int ITER = Profile.getInteger(Profile.KEY_ITERATION);
    static final int MIN_LOOPS = Profile.getInteger(Profile.KEY_MIN_LOOPS);

    static final int MAX_THREADS = Profile.getInteger(Profile.KEY_MAX_THREADS);

    /** benchmark min duration per test */
    static double MIN_DURATION = Profile.getDouble(Profile.KEY_MIN_DURATION);

    /* constants */
    static final boolean doWarmup = true;
    static final boolean doWarmupEachTest = true;

    // before 200/200
    static final int WARMUP_LOOPS_MIN = Profile.getInteger(Profile.KEY_WARMUP_LOOPS_MIN);
    static final int WARMUP_LOOPS_MAX = 2 * WARMUP_LOOPS_MIN;

    static final int WARMUP_BEFORE_TEST_THREADS = 2;
    static final int WARMUP_BEFORE_TEST_MIN_LOOPS = Math.min(10, WARMUP_LOOPS_MIN);
    static final int WARMUP_BEFORE_TEST_MIN_DURATION = 3000;

    static final int CALIBRATE_LOOPS = 3000;

    static final int MAX_REAL_THREADS = Math.max(WARMUP_BEFORE_TEST_THREADS, MAX_THREADS);
    
    static void startTests() {
        System.out.println("# Min duration per test = " + MIN_DURATION + " ms.");
        System.out.printf("##############################################################\n");
    }

}
