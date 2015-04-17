/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JFrame;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;
import sun.java2d.pipe.RenderingEngine;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapDisplay extends BaseTest {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Locale.setDefault(Locale.US);

        // Prepare view transformation:
        final AffineTransform viewAT = getViewTransform();

        if (!inputDirectory.exists()) {
            System.out.println("Invalid input directory  = " + inputDirectory);
            System.exit(1);
        }
        if (!baseResultDirectory.exists()) {
            System.out.println("Invalid result directory = " + baseResultDirectory);
            System.exit(1);
        }
        if (!resultDirectory.exists()) {
            // create directory:
            resultDirectory.mkdir();
        }

        dumpInfo();

        System.out.println("Testing renderer = " + RenderingEngine.getInstance().getClass().getName());

        System.out.println("Loading maps from = " + inputDirectory.getAbsolutePath());
        System.out.println("Saving images to  = " + resultDirectory.getAbsolutePath());

        final File[] dataFiles = inputDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testMatcher);
            }
        });

        // Sort file names:
        Arrays.sort(dataFiles);

        long start;
        for (File dataFile : dataFiles) {
            System.out.println("Loading DrawingCommands: " + dataFile);
            DrawingCommands commands = DrawingCommands.load(dataFile);

            // set view transform once:
            commands.setAt(viewAT);

            System.out.println("drawing[" + dataFile.getName() + "][width = " + commands.getWidth()
                    + ", height = " + commands.getHeight() + "] ...");

            commands.prepareCommands(MapConst.doClip, MapConst.doUseWingRuleEvenOdd, PathIterator.WIND_EVEN_ODD);

            BufferedImage image = commands.prepareImage();
            Graphics2D graphics = commands.prepareGraphics(image);

            if (doGCBeforeTest) {
                cleanup();
            }

            start = System.nanoTime();
            commands.execute(graphics);
            start = System.nanoTime() - start;

            graphics.dispose();
            commands.dispose();

            System.out.println("duration[" + dataFile.getName() + "] = " + (start / 1e6d) + " ms.");

            // Marlin stats:
            dumpRendererStats();

            ImageUtils.saveImage(image, resultDirectory, getImageFileName(dataFile));

            showImage(dataFile.getName(), dataFile, image, false);
        }
    }

    public static void showImage(final String title, final File dataFile, final BufferedImage image, final boolean doCopy) throws IOException {
        // Use image copies:
        final BufferedImage refImage = ImageUtils.loadImage(refResultDirectory, getImageFileName(dataFile));
        final BufferedImage tstImage = (doCopy) ? ImageUtils.copyImage(image) : image;

        /* compute image difference if possible */
        final BufferedImage diffImage = ImageUtils.computeDiffImage(title, image, refImage);

        if (diffImage != null) {
            ImageUtils.saveImage(diffImage, resultDirectory, "diff_" + getImageFileName(dataFile));
        }

        final BigImageFrame frame = BigImageFrame.createAndShow(title, tstImage, refImage, diffImage);
        frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static String getImageFileName(final File dataFile) {
        return dataFile.getName() + ".png";
    }

    public static void dumpInfo() {
        final StringBuilder info = new StringBuilder(512);
        info.append("Java:     ").append(System.getProperty("java.runtime.version"));
        info.append("\nVM:       ").append(System.getProperty("java.vm.name"));
        info.append(' ').append(System.getProperty("java.vm.version"));
        info.append(' ').append(System.getProperty("java.vm.info"));

        info.append("\nOS:       ").append(System.getProperty("os.name"));
        info.append(' ').append(System.getProperty("os.version"));
        info.append(' ').append(System.getProperty("os.arch"));

        info.append("\nrenderer: ").append(RenderingEngine.getInstance().getClass().getName());
        info.append("\nprofile:  ").append(Profile.getProfileName());

        logTestInfo(info.toString());
    }

    public static void logTestInfo(String content) {
        File file = new File(resultDirectory, "test.log");
        Writer w = null;
        try {
            // Should define UTF-8 encoding for cross platform compatibility 
            // but we must stay compatible with existing files (windows vs unix)
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            w.write(content);

        } catch (final IOException ioe) {
            System.out.println("IO failure : ");
            ioe.printStackTrace();
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }
}
