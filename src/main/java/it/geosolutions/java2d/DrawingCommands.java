/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

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
    private static final double margin = 50d;
    String fileName = null;
    int width;
    int height;
    ArrayList<DrawingCommand> commands;
    transient AffineTransform at;
    transient boolean clipped = false;

    public DrawingCommands(int width, int height, ArrayList<DrawingCommand> commands) {
        this.width = width;
        this.height = height;
        this.commands = commands;
    }

    public void dispose() {
        clipped = false;
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
        if (at != null && !at.isIdentity()) {
            this.at = at;
        } else {
            this.at = new AffineTransform();
        }
        this.at.translate(margin, margin);
    }

    public int getWidth() {
        return (at != null) ? 1 + (int) (at.getScaleX() * width + 2d * at.getTranslateX()) : width;
    }

    public int getHeight() {
        return (at != null) ? 1 + (int) (at.getScaleY() * height + 2d * at.getTranslateX()) : height;
    }

    public void prepareCommands(final boolean doClip) {
        if (!clipped) {
            clipped = true;

            // clip commands:
            final Rectangle2D clip = (doClip) ? new Rectangle2D.Double(0d, 0d, width, height) : null; // original size

            final ArrayList<DrawingCommand> _commands = commands;

            for (int i = 0, len = _commands.size(); i < len; i++) {
                _commands.get(i).clip(clip);
            }
        }
    }

    public BufferedImage prepareImage() {
//        return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        return ImageUtils.newImage(getWidth(), getHeight());
    }

    public Graphics2D prepareGraphics(BufferedImage image) {
        final Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setClip(new Rectangle2D.Double(0d, 0d, getWidth(), getHeight()));

        if (at != null) {
            graphics.setTransform(at);
        }

        return graphics;
    }

    public void execute(final Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        final AffineTransform _at = at;
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
