/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
import it.geosolutions.java2d.ShapeDumpingGraphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Simple Rectangle fills test to evaluate Pisces / marlin / ductus renderers
 */
public class RectTests {

    private final static float MIN = 1f;
    private final static float MAX = 500f;
    private final static float STEP = 8f;

    private final static boolean FILL = true;

    private final static int MARGIN = 10;

    public static void main(String[] args) {
        final float lineStroke = 2f;

        BasicStroke stroke = (FILL) ? null : createStroke(lineStroke);

        final int size = (int)Math.ceil(MAX * 2f);

        final String renderer = BaseTest.getRenderingEngineName();
        System.out.println("Testing renderer = " + renderer);

        System.out.println("RectTests: size = " + size);

        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE);

        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Set normalization setting:
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setClip(0, 0, size, size);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, size, size);

        g2d.setColor(Color.BLACK);
        if (stroke != null) {
            g2d.setStroke(stroke);
        }

        ShapeDumpingGraphics2D dumper = new ShapeDumpingGraphics2D(g2d, size, size,
                new File("RectTests-fill-" + MIN + "-" + MAX + "-" + FILL + ".ser"));

        final long start = System.nanoTime();

        paint(dumper, size - 2 * MARGIN);

        final long time = System.nanoTime() - start;

        System.out.println("paint: duration= " + (1e-6 * time) + " ms.");

        try {
            dumper.dispose();

            final File file = new File("RectTests-" + renderer + "-fill-" + FILL + ".png");

            System.out.println("Writing file: " + file.getAbsolutePath());
            ImageIO.write(image, "PNG", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            g2d.dispose();
        }
    }

    private static void paint(final Graphics2D g2d, final float size) {

        final Rectangle2D.Double rect = new Rectangle2D.Double();

        final double maxRadius = MAX;
        final double half = size / 2f + MARGIN;

        boolean phase = true;

        for (double radius = maxRadius; radius >= MIN; radius -= STEP) {
            rect.setFrame(
                    Math.round(half - radius),
                    Math.round(half - radius),
                    Math.round(2.0 * radius),
                    Math.round(2.0 * radius)
            );

            if (FILL) {
                System.out.println("rect: " +rect);
                g2d.setColor((phase) ? Color.BLACK : Color.GRAY);
                g2d.fill(rect);
            } else {
                g2d.draw(rect);
            }
            phase = !phase;
        }
    }

    private static BasicStroke createStroke(final float width) {
        return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
    }
}
