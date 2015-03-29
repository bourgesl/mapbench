/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.lang.reflect.Method;

/**
 *
 * @author bourgesl
 */
public class BaseTest implements MapConst {

    /** base reference results directory */
    static File refResultDirectory = new File(baseRefResultDirectory, Profile.getProfileName());

    /** base test results directory */
    static File resultDirectory = new File(baseResultDirectory, Profile.getProfileName());

    protected BaseTest() {
        super();
    }

    protected static AffineTransform getViewTransform() {
        // reset:
        AffineTransform at = null;

        if (MapConst.doTranslate) {
            double translateX = MapConst.translate_X;
            double translateY = MapConst.translate_Y;
            // Affine transform:
            if (!((Math.abs(translateX) < 1e-3d) && (Math.abs(translateY) < 1e-3d))) {
                final AffineTransform txAt = AffineTransform.getTranslateInstance(translateX, translateY);
                if (at != null) {
                    txAt.concatenate(at);
                }
                at = txAt;
                System.out.println("translating[" + translateX + " x " + translateY + "]");
            }
        }
        if (MapConst.doScale) {
            double scaleX = MapConst.scales_X;
            double scaleY = MapConst.scales_Y;
            // Affine transform:
            if (!((Math.abs(scaleX - 1d) < 1e-3d) && (Math.abs(scaleY - 1d) < 1e-3d))) {
                final AffineTransform scaleAt = AffineTransform.getScaleInstance(scaleX, scaleY);
                if (at != null) {
                    scaleAt.concatenate(at);
                }
                at = scaleAt;
                System.out.println("scaling[" + scaleX + " x " + scaleY + "]");
            }
        }
        if (MapConst.doShear) {
            double shearX = MapConst.shear_X;
            double shearY = MapConst.shear_Y;
            // Affine transform:
            if (!((Math.abs(shearX - 1d) < 1e-3d) && (Math.abs(shearY - 1d) < 1e-3d))) {
                final AffineTransform shearAt = AffineTransform.getShearInstance(shearX, shearY);
                if (at != null) {
                    shearAt.concatenate(at);
                }
                at = shearAt;
                System.out.println("shearing[" + shearX + " x " + shearY + "]");
            }
        }
        if (MapConst.doRotate) {
            double angle = MapConst.rotationAngle;
            // Affine transform:
            if (!(Math.abs(angle) < 1e-3d)) {
                final AffineTransform rotateAt = AffineTransform.getRotateInstance(Math.toRadians(angle));
                if (at != null) {
                    rotateAt.concatenate(at);
                }
                at = rotateAt;
                System.out.println("rotating[" + angle + " deg]");
            }
        }
        return at;
    }

    private static boolean dumpStatsResolved = false;
    private static Method dumpStatsMethod = null;

    static void dumpRendererStats() {
        try {
            if (!dumpStatsResolved) {
                dumpStatsResolved = true;
                dumpStatsMethod = Class.forName("org.marlin.pisces.RendererStats").getMethod("dumpStats", null);
            }
            if (dumpStatsMethod != null) {
                // static method:
                dumpStatsMethod.invoke(null, null);
            }

        } catch (Exception ex) {
            // ignore
            // ex.printStackTrace();
        }
    }
}
