/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.swing.JFrame;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;
import org.gui.ImageUtils.DiffContext;
import org.jfree.svg.SVGGraphics2D;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapDisplay extends BaseTest {

    private final static boolean EXPORT_SVG = true;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Locale.setDefault(Locale.US);

        if (!baseResultDirectory.exists()) {
            System.out.println("Invalid result directory = " + baseResultDirectory);
            System.exit(1);
        }
        if (!resultDirectory.exists()) {
            // create directory:
            resultDirectory.mkdir();
        }

        dumpInfo();

        final File[] dataFiles = getSortedFiles();

        System.out.println("Saving images to  = " + resultDirectory.getAbsolutePath());

        final DiffContext globalCtx = new DiffContext("all images");

        long time;
        for (File dataFile : dataFiles) {
            System.out.println("Loading DrawingCommands: " + dataFile);
            DrawingCommands commands = DrawingCommands.load(dataFile);

            // Prepare view transformation (scaling depends on image size):
            final AffineTransform viewAT = getViewTransform(commands);

            // set view transform once:
            commands.setAt(viewAT);

            System.out.println("drawing[" + dataFile.getName() + "][width = " + commands.getWidth()
                    + ", height = " + commands.getHeight() + "] ...");

            commands.prepareCommands(MapConst.doClip, MapConst.doUseWindingRule, MapConst.customWindingRule);

            final Image image = commands.prepareImage();
            final Graphics2D graphics = commands.prepareGraphics(image);

            if (doGCBeforeTest) {
                cleanup();
            }

            String svgDocument = null;

            try {
                time = System.nanoTime();

                commands.execute(graphics, null);

                time = System.nanoTime() - time;
                System.out.println("duration[" + dataFile.getName() + "] = " + (time / 1e6d) + " ms.");

                if (EXPORT_SVG) {
                    // Prepare SVG export:
                    final int w = image.getWidth(null);
                    final int h = image.getHeight(null);

                    final SVGGraphics2D svg2d = new SVGGraphics2D(w, h);

                    time = System.nanoTime();

                    commands.execute(svg2d, null);

                    time = System.nanoTime() - time;
                    System.out.println("duration[" + dataFile.getName() + "] = " + (time / 1e6d) + " ms.");
                    svgDocument = svg2d.getSVGDocument();
                }
            } catch (Throwable th) {
                System.out.println("Test failure :");
                th.printStackTrace(System.out);
            } finally {
                graphics.dispose();
                commands.dispose();
            }

            // Marlin stats:
            dumpRendererStats();

            final String imageFileName = getImageFileName(dataFile);

            // Save SVG:
            if (svgDocument != null) {
                final File svgFile = new File(resultDirectory, imageFileName.replace("png", "svg"));
                System.out.println("saving image as SVG [" + svgFile + "]...");

                final Writer out = new BufferedWriter(new FileWriter(svgFile));
                try {
                    out.write(svgDocument);
                } catch (IOException ioe) {
                    System.out.println("IO failure :");
                    ioe.printStackTrace(System.out);
                } finally {
                    out.close();
                }
            }

            // Save PNG:
            final BufferedImage bImg = ImageUtils.convert(image);

            ImageUtils.saveImage(bImg, resultDirectory, imageFileName);

            showImage(dataFile.getName(), dataFile, bImg, false, globalCtx);
        }

        globalCtx.dump();
    }

    public static void showImage(final String title, final File dataFile, final BufferedImage image, final boolean doCopy) throws IOException {
        showImage(title, dataFile, image, doCopy, null);
    }

    public static void showImage(final String title, final File dataFile, final BufferedImage image, final boolean doCopy,
                                 final DiffContext globalCtx) throws IOException {
        // Use image copies:
        final BufferedImage refImage = ImageUtils.loadImage(refResultDirectory, getImageFileName(dataFile));
        final BufferedImage tstImage = (doCopy) ? ImageUtils.copyImage(image) : image;

        /* compute image difference if possible */
        final BufferedImage diffImage = ImageUtils.computeDiffImage(title, image, refImage, globalCtx);

        if (diffImage != null) {
            ImageUtils.saveImage(diffImage, resultDirectory, "diff_" + getImageFileName(dataFile));
        }

        if (!HEADLESS) {
            // TODO: make mode silent : do not show GUI or exit at end => regression tests...
            final BigImageFrame frame = BigImageFrame.createAndShow(title, tstImage, refImage, diffImage, true, true);
            frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
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

        info.append("\nrenderer: ").append(BaseTest.getRenderingEngineName());
        info.append("\nprofile:  ").append(Profile.getProfileName());

        if (MapConst.useMarlinGraphics2D) {
            info.append("\nUsing MarlinGraphics2D ...");
        }

        if (HEADLESS) {
            info.append("\nHEADLESS: true");
        }

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
