/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.io.File;

/**
 * Shared constants between MapDisplay and MapBench classes
 */
public interface MapConst {

    final static boolean doCreateStrokedShape = Profile.getBoolean(Profile.KEY_DO_CREATE_STROKED_SHAPE);
    
    /** true to use the even-odd winding rule */
    final static boolean doUseWingRuleEvenOdd = Profile.getBoolean(Profile.KEY_DO_WINDING_RULE_EVEN_ODD);

    /** true to enable shape clipping before benchmark to render only visible (even partially) shapes */
    final static boolean doClip = Profile.getBoolean(Profile.KEY_DO_CLIP);
    
    /** true to perform shape scaling */
    final static boolean doScale = Profile.getBoolean(Profile.KEY_DO_SCALE);
    /** scaling factors (only first one is used by MapBench) */
    final static double[] scales_X = new double[]{Profile.getDouble(Profile.KEY_SCALE_X)};
    final static double[] scales_Y = new double[]{Profile.getDouble(Profile.KEY_SCALE_Y)};
//    final static double[] scales_X = new double[]{0.5d, 1d, 2d, 4d};
//    final static double[] scales_Y = scales_X;
    
    /** true to perform shape shearing */
    final static boolean doShear = Profile.getBoolean(Profile.KEY_DO_SHEAR);
    final static double shear_X = Profile.getDouble(Profile.KEY_SHEAR_X);
    final static double shear_Y = Profile.getDouble(Profile.KEY_SHEAR_Y);
    
    /** true to perform shape shearing */
    final static boolean doRotate = Profile.getBoolean(Profile.KEY_DO_ROTATE);
    final static double rotationAngle = Profile.getDouble(Profile.KEY_ROTATE_ANGLE);
    
    /** input directory */
    static File inputDirectory = new File("../maps");

    /** reference results directory */
    static File refResultDirectory = new File("../results/ref");

    /** test results directory */
    static File resultDirectory = new File("../results/test");

    /** test file match */
    static final String testMatcher = ".*\\.ser";

}
