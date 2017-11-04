package test;

/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import it.geosolutions.java2d.BaseTest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;
import org.gui.ImageUtils.DiffContext;
import static org.gui.ImageUtils.trimTo3Digits;

/**
 * Shape rendering test comparing clipping On / Off
 */
public final class ClipShapeTests {

    static final boolean TEST_STROKER = true;
    static final boolean TEST_FILLER = true;

    // dump path on console:
    static final boolean DUMP_SHAPE = false;

    static final int NUM_TESTS = 10000;
    static final int TESTW = 100;
    static final int TESTH = 100;
    static final ShapeMode SHAPE_MODE = ShapeMode.NINE_LINE_POLYS;
    static final boolean SHAPE_REPEAT = true;

    static final boolean SHOW_DETAILS = true;
    static final boolean SHOW_FRAME = true;

    static final boolean SHOW_OUTLINE = true;
    static final boolean SHOW_POINTS = true;
    static final boolean SHOW_INFO = false;

    static final int MAX_SHOW_FRAMES = 10;

    static final double RAND_SCALE = 3.0;

    static final double RANDW = TESTW * RAND_SCALE;
    static final double OFFW = (TESTW - RANDW) / 2.0;

    static final double RANDH = TESTH * RAND_SCALE;
    static final double OFFH = (TESTH - RANDH) / 2.0;

    static enum ShapeMode {
        TWO_CUBICS,
        FOUR_QUADS,
        FIVE_LINE_POLYS,
        NINE_LINE_POLYS,
        FIFTY_LINE_POLYS,
        MIXED
    }

    static final long SEED = 1666133789L;
    static final Random RANDOM;

    static {
        // disable static clipping setting:
        System.setProperty("sun.java2d.renderer.clip", "false");
        System.setProperty("sun.java2d.renderer.clip.runtime.enable", "true");

        // Fixed seed to avoid any difference between runs:
        RANDOM = new Random(SEED);

        // Hide diff details:
        ImageUtils.SHOW_DIFF_INFO = false;

        BigImageFrame.DEF_SCALE = 800; // 800%
    }

    static final File diffDirectory = new File("..");

    /**
     * Test
     * @param unused
     */
    public static void main(String[] unused) {

        final String renderer = BaseTest.getRenderingEngineName();
        System.out.println("Testing renderer = " + renderer);

        System.out.println("ClipShapeTests: image = " + TESTW + " x " + TESTH);

        final long start = System.nanoTime();
        try {
            // TODO: test affine transforms ?
/*
            paintPaths(new TestSetup(SHAPE_MODE, true, 20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            paintPaths(new TestSetup(SHAPE_MODE, true, 20f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            paintPaths(new TestSetup(SHAPE_MODE, true, 1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    new float[]{1f, 2f}));
*/
            if (TEST_STROKER) {
                final float[][] dashArrays = new float[2][];
                dashArrays[0] = null;
                dashArrays[1] = new float[]{1f, 2f};

                // Stroker tests:
                for (float width = 0.1f; width < 110f; width *= 4f) {
                    for (int cap = 0; cap <= 2; cap++) {
                        for (int join = 0; join <= 2; join++) {
                            for (float[] dashes : dashArrays) {
                                paintPaths(new TestSetup(SHAPE_MODE, false, width, cap, join, dashes));
                                paintPaths(new TestSetup(SHAPE_MODE, true, width, cap, join, dashes));
                            }
                        }
                    }
                }
            }

            if (TEST_FILLER) {
                // Filler tests:
                paintPaths(new TestSetup(SHAPE_MODE, false, Path2D.WIND_NON_ZERO));
                paintPaths(new TestSetup(SHAPE_MODE, true, Path2D.WIND_NON_ZERO));

                paintPaths(new TestSetup(SHAPE_MODE, false, Path2D.WIND_EVEN_ODD));
                paintPaths(new TestSetup(SHAPE_MODE, true, Path2D.WIND_EVEN_ODD));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        final long time = System.nanoTime() - start;

        System.out.println("paint: duration= " + (1e-6 * time) + " ms.");
    }

    public static void paintPaths(final TestSetup ts) throws IOException {
        // Reset seed for random numbers:
        RANDOM.setSeed(SEED);

        System.out.println("paintPaths: " + NUM_TESTS + " paths (" + SHAPE_MODE + ") - setup: " + ts);

        final boolean fill = !ts.isStroke();
        final Path2D p2d = new Path2D.Double(ts.windingRule);

        final BufferedImage imgOn = ImageUtils.newImage(TESTW, TESTH);
        final Graphics2D g2dOn = initialize(imgOn, ts);

        final BufferedImage imgOff = ImageUtils.newImage(TESTW, TESTH);
        final Graphics2D g2dOff = initialize(imgOff, ts);

        final BufferedImage imgDiff = ImageUtils.newImage(TESTW, TESTH);

        final DiffContext globalCtx = new DiffContext("All tests");

        int nd = 0;
        try {
            final DiffContext testCtx = new DiffContext("Test");

            for (int n = 0; n < NUM_TESTS; n++) {
                genShape(p2d, ts);

                // Runtime clip setting OFF:
                paintShape(p2d, g2dOff, fill, false);

                // Runtime clip setting ON:
                paintShape(p2d, g2dOn, fill, true);

                /* compute image difference if possible */
                final BufferedImage diffImage = ImageUtils.computeDiffImage(testCtx, imgOn, imgOff, imgDiff, globalCtx);

                final String testName = "Setup_" + ts.id + "_test_" + n;

                if (diffImage != null) {
                    nd++;

                    final double ratio = (100.0 * testCtx.histPix.count) / testCtx.histAll.count;
                    System.out.println("Diff ratio: " + testName + " = " + trimTo3Digits(ratio) + " %");

                    if (false) {
                        ImageUtils.saveImage(diffImage, diffDirectory, testName + ".png");
                    }

                    if (DUMP_SHAPE) {
                        dumpShape(p2d);
                    }
                    if (SHOW_FRAME && nd < MAX_SHOW_FRAMES) {
                        if (SHOW_DETAILS) {
                            paintShapeDetails(g2dOff, p2d);
                            paintShapeDetails(g2dOn, p2d);
                        }

                        final BufferedImage cImgOn = ImageUtils.copyImage(imgOn);
                        final BufferedImage cImgOff = ImageUtils.copyImage(imgOff);
                        final BufferedImage cImgDiff = ImageUtils.copyImage(imgDiff);

                        final BigImageFrame frame = BigImageFrame.createAndShow(testName, cImgOn, cImgOff, cImgDiff, true, true);
                        frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setVisible(true);
                    }
                }
            }
        } finally {
            g2dOff.dispose();
            g2dOn.dispose();

            if (nd != 0) {
                System.out.println("paintPaths: " + NUM_TESTS + " paths - Number of differences = " + nd + " ratio = " + (100f * nd) / NUM_TESTS + " %");
            }

            globalCtx.dump();
        }
    }

    private static void paintShape(final Path2D p2d, final Graphics2D g2d, final boolean fill, final boolean clip) {
        reset(g2d);

        setClip(g2d, clip);

        if (fill) {
            g2d.fill(p2d);
        } else {
            g2d.draw(p2d);
        }
    }

    private static Graphics2D initialize(final BufferedImage img, final TestSetup ts) {
        final Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (ts.isStroke()) {
            g2d.setStroke(createStroke(ts));
        }
        g2d.setColor(Color.GRAY);

        return g2d;
    }

    private static void reset(final Graphics2D g2d) {
        // Disable antialiasing:
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, TESTW, TESTH);
    }

    private static void setClip(final Graphics2D g2d, final boolean clip) {
        // Enable antialiasing:
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Enable or Disable clipping:
        System.setProperty("sun.java2d.renderer.clip.runtime", (clip) ? "true" : "false");
    }

    static void genShape(final Path2D p2d, final TestSetup ts) {
        p2d.reset();
        
        final int end = (SHAPE_REPEAT) ? 2 : 1;
        
        for (int p = 0; p < end; p++) {
            p2d.moveTo(randX(), randY());

            switch (ts.shapeMode) {
                case MIXED:
                case FIFTY_LINE_POLYS:
                case NINE_LINE_POLYS:
                case FIVE_LINE_POLYS:
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    if (ts.shapeMode == ShapeMode.FIVE_LINE_POLYS) {
                        // And an implicit close makes 5 lines
                        break;
                    }
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    p2d.lineTo(randX(), randY());
                    if (ts.shapeMode == ShapeMode.NINE_LINE_POLYS) {
                        // And an implicit close makes 9 lines
                        break;
                    }
                    if (ts.shapeMode == ShapeMode.FIFTY_LINE_POLYS) {
                        for (int i = 0; i < 41; i++) {
                            p2d.lineTo(randX(), randY());
                        }
                        // And an implicit close makes 50 lines
                        break;
                    }
                case TWO_CUBICS:
                    p2d.curveTo(randX(), randY(), randX(), randY(), randX(), randY());
                    p2d.curveTo(randX(), randY(), randX(), randY(), randX(), randY());
                    if (ts.shapeMode == ShapeMode.TWO_CUBICS) {
                        break;
                    }
                case FOUR_QUADS:
                    p2d.quadTo(randX(), randY(), randX(), randY());
                    p2d.quadTo(randX(), randY(), randX(), randY());
                    p2d.quadTo(randX(), randY(), randX(), randY());
                    p2d.quadTo(randX(), randY(), randX(), randY());
                    if (ts.shapeMode == ShapeMode.FOUR_QUADS) {
                        break;
                    }
                default:
            }

            if (ts.closed) {
                p2d.closePath();
            }
        }
    }

    static final float POINT_RADIUS = 2f;
    static final float LINE_WIDTH = 1f;

    static final Stroke OUTLINE_STROKE = new BasicStroke(LINE_WIDTH);
    static final int COLOR_ALPHA = 128;
    static final Color COLOR_MOVETO = new Color(255, 0, 0, COLOR_ALPHA);
    static final Color COLOR_LINETO_ODD = new Color(0, 0, 255, COLOR_ALPHA);
    static final Color COLOR_LINETO_EVEN = new Color(0, 255, 0, COLOR_ALPHA);

    static final Ellipse2D.Float ELL_POINT = new Ellipse2D.Float();

    private static void paintShapeDetails(final Graphics2D g2d, final Shape shape) {

        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g2d.getColor();

        setClip(g2d, false);

        if (SHOW_OUTLINE) {
            g2d.setStroke(OUTLINE_STROKE);
            g2d.setColor(COLOR_LINETO_ODD);
            g2d.draw(shape);
        }

        final float[] coords = new float[6];
        float px, py;

        int nMove = 0;
        int nLine = 0;
        int n = 0;

        for (final PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    if (SHOW_POINTS) {
                        g2d.setColor(COLOR_MOVETO);
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    if (SHOW_POINTS) {
                        g2d.setColor((nLine % 2 == 0) ? COLOR_LINETO_ODD : COLOR_LINETO_EVEN);
                    }
                    nLine++;
                    break;
                case PathIterator.SEG_CLOSE:
                    continue;
                default:
                    System.out.println("unsupported segment type= " + type);
                    continue;
            }
            px = coords[0];
            py = coords[1];

            if (SHOW_INFO) {
                System.out.println("point[" + (n++) + "|seg=" + type + "]: " + px + " " + py);
            }

            if (SHOW_POINTS) {
                ELL_POINT.setFrame(px - POINT_RADIUS, py - POINT_RADIUS,
                        POINT_RADIUS * 2f, POINT_RADIUS * 2f);
                g2d.fill(ELL_POINT);
            }
        }
        if (SHOW_INFO) {
            System.out.println("Path moveTo=" + nMove + ", lineTo=" + nLine);
            System.out.println("--------------------------------------------------");
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
    }

    private static void dumpShape(final Shape shape) {
        final float[] coords = new float[6];

        for (final PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            final int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    System.out.println("p2d.moveTo(" + coords[0] + ", " + coords[1] + ");");
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.println("p2d.lineTo(" + coords[0] + ", " + coords[1] + ");");
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.println("p2d.closePath();");
                    break;
                default:
                    System.out.println("// Unsupported segment type= " + type);
            }
        }
        System.out.println("--------------------------------------------------");
    }

    public static double randX() {
        return RANDOM.nextDouble() * RANDW + OFFW;
    }

    public static double randY() {
        return RANDOM.nextDouble() * RANDH + OFFH;
    }

    private static BasicStroke createStroke(final TestSetup ts) {
        return new BasicStroke(ts.strokeWidth, ts.strokeCap, ts.strokeJoin, 10.0f, ts.dashes, 0.0f);
    }

    private final static class TestSetup {

        static final AtomicInteger COUNT = new AtomicInteger();

        final int id;
        final ShapeMode shapeMode;
        final boolean closed;
        // stroke
        final float strokeWidth;
        final int strokeCap;
        final int strokeJoin;
        final float[] dashes;
        // fill
        final int windingRule;

        TestSetup(ShapeMode shapeMode, final boolean closed,
                  final float strokeWidth, final int strokeCap, final int strokeJoin, final float[] dashes) {
            this.id = COUNT.incrementAndGet();
            this.shapeMode = shapeMode;
            this.closed = closed;
            this.strokeWidth = strokeWidth;
            this.strokeCap = strokeCap;
            this.strokeJoin = strokeJoin;
            this.dashes = dashes;
            this.windingRule = Path2D.WIND_NON_ZERO;
        }

        TestSetup(ShapeMode shapeMode, final boolean closed, final int windingRule) {
            this.id = COUNT.incrementAndGet();
            this.shapeMode = shapeMode;
            this.closed = closed;
            this.strokeWidth = 0f;
            this.strokeCap = this.strokeJoin = -1; // invalid
            this.dashes = null;
            this.windingRule = windingRule;
        }

        boolean isStroke() {
            return this.strokeWidth > 0f;
        }

        @Override
        public String toString() {
            return "TestSetup{id=" + id + ", shapeMode=" + shapeMode + ", closed=" + closed
                    + ", strokeWidth=" + strokeWidth + ", strokeCap=" + strokeCap + ", strokeJoin=" + strokeJoin
                    + ((dashes != null) ? ", dashes: " + Arrays.toString(dashes) : "")
                    + ", windingRule=" + windingRule + '}';
        }
    }
}
