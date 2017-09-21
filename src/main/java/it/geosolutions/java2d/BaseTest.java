/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author bourgesl
 */
public class BaseTest implements MapConst {

    /** base reference results directory */
    static final File refResultDirectory = getReferenceTestDirectory();

    /** base test results directory */
    static final File resultDirectory = new File(baseResultDirectory, getTestDirectory());

    static final boolean doGCBeforeTest = true;

    public static boolean isWarmup = false;
    
    private static String reName = null;
    
    protected BaseTest() {
        super();
    }

    private static File getReferenceTestDirectory() {
        File refResultDir = new File(baseRefResultDirectory, getTestDirectory());
        if (!refResultDir.isDirectory() && !refResultDir.exists()) {
            refResultDir = new File(baseRefResultDirectory, Profile.getProfileName());
        }
        return refResultDir;
    }
    
    private static String getTestDirectory() {
        final String profile = Profile.getProfileName();
        if ("MarlinRenderingEngine".equals(BaseTest.getRenderingEngineName())) {
            String subPixel_log2_X = System.getProperty("sun.java2d.renderer.subPixel_log2_X", "3");
            String subPixel_log2_Y = System.getProperty("sun.java2d.renderer.subPixel_log2_Y", "3");
            if (!"3".equals(subPixel_log2_X) && !"3".equals(subPixel_log2_Y)) {
                return profile + "_" + subPixel_log2_X + "x" + subPixel_log2_Y;
            }
        }
        return profile;
    }
    
    protected static File[] getSortedFiles() {
        if (!inputDirectory.exists()) {
            System.out.println("Invalid input directory = " + inputDirectory);
            System.exit(1);
        }

        System.out.println("Loading maps from = " + inputDirectory.getAbsolutePath());
        
        final File[] dataFiles = inputDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testMatcher);
            }
        });

        // Sort file names:
        Arrays.sort(dataFiles);
        
        return dataFiles;
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
        if (!dumpStatsResolved) {
            dumpStatsResolved = true;
            try {
                dumpStatsMethod = Class.forName("org.marlin.pisces.RendererStats").getMethod("dumpStats", null);
            } catch (Throwable th) {
                // ignore JDK9
                // th.printStackTrace();
            }
            if (dumpStatsMethod == null) {
                try {
                    dumpStatsMethod = Class.forName("sun.java2d.marlin.RendererStats").getMethod("dumpStats", null);
                } catch (Throwable th) {
                    // ignore JDK9
                    // th.printStackTrace();
                }
            }
        }
        if (dumpStatsMethod != null) {
            try {
                // static method:
                dumpStatsMethod.invoke(null, null);
            } catch (Throwable th) {
                // ignore JDK9
                // th.printStackTrace();
                // set to null to avoid later invocations:
                dumpStatsMethod = null;
            }
        }
    }

    /**
     * Cleanup (GC + pause)
     */
    static void cleanup() {
        final long freeBefore = Runtime.getRuntime().freeMemory();
        // Perform GC:
        System.gc();
        System.gc();
        System.gc();

        // pause for 500 ms :
        try {
            Thread.sleep(500l);
        } catch (InterruptedException ie) {
            System.out.println("thread interrupted");
        }
        final long freeAfter = Runtime.getRuntime().freeMemory();
        System.out.println(String.format("cleanup (explicit Full GC): %,d / %,d bytes free.", freeBefore, freeAfter));
    }
    
    public static String getRenderingEngineName() {
        if (reName == null) {
            try {
                reName = sun.java2d.pipe.RenderingEngine.getInstance().getClass().getSimpleName();
            } catch (Throwable th) {
                // may fail with JDK9 jigsaw (jake)
                System.err.println("ERROR: Unable to get RenderingEngine.getInstance()");
//                th.printStackTrace();
                reName = "unknown";
            }
        }
        return reName;
    }
}
