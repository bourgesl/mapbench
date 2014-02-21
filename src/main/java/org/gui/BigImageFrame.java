package org.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Big image frame (zoom slider)
 * @author bourgesl
 */
public class BigImageFrame extends JFrame implements ChangeListener {

    private static final long serialVersionUID = 1L;

    /** default screen margin */
    public final static int SCREEN_MARGIN = 50;
    /** default frame margin */
    public final static int FRAME_MARGIN = 60;

    /** default scale = 100% */
    public final static int DEF_SCALE = 100;

    /* members */
    protected final JScrollPane scrollPane;
    protected final JSlider zoomSlider;
    protected final JLabel scale;

    /** image panel */
    protected final BigImagePanel imagePanel;

    protected BigImageFrame(final String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create zoom slider:
        zoomSlider = new JSlider(JSlider.VERTICAL);
        // 1%:
        zoomSlider.setMinimum(1);
        // 1000%
        zoomSlider.setMaximum(1000);
        zoomSlider.setValue(100);
        zoomSlider.addChangeListener(this);

        // Create image panel:
        imagePanel = new BigImagePanel();

        // creation du scroll pane
        scrollPane = new JScrollPane(imagePanel);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        // Create scale label:
        scale = new JLabel();

        // add components:
        final Container pane = this.getContentPane();

        pane.add(zoomSlider, BorderLayout.WEST);
        pane.add(scrollPane, BorderLayout.CENTER);

        final JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        infoPanel.add(scale);
        infoPanel.add(new JLabel(" %"));
        pane.add(infoPanel, BorderLayout.PAGE_END);
    }

    /**
     * Create a new BigImageFrame with the given image displayed to 100%
     * @param title frame's title
     * @param image image to show
     * @return created BigImageFrame instance
     */
    public static BigImageFrame createAndShow(final String title, final BufferedImage image) {
        final BigImageFrame frame = new BigImageFrame(title);

        frame.setImage(image);

        frame.pack();

        return frame;
    }

    /**
     * Initialize the image panel with the given image and reset scale to 100%
     * @param image image to show
     */
    public void setImage(final BufferedImage image) {
        imagePanel.setImage(image);

        if (image != null) {
            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int width = (int) screenSize.getWidth();
            final int height = (int) screenSize.getHeight();

            // add frame margin:
            imgWidth += FRAME_MARGIN;
            imgHeight += FRAME_MARGIN;

            if ((imgWidth + SCREEN_MARGIN < width) && (imgHeight + SCREEN_MARGIN < height)) {
                this.setPreferredSize(new Dimension(imgWidth, imgHeight));
            } else {
                // full screen:
                setPreferredSize(new Dimension(width, height));
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                revalidate();
                setLocationRelativeTo(null);
            }
        }

        // reset the scale factor to 100%:
        setState(DEF_SCALE);
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final int value = zoomSlider.getValue();
        /* TODO: use timer to defer immediate execution (10ms delay) */
        setState(value);
    }

    @Override
    public void setState(final int value) {
        scale.setText(String.valueOf(value));
        imagePanel.setScale(.01f * value);
    }

    /**
     * @return the current interpolation method
     */
    public Object getInterpolation() {
        return imagePanel.getInterpolation();
    }

    /**
     * Set the interpolation method among:
     *  - RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
     *  - RenderingHints.VALUE_INTERPOLATION_BILINEAR
     *  - RenderingHints.VALUE_INTERPOLATION_BICUBIC
     * @param interpolation RenderingHints.VALUE_INTERPOLATION_*
     */
    public void setInterpolation(final Object interpolation) {
        imagePanel.setInterpolation(interpolation);
    }

}
