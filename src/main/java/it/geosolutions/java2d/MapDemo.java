/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.gui.BigImageFrame;
import org.gui.ImageUtils;

/**
 *
 * @author bourgesl
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapDemo extends BenchTest {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        
        if (!BenchTest.useSharedImage) {
            System.out.println("Please set useSharedImage = true in your profile !");
            System.exit(1);
        }

        final int winW = 3800;
        final int winH = 1800;
        // 1800 x 900 for Full-HD
        //  976 x 640 for XGA
        final RenderCallback rdrCallback = new RenderCallback(winW, winH);

        // Prepare view transformation:
        startTests();

        final File[] dataFiles = getSortedFiles();

        final StringBuilder sbWarm = new StringBuilder(8 * 1024);
        sbWarm.append(Result.toStringHeader()).append('\n');

        final StringBuilder sbRes = new StringBuilder(16 * 1024);
        sbRes.append(Result.toStringHeader()).append('\n');

        System.out.println("Results format: \n" + Result.toStringHeader());

        // global score:
        int nTest = 0;
        double totalMed = 0.0;
        double totalPct95 = 0.0;
        double totalFps = 0.0;
        // thread score:
        int nThPass = 0;
        int threads = 1;
        while (threads <= MAX_THREADS) {
            threads *= 2;
            nThPass++;
        }
        final int nThScores = nThPass;
        final int[] nThTest = new int[nThScores];
        final double[] nThTotalMed = new double[nThScores];
        final double[] nThTotalPct95 = new double[nThScores];
        final double[] nThTotalFps = new double[nThScores];

        double initialTime;
        int testLoops;

        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        executor.prestartAllCoreThreads();

        final MapDemo bench = new MapDemo(executor, rdrCallback);
        try {
            Result res;
            String sRes;
            boolean first = true;
            DrawingCommands commands;

            if (doWarmup) {
                BaseTest.isWarmup = true;
                commands = new DrawingCommands(800, 600, new ArrayList<DrawingCommand>(0));
                commands.name = "[calibration]";

                System.out.println("\nCalibrating up with " + MAX_THREADS + " threads and " + CALIBRATE_LOOPS + " loops");
                res = bench.timedExecute(commands, MAX_THREADS, CALIBRATE_LOOPS);
                System.out.println("Calibration up took " + res.totalTime + " ms");
                sRes = res.toString();

                System.out.println(sRes);
                sbWarm.append("<<< Calib 1\n");
                sbWarm.append(sRes).append('\n');
                sbWarm.append(">>> Calib 1\n");

                // Marlin stats:
                dumpRendererStats();
            }

            for (int n = 0; n < PASS; n++) {

                for (File file : dataFiles) {
                    System.out.println("Loading drawing commands from file: " + file.getAbsolutePath());
                    commands = DrawingCommands.load(file);

                    // set view transform once:
                    // commands.setAt(viewAT);
                    System.out.println("drawing[" + file.getName() + "][width = " + commands.getWidth()
                            + ", height = " + commands.getHeight() + "] ...");

                    // run a warm up
                    if (doWarmup && first) {
                        BaseTest.isWarmup = true;
                        first = false;

                        for (int nWarmup = WARMUP_LOOPS_MIN, i = 1; nWarmup <= WARMUP_LOOPS_MAX; nWarmup *= 2, i++) {
                            System.out.println("\nWarming up with " + MAX_THREADS + " threads and " + nWarmup + " loops on " + file.getAbsolutePath());
                            res = bench.timedExecute(commands, MAX_THREADS, nWarmup);
                            System.out.println("Warm up took " + res.totalTime + " ms");
                            sRes = res.toString();

                            System.out.println(sRes);
                            sbWarm.append("<<< Warmup ").append(i).append("\n");
                            sbWarm.append(sRes).append('\n');
                            sbWarm.append(">>> Warmup ").append(i).append("\n");
                        }

                        // Marlin stats:
                        dumpRendererStats();
                    }

                    // Warmup
                    if (doWarmupEachTest) {
                        BaseTest.isWarmup = true;

                        // Estimate number of loops based on median time given by 3 loops:
                        res = bench.timedExecute(commands, 1, 3);
                        initialTime = Result.toMillis(res.nsPerOpMed);

                        System.out.println("Initial test: " + initialTime + " ms.");

                        initialTime *= 0.95d; // 5% margin
                        testLoops = Math.max(WARMUP_BEFORE_TEST_MIN_LOOPS, (int) (WARMUP_BEFORE_TEST_MIN_DURATION / initialTime));

                        System.out.println("\nWarming up with " + WARMUP_BEFORE_TEST_THREADS + " threads and "
                                + testLoops + " loops on " + file.getAbsolutePath());
                        res = bench.timedExecute(commands, WARMUP_BEFORE_TEST_THREADS, testLoops);
                        System.out.println("Warm up took " + res.totalTime + " ms");
                        sRes = res.toString();

                        System.out.println(sRes);
                        sbWarm.append(sRes).append('\n');

                        // Marlin stats:
                        dumpRendererStats();
                    }

                    BaseTest.isWarmup = false;

                    // Estimate number of loops based on median time given by 3 loops:
                    res = bench.timedExecute(commands, 1, 3);
                    initialTime = Result.toMillis(res.nsPerOpMed);

                    System.out.println("Initial test: " + initialTime + " ms.");

                    initialTime *= 0.95d; // 5% margin
                    testLoops = Math.max(MIN_LOOPS, (int) (MIN_DURATION / initialTime));

                    System.out.println("Testing file " + file.getAbsolutePath() + " for " + testLoops + " loops ...");

                    nThPass = 0;
                    threads = 1;
                    while (threads <= MAX_THREADS) {
                        res = bench.timedExecute(commands, threads, testLoops);
                        System.out.println(threads + " threads and " + testLoops + " loops per thread, time: " + res.totalTime + " ms");
                        sRes = res.toString();

                        nTest++;
                        totalMed += res.nsPerOpMed;
                        totalPct95 += res.nsPerOpPct95;
                        totalFps += res.getFpsMed();

                        nThTest[nThPass]++;
                        nThTotalMed[nThPass] += res.nsPerOpMed;
                        nThTotalPct95[nThPass] += res.nsPerOpPct95;
                        nThTotalFps[nThPass] += res.getFpsMed();

                        System.out.println(sRes);
                        sbRes.append(sRes).append('\n');

                        threads *= 2;
                        nThPass++;
                    }

                    System.out.println("\n");

                } // files

                System.out.println("WARMUP results:");
                System.out.println(sbWarm.toString());
                System.out.println("TEST results:");
                System.out.println(sbRes.toString());

                // Scores:
                final StringBuilder sbScore = new StringBuilder(1024);

                sbScore.append("Tests\t");
                sbScore.append(nTest).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(nThTest[nThPass]).append('\t');
                    threads *= 2;
                    nThPass++;
                }

                sbScore.append("\nThreads\t");
                sbScore.append(MAX_THREADS).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(threads).append('\t');
                    threads *= 2;
                    nThPass++;
                }

                // Median:
                sbScore.append("\nMed\t");
                sbScore.append(String.format("%.3f",
                        Result.toMillis(totalMed / (double) nTest))).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(String.format("%.3f",
                            Result.toMillis(nThTotalMed[nThPass] / (double) nThTest[nThPass]))).append('\t');
                    threads *= 2;
                    nThPass++;
                }
                
                // 95 percentile:
                sbScore.append("\nPct95\t");
                sbScore.append(String.format("%.3f",
                        Result.toMillis(totalPct95 / (double) nTest))).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(String.format("%.3f",
                            Result.toMillis(nThTotalPct95[nThPass] / (double) nThTest[nThPass]))).append('\t');
                    threads *= 2;
                    nThPass++;
                }

                // Fps:
                sbScore.append("\nFPS\t");
                sbScore.append(String.format("%.3f", 
                        totalFps / (double) nTest)).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(String.format("%.3f", 
                            nThTotalFps[nThPass] / (double) nThTest[nThPass])).append('\t');
                    threads *= 2;
                    nThPass++;
                }
                sbScore.append('\n');
                
                System.out.println("Scores:");
                System.out.println(sbScore.toString());

            } // PASS

        } finally {
            bench.dispose();
        }
    }

    // members:
    private final ExecutorService executor;
    private final RenderCallback rdrCallback;

    MapDemo(ExecutorService executor, RenderCallback rdrCallback) {
        this.executor = executor;
        this.rdrCallback = rdrCallback;
    }

    void dispose() {
        this.executor.shutdown();
        this.rdrCallback.dispose();
    }

    Result timedExecute(final DrawingCommands commands, final int numThreads, final int loops)
            throws InterruptedException, ExecutionException, IOException {
        if (doGCBeforeTest) {
            cleanup();
        }
        final int nOps = numThreads * loops;
        long[] opss = new long[nOps];
        long[] nanoss = new long[nOps];

        long start = System.nanoTime();
        execute(commands, numThreads, loops, opss, nanoss);
        long end = System.nanoTime();

        final Result res = new Result(commands.name, numThreads, nOps, opss, nanoss);
        res.totalTime = Result.toMillis(end - start);
        return res;
    }

    private void execute(final DrawingCommands commands, final int numThreads, final int loops,
                         final long[] opss, final long[] nanoss)
            throws InterruptedException, ExecutionException, IOException {
        final ExecutorService _executor = executor;
        final RenderCallback _rdrCallback = rdrCallback;
        final int _numThreads = numThreads;

        _rdrCallback.prepare(numThreads);

        commands.setAt(null);
        commands.prepareCommands(MapConst.doClip, MapConst.doUseWingRuleEvenOdd, PathIterator.WIND_EVEN_ODD);
        commands.setAt(null);

        final ArrayList<Callable<Image>> jobs = new ArrayList<Callable<Image>>(_numThreads);
        final ArrayList<Future<Image>> futures = new ArrayList<Future<Image>>(_numThreads);

        // Prepare jobs:
        for (int i = 0; i < _numThreads; i++) {
            jobs.add(new Test(opss, nanoss, i, numThreads, loops, commands, _rdrCallback));
        }

        // Submit jobs as fast as possible to avoid any ramp:
        for (int i = 0; i < _numThreads; i++) {
            futures.add(_executor.submit(jobs.get(i)));
        }

        for (int i = 0; i < _numThreads; i++) {
            final Image image = futures.get(i).get();

            if (showImage) {
                MapDisplay.showImage("TH" + i + " " + commands.name, commands.file, ImageUtils.convert(image), false); // do not copy
            }
        }

        commands.dispose();

        // Marlin stats:
        dumpRendererStats();
    }

    final static class Test implements Callable<Image> {

        final static double ANGLE_STEP = 1.0 / 512; // 2 pixels at distance = 512px

        private final long[] opss;
        private final long[] nanoss;
        private final int nThread;
        private final int loops;
        private final DrawingCommands commands;
        private final Image image;
        private final Graphics2D graphics;
        private final RenderCallback rdrCallback;

        Test(final long[] opss, final long[] nanoss, final int nThread, final int numThreads, final int loops,
             final DrawingCommands commands, RenderCallback rdrCallback) {
            this.opss = opss;
            this.nanoss = nanoss;
            this.nThread = nThread;
            this.loops = loops;
            this.commands = commands;
            /* prepare image and graphics before benchmark */
            image = commands.prepareImage(rdrCallback.blockWidth, rdrCallback.blockHeight);
            graphics = commands.prepareGraphics(image);
            this.rdrCallback = rdrCallback;
        }

        @Override
        public Image call() throws Exception {
            // copy members to local vars:
            final long[] _opss = opss;
            final long[] _nanoss = nanoss;
            final int _nThread = nThread;
            final int _loops = loops;
            final DrawingCommands _commands = commands;
            final RenderCallback _rdrCallback = rdrCallback;
            Image _image = image;
            Graphics2D _graphics = graphics;
            final int off = (_nThread * _loops) - 1;
            long time;
            long total = 0L;
            double average;

            // Prepare the animation affine transform:
            final double cx = (_commands.width / 2.0);
            final double cy = (_commands.height / 2.0);
            final double hx = Math.max(0, cx - _rdrCallback.blockWidth / 2.0);
            final double hy = Math.max(0, cy - _rdrCallback.blockHeight / 2.0);
            
            final AffineTransform animAt = new AffineTransform();
            animAt.translate(-hx, -hy);

            /* benchmark loop */
            for (int i = 1; i <= _loops; i++) {
                _opss[off + i]++;
                time = System.nanoTime();

                _commands.execute(_graphics, animAt);

                time = System.nanoTime() - time;
                // set timing:
                _nanoss[off + i] += time;
                total += time;
                average = ((double) total) / i;

                // Update displayed image:
                _rdrCallback.refresh(_nThread, _image, _graphics, average);

                // animate graphics:
                animAt.rotate(ANGLE_STEP, cx, cy);
            }
            _graphics.dispose();
            return _image;
        }
    }

    final static class RenderCallback {

        /** default timer delay (20 milliseconds) ie max 50 FPS (displayed) */
        public final static int DELAY = 20;
        /** large font to display FPS */
        public final static Font FONT_FPS = new Font(Font.SANS_SERIF, Font.BOLD, 20);

        // members :
        private final int width;
        private final int height;
        private final BufferedImage image;
        private final Graphics2D graphics;
        private final BigImageFrame frame;
        private final Timer timer;
        // dirty image flag:
        private final AtomicInteger dirty = new AtomicInteger();

        // variable:
        int blockWidth = 0;
        int blockHeight = 0;
        private final int[] blockX = new int[MAX_REAL_THREADS];
        private final int[] blockY = new int[MAX_REAL_THREADS];

        RenderCallback(final int width, final int height) {
            this.width = width;
            this.height = height;
            this.image = ImageUtils.newImage(this.width, this.height);
            this.graphics = (Graphics2D) this.image.getGraphics();
            initialize(this.graphics);
//            System.out.println("Demo Image Graphics: " + this.graphics.getRenderingHints());

            this.frame = BigImageFrame.createAndShow("MapDemo: " + BaseTest.getRenderingEngineName(), 
                    this.image, null, null, false, false);
            
            frame.setInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            timer = new Timer(DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    paintImage();
                }

            });
            timer.setRepeats(true);
            timer.restart();
        }

        void dispose() {
            this.timer.stop();
            this.frame.dispose();
            this.graphics.dispose();
        }

        void prepare(final int numThreads) {
            final int stepX = (int) Math.ceil(Math.sqrt(numThreads));
            this.blockWidth = width / stepX;
            final int stepY = (int) Math.ceil((1.0 * numThreads) / stepX);
            this.blockHeight = height / stepY;

//            System.out.println("numThreads: " + numThreads);
//            System.out.println("blocks: " + blockWidth + " x " + blockHeight);
            Arrays.fill(blockX, 0);
            Arrays.fill(blockY, 0);

            for (int i = 0; i < stepY; i++) {
                for (int j = 0, k; j < stepX; j++) {
                    k = (i * stepY) + j;
                    blockX[k] = j * blockWidth;
                    blockY[k] = i * blockHeight;
                }
            }
//            System.out.println("blockX: " + Arrays.toString(blockX));
//            System.out.println("blockY: " + Arrays.toString(blockY));

            graphics.clearRect(0, 0, width, height);
        }

        void refresh(final int nThread, final Image rdrImage, final Graphics2D rdrGraphics, double average) {
//            System.out.println("topLeft: " + blockX[nThread] + ","+ blockY[nThread]);

            // Add FPS into rendered image:
            if (rdrGraphics != null) {
                initialize(rdrGraphics);
                rdrGraphics.clearRect(0, 0, 140, 24);
                rdrGraphics.drawString(String.format("FPS: %.1f", 1e9 / average), 1, 18);
            }

            // do copy into displayable image:
            graphics.drawImage(rdrImage, blockX[nThread], blockY[nThread], null);

            // set dirty flag once image is ready:
            dirty.compareAndSet(0, 1);
        }

        void paintImage() {
            // use synchro (within EDT)
            if (dirty.compareAndSet(1, 0)) {
                this.frame.setImage(image);
            }
        }

        static void initialize(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setTransform(IDENTITY);
            g2d.setBackground(Color.LIGHT_GRAY);
            g2d.setColor(Color.BLUE);
            g2d.setFont(FONT_FPS);
        }
    }
}
