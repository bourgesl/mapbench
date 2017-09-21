/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;

/**
 * Shared constants between MapDisplay and MapBench classes
 */
public interface MapConst {

    final static boolean skipDraw = Boolean.getBoolean("MapBench.skipDraw");
    final static boolean skipFill = Boolean.getBoolean("MapBench.skipFill");
    
    final static boolean useClipSmall = Boolean.getBoolean("MapBench.clip.small");

    final static boolean useClipDemo = true;
    
    // TODO: use profile property ?
    /* flag indicating to use BufferedImage.TYPE_INT_ARGB_PRE or BufferedImage.TYPE_INT_ARGB */
    final static boolean premultiplied = Boolean.getBoolean("MapBench.premultiplied"); // false by default

    final static boolean useAcceleration = Boolean.getBoolean("MapBench.acceleration"); // false by default

    final static boolean useVolatile = Boolean.getBoolean("MapBench.volatile"); // false by default
    
    final static boolean qualityMode = Boolean.getBoolean("MapBench.qualityMode");

    final static boolean filterSize = Boolean.getBoolean("MapBench.filter.size");
    
    final static Rectangle2D sizeRanges = (filterSize)
            ? new Rectangle2D.Double(
                    Profile.getDouble("MapBench.filter.minWidth", 0, 0, Double.POSITIVE_INFINITY),
                    Profile.getDouble("MapBench.filter.minHeight", 0, 0, Double.POSITIVE_INFINITY),
                    Profile.getDouble("MapBench.filter.maxWidth", Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY),
                    Profile.getDouble("MapBench.filter.maxHeight", Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY)
            ) : null;
    
    
    // TODO: use profile property ?
    final static boolean useMarlinGraphics2D = Boolean.getBoolean("MapBench.useMarlinGraphics2D");
    
    /** true to create stroked shape instead of draw(shape) and then fill(strokedShape) */
    final static boolean doCreateStrokedShape = Profile.getBoolean(Profile.KEY_DO_CREATE_STROKED_SHAPE);

    /** true to use the even-odd winding rule */
    final static boolean doUseWingRuleEvenOdd = Profile.getBoolean(Profile.KEY_DO_WINDING_RULE_EVEN_ODD);

    /** true to use dashed stroke instead of shape's stroke */
    final static boolean doUseDashedStroke = Profile.getBoolean(Profile.KEY_DO_USE_DASHED_STROKE);

    /** true to use gradient instead of shape's paint */
    final static boolean doUseGradient = Profile.getBoolean(Profile.KEY_DO_USE_GRADIENT);
    
    /** true to use texture paint instead of shape's paint */
    final static boolean doUseTexture = Profile.getBoolean(Profile.KEY_DO_USE_TEXTURE);
    
    /** true to enable shape clipping before benchmark to render only visible (even partially) shapes */
    final static boolean doClip = Profile.getBoolean(Profile.KEY_DO_CLIP);

    /** true to perform shape translating */
    final static boolean doTranslate = Profile.getBoolean(Profile.KEY_DO_TRANSLATE);
    final static double translate_X = Profile.getDouble(Profile.KEY_TRANSLATE_X);
    final static double translate_Y = Profile.getDouble(Profile.KEY_TRANSLATE_Y);

    /** true to perform shape scaling */
    final static boolean doScale = Profile.getBoolean(Profile.KEY_DO_SCALE);
    /** scaling factors */
    final static double scales_X = Profile.getDouble(Profile.KEY_SCALE_X);
    final static double scales_Y = Profile.getDouble(Profile.KEY_SCALE_Y);

    /** true to perform shape shearing */
    final static boolean doShear = Profile.getBoolean(Profile.KEY_DO_SHEAR);
    final static double shear_X = Profile.getDouble(Profile.KEY_SHEAR_X);
    final static double shear_Y = Profile.getDouble(Profile.KEY_SHEAR_Y);

    /** true to perform shape rotating */
    final static boolean doRotate = Profile.getBoolean(Profile.KEY_DO_ROTATE);
    final static double rotationAngle = Profile.getDouble(Profile.KEY_ROTATE_ANGLE);

    /** input directory */
    static File inputDirectory = new File("../maps");

    /** base reference results directory */
    static File baseRefResultDirectory = new File("../results/ref");

    /** base test results directory */
    static File baseResultDirectory = new File("../results/test");

    /** test file match */
    static final String testMatcher = ".*\\.ser";

    static final BasicStroke STROKE_THIN = new BasicStroke(1.0f);

    static final BasicStroke STROKE_DOTTED = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{3.0f}, 0.0f);

    static final AffineTransform IDENTITY = new AffineTransform();
}
