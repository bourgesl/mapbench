/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package test;

import it.geosolutions.java2d.ShapeDumpingGraphics2D;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Hashtable;
import javax.imageio.ImageIO;

/**
 * Simple Line rendering test using GeneralPath to enable Pisces / marlin / ductus renderers
 */
public class TextPageTest {

    private final static boolean DO_DUMP = true;

    private final static int N = (DO_DUMP) ? 1 : 50;

    private final static int FONT_SIZE = 12;

    private final static String FILE_NAME = "TextPageTest_";

    private static final TextState state;

    static {
        final Hashtable map = new Hashtable();
        map.put(TextAttribute.FAMILY, "SansSerif");
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Float(FONT_SIZE));

        final AttributedString text = new AttributedString(
                "Many people believe that Vincent van Gogh painted his best works "
                + "during the two-year period he spent in Provence. Here is where he "
                + "painted The Starry Night--which some consider to be his greatest "
                + "work of all. However, as his artistic brilliance reached new heights "
                + "in Provence, his physical and mental health plummeted. ",
                map);
        state = new TextState(text);
    }

    private final static int MARGIN = 8;
    private final static int WIDTH = 2000;

    public static void main(String[] args) {
        final int sqW = WIDTH + 2 * MARGIN;
        final int sqH = sqW;

        final int square = Math.max(sqW, sqH) + 2;

        System.out.println("Square[" + square + "] : " + sqW + " x " + sqH);

        final int width = square;
        final int height = square;

        System.out.println("TextPageTest: size = " + width + " x " + height);

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setClip(0, 0, width, height);

        final AffineTransform ident = g2d.getTransform();

        final ShapeDumpingGraphics2D dumper = (DO_DUMP) ? new ShapeDumpingGraphics2D(g2d, width, height,
                new File("TextPageTest.ser")) : null;

        final Graphics2D g2dPaint = (dumper != null) ? dumper : g2d;

        for (int n = 0; n < N; n++) {
            paint(g2dPaint, ident, width, height, square);
        }

        try {
            if (dumper != null) {
                dumper.dispose();
            }

            final File file = new File(FILE_NAME + ".png");

            System.out.println("Writing file: " + file.getAbsolutePath());;
            ImageIO.write(image, "PNG", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            g2d.dispose();
        }
    }

    private static void paint(final Graphics2D g2d, final AffineTransform ident,
                              final int width, final int height, final int square) {

        g2d.setTransform(ident);
        g2d.setBackground(Color.WHITE);
        g2d.setColor(Color.BLACK);
        g2d.clearRect(0, 0, width, height);

        final long start = System.nanoTime();

        paint(g2d, ident, square);

        final long time = (System.nanoTime() - start);
        System.out.println("paint: duration= " + (1e-6 * time) + " ms.");
    }

    private static void paint(final Graphics2D g2d, final AffineTransform ident, final int square) {
        // Set formatting width to width of Component.
        final float formatWidth = (float) (square - MARGIN);
        final float formatHeight = (float) (square - MARGIN);
        float drawPosY = 0.5f;

        // Create a new LineBreakMeasurer from the paragraph.
        final LineBreakMeasurer lineMeasurer = state.lineMeasurer;
        final int paragraphStart = state.paragraphStart;
        final int paragraphEnd = state.paragraphEnd;

        while (drawPosY < formatHeight) {

            lineMeasurer.setPosition(paragraphStart);

            // Get lines from lineMeasurer until the entire
            // paragraph has been displayed.
            while (lineMeasurer.getPosition() < paragraphEnd) {
                // Retrieve next layout.
                final TextLayout layout = lineMeasurer.nextLayout(formatWidth);

                // Move y-coordinate by the ascent of the layout.
                drawPosY += layout.getAscent();

                // Compute pen x position. If the paragraph is
                // right-to-left, we want to align the TextLayouts
                // to the right edge of the panel.
                float drawPosX;
                if (layout.isLeftToRight()) {
                    drawPosX = MARGIN + 0.5f;
                } else {
                    drawPosX = formatWidth - layout.getAdvance() + 0.5f;
                }

                // Draw the TextLayout at (drawPosX, drawPosY).
                if (false) {
                    layout.draw(g2d, drawPosX, drawPosY);
                } else {
                    final Shape s = layout.getOutline(null);

                    g2d.setTransform(ident);
                    g2d.translate(drawPosX, drawPosY);
                    g2d.fill(s);
                }

                // Move y-coordinate in preparation for next layout.
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            // System.out.println("drawPosY: " + drawPosY);
        }
    }

    private final static class TextState {

        final AttributedCharacterIterator paragraph;
        final int paragraphStart;
        final int paragraphEnd;

        // Create a new LineBreakMeasurer from the paragraph.
        final LineBreakMeasurer lineMeasurer;

        TextState(final AttributedString text) {
            paragraph = text.getIterator();
            paragraphStart = paragraph.getBeginIndex();
            paragraphEnd = paragraph.getEndIndex();

            // Create a new LineBreakMeasurer from the paragraph.
            lineMeasurer = new LineBreakMeasurer(paragraph, new FontRenderContext(null, true, true));
        }
    }
}
