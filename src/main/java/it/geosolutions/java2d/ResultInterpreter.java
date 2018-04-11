/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package it.geosolutions.java2d;

import com.opencsv.CSVReader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author bourgesl
 */
public class ResultInterpreter {

    //Test|Threads|Ops|Med|Pct95|Avg|StdDev|Min|Max|FPS(avg)|TotalOps|[ms/op]|
    private static final String COL_TEST = "test";
    private static final String COL_TH = "threads";
    private static final String COL_PCT95 = "pct95";
    private static final String COL_FPS = "fps(med)";

    private static final Font DEFAULT_TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 32);
    private static final Font DEFAULT_TICK_FONT = new Font("SansSerif", Font.PLAIN, 20);
    private static final Font SMALL_TICK_FONT = new Font("SansSerif", Font.PLAIN, 18);

    private static final Map<String, PlotSetup> setups = new HashMap<String, PlotSetup>();

    private static final float ASPECT_RATIO = 1200f / 525;// 16f / 9f;

    private static final String CURRENT_SETUP;

    private static int CURRENT_REF_INDEX = 0;
    private static double GAIN_SIGN = 1.0;

    // original colors
    private static Color COL_1 = ChartColor.DARK_BLUE;
    private static Color COL_2 = ChartColor.DARK_RED;
    private static Color COL_3 = ChartColor.DARK_GREEN;
    private static Color COL_4 = ChartColor.PINK;
    private static Color COL_5 = ChartColor.YELLOW;

    private static boolean USE_BAR = true;

    private static int ONLY_TH = 0;

    private static boolean MEAN_ONLY = false;

    private static boolean USE_TITLE = true;

    static {
        Locale.setDefault(Locale.US);

        String JVM = "(jdk-9 b181)";

        setups.put("BEFORE", new PlotSetup("compare-before.png", "Ductus vs Pisces - Time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 250));

        setups.put("WITH_MARLIN", new PlotSetup("compare-with-marlin.png", "Ductus vs Pisces vs Marlin - Time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 250));
        setups.put("WITH_MARLIN_ZOOM", new PlotSetup("compare-with-marlin-zoom.png", "Ductus vs Pisces vs Marlin - Time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 100, 800));
        setups.put("MARLIN_RATIO_1T", new PlotSetup("compare-with-marlin-gain-1T.png", "Ductus vs Pisces vs Marlin - Rel. Gain - 1 Thread " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 25, -25, 500));
        setups.put("MARLIN_RATIO_2T", new PlotSetup("compare-with-marlin-gain-2T.png", "Ductus vs Pisces vs Marlin - Rel. Gain - 2 Threads " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 25, -25, 500));
        setups.put("MARLIN_RATIO_4T", new PlotSetup("compare-with-marlin-gain-4T.png", "Ductus vs Pisces vs Marlin - Rel. Gain - 4 Threads " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 25, -25, 500));
        setups.put("MARLIN_RATIO_MEAN", new PlotSetup("compare-with-marlin-gain.png", "Ductus vs Pisces vs Marlin - Rel. Gain " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 20));

        setups.put("DEFAULT", new PlotSetup("compare-demo-fps.png", "Ductus vs Pisces vs Marlin - FPS " + JVM,
                "Test Name - Number of threads", "FPS", Double.NaN, Double.NaN));

        setups.put("VOLATILE", new PlotSetup("compare-accel.png", "Marlin - Volatile vs Buffered Image - 95% time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", Double.NaN, Double.NaN));

        /*
        setups.put("DEF", new PlotSetup("compare-fps.png", "Marlin 0.8 (clipper ON / OFF) - DEMO - FPS " + JVM,
                "Test Name - Number of threads", "FPS(med)", Double.NaN, Double.NaN));
         */
        setups.put("MARLIN_VAR", new PlotSetup("diff-marlin-time.png", "Marlin jdk-10 vs jdk-9 - 95% time ",
                "Test Name - Number of threads", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_RATIO", new PlotSetup("diff-marlin-gain.png", "Marlin jdk-10 vs jdk-9 - Rel. Gain ",
                "Test Name - Number of threads", "GAIN (%)", 2, -12, 16));

        setups.put("MARLIN_VAR_SP", new PlotSetup("subpixel-marlin-time.png", "Marlin jdk-9 - Subpixel setting - 95% time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_SP_RATIO", new PlotSetup("subpixel-marlin-gain.png", "Marlin jdk-9 - Subpixel setting - Rel Gain " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 20, -60, 50));

        JVM = "(jdk-10 17.10)";
        setups.put("MARLIN_VAR_TILE", new PlotSetup("tile-marlin-time.png", "Marlin jdk-10 - TileSize setting - 95% time " + JVM,
                "Test Name - 1T", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_TILE_RATIO", new PlotSetup("tile-marlin-gain.png", "Marlin jdk-10 - TileSize setting - Rel Gain " + JVM,
                "Test Name - 1T", "GAIN (%)", 5, -25, 70));

        setups.put("MARLIN_VAR_TILE_BEST", new PlotSetup("tile-best-marlin-time.png", "Marlin jdk-10 - Best TileSize setting - 95% time " + JVM,
                "Test Name - 1T", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_TILE_BEST_RATIO", new PlotSetup("tile-best-marlin-gain.png", "Marlin jdk-10 - Best TileSize setting - Rel Gain " + JVM,
                "Test Name - 1T", "GAIN (%)", 5, -25, 70));

        JVM = "(jdk-8 b144)";

        setups.put("MARLIN_VAR_CLIP", new PlotSetup("clip-marlin-time.png", "Marlin 0.8.1 - Path Clipping On vs Off - 95% time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 25, 350));
        setups.put("MARLIN_VAR_CLIP_RATIO", new PlotSetup("clip-marlin-gain.png", "Marlin 0.8.1 - Path Clipping On vs Off - Rel Gain " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 10, -2, 70));

        setups.put("MARLIN_VAR_HIST", new PlotSetup("release-marlin-time.png", "Marlin 0.3 vs 0.5.6 vs 0.7.4 (jdk-9) vs 0.8.1 - 95% time " + JVM,
                "Test Name", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_HIST_RATIO", new PlotSetup("release-marlin-gain.png", "Marlin 0.3 vs 0.5.6 vs 0.7.4 (jdk-9) vs 0.8.1 - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10, -40, 90));

        setups.put("MARLIN_VAR_PIPE", new PlotSetup("pipeline-marlin-time.png", "Java2D & Marlin stages - 95% time " + JVM,
                "Test Name", "95% Time (ms)", 250));
        setups.put("MARLIN_VAR_PIPE_RATIO", new PlotSetup("pipeline-marlin-gain.png", "Java2D & Marlin stages - Rel Gain " + JVM,
                "Test Name", "RATIO (%)", 10));

        setups.put("MARLIN_FX", new PlotSetup("fx-marlin.png", "Native Pisces vs Java Pisces vs MarlinFX - Time " + JVM,
                "Test Name", "95% Time (ms)", 100));
        setups.put("MARLIN_FX_RATIO", new PlotSetup("fx-marlin-gain.png", "Native Pisces vs Java Pisces vs MarlinFX - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10));

        JVM = "(jdk-9 b181)";
        setups.put("MARLIN_FX9", new PlotSetup("fx9-marlin.png", "Native Pisces vs Java Pisces vs MarlinFX - Time " + JVM,
                "Test Name", "95% Time (ms)", 100));
        setups.put("MARLIN_FX9_RATIO", new PlotSetup("fx9-marlin-gain.png", "Native Pisces vs Java Pisces vs MarlinFX - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10));

        // 2018.2: 
        JVM = "(jdk-8 b152)";
        setups.put("MARLIN_091", new PlotSetup("clip-marlin-091-time.png", "Marlin 0.9.1 vs 0.8.2 - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 20, 0, 180));
        setups.put("MARLIN_091_RATIO", new PlotSetup("clip-marlin-091-gain.png", "Marlin 0.9.1 vs 0.8.2 - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 5, -5, 60));

        setups.put("MARLIN_091_4K", new PlotSetup("clip-marlin-091-4K-time.png", "Marlin 0.9.1 vs 0.8.2 - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 50));
        setups.put("MARLIN_091_4K_RATIO", new PlotSetup("clip-marlin-091-4K-gain.png", "Marlin 0.9.1 vs 0.8.2 - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 5, -25, 25));

        setups.put("MARLIN_091_4K_VOL", new PlotSetup("marlin-091-4K-vol-time.png", "Marlin 0.9.1 vs 0.8.2 - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 50));
        setups.put("MARLIN_091_4K_VOL_RATIO", new PlotSetup("marlin-091-4K-vol-gain.png", "Marlin 0.9.1 vs 0.8.2 - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 5, -10, 80));

        JVM = "(jetbrains jdk-8 b152)";
        setups.put("MARLIN_091_4K_VOL_JB", new PlotSetup("marlin-091-4K-vol-jb-time.png", "Marlin 0.9.1 vs 0.7.3.4 - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 50));
        setups.put("MARLIN_091_4K_VOL_JB_RATIO", new PlotSetup("marlin-091-4K-vol-jb-gain.png", "Marlin 0.9.1 vs 0.7.3.4 - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 5, -10, 80));

        // 2018.3: 
        JVM = "(jdk-8 b161)";
        setups.put("MARLIN_091_HD_VOL_CMP", new PlotSetup("marlin-091-d3d-xr-time.png", "Marlin 0.9.1 with Direct3D / XRender java2D pipelines - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 50));
        setups.put("MARLIN_091_HD_VOL_CMP_RATIO", new PlotSetup("marlin-091-d3d-xr-gain.png", "Marlin 0.9.1 with Direct3D / XRender java2D pipelines - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10, -250, 20));
        
        JVM = "(jdk-11 ea)";
        setups.put("MARLIN_092_4K_VOL_CMP", new PlotSetup("marlin-092-4k-ogl-time.png", "Marlin 0.9.2 with OpenGL / XRender java2D pipelines - 95% Time " + JVM,
                "Test Name", "95% Time (ms)", 50));
        setups.put("MARLIN_092_4K_VOL_CMP_RATIO", new PlotSetup("marlin-092-4k-ogl-gain.png", "Marlin 0.9.1 with OpenGL / XRender java2D pipelines - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10, -120, 40));

        CURRENT_SETUP = "MARLIN_VAR_RATIO";
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("ResultInterpreter requires arguments !");

            doScript();
        } else {
            final List<String> testFiles = new ArrayList<String>();

            // parse command line arguments :
            for (final String arg : args) {
                if (arg.startsWith("-")) {
                    if (arg.equals("-h") || arg.equals("-help")) {
                        showArgumentsHelp();
                        System.exit(0);
                    } else {
                        System.err.println("'" + arg + "' option not supported.");
                    }
                } else {
                    testFiles.add(arg);
                }
            }

            if (testFiles.isEmpty()) {
                System.err.println("Missing file name argument.");
            }

            new ResultInterpreter(CURRENT_SETUP, testFiles).showAndSavePlot();
        }
        System.out.println("ResultInterpreter: done.");
    }

    private static void doScript() throws IOException {
        final Color red = new Color(239, 41, 41);
        final Color blue = new Color(52, 101, 164);
        final Color green = new Color(67, 195, 48);
        final Color violet = new Color(194, 84, 210);
        final Color turquoise = new Color(99, 187, 238);

        final List<String> testFiles = new ArrayList<String>();

        if (false) {

            ONLY_TH = 0;
            USE_BAR = true;
            COL_1 = red; // oracle = ductus
            COL_2 = blue; // pisces
            COL_3 = green; // marlin

            testFiles.clear();
            testFiles.add("jdk9_dc.log");
            testFiles.add("jdk9_pisces.log");
            new ResultInterpreter("BEFORE", testFiles).showAndSavePlot();

            testFiles.clear();
            testFiles.add("jdk9_dc.log");
            testFiles.add("jdk9_pisces.log");
            testFiles.add("jdk9_marlin.log");
            new ResultInterpreter("WITH_MARLIN", testFiles).showAndSavePlot();
//        new ResultInterpreter("WITH_MARLIN_ZOOM", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2;
            ONLY_TH = 1;
            new ResultInterpreter("MARLIN_RATIO_1T", testFiles).showAndSavePlot();
            ONLY_TH = 2;
            new ResultInterpreter("MARLIN_RATIO_2T", testFiles).showAndSavePlot();
            ONLY_TH = 4;
            new ResultInterpreter("MARLIN_RATIO_4T", testFiles).showAndSavePlot();

            ONLY_TH = 0;
            MEAN_ONLY = true;
            new ResultInterpreter("MARLIN_RATIO_MEAN", testFiles).showAndSavePlot();
            MEAN_ONLY = false;

            COL_1 = green;
            COL_2 = violet;
            COL_3 = turquoise;
            COL_4 = red;
            COL_5 = blue;
            ONLY_TH = 1;
            GAIN_SIGN = -1.0;

            testFiles.clear();
            testFiles.add("jdk9_marlin.log");
            testFiles.add("jdk10_marlin_curve_th_074.log");
            testFiles.add("jdk10_marlin.log");
            testFiles.add("jdk10_marlinD.log");
            new ResultInterpreter("MARLIN_VAR", testFiles).showAndSavePlot();

            USE_BAR = false;
            CURRENT_REF_INDEX = 0;
            new ResultInterpreter("MARLIN_VAR_RATIO", testFiles).showAndSavePlot();

            USE_BAR = true;
            testFiles.clear();
            testFiles.add("jdk9_marlin_sp1.log");
            testFiles.add("jdk9_marlin_sp2.log");
            testFiles.add("jdk9_marlin.log");
            testFiles.add("jdk9_marlin_sp4.log");
            testFiles.add("jdk9_marlin_sp6.log");
            new ResultInterpreter("MARLIN_VAR_SP", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2; // 3x3
            USE_BAR = false;
            // ignore last 6
            testFiles.remove(testFiles.size() - 1);
            new ResultInterpreter("MARLIN_VAR_SP_RATIO", testFiles).showAndSavePlot();

            // Tile
            USE_BAR = true;
            testFiles.clear();
            testFiles.add("jdk10_buf_32.log");
            testFiles.add("jdk10_buf_128x64.log");
            testFiles.add("jdk10_volatile_64.log");
            testFiles.add("jdk10_volatile_128.log");
            testFiles.add("jdk10_volatile_256.log");
            new ResultInterpreter("MARLIN_VAR_TILE", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0; // 32x32
            USE_BAR = false;
            new ResultInterpreter("MARLIN_VAR_TILE_RATIO", testFiles).showAndSavePlot();

            USE_BAR = true;
            testFiles.clear();
            testFiles.add("jdk10_buf_32.log");
            testFiles.add("jdk10_buf_128x64.log");
            testFiles.add("jdk10_volatile_128x32.log");
            testFiles.add("jdk10_volatile_128x64.log");
            testFiles.add("jdk10_volatile_128.log");
            new ResultInterpreter("MARLIN_VAR_TILE_BEST", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0; // 32x32
            USE_BAR = false;
            new ResultInterpreter("MARLIN_VAR_TILE_BEST_RATIO", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2; // 0.7.4
            USE_BAR = true;
            testFiles.clear();
            testFiles.add("jdk8_marlin_03.log");
            testFiles.add("jdk8_marlin_056.log");
            testFiles.add("jdk9_marlin.log");
            testFiles.add("jdk8_marlin_0811.log");
            new ResultInterpreter("MARLIN_VAR_HIST", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0; // 0.3
            COL_1 = violet;
            COL_2 = turquoise;
            COL_3 = red;
            new ResultInterpreter("MARLIN_VAR_HIST_RATIO", testFiles).showAndSavePlot();
            COL_1 = green;
            COL_2 = violet;
            COL_3 = turquoise;

            USE_BAR = true;
            GAIN_SIGN = 1.0;
            testFiles.clear();
            testFiles.add("jdk8_marlin_no_render.log"); // stage1: preparing path
            testFiles.add("jdk8_marlin_no_blending.log"); // stage1+2: preparing path + rendering
            testFiles.add("jdk8_marlin_0811.log");      // stage1+2+3: preparing path + rendering + blending
            new ResultInterpreter("MARLIN_VAR_PIPE", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2;
            new ResultInterpreter("MARLIN_VAR_PIPE_RATIO", testFiles).showAndSavePlot();

            // Clipping 0.8.1:
            USE_BAR = true;
            GAIN_SIGN = -1.0;
            COL_1 = green;
            COL_2 = violet;
            testFiles.clear();
            testFiles.add("big_test_clip_off.log");
            testFiles.add("big_test_clip_on.log");
            new ResultInterpreter("MARLIN_VAR_CLIP", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0;
            COL_1 = violet;
            new ResultInterpreter("MARLIN_VAR_CLIP_RATIO", testFiles).showAndSavePlot();

            COL_1 = red; // native pisces
            COL_2 = blue; // java pisces
            COL_3 = green; // marlin

            testFiles.clear();
            testFiles.add("fxdemo8_native-pisces.log");
            testFiles.add("fxdemo8_java-pisces.log");
            testFiles.add("fxdemo8_marlinD.log");
            new ResultInterpreter("MARLIN_FX", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2;
            GAIN_SIGN = 1.0;
            new ResultInterpreter("MARLIN_FX_RATIO", testFiles).showAndSavePlot();

            testFiles.clear();
            testFiles.add("fxdemo9_native-pisces.log");
            testFiles.add("fxdemo9_java-pisces.log");
            testFiles.add("fxdemo9_marlinD.log");
            new ResultInterpreter("MARLIN_FX9", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 2;
            GAIN_SIGN = 1.0;
            new ResultInterpreter("MARLIN_FX9_RATIO", testFiles).showAndSavePlot();

            // Clipping 0.8.1:
            USE_BAR = true;
            GAIN_SIGN = -1.0;
            COL_1 = blue;
            COL_2 = green;
            testFiles.clear();
            testFiles.add("marlin_082_clip_quality.log");
            testFiles.add("marlin_091_clip_subd_xing_quality_opt_100_final.log");
            new ResultInterpreter("MARLIN_091", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0;
            COL_1 = green;
            new ResultInterpreter("MARLIN_091_RATIO", testFiles).showAndSavePlot();

            USE_BAR = true;
            GAIN_SIGN = -1.0;
            COL_1 = blue;
            COL_2 = green;
            COL_3 = violet;
            testFiles.clear();
            testFiles.add("marlin_082_4K_dashed_quality.log");
            testFiles.add("marlin_091_4K_dashed_quality_subd_xing_last_opt_100_final.log");
            testFiles.add("marlin_091_4K_dashed_quality_subd_xing_last_opt_100_disable.log");
            new ResultInterpreter("MARLIN_091_4K", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0;
            COL_1 = green;
            COL_2 = violet;
            new ResultInterpreter("MARLIN_091_4K_RATIO", testFiles).showAndSavePlot();

            // Volatile
            USE_BAR = true;
            GAIN_SIGN = -1.0;
            COL_1 = green;
            COL_2 = blue;
            testFiles.clear();
            testFiles.add("marlin_082_4K_dashed_vol.log");
            testFiles.add("marlin_091_4K_dashed_vol.log");
            new ResultInterpreter("MARLIN_091_4K_VOL", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0;
            COL_1 = blue;
            new ResultInterpreter("MARLIN_091_4K_VOL_RATIO", testFiles).showAndSavePlot();

            // JETBRAINS JDK8
            USE_BAR = true;
            GAIN_SIGN = -1.0;
            COL_1 = green;
            COL_2 = blue;
            testFiles.clear();
            testFiles.add("marlin_074_4K_dashed_vol_jb_origin.log");
            testFiles.add("marlin_091_4K_dashed_vol_jb.log");
            new ResultInterpreter("MARLIN_091_4K_VOL_JB", testFiles).showAndSavePlot();

            CURRENT_REF_INDEX = 0;
            COL_1 = blue;
            new ResultInterpreter("MARLIN_091_4K_VOL_JB_RATIO", testFiles).showAndSavePlot();
        }

        USE_BAR = true;
        GAIN_SIGN = -1.0;
        COL_1 = red;
        COL_2 = blue;
        COL_3 = green;
        COL_4 = violet;
        COL_5 = turquoise;
        testFiles.clear();
        testFiles.add("win_marlin_082_tile_32x32.log");
        testFiles.add("win_marlin_091_tile_128x64.log");
        testFiles.add("win_marlin_091_tile_32x32.log");
        testFiles.add("win_092_4M_tile_32x32.log");
//       testFiles.add("win_091_ogl_tile_128x64.log");
        testFiles.add("linux_nv1070_xr_082_tile_32x32.log");
        testFiles.add("linux_nv1070_xr_091_tile_128x64.log");

        new ResultInterpreter("MARLIN_091_HD_VOL_CMP", testFiles).showAndSavePlot();

        CURRENT_REF_INDEX = 5;
        new ResultInterpreter("MARLIN_091_HD_VOL_CMP_RATIO", testFiles).showAndSavePlot();

        /*
        marlin_091_4K_dashed_vol_ogl_32K.log
        marlin_091_4K_dashed_vol_ogl_4M_20.log
        marlin_091_4K_dashed_vol_ojdkcl_ogl_4M_20_tiles_128x64.log
         */
         USE_BAR = true;
        GAIN_SIGN = -1.0;
        COL_1 = red;
        COL_2 = blue;
        COL_3 = green;
               testFiles.clear();
        testFiles.add("marlin_091_4K_dashed_vol_ogl_32K.log");
        testFiles.add("marlin_091_4K_dashed_vol_ogl_4M_20.log");
        testFiles.add("marlin_091_4K_dashed_vol_ojdkcl_ogl_4M_20_tiles_128x64.log");
    
        testFiles.add("marlin_091_4K_dashed_vol_ojdkcl_xr_def_off.log");

        new ResultInterpreter("MARLIN_092_4K_VOL_CMP", testFiles).showAndSavePlot();

        CURRENT_REF_INDEX = 3;
        new ResultInterpreter("MARLIN_092_4K_VOL_CMP_RATIO", testFiles).showAndSavePlot();
    }

    /** Show command arguments help */
    private static void showArgumentsHelp() {
        System.out.println(
                "-------------------------------------------------------------------------");
        System.out.println(
                "Usage: ResultInterpreter <file names>");
        System.out.println(
                "------------- Arguments help --------------------------------------------");
        System.out.println(
                "| Key          Value           Description                              |");
        System.out.println(
                "|-----------------------------------------------------------------------|");
        System.out.println(
                "| [-h|-help]                   Show arguments help                      |");
        System.out.println(
                "-------------------------------------------------------------------------");
    }

    /* members */
    private final String id;
    private final List<ResultParser> results = new ArrayList<ResultParser>();

    public ResultInterpreter(final String id, final List<String> testFiles) throws IOException {
        this.id = id;
        for (String test : testFiles) {
            results.add(new ResultParser(new File(test)));
        }
    }

    public void showAndSavePlot() throws IOException {
        final PlotSetup setup = setups.get(id);

        if (setup == null) {
            throw new IllegalStateException("Missing plot setup for id: " + id);
        }

        showPanel(createDemoPanel(createDataset(setup)));
        if (false) {
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException ie) {
            } finally {
            }
        }
    }

    private CustomCategoryDataset createDataset(final PlotSetup setup) {

        final CustomCategoryDataset dataset = new CustomCategoryDataset(setup);

        // compare with nth dataset:
        final boolean doRatio = setup.yLabel.startsWith("RATIO");
        final boolean doGain = setup.yLabel.startsWith("GAIN");

        final ResultParser ref = ((doRatio || doGain) && results.size() > CURRENT_REF_INDEX) ? results.get(CURRENT_REF_INDEX) : null;

        final String dataCol = COL_PCT95;

        final int refIdxVAL;
        final List<Data> refDatas;

        if (ref == null) {
            refIdxVAL = -1;
            refDatas = null;
        } else {
            System.out.println("REF: " + ref.filepath);
            refIdxVAL = ref.columns.get(dataCol);
            refDatas = ref.datas;
        }

        for (ResultParser result : results) {
            if (doGain && USE_BAR && (ref != null) && (ref == result)) {
                continue;
            }

            final String datasetKey = result.filepath.getName();
            final Map<String, Integer> cols = result.columns;
            final List<Data> datas = result.datas;

            final int idxTH = cols.get(COL_TH);
            final int idxVAL = cols.get(dataCol);
            
            if (refDatas != null && datas.size() != refDatas.size()) {
                throw new IllegalStateException("incompatible test result vs ref result");
            }

            final Map<Integer, Accumulator> statTH = new LinkedHashMap<Integer, Accumulator>();

            for (int i = 0, len = datas.size(); i < len; i++) {
                final Data d = datas.get(i);
                final int th = new Double(d.values[idxTH]).intValue();

                if ((ONLY_TH != 0) && (ONLY_TH != th)) {
                    continue;
                }

                double val = d.values[idxVAL];

                if (refIdxVAL != -1) {
                    // gain:
                    val = 100.0 * GAIN_SIGN * ((val / refDatas.get(i).values[refIdxVAL]) + ((doGain) ? -1.0 : 0.0)); // -1 to have relative gain
                }

                if (!MEAN_ONLY) {
                    // Fix key: 'test - nT'
                    String key = d.key.replaceFirst(".ser", "").replaceFirst("dc_", "").replaceFirst("shp_", "");
                    if (key.length() > 20) {
                        key = key.substring(0, 20);
                    }
                    key += " - " + th + "T";

                    //                System.out.println(key + " = "+val);
                    dataset.addValue(val, datasetKey, key);
                }

                Accumulator acc = statTH.get(th);
                if (acc == null) {
                    acc = new Accumulator();
                    statTH.put(th, acc);
                }
                acc.add(val);
            }

            System.out.println("Stats[" + datasetKey + "]: " + statTH);

            for (Map.Entry<Integer, Accumulator> e : statTH.entrySet()) {
                final int th = e.getKey();
                final double val = e.getValue().mean();

                dataset.addValue(val, datasetKey, "MEAN - " + th + "T");
            }
        }
        return dataset;
    }

    final static class ResultParser {

        /* members */
        final File filepath;
        final Map<String, Integer> columns;
        final List<Data> datas;

        ResultParser(File filepath) throws IOException {
            this.filepath = filepath;

            // Extract TSV part:
            final String dataTSV = extractTSVBlock(filepath);
            if (false) {
                System.out.println("filepath: " + filepath);
                System.out.println("TSV:\n" + dataTSV);
            }

            CSVReader reader = new CSVReader(new StringReader(dataTSV), '\t');

            List<String[]> rows = reader.readAll();

            final int nRows = rows.size() - 1;
            if (nRows < 1) {
                throw new IllegalStateException("empty data rows !");
            }

            // header
            this.columns = analyzeCols(rows.get(0));
//            System.out.println("columnMap:\n" + columns);

            final int nCols = columns.size();
//            System.out.println("nCols:\n" + nCols);
            if (nCols <= 3) {
                throw new IllegalStateException("empty cols !");
            }

            this.datas = new ArrayList<Data>(nRows);

            final int idxTest = columns.get(COL_TEST);

            for (int i = 1; i < rows.size(); i++) {
                final String[] row = rows.get(i);

                if (false) {
                    for (String val : row) {
                        System.out.print(val.trim());
                        System.out.print('|');
                    }
                    System.out.println();
                }

                final Data data = new Data();
                data.key = row[idxTest].trim();
                data.values = new double[nCols];

                for (int j = 0; j < row.length; j++) {
                    if (j != idxTest) {
                        data.values[j] = Double.parseDouble(row[j]);
                    }
                }

                this.datas.add(data);
            }

//            System.out.println("Datas:\n" + this.datas);
        }

        private Map<String, Integer> analyzeCols(String[] row) {
            //Test|Threads|Ops|Med|Pct95|Avg|StdDev|Min|Max|FPS(avg)|TotalOps|[ms/op]|
            final Map<String, Integer> columnMap = new LinkedHashMap<String, Integer>(row.length);

            int i = 0;
            for (String val : row) {
                val = val.trim().toLowerCase();

                if (!val.startsWith("[")) {
                    columnMap.put(val, i);
                    i++;
                }
            }
            return columnMap;
        }

        void dump() {

        }

        private String extractTSVBlock(final File filepath) throws IOException {
            String result = null;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"));

                // Use one string buffer with the best guessed initial capacity:
                final StringBuilder sb = new StringBuilder(8192);

                boolean isData = false;

                String line;
                while (reader.ready()) {
                    line = reader.readLine();

                    if (isData) {
                        if (line.isEmpty() || line.startsWith("Scores:")) {
                            break;
                        }
                        sb.append(line).append('\n');
                    } else // look for "TEST results:"
                    if (line.startsWith("TEST results:")) {
                        isData = true;
                    }
                }

                result = sb.toString();

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            return result;
        }

    }

    final static class Data {

        String key;
        double values[];

        public String toString() {
            return key + ":" + Arrays.toString(values);
        }
    }

    public static void showPanel(final JPanel panel) {
        final JFrame frame = new JFrame("Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
    }

    private static JFreeChart createChart(CustomCategoryDataset dataset) {
        final String title;
        final String xLabel;
        if (USE_TITLE) {
            title = dataset.info.title;
            xLabel = dataset.info.xLabel;
        } else {
            title = null;
            xLabel = null;
        }
        JFreeChart chart;
        if (USE_BAR) {
            chart = ChartFactory.createBarChart(title, xLabel, dataset.info.yLabel,
                    dataset, PlotOrientation.VERTICAL, true, true, false);
        } else {
            chart = ChartFactory.createLineChart(title, xLabel, dataset.info.yLabel,
                    dataset, PlotOrientation.VERTICAL, true, true, false);
        }

        if (chart.getTitle() != null) {
            chart.getTitle().setFont(DEFAULT_TITLE_FONT);
        }
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);

        ValueAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (!Double.isNaN(dataset.info.yTick)) {
            ((NumberAxis) axis).setTickUnit(new NumberTickUnit(dataset.info.yTick));
        }
        if (!Double.isNaN(dataset.info.yMax)) {
            axis.setRange(dataset.info.yMin, dataset.info.yMax);
        }
        axis.setLabelFont(DEFAULT_LABEL_FONT);
        axis.setTickLabelFont(DEFAULT_TICK_FONT);

        CategoryItemRenderer rdr = plot.getRenderer();
        if (rdr instanceof BarRenderer) {
            BarRenderer br = (BarRenderer) rdr;
            br.setItemMargin(0.05);
            br.setDrawBarOutline(false);
            br.setShadowVisible(false);
            br.setBarPainter(new StandardBarPainter());
        } else {
            LineAndShapeRenderer lr = (LineAndShapeRenderer) rdr;
            lr.setBaseShapesVisible(true);
            lr.setAutoPopulateSeriesStroke(false);
            lr.setBaseStroke(new BasicStroke(4f));
            lr.setAutoPopulateSeriesShape(false);
        }

        rdr.setSeriesPaint(0, COL_1);
        rdr.setSeriesPaint(1, COL_2);
        rdr.setSeriesPaint(2, COL_3);
        rdr.setSeriesPaint(3, COL_4);
        rdr.setSeriesPaint(4, COL_5);

        CategoryAxis catAxis = plot.getDomainAxis();
        catAxis.setLabelFont(DEFAULT_TICK_FONT);
        catAxis.setLowerMargin(0.01);
        catAxis.setCategoryMargin(0.2);
        catAxis.setUpperMargin(0.01);
        catAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
        catAxis.setTickLabelFont(SMALL_TICK_FONT);

        chart.getLegend().setItemFont(DEFAULT_LABEL_FONT);
        return chart;
    }

    static JPanel createDemoPanel(CustomCategoryDataset dataset) throws IOException {
        JFreeChart chart = createChart(dataset);

        final int width = 1920;
        final int height = ratioHeight(width);

        final String file = ((MEAN_ONLY) ? "mean-" : "") + dataset.info.pngFile;

        System.out.println("Saving " + file);
        ChartUtilities.saveChartAsPNG(new File(file), chart, width, height);

        final ChartPanel panel = new ChartPanel(chart, width, height,
                300, 200, 1920, 1080,
                true, true, true, true, true, true);

        panel.setBackground(Color.WHITE);

        return panel;
    }

    static int ratioHeight(int width) {
        return Math.round(width / ASPECT_RATIO);
    }

    final static class CustomCategoryDataset extends DefaultCategoryDataset {

        final PlotSetup info;

        CustomCategoryDataset(PlotSetup plotInfo) {
            this.info = plotInfo;
        }
    }

    final static class PlotSetup {

        final String pngFile;
        final String title;
        final String xLabel;
        final String yLabel;
        final double yTick;
        final double yMin;
        final double yMax;

        PlotSetup(String pngFile, String title, String xLabel, String yLabel) {
            this(pngFile, title, xLabel, yLabel, Double.NaN, Double.NaN);
        }

        PlotSetup(String pngFile, String title, String xLabel, String yLabel, double yTick) {
            this(pngFile, title, xLabel, yLabel, yTick, Double.NaN);
        }

        PlotSetup(String pngFile, String title, String xLabel, String yLabel, double yTick, double yMax) {
            this(pngFile, title, xLabel, yLabel, yTick, Double.NaN, yMax);
        }

        PlotSetup(String pngFile, String title, String xLabel, String yLabel, double yTick, double yMin, double yMax) {
            this.pngFile = pngFile;
            this.title = title;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.yTick = yTick;
            this.yMin = (!Double.isNaN(yMin)) ? yMin : 0;
            this.yMax = yMax;
        }
    }

    final static class Accumulator {

        int n = 0;
        double sum = 0.0;

        void add(double v) {
            sum += v;
            n++;
        }

        double mean() {
            return (n != 0) ? (sum / n) : 0;
        }

        @Override
        public String toString() {
            return "mean=" + mean();
        }

    }
}
