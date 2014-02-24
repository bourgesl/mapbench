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

public class ShapeDumpingGraphics2D extends Graphics2D {

    Graphics2D delegate;
    ObjectOutputStream oos;
    int width;
    int height;
    ArrayList<DrawingCommand> commands = new ArrayList<DrawingCommand>();

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

    public void draw(Shape s) {
        createCommand(s, false);
        delegate.draw(s);
    }

    public void fill(Shape s) {
        createCommand(s, true);
        delegate.fill(s);
    }

    void createCommand(Shape shape, boolean fill) {
        DrawingCommand command = new DrawingCommand(getShape(shape), delegate, fill);
        commands.add(command);
    }

    private static Shape getShape(Shape src) {
        if (src instanceof GeneralPath) {
            return src;
        } else if (src instanceof Line2D) {
            return src;
        } else if (src instanceof Rectangle2D) {
            return src;
        }
        return new GeneralPath(src);
    }

    public void dispose() {
        try {
            DrawingCommands dc = new DrawingCommands(width, height, commands);
            oos.writeObject(dc);
            oos.close();
        } catch (IOException e) {
            // ignore it
        }
        commands = null;
        delegate.dispose();
    }

    public Graphics create() {
        return delegate.create();
    }

    public Graphics create(int x, int y, int width, int height) {
        return delegate.create(x, y, width, height);
    }

    public Color getColor() {
        return delegate.getColor();
    }

    public void setColor(Color c) {
        delegate.setColor(c);
    }

    public void setPaintMode() {
        delegate.setPaintMode();
    }

    public void setXORMode(Color c1) {
        delegate.setXORMode(c1);
    }

    public Font getFont() {
        return delegate.getFont();
    }

    public void setFont(Font font) {
        delegate.setFont(font);
    }

    public FontMetrics getFontMetrics() {
        return delegate.getFontMetrics();
    }

    public FontMetrics getFontMetrics(Font f) {
        return delegate.getFontMetrics(f);
    }

    public Rectangle getClipBounds() {
        return delegate.getClipBounds();
    }

    public void clipRect(int x, int y, int width, int height) {
        delegate.clipRect(x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height) {
        delegate.setClip(x, y, width, height);
    }

    public Shape getClip() {
        return delegate.getClip();
    }

    public void setClip(Shape clip) {
        delegate.setClip(clip);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        delegate.copyArea(x, y, width, height, dx, dy);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        delegate.drawLine(x1, y1, x2, y2);
    }

    public void fillRect(int x, int y, int width, int height) {
        delegate.fillRect(x, y, width, height);
    }

    public void drawRect(int x, int y, int width, int height) {
        delegate.drawRect(x, y, width, height);
    }

    public void clearRect(int x, int y, int width, int height) {
        delegate.clearRect(x, y, width, height);
    }

    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.draw3DRect(x, y, width, height, raised);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.fill3DRect(x, y, width, height, raised);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return delegate.drawImage(img, xform, obs);
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        delegate.drawImage(img, op, x, y);
    }

    public void drawOval(int x, int y, int width, int height) {
        delegate.drawOval(x, y, width, height);
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        delegate.drawRenderedImage(img, xform);
    }

    public void fillOval(int x, int y, int width, int height) {
        delegate.fillOval(x, y, width, height);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        delegate.drawRenderableImage(img, xform);
    }

    public void drawString(String str, int x, int y) {
        delegate.drawString(str, x, y);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawString(String str, float x, float y) {
        delegate.drawString(str, x, y);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolyline(xPoints, yPoints, nPoints);
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        delegate.drawString(iterator, x, y);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        delegate.drawString(iterator, x, y);
    }

    public void drawPolygon(Polygon p) {
        delegate.drawPolygon(p);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        delegate.drawGlyphVector(g, x, y);
    }

    public void fillPolygon(Polygon p) {
        delegate.fillPolygon(p);
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return delegate.hit(rect, s, onStroke);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y) {
        delegate.drawChars(data, offset, length, x, y);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return delegate.getDeviceConfiguration();
    }

    public void setComposite(Composite comp) {
        delegate.setComposite(comp);
    }

    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        delegate.drawBytes(data, offset, length, x, y);
    }

    public void setPaint(Paint paint) {
        delegate.setPaint(paint);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return delegate.drawImage(img, x, y, observer);
    }

    public void setStroke(Stroke s) {
        delegate.setStroke(s);
    }

    public void setRenderingHint(Key hintKey, Object hintValue) {
        delegate.setRenderingHint(hintKey, hintValue);
    }

    public Object getRenderingHint(Key hintKey) {
        return delegate.getRenderingHint(hintKey);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, observer);
    }

    public void setRenderingHints(Map<?, ?> hints) {
        delegate.setRenderingHints(hints);
    }

    public void addRenderingHints(Map<?, ?> hints) {
        delegate.addRenderingHints(hints);
    }

    public RenderingHints getRenderingHints() {
        return delegate.getRenderingHints();
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, x, y, bgcolor, observer);
    }

    public void translate(int x, int y) {
        delegate.translate(x, y);
    }

    public void translate(double tx, double ty) {
        delegate.translate(tx, ty);
    }

    public void rotate(double theta) {
        delegate.rotate(theta);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
                             ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public void rotate(double theta, double x, double y) {
        delegate.rotate(theta, x, y);
    }

    public void scale(double sx, double sy) {
        delegate.scale(sx, sy);
    }

    public void shear(double shx, double shy) {
        delegate.shear(shx, shy);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    public void transform(AffineTransform Tx) {
        delegate.transform(Tx);
    }

    public void setTransform(AffineTransform Tx) {
        delegate.setTransform(Tx);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    public AffineTransform getTransform() {
        return delegate.getTransform();
    }

    public Paint getPaint() {
        return delegate.getPaint();
    }

    public Composite getComposite() {
        return delegate.getComposite();
    }

    public void setBackground(Color color) {
        delegate.setBackground(color);
    }

    public Color getBackground() {
        return delegate.getBackground();
    }

    public Stroke getStroke() {
        return delegate.getStroke();
    }

    public void clip(Shape s) {
        delegate.clip(s);
    }

    public FontRenderContext getFontRenderContext() {
        return delegate.getFontRenderContext();
    }

    public void finalize() {
        delegate.finalize();
    }

    public String toString() {
        return delegate.toString();
    }

    public Rectangle getClipRect() {
        return delegate.getClipRect();
    }

    public boolean hitClip(int x, int y, int width, int height) {
        return delegate.hitClip(x, y, width, height);
    }

    public Rectangle getClipBounds(Rectangle r) {
        return delegate.getClipBounds(r);
    }
}
