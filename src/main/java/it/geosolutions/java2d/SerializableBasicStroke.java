/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.BasicStroke;
import java.io.Serializable;

public final class SerializableBasicStroke implements Serializable {
    
    private static final boolean DISABLE_JOIN_MITER = false;

    private static final long serialVersionUID = 8157570308117400971L;
    private float width;
    private int endCap;
    private int lineJoin;
    private float miterLimit;
    private float[] dashArray;
    private float dashPhase;
    private transient BasicStroke bs = null;

    public SerializableBasicStroke(BasicStroke stroke) {
        this.width = stroke.getLineWidth();
        this.endCap = stroke.getEndCap();
        this.lineJoin = stroke.getLineJoin();
        this.miterLimit = stroke.getMiterLimit();
        this.dashArray = stroke.getDashArray();
        this.dashPhase = stroke.getDashPhase();
    }

    public BasicStroke toStroke() {
        // cache the BasicStroke (GC friendly)
        if (bs == null) {
            if (DISABLE_JOIN_MITER) {
                bs = new BasicStroke(width, endCap, BasicStroke.JOIN_BEVEL, miterLimit, dashArray, dashPhase);
            } else {
                bs = new BasicStroke(width, endCap, lineJoin, miterLimit, dashArray, dashPhase);
            }
        }
        return bs;
    }
}
