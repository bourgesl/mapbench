/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package org.gui;

import it.geosolutions.java2d.MapConst;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.VolatileImage;
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

    public static final boolean USE_GRAPHICS_ACCELERATION = MapConst.useAcceleration || MapConst.useVolatile;
    public static final boolean USE_VOLATILE = MapConst.useVolatile;

    private static final boolean NORMALIZE_DIFF = true;

    public static boolean SHOW_DIFF_INFO = true;

    private final static GraphicsConfiguration gc = (USE_GRAPHICS_ACCELERATION)
            ? GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration() : null;

    private ImageUtils() {
    }

    public static Image newFastImage(final int w, final int h) {
        if (USE_GRAPHICS_ACCELERATION) {
            return (USE_VOLATILE) ? gc.createCompatibleVolatileImage(w, h) : gc.createCompatibleImage(w, h);
        }
        return newImage(w, h);
    }

    public static BufferedImage newImage(final int w, final int h) {
        if (USE_GRAPHICS_ACCELERATION) {
            return gc.createCompatibleImage(w, h);
        }
        return new BufferedImage(w, h,
                (MapConst.premultiplied) ? BufferedImage.TYPE_INT_ARGB_PRE : BufferedImage.TYPE_INT_ARGB);
    }

    public static Graphics2D createGraphics(final Image img) {
        if (img instanceof BufferedImage) {
            return ((BufferedImage) img).createGraphics();
        }
        if (img instanceof VolatileImage) {
            return ((VolatileImage) img).createGraphics();
        }
        throw new IllegalStateException("image not supported: " + img);
    }

    public static BufferedImage convert(final Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        if (img instanceof VolatileImage) {
            final VolatileImage vol = (VolatileImage) img;
            return vol.getSnapshot();
        }
        throw new IllegalStateException("image not supported: " + img);
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

    public static BufferedImage loadImage(final File refDirectory, final String imageFileName) throws IOException {
        final File imageFile = new File(refDirectory, imageFileName);
        if (!imageFile.canRead()) {
            System.out.println("loadImage: missing image [" + imageFile + "].");
            return null;
        }
        final BufferedImage image = ImageIO.read(imageFile);
        // convert image to compatible image:
        return copyImage(image, newImage(image.getWidth(), image.getHeight()));
    }

    public static void saveImage(final BufferedImage image, final File resDirectory, final String imageFileName) throws IOException {
        final Iterator<ImageWriter> itWriters = ImageIO.getImageWritersByFormatName("PNG");
        if (itWriters.hasNext()) {
            final ImageWriter writer = itWriters.next();

            final ImageWriteParam writerParams = writer.getDefaultWriteParam();
            writerParams.setProgressiveMode(ImageWriteParam.MODE_DISABLED);

            final File imgFile = new File(resDirectory, imageFileName);

            if (!imgFile.exists() || imgFile.canWrite()) {
                System.out.println("saveImage: saving image as PNG [" + imgFile + "]...");
                imgFile.delete();

                // disable cache in temporary files:
                ImageIO.setUseCache(false);

                final long start = System.nanoTime();

                // PNG uses already buffering:
                final ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(new FileOutputStream(imgFile));

                writer.setOutput(imgOutStream);
                try {
                    writer.write(null, new IIOImage(image, null, null), writerParams);
                } finally {
                    imgOutStream.close();

                    final long time = System.nanoTime() - start;
                    System.out.println("saveImage: duration= " + (time / 1000000l) + " ms.");
                }
            }
        }
    }

    public static BufferedImage computeDiffImage(final String imageFileName, final BufferedImage tstImage, final BufferedImage refImage,
                                                 final DiffContext globalCtx) {
        return computeDiffImage(new DiffContext(imageFileName), tstImage, refImage, null, globalCtx);
    }

    public static BufferedImage computeDiffImage(final DiffContext localCtx, final BufferedImage tstImage, final BufferedImage refImage,
                                                 final BufferedImage diffImage, final DiffContext globalCtx) {
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
            if (SHOW_DIFF_INFO) {
                System.out.println("computeDiffImage: computing difference values ...");
            }

            final long start = System.nanoTime();

            // TODO: check diffImage size:
            final BufferedImage dImage = (diffImage != null) ? diffImage : newImage(width, height);

            final int aRefPix[] = ((DataBufferInt) refDataBuffer).getData();
            final int aTstPix[] = ((DataBufferInt) tstDataBuffer).getData();
            final int aDifPix[] = ((DataBufferInt) dImage.getRaster().getDataBuffer()).getData();

            // reset local diff context:
            localCtx.reset();

            final boolean useGrayScale = true;

            int dr, dg, db, v, max = 0;
            for (int i = 0, len = aRefPix.length; i < len; i++) {

                /* put condition out of loop */
                if (useGrayScale) {
                    // grayscale diff:
                    dg = (r(aRefPix[i]) + g(aRefPix[i]) + b(aRefPix[i]))
                            - (r(aTstPix[i]) + g(aTstPix[i]) + b(aTstPix[i]));

                    // max difference on grayscale values:
                    v = (int) Math.ceil(Math.abs(dg / 3.0));

                    if (v > max) {
                        max = v;
                    }
                    aDifPix[i] = toInt(v, v, v);

                } else {
                    dr = r(aRefPix[i]) - r(aTstPix[i]);
                    dg = g(aRefPix[i]) - g(aTstPix[i]);
                    db = b(aRefPix[i]) - b(aTstPix[i]);

                    if (dr > max) {
                        max = v;
                    }
                    if (dg > max) {
                        max = v;
                    }
                    if (db > max) {
                        max = v;
                    }
                    aDifPix[i] = toInt(clamp127(dr), clamp127(dg), clamp127(db));

                    // grayscale diff:
                    dg = (r(aRefPix[i]) + g(aRefPix[i]) + b(aRefPix[i]))
                            - (r(aTstPix[i]) + g(aTstPix[i]) + b(aTstPix[i]));

                    // max difference on grayscale values:
                    v = (int) Math.ceil(Math.abs(dg / 3.0));
                }

                localCtx.add(v);
                globalCtx.add(v);
            }

            if (SHOW_DIFF_INFO) {
                final long time = System.nanoTime() - start;
                System.out.println("computeDiffImage: duration= " + (time / 1000000l) + " ms.");
            }

            if (!localCtx.isDiff()) {
                return null;
            }
            if (SHOW_DIFF_INFO) {
                System.out.println("computeDiffImage: max delta: " + max);

                localCtx.dump();
            }

            if (NORMALIZE_DIFF) {
                /* normalize diff image vs mean(diff) */
                if ((max > 0) && (max < 255)) {
                    if (useGrayScale) {
                        final float factor = 255f / max;
                        for (int i = 0, len = aDifPix.length; i < len; i++) {
                            v = (int) Math.ceil(factor * b(aDifPix[i]));
                            aDifPix[i] = toInt(v, v, v);
                        }
                    } else {
                        System.out.println("TODO: normalize color image");
                    }
                }
            }

            return dImage;
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

        public final double average() {
            return ((double) sum) / count;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(128);
            toString(sb);
            return sb.toString();
        }

        public final StringBuilder toString(final StringBuilder sb) {
            sb.append(name).append("[n: ").append(count);
            sb.append("] sum: ").append(sum).append(" avg: ").append(trimTo3Digits(average()));
            sb.append(" [").append(min).append(" | ").append(max).append("]");
            return sb;
        }

    }

    public final static class Histogram extends StatInteger {

        static final int BUCKET = 2;
        static final int MAX = 20;
        static final int LAST = MAX - 1;
        static final int[] STEPS = new int[MAX];

        static {
            STEPS[0] = 0;
            STEPS[1] = 1;

            for (int i = 2; i < MAX; i++) {
                STEPS[i] = STEPS[i - 1] * BUCKET;
            }
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

        private final StatInteger[] stats = new StatInteger[MAX];

        public Histogram(String name) {
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

    public static final class DiffContext {

        public final Histogram histAll;
        public final Histogram histPix;

        public DiffContext(String name) {
            histAll = new Histogram("All  Pixels [" + name + "]");
            histPix = new Histogram("Diff Pixels [" + name + "]");
        }

        public void reset() {
            histAll.reset();
        }

        public void dump() {
            if (isDiff()) {
                System.out.println("Differences [" + histAll.name + "]:");
                System.out.println("Total [all pixels]:\n" + histAll.toString());
                System.out.println("Total [different pixels]:\n" + histPix.toString());
            } else {
                System.out.println("No difference for [" + histAll.name + "].");
            }
        }

        void add(int val) {
            histAll.add(val);
            if (val != 0) {
                histPix.add(val);
            }
        }

        boolean isDiff() {
            return histAll.sum != 0l;
        }
    }
}
