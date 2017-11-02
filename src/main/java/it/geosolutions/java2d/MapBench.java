/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.gui.ImageUtils;

/**
 * Map benchmark
 *
 * TODO: save results (images) ...
 *
 * @author bourgesl
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapBench extends BenchTest {

    private final static boolean POOL_DEBUG = false;

    private final static int N_ITERATION = 5;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        // Prepare view transformation:
        final AffineTransform viewAT = getViewTransform();

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

        final MapBench bench = new MapBench();
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
                    commands.setAt(viewAT);

                    System.out.println("drawing[" + file.getName() + "][width = " + commands.getWidth()
                            + ", height = " + commands.getHeight() + "] ...");

                    for (int p = 0; p < ITER; p++) {
                        if (true) {
                            bench.prepareExecutor();
                            if (ITER > 1) {
                                System.out.println(">>> Iteration: " + p + " <<<");
                            }
                        }
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

                    } // iterations

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
            bench.shutdown();
        }
    }

    // members:
    private ThreadPoolExecutor executor = null;

    MapBench() {
    }

    void shutdown() {
        shutdown(executor);
    }

    private static void shutdown(ExecutorService executor) {
        if (executor != null) {
            try {
                if (POOL_DEBUG) {
                    System.out.println("shutdown: " + executor);
                }
                executor.shutdownNow();
                if (!executor.awaitTermination(1L, TimeUnit.SECONDS)) {
                    System.out.println("shutdown: threads are still running ...");
                }
            } catch (InterruptedException ie) {
                System.out.println("shutdown: Interrupted !");
            }
        }
    }

    void prepareExecutor() {
        if (executor != null) {
            shutdown(executor);
            executor = null;
        }
        if (executor == null) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
            executor.prestartAllCoreThreads();

            if (POOL_DEBUG) {
                    System.out.println("newFixedThreadPool: " + executor);
            }
        }
    }

    private ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            prepareExecutor();
        }
        return executor;
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

        final ThreadPoolExecutor _executor = getExecutor();
        final int _numThreads = numThreads;

        commands.prepareCommands(MapConst.doClip, MapConst.doUseWingRuleEvenOdd, PathIterator.WIND_EVEN_ODD);

        final ArrayList<Callable<Image>> jobs = new ArrayList<Callable<Image>>(_numThreads);
        final ArrayList<Future<Image>> futures = new ArrayList<Future<Image>>(_numThreads);

        // Prepare jobs:
        for (int i = 0; i < _numThreads; i++) {
            jobs.add(new Test(opss, nanoss, i, numThreads, loops, commands));
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

        final long[] opss;
        final long[] nanoss;
        final int nThread;
        final int numThreads;
        final int loops;
        final DrawingCommands commands;
        final Image image;
        final Graphics2D graphics;

        Test(final long[] opss, final long[] nanoss, final int nThread, final int numThreads, final int loops,
             final DrawingCommands commands) {
            this.opss = opss;
            this.nanoss = nanoss;
            this.nThread = nThread;
            this.numThreads = numThreads;
            this.loops = loops;
            this.commands = commands;
            /* prepare image and graphics before benchmark */
            image = (BenchTest.useSharedImage) ? commands.prepareImage() : null;
            graphics = (BenchTest.useSharedImage) ? commands.prepareGraphics(image) : null;
        }

        @Override
        public Image call() throws Exception {
             if (POOL_DEBUG) {
                 System.out.println("call() thread: " + Thread.currentThread().getName());
             }

            // copy members to local vars:
            final long[] _opss = opss;
            final long[] _nanoss = nanoss;
            final int _nThread = nThread;
            final int _loops = loops;
            final DrawingCommands _commands = commands;
            Image _image = image;
            Graphics2D _graphics = graphics;
            int off = _nThread * _loops;
            long start;
            long end;
            /* benchmark loop */
            for (int i = 0; i < _loops; i++) {
                _opss[off + i]++;
                start = System.nanoTime();
                if (!BenchTest.useSharedImage) {
                    _image = _commands.prepareImage();
                    _graphics = _commands.prepareGraphics(_image);
                }
                _commands.execute(_graphics, null);
                if (!BenchTest.useSharedImage) {
                    _graphics.dispose();
                }
                end = System.nanoTime();
                if (showImageIntermediate) {
                    if ((i % 10) == 1) {
                        MapDisplay.showImage("Int. TH" + _nThread + " " + _commands.name, _commands.file, ImageUtils.convert(_image), true); // do copy
                    }
                }
                _nanoss[off + i] += (end - start);
            }
            if (BenchTest.useSharedImage) {
                _graphics.dispose();
            }
            return _image;
        }
    }
}
