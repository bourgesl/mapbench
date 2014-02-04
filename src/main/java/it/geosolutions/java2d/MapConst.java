/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.io.File;

/**
 * Shared constants between MapDisplay and MapBench classes
 */
public interface MapConst {

    /** true to enable shape clipping before benchmark to render only visible (even partially) shapes */
    final static boolean doClip = Profile.getBoolean(Profile.KEY_DO_CLIP);
    /** true to perform shape scaling */
    final static boolean doScale = Profile.getBoolean(Profile.KEY_DO_SCALE);

    /** scaling factors (only first one is used by MapBench) */
    final static double[] scales = new double[]{Profile.getDouble(Profile.KEY_SCALE)};
//    final static double[] scales = new double[]{0.5d, 1d, 2d, 4d};

    /** input directory */
    static File inputDirectory = new File("../maps");

    /** result directory */
    static File resultDirectory = new File("../results/test");

    /** test file match */
    static final String testMatcher = ".*\\.ser";
//    static final String testMatcher = "dc_shp_alllayers_2013-00-30-07-00-47.ser";

}
