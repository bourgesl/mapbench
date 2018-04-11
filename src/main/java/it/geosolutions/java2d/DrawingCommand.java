/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import org.gui.ImageUtils;

public final class DrawingCommand implements Serializable {

    private static final boolean HIDE_CLIPPED_SHAPE = true;
    private static final boolean SHOW_BBOX = false;
    private static final Color BBOX_PAINT = new Color(128, 128, 128, 32);

    private static final long serialVersionUID = -6164586104804552649L;
    private static final BasicStroke defaultStroke = new BasicStroke(1f);
    private static Map<BasicStroke, SerializableBasicStroke> mapStrokes = new IdentityHashMap<BasicStroke, SerializableBasicStroke>();
    private static Map<AlphaComposite, SerializableAlphaComposite> mapComposites = new IdentityHashMap<AlphaComposite, SerializableAlphaComposite>();

    /** cached gradient paint */
    private static final Paint GRADIENT_PAINT = createGradientPaint();
    /** cached texture paint */
    private static final Paint TEXTURE_PAINT = createTexturePaint();

    private static MultipleGradientPaint createGradientPaint() {
        return new RadialGradientPaint(200f, 200f, 1000f,
                new float[]{0.25f, 0.75f, 1f},
                new Color[]{Color.RED, Color.GREEN, Color.BLUE},
                CycleMethod.REFLECT);
    }

    private static TexturePaint createTexturePaint() {
        final int size = 32; // small
        final BufferedImage bimg = ImageUtils.newImage(size, size);
        final Graphics2D g = bimg.createGraphics();
        try {
            g.setBackground(Color.LIGHT_GRAY);
            g.clearRect(0, 0, size, size);

            // Enable antialiasing:
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(Color.BLACK);
            g.setStroke(new BasicStroke(3f));
            g.draw(new Line2D.Double(-3, -3, size + 3, size + 3));

        } finally {
            g.dispose();
        }
        return new TexturePaint(bimg, new Rectangle2D.Double(0, 0, size, size));
    }

    private static SerializableBasicStroke getSerializableStroke(BasicStroke bs) {
        SerializableBasicStroke s = mapStrokes.get(bs);
        if (s == null) {
            s = new SerializableBasicStroke(bs);
            mapStrokes.put(bs, s);
        }
        return s;
    }

    private static SerializableAlphaComposite getSerializableComposite(AlphaComposite ac) {
        SerializableAlphaComposite s = mapComposites.get(ac);
        if (s == null) {
            s = new SerializableAlphaComposite(ac);
            mapComposites.put(ac, s);
        }
        return s;
    }

    /* members */
    private SerializableBasicStroke stroke;
    private Paint paint;
    private Shape shape;
    private SerializableAlphaComposite composite;
    private AffineTransform transform;

    public DrawingCommand(Shape shape, Graphics2D graphics, boolean fill) {
        this.shape = shape;

        this.paint = graphics.getPaint();
        // in case it's not a color simplify things out
        if (!(paint instanceof Color)) {
            this.paint = Color.BLACK;
        }
        if (!fill) {
            Stroke s = graphics.getStroke();
            // basic stroke is not serializable, trying to extend it to make the subclass
            // serializable does not seem to work (deserializes with the wrong width?) so
            // let's hack a DTO...
            if (s instanceof BasicStroke) {
                this.stroke = getSerializableStroke((BasicStroke) s);
            } else {
                // simplify things out for this case as well
                this.stroke = getSerializableStroke(defaultStroke);
            }
        }

        Composite c = graphics.getComposite();
        if (c != null && c instanceof AlphaComposite) {
// Buggy ?
            this.composite = getSerializableComposite((AlphaComposite) c);
        }

        this.transform = graphics.getTransform();
        if (this.transform.isIdentity()) {
            this.transform = null;
        }
    }

    public SerializableBasicStroke getStroke() {
        return stroke;
    }

    public Paint getPaint() {
        return paint;
    }

    public Shape getShape() {
        return shape;
    }
    // transient combined transform:
    private transient AffineTransform effectiveTransform = null;
    /** clip test */
    private transient boolean visible = true;
    /** bounding box */
    private transient Rectangle2D bbox = null;

    public void dispose() {
        effectiveTransform = null;
        visible = true;
    }

    public boolean filter(final Rectangle2D clip, final Rectangle2D sizeRanges) {
        visible = true;
        if (clip != null || sizeRanges != null) {
            Rectangle2D bounds = shape.getBounds2D();
            if (transform != null && !transform.isIdentity()) {
                bounds = transform.createTransformedShape(bounds).getBounds2D();
            }

            if (visible && clip != null) {
                if (stroke != null) {
                    final BasicStroke bs = stroke.toStroke();

                    double lw = bs.getLineWidth();

                    bounds.setRect(bounds.getX() - lw, bounds.getY() - lw,
                            bounds.getWidth() + 2d * lw, bounds.getHeight() + 2d * lw);
                }

                visible = clip.intersects(bounds);
                // TODO: use clipper ?
            }
            if (visible && sizeRanges != null) {
                final double width = bounds.getWidth();
                final double height = bounds.getHeight();

                if (width < sizeRanges.getX()) {
                    visible = false;
                } else if (width > sizeRanges.getWidth()) {
                    visible = false;
                } else if (height < sizeRanges.getY()) {
                    visible = false;
                } else if (height > sizeRanges.getHeight()) {
                    visible = false;
                }
                if (false && !visible) {
                    System.out.println("shape filtered: " + bounds);
                }
                if (visible) {
                    // keep bbox:
                    bbox = bounds;
                }
            }
        }
        return !visible;
    }

    public void prepareTransform(final AffineTransform graphicsTx) {
        this.effectiveTransform = getCombinedTransform(graphicsTx);
    }

    private AffineTransform getCombinedTransform(final AffineTransform graphicsTx) {
        if (transform != null && !transform.isIdentity()) {
            if (graphicsTx != null) {
                // combine transform with one coming from graphics:
                // cached until explicit cleanup
                final AffineTransform tx = new AffineTransform(graphicsTx);
                tx.concatenate(transform);
                return tx;
            }
            return transform;
        }
        return null;
    }

    public void setWindingRule(final int windingRule) {
        if (shape instanceof GeneralPath) {
            final GeneralPath path = (GeneralPath) shape;
            path.setWindingRule(windingRule);
        } else {
            System.out.println("Unsupported shape [" + shape.getClass().getName() + "] for setWindingRule(int) !");
        }
        if ((windingRule == PathIterator.WIND_EVEN_ODD) && (stroke != null)) {
            this.stroke = null;
        }
    }

    public void execute(final Graphics2D g2d, final AffineTransform graphicsTx, final boolean usePreparedTx) {
        if (!visible) {
            if (HIDE_CLIPPED_SHAPE) {
                return;
            }
//            System.out.println("shape invisible = " + shape);
            g2d.setPaint(Color.ORANGE);
        } else {
            g2d.setPaint(paint);
        }

        if (SHOW_BBOX && bbox != null) {
            Paint old = g2d.getPaint();
            g2d.setPaint(BBOX_PAINT);
            g2d.fill(bbox);
            g2d.setPaint(old);
        }

        if (composite != null) {
            g2d.setComposite(composite.toAlphaComposite());
        }

        final AffineTransform at = (usePreparedTx) ? effectiveTransform : getCombinedTransform(graphicsTx);
        if (at != null) {
            g2d.setTransform(at);
// TEST:
//            g2d.setPaint(Color.GREEN);
        }

        // Draw / Fill command:
        if (stroke != null) {
            if (MapConst.doCreateStrokedShape) {
                final Shape strokedShape = stroke.toStroke().createStrokedShape(shape);
// TEST:
//            g2d.setPaint(Color.GREEN);
                g2d.fill(strokedShape);
            } else if (!MapConst.skipDraw) {
                g2d.setStroke((MapConst.doUseDashedStroke) ? MapConst.STROKE_DOTTED : stroke.toStroke());
                g2d.draw(shape);
            }
        } else if (!MapConst.skipFill) {
            if (SHOW_BBOX && bbox != null) {
                Paint old = g2d.getPaint();
                if (old instanceof Color) {
                    Color c = (Color) old;
                    g2d.setPaint(new Color(0xA0000000 | (c.getRGB() & 0x00FFFFFF), true));
                }
            }
            if (MapConst.doUseTexture) {
                g2d.setPaint(TEXTURE_PAINT);
            }
            if (MapConst.doUseGradient) {
                g2d.setPaint(GRADIENT_PAINT);
            }

//            g2d.setPaint(Color.PINK);
            g2d.fill(shape);
        }

        // finally restore state:
        if (at != null && graphicsTx != null) {
            g2d.setTransform(graphicsTx);
        }
        if (composite != null) {
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }
}
