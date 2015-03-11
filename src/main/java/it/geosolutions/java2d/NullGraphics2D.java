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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class NullGraphics2D extends Graphics2D {

    Graphics2D delegate;

    public NullGraphics2D(Graphics2D graphic) {
        this.delegate = graphic;
    }

    @Override
    public void draw(Shape s) {
    }

    @Override
    public void fill(Shape s) {
    }

    @Override
    public void dispose() {
        delegate.dispose();
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
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return true;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    }

    @Override
    public void drawString(String str, int x, int y) {
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void drawString(String str, float x, float y) {
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    }

    @Override
    public void drawPolygon(Polygon p) {
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
    }

    @Override
    public void fillPolygon(Polygon p) {
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return delegate.hit(rect, s, onStroke);
    }

    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
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
    }

    @Override
    public void setPaint(Paint paint) {
        delegate.setPaint(paint);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return true;
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
        return true;
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
        return true;
    }

    @Override
    public void translate(int x, int y) {
    }

    @Override
    public void translate(double tx, double ty) {
    }

    @Override
    public void rotate(double theta) {
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
                             ImageObserver observer) {
        return true;
    }

    @Override
    public void rotate(double theta, double x, double y) {
    }

    @Override
    public void scale(double sx, double sy) {
    }

    @Override
    public void shear(double shx, double shy) {
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, ImageObserver observer) {
        return true;
    }

    @Override
    public void transform(AffineTransform Tx) {
    }

    @Override
    public void setTransform(AffineTransform Tx) {
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return true;
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
