/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import static it.geosolutions.java2d.MapConst.HEADLESS;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JFrame;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;
import org.gui.ImageUtils.DiffContext;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class ImageDiffDisplay {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Locale.setDefault(Locale.US);

        if (args.length < 3) {
            System.err.println("Missing arguments: <dir> <ref.png> <other.png> (<diff.png>)");
            System.exit(1);
        }

        final File currentDir = new File(args[0]);
        System.out.println("Using directory = " + currentDir.getAbsolutePath());

        final File imgRef = new File(currentDir, args[1]);
        if (!imgRef.canRead() || !imgRef.getName().endsWith(".png")) {
            System.err.println("Invalid argument(1): " + imgRef);
            System.exit(1);
        }

        final File imgOther = new File(currentDir, args[2]);
        if (!imgOther.canRead() || !imgOther.getName().endsWith(".png")) {
            System.err.println("Invalid argument(2): " + imgOther);
            System.exit(1);
        }

        final DiffContext globalCtx = new DiffContext("all images");

        final File imgDiffFile = new File(currentDir, getImageFileName(imgRef, imgOther));

        showImage(imgRef, imgOther, imgDiffFile, globalCtx);

        globalCtx.dump();
    }

    public static void showImage(final File imgRef, final File imgOther, final File diffFile,
                                 final DiffContext globalCtx) throws IOException {
        // Use image copies:
        final BufferedImage refImage = ImageUtils.loadImage(imgRef);
        final BufferedImage tstImage = ImageUtils.loadImage(imgOther);

        /* compute image difference if possible */
        final BufferedImage diffImage = ImageUtils.computeDiffImage(diffFile.getName(), tstImage, refImage, globalCtx);

        if (diffImage != null) {
            ImageUtils.saveImage(diffImage, diffFile);
        }

        if (!HEADLESS) {
            // TODO: make mode silent : do not show GUI or exit at end => regression tests...
            final BigImageFrame frame = BigImageFrame.createAndShow(diffFile.getName(), tstImage, refImage, diffImage, true, true);
            frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    public static String getImageFileName(final File imgSrc, final File imgOther) {
        String nameSrc = imgSrc.getName();
        int pos = nameSrc.lastIndexOf(".");
        if (pos > 0) {
            nameSrc = nameSrc.substring(0, pos);
        }
        String nameOther = imgOther.getName();
        pos = nameOther.lastIndexOf(".");
        if (pos > 0) {
            nameOther = nameOther.substring(0, pos);
        }
        return "diff_" + nameSrc + "_vs_" + nameOther + ".png";
    }
}
