/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import it.geosolutions.java2d.ShapeDumpingGraphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import static java.awt.geom.Path2D.WIND_NON_ZERO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Simple Polyline rendering test using GeneralPath
 */
public class PolylineTests {

    public static void main(String[] args) {
        // First display which renderer is tested:
        // JDK9 only:
        System.setProperty("sun.java2d.renderer.verbose", "true");
        System.out.println("Testing renderer: ");
        // Other JDK:
        String renderer = "undefined";
        try {
            renderer = sun.java2d.pipe.RenderingEngine.getInstance().getClass().getName();
            System.out.println(renderer);
        } catch (Throwable th) {
            // may fail with JDK9 jigsaw (jake)
            if (false) {
                System.err.println("Unable to get RenderingEngine.getInstance()");
                th.printStackTrace();
            }
        }

        final boolean useDashes = false;
        final float lineStroke = 1f;

        BasicStroke stroke = createStroke(lineStroke, useDashes);

        final int size = 100;

        System.out.println("PolylineTests: size = " + size);

        for (int n = 100; n <= 500000; n *= 5) {
            final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            final Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2d.setClip(0, 0, size, size);

            g2d.setStroke(stroke);

            g2d.setBackground(Color.WHITE);
            g2d.setColor(Color.BLACK);

            final ShapeDumpingGraphics2D dumper = new ShapeDumpingGraphics2D(g2d, size, size,
                    new File("PolylineTests-" + n + ".ser"));

            final long start = System.nanoTime();

            g2d.clearRect(0, 0, size, size);

            paint(dumper, size, n);

            final long time = System.nanoTime() - start;

            System.out.println("paint: duration= " + (1e-6 * time) + " ms.");

            try {
                dumper.dispose();

                final File file = new File("PolylineTests-" + n + "-" + renderer + "-dash-" + useDashes + ".png");

                System.out.println("Writing file: " + file.getAbsolutePath());;
                ImageIO.write(image, "PNG", file);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void paint(final Graphics2D g2d, final int size, final int n) {

        final Path2D.Float path = new Path2D.Float(WIND_NON_ZERO, n);
        final Random rnd = new Random();

        for (int i = 0, x, y; i < n; i++) {
            x = rnd.nextInt(size);
            y = rnd.nextInt(size);

            if (i == 0) {
                path.moveTo(0f, 0f);
            } else {
                path.lineTo(x, y);
            }
        }
        System.out.println("draw : " + n + " lines.");
        g2d.draw(path);
    }

    private static BasicStroke createStroke(final float width, final boolean useDashes) {
        final float[] dashes;

        if (useDashes) {
            dashes = new float[8192];

            float cur, step = 0.1f;
            cur = step;
            for (int i = 0; i < dashes.length; i++) {
                dashes[i] = cur;
                cur += step;
            }
        } else {
            dashes = null;
        }

        return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dashes, 0.0f);
    }
}
