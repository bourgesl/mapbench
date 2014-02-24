/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFrame;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapDisplay implements MapConst {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if (!inputDirectory.exists()) {
            System.out.println("Invalid input directory  = " + inputDirectory);
            System.exit(1);
        }
        if (!resultDirectory.exists()) {
            System.out.println("Invalid result directory = " + resultDirectory);
            System.exit(1);
        }

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

            for (int i = 0, len = (doScale) ? scales.length : 1; i < len; i++) {
                double scale = 1d;
                if (doScale) {
                    // Affine transform:
                    scale = scales[i];
                    if (Math.abs(scale - 1d) < 1e-3d) {
                        commands.setAt(null);
                    } else {
                        commands.setAt(AffineTransform.getScaleInstance(scale, scale));
                    }
                }
                System.out.println("drawing[" + dataFile.getName() + "][width = " + commands.getWidth()
                        + ", height = " + commands.getHeight() + "] at scale = " + scale + "...");

                commands.prepareCommands(doClip);

                BufferedImage image = commands.prepareImage();
                Graphics2D graphics = commands.prepareGraphics(image);

                start = System.nanoTime();
                commands.execute(graphics);
                start = System.nanoTime() - start;

                graphics.dispose();
                commands.dispose();

                // Pisces stats:
//                sun.java2d.pisces.ArrayCache.dumpStats();
                System.out.println("duration[" + dataFile.getName() + "] = " + (start / 1e6d) + " ms.");

                ImageUtils.saveImage(image, getImageFileName(dataFile));

                showImage(dataFile.getName(), dataFile, image, false);
            }
        }
    }

    public static void showImage(final String title, final File dataFile, final BufferedImage image, final boolean doCopy) throws IOException {
        // Use image copies:
        final BufferedImage refImage = ImageUtils.loadImage(getImageFileName(dataFile));
        final BufferedImage tstImage = (doCopy) ? ImageUtils.copyImage(image) : image;
        final BigImageFrame frame = BigImageFrame.createAndShow(title, tstImage, refImage);
        frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static String getImageFileName(final File dataFile) {
        return dataFile.getName() + ".png";
    }
}
