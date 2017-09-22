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

    private static final Font DEFAULT_TITLE_FONT = new Font("SansSerif", Font.BOLD, 32);
    private static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font DEFAULT_TICK_FONT = new Font("SansSerif", Font.PLAIN, 24);
    private static final Font SMALL_TICK_FONT = new Font("SansSerif", Font.PLAIN, 18);

    private static final Map<String, PlotSetup> setups = new HashMap<String, PlotSetup>();

    private static final float ASPECT_RATIO = 16f / 9f;

    private static final String CURRENT_SETUP;

    private static int CURRENT_REF_INDEX = 0;
    private static double GAIN_SIGN = 1.0;

    // original colors    
    private static Color COL_1 = ChartColor.DARK_BLUE;
    private static Color COL_2 = ChartColor.DARK_RED;
    private static Color COL_3 = ChartColor.DARK_GREEN;

    private static boolean USE_BAR = true;

    private static int ONLY_TH = 0;

    private static boolean MEAN_ONLY = false;

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

        JVM = "(jdk-8 b144)";
        
        setups.put("MARLIN_VAR_CLIP", new PlotSetup("clip-marlin-time.png", "Marlin 0.8.1 - Path Clipping On vs Off - 95% time " + JVM,
                "Test Name - Number of threads", "95% Time (ms)", 25, 350));
        setups.put("MARLIN_VAR_CLIP_RATIO", new PlotSetup("clip-marlin-gain.png", "Marlin 0.8.1 - Path Clipping On vs Off - Rel Gain " + JVM,
                "Test Name - Number of threads", "GAIN (%)", 10, -2, 70));

        setups.put("MARLIN_FX", new PlotSetup("fx-marlin.png", "Native Pisces vs Java Pisces vs MarlinFX - Time " + JVM,
                "Test Name", "95% Time (ms)", 100));
        setups.put("MARLIN_FX_RATIO", new PlotSetup("fx-marlin-gain.png", "Native Pisces vs Java Pisces vs MarlinFX - Rel Gain " + JVM,
                "Test Name", "GAIN (%)", 10));
        
        CURRENT_SETUP = "MARLIN_VAR_RATIO";
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("ResultInterpreter requires arguments !");

            doScript();
            return;
        }

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

    private static void doScript() throws IOException {
        final Color red = new Color(239, 41, 41);
        final Color blue = new Color(52, 101, 164);
        final Color green = new Color(67, 195, 48);
        final Color violet = new Color(194, 84, 210);
        final Color turquoise = new Color(99, 187, 238);

        ONLY_TH = 0;
        USE_BAR = true;
        COL_1 = red; // oracle = ductus
        COL_2 = blue; // pisces
        COL_3 = green; // marlin

        final List<String> testFiles = new ArrayList<String>();

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

        // Clipping 0.8.1:
        USE_BAR = true;
        testFiles.clear();
        testFiles.add("big_test_clip_off.log");
        testFiles.add("big_test_clip_on.log");
        new ResultInterpreter("MARLIN_VAR_CLIP", testFiles).showAndSavePlot();

        CURRENT_REF_INDEX = 0;
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
        final boolean doRatio = setup.yLabel.startsWith("GAIN");

        final ResultParser ref = (doRatio && results.size() > CURRENT_REF_INDEX) ? results.get(CURRENT_REF_INDEX) : null;

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
            if (USE_BAR && ref != null && ref == result) {
                continue;
            }
            
            final String datasetKey = result.filepath.getName();
            final Map<String, Integer> cols = result.columns;
            final List<Data> datas = result.datas;

            final int idxTH = cols.get(COL_TH);
            final int idxVAL = cols.get(dataCol);

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
                    val = 100.0 * GAIN_SIGN * ((val / refDatas.get(i).values[refIdxVAL]) - 1.0); // -1 to have relative gain
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
            if (nCols < 1) {
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
        JFreeChart chart;
        if (USE_BAR) {
            chart = ChartFactory.createBarChart(dataset.info.title, dataset.info.xLabel, dataset.info.yLabel,
                    dataset, PlotOrientation.VERTICAL, true, true, false);
        } else {
            chart = ChartFactory.createLineChart(dataset.info.title, dataset.info.xLabel, dataset.info.yLabel,
                    dataset, PlotOrientation.VERTICAL, true, true, false);
        }

        chart.getTitle().setFont(DEFAULT_TITLE_FONT);
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

        CategoryAxis catAxis = plot.getDomainAxis();
        catAxis.setLabelFont(DEFAULT_LABEL_FONT);
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
