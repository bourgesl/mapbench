/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.AlphaComposite;
import java.io.Serializable;

public final class SerializableAlphaComposite implements Serializable {
    private static final long serialVersionUID = 6655986779807782243L;
    
    private float alpha;
    private int rule;
    private transient AlphaComposite ac = null;

    public SerializableAlphaComposite(AlphaComposite composite) {
        alpha = composite.getAlpha();
        rule = composite.getRule();
    }
    
    public AlphaComposite toAlphaComposite() {
        // cache the AlphaComposite (GC friendly)
        if (ac == null) {
            ac = AlphaComposite.getInstance(rule, alpha);
        }
        return ac;
    }

}
