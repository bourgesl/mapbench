/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

public final class DrawingCommand implements Serializable {

    private static final boolean HIDE_CLIPPED_SHAPE = true;

    private static final long serialVersionUID = -6164586104804552649L;
    private static final BasicStroke defaultStroke = new BasicStroke(1);
    private static Map<BasicStroke, SerializableBasicStroke> mapStrokes = new IdentityHashMap<>();
    private static Map<AlphaComposite, SerializableAlphaComposite> mapComposites = new IdentityHashMap<>();

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
    SerializableBasicStroke stroke;
    Paint paint;
    Shape shape;
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
    private transient AffineTransform combinedTransform = null;
    /** clip test */
    private transient boolean visible = true;

    public void dispose() {
        combinedTransform = null;
        visible = true;
    }

    public void clip(final Rectangle2D clip) {
        visible = true;
        if (clip != null) {
            if (transform != null && !transform.isIdentity()) {
                // TODO: bounds = transform.createTransformedShape(bounds);
                return;
            }

            final Rectangle2D bounds = shape.getBounds2D();

            if (stroke != null) {
                final BasicStroke bs = stroke.toStroke();

                double lw = bs.getLineWidth();

                bounds.setRect(bounds.getX() - lw, bounds.getY() - lw,
                        bounds.getWidth() + 2d * lw, lw + 2d * lw);
            }

            visible = clip.intersects(bounds);
        }
    }

    public void execute(Graphics2D g2d, final AffineTransform graphicsTx) {
        if (!visible) {
            if (HIDE_CLIPPED_SHAPE) {
                return;
            }
//            System.out.println("shape invisible = " + shape);
            g2d.setPaint(Color.ORANGE);
        } else {
            g2d.setPaint(paint);
        }
        if (composite != null) {
            g2d.setComposite(composite.toAlphaComposite());
        } else {
            g2d.setComposite(null);
        }
        if (transform != null && !transform.isIdentity()) {
            if (graphicsTx != null) {
                if (combinedTransform == null) {
                    /* TODO: prepare combined transform once before tests */
                    // combine transform with one coming from graphics:
                    // cached until explicit cleanup
                    combinedTransform = new AffineTransform(graphicsTx);
                    combinedTransform.concatenate(transform);
                }
                g2d.setTransform(combinedTransform);
            } else {
                g2d.setTransform(transform);
            }
        }
        if (stroke != null) {
            g2d.setStroke(stroke.toStroke());
            g2d.draw(shape);
        } else {
            g2d.fill(shape);
        }
    }
}