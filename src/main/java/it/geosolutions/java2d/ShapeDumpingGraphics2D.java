/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Map;

/*
TODO: wrap calls to draw(Shape) and fill(Shape)
*/
public class ShapeDumpingGraphics2D extends Graphics2D {

    private final static boolean DEBUG = false;
    private final static boolean FORCE_USE_PATH2D = false;
    
    Graphics2D delegate;
    ObjectOutputStream oos;
    int width;
    int height;
    ArrayList<DrawingCommand> commands = new ArrayList<DrawingCommand>(1000);

    public ShapeDumpingGraphics2D(Graphics2D graphic, int width, int height, File outputFile) {
        this.delegate = graphic;
        System.out.println("Dumping commands to " + outputFile.getAbsolutePath());
        this.width = width;
        this.height = height;
        try {
            this.oos = new ObjectOutputStream(new FileOutputStream(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw(Shape s) {
        createCommand(s, false);
        delegate.draw(s);
    }

    @Override
    public void fill(Shape s) {
        createCommand(s, true);
        delegate.fill(s);
    }

    void createCommand(Shape shape, boolean fill) {
        DrawingCommand command = new DrawingCommand(getShape(shape), delegate, fill);
        commands.add(command);
    }

    private static Shape getShape(Shape src) {
        if (!FORCE_USE_PATH2D) {
            if (src instanceof Line2D) {
                return (Line2D)((Line2D) src).clone();
            } else if (src instanceof Rectangle2D) {
                return (Rectangle2D)((Rectangle2D) src).clone();
            }
        }
        return new Path2D.Double(src);
    }

    @Override
    public void dispose() {
        try {
            System.out.println("Writing " + commands.size() + " commands ...");
            DrawingCommands dc = new DrawingCommands(width, height, commands);
            oos.writeObject(dc);
        } catch (IOException ioe) {
            System.out.println("error while writing commands");
            ioe.printStackTrace(System.out);
        } finally {
            try {
                oos.close();
            } catch (IOException ioe) {
                // ignore
            }
            commands = null;
            delegate.dispose();
        }
    }

    @Override
    public Graphics create() {
        return delegate.create();
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        return delegate.create(x, y, width, height);
    }

    @Override
    public Color getColor() {
        return delegate.getColor();
    }

    @Override
    public void setColor(Color c) {
        delegate.setColor(c);
    }

    @Override
    public void setPaintMode() {
        delegate.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        delegate.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return delegate.getFont();
    }

    @Override
    public void setFont(Font font) {
        delegate.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics() {
        return delegate.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return delegate.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return delegate.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        delegate.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        delegate.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return delegate.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        delegate.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        delegate.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        if (DEBUG) {
            System.out.println("drawLine: (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
        }
        delegate.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        if (DEBUG) {
            System.out.println("fillRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.fillRect(x, y, width, height);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        if (DEBUG) {
            System.out.println("drawRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.drawRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        if (DEBUG) {
            System.out.println("clearRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.clearRect(x, y, width, height);
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        if (DEBUG) {
            System.out.println("draw3DRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.draw3DRect(x, y, width, height, raised);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (DEBUG) {
            System.out.println("drawRoundRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        if (DEBUG) {
            System.out.println("fill3DRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.fill3DRect(x, y, width, height, raised);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (DEBUG) {
            System.out.println("fillRoundRect: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return delegate.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        delegate.drawImage(img, op, x, y);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        if (DEBUG) {
            System.out.println("drawOval: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.drawOval(x, y, width, height);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        delegate.drawRenderedImage(img, xform);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        if (DEBUG) {
            System.out.println("fillOval: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (DEBUG) {
            System.out.println("drawArc: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        delegate.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        if (DEBUG) {
            System.out.println("drawString: (" + x + "," + y + "): " + str);
        }
        delegate.drawString(str, x, y);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (DEBUG) {
            System.out.println("fillArc: (" + x + "," + y + ") to (" + (x + width) + "," + (y + height) + ")");
        }
        delegate.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawString(String str, float x, float y) {
        if (DEBUG) {
            System.out.println("drawString: (" + x + "," + y + "): " + str);
        }
        delegate.drawString(str, x, y);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        if (DEBUG) {
            System.out.println("drawPolyline: (" + nPoints + " points)");
        }
        delegate.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        delegate.drawString(iterator, x, y);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        if (DEBUG) {
            System.out.println("drawPolygon: (" + nPoints + " points)");
        }
        delegate.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        delegate.drawString(iterator, x, y);
    }

    @Override
    public void drawPolygon(Polygon p) {
        if (DEBUG) {
            System.out.println("drawPolygon: " + p);
        }
        delegate.drawPolygon(p);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        if (DEBUG) {
            System.out.println("fillPolygon: (" + nPoints + " points)");
        }
        delegate.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        delegate.drawGlyphVector(g, x, y);
    }

    @Override
    public void fillPolygon(Polygon p) {
        if (DEBUG) {
            System.out.println("fillPolygon: " + p);
        }
        delegate.fillPolygon(p);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return delegate.hit(rect, s, onStroke);
    }

    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        delegate.drawChars(data, offset, length, x, y);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return delegate.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite comp) {
        delegate.setComposite(comp);
    }

    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        delegate.drawBytes(data, offset, length, x, y);
    }

    @Override
    public void setPaint(Paint paint) {
        delegate.setPaint(paint);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return delegate.drawImage(img, x, y, observer);
    }

    @Override
    public void setStroke(Stroke s) {
        delegate.setStroke(s);
    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {
        delegate.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(Key hintKey) {
        return delegate.getRenderingHint(hintKey);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        delegate.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        delegate.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return delegate.getRenderingHints();
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public void translate(int x, int y) {
        delegate.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        delegate.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        delegate.rotate(theta);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
                             ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        delegate.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        delegate.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        delegate.shear(shx, shy);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public void transform(AffineTransform Tx) {
        delegate.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        delegate.setTransform(Tx);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public AffineTransform getTransform() {
        return delegate.getTransform();
    }

    @Override
    public Paint getPaint() {
        return delegate.getPaint();
    }

    @Override
    public Composite getComposite() {
        return delegate.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        delegate.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return delegate.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return delegate.getStroke();
    }

    @Override
    public void clip(Shape s) {
        delegate.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return delegate.getFontRenderContext();
    }

    @Override
    public void finalize() {
        delegate.finalize();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Rectangle getClipRect() {
        return delegate.getClipRect();
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        return delegate.hitClip(x, y, width, height);
    }

    @Override
    public Rectangle getClipBounds(Rectangle r) {
        return delegate.getClipBounds(r);
    }
}
