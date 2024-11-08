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
import it.geosolutions.java2d.ShapeDumpingGraphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Simple Circle rendering test using GeneralPath to enable Pisces / marlin /
 * ductus renderers
 */
public class CircleTests {

    public static void main(String[] args) {
        final float lineStroke = 1f;

        BasicStroke stroke = createStroke(lineStroke);

        final int size = 2048;

        final String renderer = BaseTest.getRenderingEngineName();
        System.out.println("Testing renderer = " + renderer);

        System.out.println("CircleTests: size = " + size);

        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE);

        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // TODO: add an user preference for the normalization setting:
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setClip(0, 0, size, size);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, size, size);

        g2d.setStroke(stroke);
        g2d.setColor(Color.BLACK);

        ShapeDumpingGraphics2D dumper = new ShapeDumpingGraphics2D(g2d, size, size,
                new File("CircleTests.ser"));

        final long start = System.nanoTime();

        paint(dumper, size - 10f);

        final long time = System.nanoTime() - start;

        System.out.println("paint: duration= " + (1e-6 * time) + " ms.");

        try {
            dumper.dispose();

            final File file = new File("CircleTests-" + renderer + ".png");

            System.out.println("Writing file: " + file.getAbsolutePath());
            ImageIO.write(image, "PNG", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            g2d.dispose();
        }
    }

    private static void paint(final Graphics2D g2d, final float size) {

        final Ellipse2D.Double ellipse = new Ellipse2D.Double();

        double half = size / 2.0;
        final int N = 5000;

        double radius = half - 50.1;
        final double maxR = 31.1;
        final double stepR = 2.55 * maxR / N;
        double theta = 0.0;
        double dtheta = 7.6 / 180.0 * Math.PI;

        for (int i = 0; i <= N; i++) {
            theta += dtheta;
            double x = half + radius * Math.cos(theta);
            double y = half + radius * Math.sin(theta);
            double r = maxR - i * stepR;

            if (r <= 0) {
                System.out.println("r negative");
                break;
            }
            radius -= 0.49;
            if (radius <= 0) {
                System.out.println("radius negative");
                break;
            }

            ellipse.setFrame(x, y, 2.0 * r, 2.0 * r);
            g2d.draw(ellipse);
        }

        for (int i = 0; i <= 39; i++) {
            double r = 4.0;
            float thickness = (i + 1) / 10f;

            g2d.setStroke(createStroke(thickness));

            double x = 20 + i * 12.5 - r;
            double y = 16;

            ellipse.setFrame(x, y, 2.0 * r, 2.0 * r);
            g2d.draw(ellipse);
        }
    }

    private static BasicStroke createStroke(final float width) {
        return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
    }
}
