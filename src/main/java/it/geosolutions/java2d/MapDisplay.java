/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

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

        File[] files = inputDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testMatcher);
            }
        });

        // Sort file names:
        Arrays.sort(files);

        long start;
        for (File file : files) {
            System.out.println("Loading DrawingCommands: " + file);
            DrawingCommands commands = DrawingCommands.load(file);

            for (int i = 0, len = (useAffineTransform) ? scales.length : 1; i < len; i++) {
                double scale = 1d;
                if (useAffineTransform) {
                    // Affine transform:
                    scale = scales[i];
                    if (Math.abs(scale - 1d) < 1e-3d) {
                        commands.setAt(null);
                    } else {
                        commands.setAt(AffineTransform.getScaleInstance(scale, scale));
                    }
                }
                System.out.println("drawing[" + file.getName() + "][width = " + commands.getWidth()
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
                System.out.println("duration[" + file.getName() + "] = " + (start / 1e6d) + " ms.");

                ImageIO.write(image, "PNG", new File(resultDirectory, file.getName() + ".png"));

                showImage(file.getName(), image);
            }
        }
    }

    public static void showImage(final String fileName, final BufferedImage image) {
        final ImageIcon icon = new ImageIcon(image);
        final JLabel label = new JLabel(icon);
        final JFrame frame = new JFrame();
        frame.setTitle(fileName);
        frame.setContentPane(new JScrollPane(label));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static BufferedImage deepCopyImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
