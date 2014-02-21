package org.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Enhanced JPanel to display only a region of the given image

 TODO: add EDT checks in setImage / setScale

 * @author bourgesl
 */
public class BigImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /* members */
    /** 
     * Interpolation method among:
     *  - RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
     *  - RenderingHints.VALUE_INTERPOLATION_BILINEAR
     *  - RenderingHints.VALUE_INTERPOLATION_BICUBIC
     */
    protected Object interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

    /** scale factor */
    protected float scale = 1.0f;
    /** image width and height */
    protected int iw, ih;
    /** image */
    protected BufferedImage image;
    /** image rectangle */
    protected final Rectangle imgRect;
    /* source rectangle : coords on image */
    private Rectangle srcRect;
    /* destination rectangle : coords on frame */
    private Rectangle dstRect;

    /**
     * Protected constructor
     */
    protected BigImagePanel() {
        this.imgRect = new Rectangle();
        this.srcRect = new Rectangle();
        this.dstRect = new Rectangle();
    }

    protected void setImage(final BufferedImage image) {
        this.image = image;
        this.iw = (image != null) ? image.getWidth() : 0;
        this.ih = (image != null) ? image.getHeight() : 0;

        imgRect.setSize(this.iw, this.ih);

//        System.out.println("image : " + imgRect);
        updatePreferredSize();
    }

    protected void setScale(final float factor) {
        /* TODO: adjust frame center */
        // use standard position to adjust center window :
/*        
        float cx = this.srcRect.x + 0.5f * this.srcRect.width;
        float cy = this.srcRect.y + 0.5f * this.srcRect.height;
        System.out.println("center : " + cx + ", " + cy);
*/        
        this.scale = factor;

        updatePreferredSize();
    }

    /**
     * @return the current interpolation method
     */
    protected Object getInterpolation() {
        return interpolation;
    }

    /**
     * Set the interpolation method among:
     *  - RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
     *  - RenderingHints.VALUE_INTERPOLATION_BILINEAR
     *  - RenderingHints.VALUE_INTERPOLATION_BICUBIC
     * @param interpolation RenderingHints.VALUE_INTERPOLATION_*
     */
    protected void setInterpolation(final Object interpolation) {
        if (interpolation != null) {
            this.interpolation = interpolation;
        }
    }

    protected void updatePreferredSize() {
        if (image != null && scale > 0f) {
            setPreferredSize(new Dimension((int) (this.iw * scale), (int) (this.ih * scale)));
            revalidate();
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            final Graphics2D g2 = (Graphics2D) g;

            // Set chosen interpolation method:
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);

//            final long start = System.nanoTime();
            int dx, dy, dx2, dy2;
            int sx, sy, sx2, sy2;

            // multiple scrolls can call multiple times this method :
            dstRect = g2.getClipBounds(dstRect);

            // convert clip coordinates to image coordinates            
            srcRect.x = Math.round(dstRect.x / scale);
            srcRect.y = Math.round(dstRect.y / scale);
            srcRect.width = Math.round(dstRect.width / scale);
            srcRect.height = Math.round(dstRect.height / scale);

            // srcRect larger than imgRect :
            if (!imgRect.contains(srcRect)) {
                g2.setColor(getBackground());
                g2.fillRect(dstRect.x, dstRect.y, dstRect.width, dstRect.height);

                // updates srcRect :
                srcRect = srcRect.intersection(imgRect);

                // updates dstRect :
                dstRect.setBounds(Math.round(srcRect.x * scale), Math.round(srcRect.y * scale),
                        Math.round(srcRect.width * scale), Math.round(srcRect.height * scale));
            }

            dx = dstRect.x;
            dy = dstRect.y;
            dx2 = dstRect.x + dstRect.width;
            dy2 = dstRect.y + dstRect.height;

            sx = srcRect.x;
            sy = srcRect.y;
            sx2 = srcRect.x + srcRect.width;
            sy2 = srcRect.y + srcRect.height;

            // Interet : back buffer par Swing => paintComponent appellé sur zone modifiée : all si rescale : 
            g2.drawImage(image, dx, dy, dx2, dy2, sx, sy, sx2, sy2, null);
            /*
             final long time = System.nanoTime() - start;
             System.out.println("paint : [" + srcRect.width + " x " + srcRect.height + "] => ["
             + dstRect.width + " x " + dstRect.height + "] : " + (time / 1000000l) + " ms.");
             */
        }
    }

}
