/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MapBench implements MapConst {

    final static boolean showImage = false;

    /* use shared image for all iterations or create 1 image per iteration */
    final static boolean useSharedImage = false;

    // TODO: save results (images) ...
    final static double atScale = scales[0]; /* large image [width = 9001, height = 5401] */

    // 2 real cores on my cpu / 4 virtual cpus:
    static final Runtime runtime = Runtime.getRuntime();
    // constants:
    static final int PASS = 1;
    static final int LOOPS = 25;
    static final boolean doWarmup = true;
    static final boolean doGCBeforeTest = true;
//    static final int LOOPS = 2;    
//    static final boolean doWarmup = false;
    static final int WARMUP_LOOPS_MIN = 200;
    static final int WARMUP_LOOPS_MAX = 200;
    /*    
     static final int N_WARMUP = 1;
     static final int WARMUP_LOOPS = 200;
     */
    static final int N_THREAD_PER_CORE = 1;
    static final int MAX_THREADS = runtime.availableProcessors() * N_THREAD_PER_CORE;
    static final double DEFAULT_MIN_DURATION = 5000d;
    /** benchmark min duration per test */
    static double MIN_DURATION = DEFAULT_MIN_DURATION;
    /*
     // Regression test / Profiler
     static final int PASS = 1;
     static final int LOOPS = 1;
     static final int MAX_THREADS = 1; // 2 cores on my cpu
     */

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        if (args != null && args.length > 0) {
            System.out.println("# Parsing min duration = " + args[0] + " (ms)");
            MIN_DURATION = Double.parseDouble(args[0]);
        }

        final AffineTransform at;
        if (useAffineTransform) {
            // Affine transform:
            at = AffineTransform.getScaleInstance(atScale, atScale);

            System.out.println("Using AffineTransform at scale = " + atScale + "...");
        } else {
            at = null;
        }

        startTests();

        if (!inputDirectory.exists()) {
            System.out.println("Invalid input directory = " + inputDirectory);
            System.exit(1);
        }

        System.out.println("Loading maps from = " + inputDirectory.getAbsolutePath());

        File[] files = inputDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testMatcher);
            }
        });

        // Sort file names:
        Arrays.sort(files);

        final StringBuilder sbResults = new StringBuilder(128 * 1024);
        sbResults.append(Result.toStringHeader()).append('\n');

        System.out.println("Results format: \n" + Result.toStringHeader());

        int nTest = 0;
        double totalTest = 0d;
        double initialTime;
        int testLoops;

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        try {
            DrawingCommands commands;
            Result res;
            String sRes;
            boolean first = true;

            for (int n = 0; n < PASS; n++) {

                for (File file : files) {
                    System.out.println("Loading drawing commands from file: " + file.getAbsolutePath());
                    commands = DrawingCommands.load(file);

                    if (useAffineTransform) {
                        // apply affine transform:
                        commands.setAt(at);
                    }

                    System.out.println("drawing[" + file.getName() + "][width = " + commands.getWidth()
                            + ", height = " + commands.getHeight() + "] ...");

                    // run a warm up
                    if (doWarmup && first) {
                        first = false;

                        for (int nWarmup = WARMUP_LOOPS_MIN, i = 0; nWarmup <= WARMUP_LOOPS_MAX; nWarmup *= 2, i++) {
                            System.out.println("\nWarming up with " + MAX_THREADS + " threads and " + nWarmup + " loops on  " + file.getAbsolutePath());
                            res = new MapBench(executor, commands, MAX_THREADS, nWarmup).timedExecute();
                            System.out.println("Warm up took " + res.totalTime + " ms");
                            sRes = res.toString();

                            System.out.println(sRes);
                            sbResults.append("<<< Warmup ").append(i).append("\n");
                            sbResults.append(sRes).append('\n');
                            sbResults.append(">>> Warmup ").append(i).append("\n");
                        }

                        // Pisces stats:
//                        sun.java2d.pisces.ArrayCache.dumpStats();
                    }

                    // Estimate number of loops based on median time given by 3 loops:
                    res = new MapBench(executor, commands, 1, 3).timedExecute();
                    initialTime = Result.toMillis(res.nsPerOpMed) * 0.95d; // 5% margin

                    System.out.println("Initial test: " + initialTime + " ms.");

                    testLoops = Math.max(LOOPS, (int) (MIN_DURATION / initialTime));

                    System.out.println("Testing file " + file.getAbsolutePath() + " for " + testLoops + " loops ...");

                    int threads = 1;
                    while (threads <= MAX_THREADS) {
                        res = new MapBench(executor, commands, threads, testLoops).timedExecute();
                        System.out.println(threads + " threads and " + testLoops + " loops per thread, time: " + res.totalTime + " ms");
                        sRes = res.toString();

                        nTest++;
                        totalTest += res.nsPerOpMedStdDev;

                        System.out.println(sRes);
                        sbResults.append(sRes).append('\n');
                        threads *= 2;
                    }

                    System.out.println("\n");

                    // Pisces stats:
//                        sun.java2d.pisces.ArrayCache.dumpStats();
                } // files

                System.out.println("done: score = " + Result.toMillis(totalTest / (double) nTest) + " (" + nTest + " tests)");
                System.out.println("Complete results:\n" + sbResults.toString());

            } // PASS

        } finally {
            executor.shutdown();
        }
    }
    private DrawingCommands commands;
    private int numThreads;
    private int loops;
    private ExecutorService executor;

    public MapBench(ExecutorService executor, DrawingCommands commands, int numThreads, int loops) {
        this.commands = commands;
        this.numThreads = numThreads;
        this.loops = loops;
        this.executor = executor;
    }

    public Result timedExecute() throws InterruptedException, ExecutionException {
        if (doGCBeforeTest) {
            cleanup();
        }
        final int nOps = numThreads * loops;
        long[] opss = new long[nOps];
        long[] nanoss = new long[nOps];

        long start = System.nanoTime();
        execute(opss, nanoss);
        long end = System.nanoTime();

        final Result res = new Result(commands.fileName, numThreads, nOps, opss, nanoss);
        res.totalTime = Result.toMillis(end - start);
        return res;
    }

    public void execute(final long[] opss, final long[] nanoss) throws InterruptedException, ExecutionException {

        final int _numThreads = numThreads;
        final ExecutorService _executor = executor;

        commands.prepareCommands(doClip);

        final ArrayList<Callable<BufferedImage>> jobs = new ArrayList<Callable<BufferedImage>>(_numThreads);
        final ArrayList<Future<BufferedImage>> futures = new ArrayList<Future<BufferedImage>>(_numThreads);

        // Prepare jobs:
        for (int i = 0; i < _numThreads; i++) {
            jobs.add(new Test(opss, nanoss, i, numThreads, loops, commands));
        }

        // Submit jobs as fast as possible to avoid any ramp:
        for (int i = 0; i < _numThreads; i++) {
            futures.add(_executor.submit(jobs.get(i)));
        }

        for (int i = 0; i < _numThreads; i++) {
            final BufferedImage image = futures.get(i).get();

            if (showImage) {
                MapDisplay.showImage("TH" + i + " " + commands.fileName, image); // no copy
            }
        }

        // Pisces stats:
//        sun.java2d.pisces.ArrayCache.dumpStats();
        commands.dispose();
    }

    final static class Test implements Callable<BufferedImage> {

        final long[] opss;
        final long[] nanoss;
        final int nThread;
        final int numThreads;
        final int loops;
        final DrawingCommands commands;

        final BufferedImage image;
        final Graphics2D graphics;

        Test(final long[] opss, final long[] nanoss, final int nThread, final int numThreads, final int loops, final DrawingCommands commands) {
            this.opss = opss;
            this.nanoss = nanoss;
            this.nThread = nThread;
            this.numThreads = numThreads;
            this.loops = loops;
            this.commands = commands;
            /* prepare image and graphics before benchmark */
            image = (useSharedImage) ? commands.prepareImage() : null;
            graphics = (useSharedImage) ? commands.prepareGraphics(image) : null;
        }

        @Override
        public BufferedImage call() throws Exception {
            // copy members to local vars:
            final long[] _opss = opss;
            final long[] _nanoss = nanoss;
            final int _nThread = nThread;
            final int _numThreads = numThreads;
            final int _loops = loops;
            final DrawingCommands _commands = commands;
            BufferedImage _image = image;
            Graphics2D _graphics = graphics;

            int off = _nThread * _loops;
            long start, end;

            /* benchmark loop */
            for (int i = 0; i < _loops; i++) {
                _opss[off + i]++;

                start = System.nanoTime();

                if (!useSharedImage) {
                    _image = _commands.prepareImage();
                    _graphics = _commands.prepareGraphics(_image);
                }

                _commands.execute(_graphics);

                if (!useSharedImage) {
                    _graphics.dispose();
                }

                end = System.nanoTime();

                if (showImage) {
                    if ((i == 0) && (_nThread == _numThreads >> 1)) {
                        MapDisplay.showImage("TH" + _nThread + " " + _commands.fileName, MapDisplay.deepCopyImage(_image));
                    }
                }

                _nanoss[off + i] += (end - start);
            }

            if (useSharedImage) {
                _graphics.dispose();
            }

            return _image;
        }
    }

    /**
     * Cleanup (GC + pause)
     */
    private static void cleanup() {
        final long freeBefore = runtime.freeMemory();
        // Perform GC:
        System.gc();
        System.gc();
        System.gc();

        // pause for 500 ms :
        try {
            Thread.sleep(500l);
        } catch (InterruptedException ie) {
            System.out.println("thread interrupted");
        }
        final long freeAfter = runtime.freeMemory();
        System.out.println(String.format("cleanup (explicit Full GC): %,d / %,d bytes free.", freeBefore, freeAfter));
    }

    /**
     * Test result calculator/formatter
     */
    public static final class Result {

        double totalTime = 0d;
        public final String testName;
        public final int param;
        public final int threads;
        public final double nsPerOpAvg, nsPerOpMed, nsPerOpSigma, nsPerOpRms, nsPerOpMedStdDev;
        public final double nsPerOpMin, nsPerOpMax;
        private final long[] opss, nanoss;
        private final double nsPerOps[];
        private final long opsSum;

        // TODO: rename threads to N or N elements:
        public Result(String testName, int param, int threads, long[] opss, long[] nanoss) {
            this.testName = testName;
            this.param = param;
            this.threads = threads;
            this.opss = opss;
            this.nanoss = nanoss;

            long _opsSum = 0L;
            long _nanosSum = 0L;
            final double[] _nsPerOps = new double[threads];

            for (int i = 0; i < threads; i++) {
                _nsPerOps[i] = ((double) nanoss[i]) / (double) opss[i];
                _opsSum += opss[i];
                _nanosSum += nanoss[i];
            }
            this.nsPerOps = _nsPerOps;
            this.opsSum = _opsSum;

            final double _nsPerOpAvg = ((double) _nanosSum) / (double) _opsSum;
            this.nsPerOpAvg = _nsPerOpAvg;

            // extrema (outliers):
            double _nsPerOpMin = Double.MAX_VALUE;
            double _nsPerOpMax = -0d;

            for (int i = 0; i < threads; i++) {
                if (_nsPerOpMin > _nsPerOps[i]) {
                    _nsPerOpMin = _nsPerOps[i];
                }
                if (_nsPerOpMax < _nsPerOps[i]) {
                    _nsPerOpMax = _nsPerOps[i];
                }
            }
            this.nsPerOpMin = _nsPerOpMin;
            this.nsPerOpMax = _nsPerOpMax;

            // median:
            long nsPerOpValid = 0L;
            double nsPerOpSum = 0d;

            for (int i = 0; i < threads; i++) {
                if ((_nsPerOps[i] != _nsPerOpMin) && (_nsPerOps[i] != _nsPerOpMax)) {
                    nsPerOpSum += _nsPerOps[i];
                    nsPerOpValid++;
                }
            }
            final double _nsPerOpMed = (nsPerOpValid != 0L) ? (nsPerOpSum / (double) nsPerOpValid) : Double.NaN;
            this.nsPerOpMed = _nsPerOpMed;

            // stddev % median:
            if (Double.isNaN(_nsPerOpMed)) {
                this.nsPerOpSigma = this.nsPerOpRms = this.nsPerOpMedStdDev = Double.NaN;
            } else {
                double nsPerOpVar = 0d;
                double nsPerOpDiff;

                for (int i = 0; i < threads; i++) {
                    if ((_nsPerOps[i] != _nsPerOpMin) && (_nsPerOps[i] != _nsPerOpMax)) {
                        nsPerOpDiff = _nsPerOpMed - _nsPerOps[i]; // distance to median
                        nsPerOpVar += nsPerOpDiff * nsPerOpDiff;
                    }
                }

                nsPerOpVar /= (double) nsPerOpValid;
                this.nsPerOpSigma = Math.sqrt(nsPerOpVar);

                // median + stddev = 90% events:
                this.nsPerOpMedStdDev = _nsPerOpMed + this.nsPerOpSigma;

                // RMS see https://en.wikipedia.org/wiki/Root_mean_square:
                this.nsPerOpRms = Math.sqrt(_nsPerOpMed * _nsPerOpMed + nsPerOpVar);
            }
        }

        @Override
        public String toString() {
            return toString(false);
        }
        private final static String separator = "\t";

        public static String toStringHeader() {
            return String.format("test%sthreads%sops%sTavg%sTmed%sstdDev%srms%sMed+Stddev%smin%smax%sTotalOps%s[ms/op]",
                    separator, separator, separator, separator, separator, separator, separator, separator, separator, separator, separator);
        }

        public String toString(boolean dumpIndividualThreads) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(String.format("%s%s%d%s%d%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%d",
                    testName, separator, param, separator, threads, separator, toMillis(nsPerOpAvg), separator, toMillis(nsPerOpMed), separator,
                    toMillis(nsPerOpSigma), separator, toMillis(nsPerOpRms), separator, toMillis(nsPerOpMedStdDev), separator,
                    toMillis(nsPerOpMin), separator, toMillis(nsPerOpMax), separator,
                    opsSum));

            if (dumpIndividualThreads) {
                sb.append(" [");
                for (int i = 0; i < nsPerOps.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(String.format("%.3f", toMillis(nsPerOps[i])));
                    sb.append(" (").append(this.opss[i]).append(')');
                }
                sb.append("]");
            }
            return sb.toString();
        }

        public static double toMillis(final double ns) {
            return ns / 1e6d;
        }
    }

    public static void startTests() {
        System.out.printf("##############################################################\n");
        System.out.printf("# Java: %s\n", System.getProperty("java.runtime.version"));
        System.out.printf("#   VM: %s %s (%s)\n", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.info"));
        System.out.printf("#   OS: %s %s (%s)\n", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        System.out.printf("# CPUs: %d (virtual)\n", Runtime.getRuntime().availableProcessors());
        System.out.printf("#\n");
        System.out.println("# Min duration per test = " + MIN_DURATION + " ms.");
        System.out.printf("##############################################################\n");
    }
}
