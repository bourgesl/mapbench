package org.gui;

import static it.geosolutions.java2d.MapConst.refResultDirectory;
import static it.geosolutions.java2d.MapConst.resultDirectory;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author bourgesl
 */
public final class ImageUtils {

    public static final int DCM_ALPHA_MASK = 0xff000000;

    private final static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    private ImageUtils() {
    }

    public static BufferedImage newImage(final int w, final int h) {
        return gc.createCompatibleImage(w, h);
    }

    public static BufferedImage copyImage(final BufferedImage srcImage) {
        return copyImage(srcImage, newImage(srcImage.getWidth(), srcImage.getHeight()));
    }

    public static BufferedImage copyImage(final BufferedImage srcImage, final BufferedImage destImage) {
        // Copy image to the output buffered image
        final Graphics2D g = destImage.createGraphics();
        try {
            g.drawImage(srcImage, 0, 0, null);
        } finally {
            g.dispose();
        }
        return destImage;
    }

    public static BufferedImage loadImage(final String imageFileName) throws IOException {
        final File imageFile = new File(refResultDirectory, imageFileName);
        if (!imageFile.canRead()) {
            return null;
        }
        final BufferedImage image = ImageIO.read(imageFile);
        // convert image to compatible image:
        return copyImage(image, newImage(image.getWidth(), image.getHeight()));
    }

    public static void saveImage(final BufferedImage image, final String imageFileName) throws IOException {
        final Iterator<ImageWriter> itWriters = ImageIO.getImageWritersByFormatName("PNG");
        if (itWriters.hasNext()) {
            final ImageWriter writer = itWriters.next();
            final ImageWriteParam writerParams = writer.getDefaultWriteParam();
            writerParams.setProgressiveMode(ImageWriteParam.MODE_DISABLED);

            final File imgFile = new File(resultDirectory, imageFileName);

            if (!imgFile.exists() || imgFile.canWrite()) {
                System.out.println("saveImage: saving image as PNG ...");
                imgFile.delete();

                // disable cache in temporary files:
                ImageIO.setUseCache(false);

                final long start = System.nanoTime();

                // use buffered output stream (64K):
                final ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(new BufferedOutputStream(new FileOutputStream(imgFile), 64 * 1024));

                writer.setOutput(imgOutStream);
                try {
                    writer.write(new IIOImage(image, null, null));
                } finally {
                    imgOutStream.close();

                    final long time = System.nanoTime() - start;
                    System.out.println("saveImage: duration= " + (time / 1000000l) + " ms.");
                }
            }
        }
    }

    public static BufferedImage computeDiffImage(final String imageFileName, final BufferedImage tstImage, final BufferedImage refImage) {
        if (tstImage == null) {
            System.out.println("computeDiffImage: test image is null !");
            return null;
        }
        if (refImage == null) {
            System.out.println("computeDiffImage: ref image is null !");
            return null;
        }
        final int width = tstImage.getWidth();
        final int height = tstImage.getHeight();

        if (width != refImage.getWidth() || height != refImage.getHeight()) {
            System.out.println("computeDiffImage: incompatible images [" + width + " x " + height + "]"
                    + " vs [" + refImage.getWidth() + " x " + refImage.getHeight() + "] !");
            return null;
        }

        final DataBuffer refDataBuffer = refImage.getRaster().getDataBuffer();
        final DataBuffer tstDataBuffer = tstImage.getRaster().getDataBuffer();

        if (refDataBuffer.getClass() != tstDataBuffer.getClass()) {
            System.out.println("computeDiffImage: incompatible data buffers [" + refDataBuffer + "]"
                    + " vs [" + tstDataBuffer + "] !");
            return null;
        }

        if (refDataBuffer instanceof DataBufferInt) {
            System.out.println("computeDiffImage: computing difference values ...");

            final long start = System.nanoTime();

            final BufferedImage diffImage = newImage(width, height);

            final int aRefPix[] = ((DataBufferInt) refDataBuffer).getData();
            final int aTstPix[] = ((DataBufferInt) tstDataBuffer).getData();
            final int aDifPix[] = ((DataBufferInt) diffImage.getRaster().getDataBuffer()).getData();

            final Histogram h = new Histogram("diff[" + imageFileName + "]");

            int dr, dg, db;
            for (int i = 0, len = aRefPix.length; i < len; i++) {
                dr = r(aRefPix[i]) - r(aTstPix[i]);
                dg = g(aRefPix[i]) - g(aTstPix[i]);
                db = b(aRefPix[i]) - b(aTstPix[i]);
                aDifPix[i] = toInt(clamp127(dr), clamp127(dg), clamp127(db));

                h.add((dr * dr + dg * dg + db * db) / 3);
            }

            final long time = System.nanoTime() - start;
            System.out.println("computeDiffImage: duration= " + (time / 1000000l) + " ms.");

            if (h.sum == 0l) {
                System.out.println("computeDiffImage: No difference for images: " + imageFileName);
                return null;
            }
            System.out.println("computeDiffImage: Histogram:\n" + h);

            return diffImage;
        }
        System.out.println("computeDiffImage: unsupported data buffer [" + refDataBuffer + "] !");
        return null;
    }

    public static int r(final int v) {
        return (v >> 16 & 0xff);
    }

    public static int g(final int v) {
        return (v >> 8 & 0xff);
    }

    public static int b(final int v) {
        return (v & 0xff);
    }

    public static int clamp127(final int v) {
        return (v < 128) ? (v > -127 ? (v + 127) : 0) : 255;
    }

    public static int toInt(final int r, final int g, final int b) {
        return DCM_ALPHA_MASK | (r << 16) | (g << 8) | b;
    }
    /* stats */

    static class StatInteger {

        public final String name;
        public long count = 0l;
        public long sum = 0l;
        public long min = Integer.MAX_VALUE;
        public long max = Integer.MIN_VALUE;

        StatInteger(String name) {
            this.name = name;
        }

        void reset() {
            count = 0l;
            sum = 0l;
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
        }

        void add(int val) {
            count++;
            sum += val;
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
        }

        void add(long val) {
            count++;
            sum += val;
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(128);
            toString(sb);
            return sb.toString();
        }

        public final StringBuilder toString(final StringBuilder sb) {
            sb.append(name).append('[').append(count);
            sb.append("] sum: ").append(sum).append(" avg: ").append(trimTo3Digits(((double) sum) / count));
            sb.append(" [").append(min).append(" | ").append(max).append("]");
            return sb;
        }

    }

    static class Histogram extends StatInteger {

        static int BUCKET = 2;
        static int MAX = 20;
        static int LAST = MAX - 1;
        static int[] STEPS = new int[MAX];

        static {
            STEPS[0] = 1;

            for (int i = 1; i < MAX; i++) {
                STEPS[i] = STEPS[i - 1] * BUCKET;
            }
            STEPS[0] = 0;

//            System.out.println("Histogram.STEPS = " + Arrays.toString(STEPS));
        }

        static int bucket(int val) {
            for (int i = 1; i < MAX; i++) {
                if (val < STEPS[i]) {
                    return i - 1;
                }
            }
            return LAST;
        }

        private StatInteger[] stats = new StatInteger[MAX];

        Histogram(String name) {
            super(name);
            for (int i = 0; i < MAX; i++) {
                stats[i] = new StatInteger(String.format("%5s .. %5s", STEPS[i], ((i + 1 < MAX) ? STEPS[i + 1] : "~")));
            }
        }

        @Override
        final void reset() {
            super.reset();
            for (int i = 0; i < MAX; i++) {
                stats[i].reset();
            }
        }

        @Override
        final void add(int val) {
            super.add(val);
            stats[bucket(val)].add(val);
        }

        @Override
        final void add(long val) {
            add((int) val);
        }

        @Override
        public final String toString() {
            final StringBuilder sb = new StringBuilder(2048);
            super.toString(sb).append(" { ");

            for (int i = 0; i < MAX; i++) {
                if (stats[i].count != 0l) {
                    sb.append("\n        ").append(stats[i].toString());
                }
            }

            return sb.append(" }").toString();
        }
    }

    /**
     * Adjust the given double value to keep only 3 decimal digits
     * @param value value to adjust
     * @return double value with only 3 decimal digits
     */
    public static double trimTo3Digits(final double value) {
        return ((long) (1e3d * value)) / 1e3d;
    }

}
