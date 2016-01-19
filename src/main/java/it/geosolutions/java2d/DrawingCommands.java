/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import org.gui.ImageUtils;

public final class DrawingCommands implements Serializable {

    private static final long serialVersionUID = 4224204056540669349L;

    /* members */
    int width;
    int height;
    ArrayList<DrawingCommand> commands;
    // transient fields:
    transient String name = null;
    transient File file = null;
    transient AffineTransform at;
    transient boolean prepared = false;
    transient int imgWidth = 0;
    transient int imgHeight = 0;
    transient Rectangle2D.Double clip;

    public DrawingCommands(int width, int height, ArrayList<DrawingCommand> commands) {
        this.width = width;
        this.height = height;
        this.commands = commands;
    }

    public void dispose() {
        this.prepared = false;
        final ArrayList<DrawingCommand> _commands = commands;

        for (int i = 0, len = _commands.size(); i < len; i++) {
            _commands.get(i).dispose();
        }
    }

    @Override
    public String toString() {
        return "DrawingCommands{" + "width=" + width + ", height=" + height + ", commands=" + commands.size() + '}';
    }

    public AffineTransform getAt() {
        return at;
    }

    public void setAt(AffineTransform at) {
        if ((at == null) || (!at.isIdentity())) {
            this.at = at;
            this.prepared = false;
        }
    }

    public int getWidth() {
        return (at != null) ? 1 + (int) (at.getScaleX() * width + 2d * at.getTranslateX()) : width;
    }

    public int getHeight() {
        return (at != null) ? 1 + (int) (at.getScaleY() * height + 2d * at.getTranslateY()) : height;
    }

    public void prepareCommands(final boolean doClip, final boolean doOverrideWindingRule, final int windingRule) {
        if (!prepared) {
            int n = 0;
            
            final boolean isWarmup = BaseTest.isWarmup;
            
            // filter commands:
            final Rectangle2D clip = (doClip && !isWarmup) ? new Rectangle2D.Double(0d, 0d, width, height) : null;
            final Rectangle2D sizeRanges = (!isWarmup) ? MapConst.sizeRanges : null;

            final ArrayList<DrawingCommand> _commands = commands;

            for (int i = 0, len = _commands.size(); i < len; i++) {
                if (_commands.get(i).filter(clip, sizeRanges)) {
                    n++;
                }
            }
            if (n != 0) {
                System.out.println("prepareCommands: clipped (ie invisible) shapes = " + n);
            }

            // Prepare transforms:
            prepareCommandsForAffineTransform();

            if (doOverrideWindingRule) {
                // Update winding Rule:
                for (int i = 0, len = _commands.size(); i < len; i++) {
                    _commands.get(i).setWindingRule(windingRule);
                }
            }

            prepared = true;
        }
    }
    
    public void prepareCommandsForAffineTransform() {
        if (!prepared) {
            final ArrayList<DrawingCommand> _commands = commands;
            
            // Prepare transforms:
            final AffineTransform _at = getAt();

            for (int i = 0, len = _commands.size(); i < len; i++) {
                _commands.get(i).prepareTransform(_at);
            }

            prepared = true;
        }
    }

    public Image prepareImage() {
        return prepareImage(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    public Image prepareImage(int maxWidth, int maxHeight) {
        final int w = Math.min(maxWidth, getWidth());
        final int h = Math.min(maxHeight, getHeight());
        return ImageUtils.newFastImage(w, h);
    }

    public Graphics2D prepareGraphics(Image image) {
        final Graphics2D graphics;
        if (MapConst.useMarlinGraphics2D) {
            // TODO: fix
            graphics = new org.marlin.graphics.MarlinGraphics2D((BufferedImage) image);
        } else {
            graphics = ImageUtils.createGraphics(image);
            setDefaultRenderingHints(graphics);
        }
        final int w = image.getWidth(null);
        final int h = image.getHeight(null);
        
        if (MapConst.useClipSmall) {
            clip = new Rectangle2D.Double((w >> 1) - 200, (h >> 1) - 200, 400, 400);
        } else if (MapConst.useClipDemo) {
            clip = null;
        } else {
            clip = new Rectangle2D.Double(0d, 0d, w, h);
        }
//        System.out.println("clip: " + clip);
        
        this.imgWidth = w;
        this.imgHeight = h;

        return graphics;
    }

    private static void setDefaultRenderingHints(final Graphics2D graphics) {
        if (MapConst.qualityMode) {
            // Use pure stroke (no normalization of pixel centers to pixel boundaries):
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            // Quality settings:
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    public void execute(final Graphics2D graphics, final AffineTransform animAt) {
        // reset graphics (transform / image):
        graphics.setTransform(MapConst.IDENTITY);
        graphics.setBackground(Color.WHITE);
        graphics.setClip(null);

        // Disable antialiasing:
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.clearRect(0, 0, imgWidth, imgHeight);

        // Enable antialiasing:
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final AffineTransform _at;
        if (animAt != null) {
            _at = animAt;
            graphics.setTransform(_at);
            graphics.setColor(Color.RED);
            graphics.setStroke(MapConst.STROKE_THIN);
            graphics.drawRect(0, 0, width, height); // original size
// show center:
//            graphics.fillRect((width >> 1) - 4, (height >> 1) - 4, 9, 9);
        } else if (at != null) {
            _at = at;
            graphics.setTransform(_at);
            graphics.setColor(Color.RED);
            graphics.setStroke(MapConst.STROKE_THIN);
            graphics.drawRect(0, 0, width, height); // original size
        } else {
            _at = MapConst.IDENTITY;
        }
        if (clip != null) {
            graphics.setClip(clip);
        }
        
        final ArrayList<DrawingCommand> _commands = commands;
        final boolean usePreparedTx = this.prepared && (animAt == null);

        for (int i = 0, len = _commands.size(); i < len; i++) {
            _commands.get(i).execute(graphics, _at, usePreparedTx);
        }
    }

    public static DrawingCommands load(File input) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(input));
            final DrawingCommands dc = (DrawingCommands) ois.readObject();
            dc.name = input.getName();
            dc.file = input;

            System.out.println("Loaded DrawingCommands: " + String.valueOf(dc));

            return dc;

        } finally {
            if (ois != null) {
                ois.close();
            }
        }
    }
}
