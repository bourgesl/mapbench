/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
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
    String fileName = null;
    int width;
    int height;
    ArrayList<DrawingCommand> commands;
    transient AffineTransform at;
    transient boolean prepared = false;

    public DrawingCommands(int width, int height, ArrayList<DrawingCommand> commands) {
        this.width = width;
        this.height = height;
        this.commands = commands;
    }

    public void dispose() {
        prepared = false;
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
            prepared = true;

            int n = 0;
            // clip commands:
            final Rectangle2D clip = (doClip) ? new Rectangle2D.Double(0d, 0d, width, height) : null; // original size

            final ArrayList<DrawingCommand> _commands = commands;

            for (int i = 0, len = _commands.size(); i < len; i++) {
                if (_commands.get(i).clip(clip)) {
                    n++;
                }
            }
            if (n != 0) {
                System.out.println("prepareCommands: clipped (ie invisible) shapes = " + n);
            }

            // Prepare transforms:
            final AffineTransform _at = getAt();

            for (int i = 0, len = _commands.size(); i < len; i++) {
                _commands.get(i).prepareTransform(_at);
            }

            if (doOverrideWindingRule) {
                // Update winding Rule:
                for (int i = 0, len = _commands.size(); i < len; i++) {
                    _commands.get(i).setWindingRule(windingRule);
                }
            }
        }
    }

    public BufferedImage prepareImage() {
        return ImageUtils.newImage(getWidth(), getHeight());
    }

    public Graphics2D prepareGraphics(BufferedImage image) {
        final Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setClip(new Rectangle2D.Double(0d, 0d, image.getWidth(), image.getHeight()));

        return graphics;
    }

    public void execute(final Graphics2D graphics) {
        // reset graphics (transform / image):
        graphics.setTransform(MapConst.IDENTITY);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        final AffineTransform _at;
        if (at != null) {
            _at = at;
            graphics.setTransform(_at);
            graphics.setColor(Color.gray);
            graphics.setStroke(MapConst.STROKE_THIN);
            graphics.drawRect(0, 0, width, height); // original size
        } else {
            _at = MapConst.IDENTITY;
        }
        final ArrayList<DrawingCommand> _commands = commands;

        for (int i = 0, len = _commands.size(); i < len; i++) {
            _commands.get(i).execute(graphics, _at);
        }
    }

    public static DrawingCommands load(File input) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(input));
            final DrawingCommands dc = (DrawingCommands) ois.readObject();
            dc.fileName = input.getName();

            System.out.println("Loaded DrawingCommands: " + String.valueOf(dc));

            return dc;

        } finally {
            if (ois != null) {
                ois.close();
            }
        }
    }
}
