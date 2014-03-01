package org.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Big image frame (zoom slider)
 * @author bourgesl
 */
public class BigImageFrame extends JFrame implements ChangeListener, ItemListener {

    /**
     * Create a new BigImageFrame with the given image displayed to 100%
     * @param title frame's title
     * @param image image to show
     * @param refImage reference image to show
     * @param diffImage difference image to show
     * @return created BigImageFrame instance
     */
    public static BigImageFrame createAndShow(final String title, final BufferedImage image,
                                              final BufferedImage refImage, final BufferedImage diffImage) {
        final BigImageFrame frame = new BigImageFrame(title);
        frame.image = image;
        frame.refImage = refImage;
        frame.diffImage = diffImage;

        frame.viewCombo.setSelectedItem(ViewMode.Test);
        frame.viewCombo.setEnabled((refImage != null) || (diffImage != null));

        frame.setImage(image);
        frame.reset();
        frame.pack();

        return frame;
    }

    private static final long serialVersionUID = 1L;

    private enum ViewMode {

        Test,
        Ref,
        Diff;
    }

    /* default timer delay (10 milliseconds) */
    public final static int DELAY = 10;
    /** default screen margin */
    public final static int SCREEN_MARGIN = 50;
    /** default frame margin */
    public final static int FRAME_MARGIN = 80;
    /** default scale = 100% */
    public final static int DEF_SCALE = 100;

    /* members */
    protected final JScrollPane scrollPane;
    protected final JSlider zoomSlider;
    protected final JLabel scale;
    protected final JComboBox<ViewMode> viewCombo;
    protected final Timer timer;

    /** image panel */
    protected final BigImagePanel imagePanel;
    /** main image */
    protected BufferedImage image = null;
    /** ref image */
    protected BufferedImage refImage = null;
    /** diff image */
    protected BufferedImage diffImage = null;
    /** current image displayed */
    protected BufferedImage current = null;

    /**
     * Protected Constructor
     * @param title frame's title
     */
    protected BigImageFrame(final String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        timer = new Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScaleImmediately(zoomSlider.getValue());
            }

        });
        timer.setRepeats(false);

        // Create zoom slider:
        zoomSlider = new JSlider(JSlider.VERTICAL);
        // 1%:
        zoomSlider.setMinimum(1);
        // 1000%
        zoomSlider.setMaximum(1000);
        zoomSlider.setValue(DEF_SCALE);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setMajorTickSpacing(100);
        zoomSlider.setMinorTickSpacing(25);
        zoomSlider.addChangeListener(this);

        // Create image panel:
        imagePanel = new BigImagePanel();

        // creation du scroll pane
        scrollPane = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        /* to avoid xrender artifacts: use simple scroll mode */
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);

        // Create scale label:
        scale = new JLabel();

        // add components:
        final Container pane = this.getContentPane();

        pane.add(zoomSlider, BorderLayout.WEST);
        pane.add(scrollPane, BorderLayout.CENTER);

        final JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        infoPanel.add(scale);
        infoPanel.add(new JLabel(" %"));

        viewCombo = new JComboBox<ViewMode>(ViewMode.values());
        viewCombo.addItemListener(this);
//        viewCombo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
        infoPanel.add(viewCombo);

        pane.add(infoPanel, BorderLayout.PAGE_END);
    }

    @Override
    public void dispose() {
        super.dispose();
        timer.stop();
    }

    /**
     * Update the image panel with the given image
     * @param image image to show
     */
    public void setImage(final BufferedImage image) {
//        System.out.println("setImage : " + image);
        current = image;
        imagePanel.setImage(image);
    }

    public void reset() {
        if (current != null) {
            int imgWidth = current.getWidth();
            int imgHeight = current.getHeight();

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
        zoomSlider.setValue(DEF_SCALE);
    }

    @Override
    public void stateChanged(final ChangeEvent ce) {
        /* use timer to defer immediate execution (10ms delay) */
        timer.restart();
    }

    public void updateScaleImmediately(final int value) {
        scale.setText(String.valueOf(value));

        // Get viewport center point before changing scale:
        final Dimension viewportSize = scrollPane.getViewport().getSize();
        final Dimension viewSize = scrollPane.getViewport().getView().getSize();

        final Point corner = scrollPane.getViewport().getViewPosition();

        final float xPct = (corner.x + viewportSize.width / 2) / ((float) viewSize.width);
        final float yPct = (corner.y + viewportSize.height / 2) / ((float) viewSize.height);

        // change scale:
        imagePanel.setScale(.01f * value);

        // Get new view size:
        final Dimension newViewSize = imagePanel.getPreferredSize();

        // Compute the new viewport center point according to changed scale:
        final int px = (int) (xPct * newViewSize.width) - viewportSize.width / 2;
        final int py = (int) (yPct * newViewSize.height) - viewportSize.height / 2;

        final Point newCorner = new Point(px, py);

        scrollPane.getViewport().setViewPosition(newCorner);
    }

    @Override
    public void itemStateChanged(final ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            BufferedImage newImage = null;
            switch ((ViewMode) ie.getItem()) {
                default:
                case Test:
                    newImage = image;
                    break;
                case Ref:
                    newImage = refImage;
                    break;
                case Diff:
                    newImage = diffImage;
                    break;
            }
            if (newImage != current) {
                setImage(newImage);
            }
        }
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
