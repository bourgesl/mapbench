/*******************************************************************************
 * MapBench project (GPLv2 + CP)
 ******************************************************************************/
package it.geosolutions.java2d;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Map benchmark 
 *
 * TODO: save results (images) ...
 *
 * @author bourgesl
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class MapBench extends BaseTest {

    /* profile settings */
    final static boolean showImage = false;
    final static boolean showImageIntermediate = false;

    /* use shared image for all iterations or create 1 image per iteration */
    final static boolean useSharedImage = Profile.getBoolean(Profile.KEY_USE_SHARED_IMAGE);

    // constants:
    static final int PASS = Profile.getInteger(Profile.KEY_PASS);
    static final int MIN_LOOPS = Profile.getInteger(Profile.KEY_MIN_LOOPS);

    static final int MAX_THREADS = Profile.getInteger(Profile.KEY_MAX_THREADS);

    /** benchmark min duration per test */
    static double MIN_DURATION = Profile.getDouble(Profile.KEY_MIN_DURATION);

    /* constants */
    static final boolean doWarmup = true;
    static final boolean doWarmupEachTest = true;
    static final boolean doGCBeforeTest = true;

    // before 200/200
    static final int WARMUP_LOOPS_MIN = 80;
    static final int WARMUP_LOOPS_MAX = 2 * WARMUP_LOOPS_MIN;

    static final int WARMUP_BEFORE_TEST_THREADS = 2;
    static final int WARMUP_BEFORE_TEST_MIN_LOOPS = 10;
    static final int WARMUP_BEFORE_TEST_MIN_DURATION = 3000;

    static final int CALIBRATE_LOOPS = 3000;

    private static File cmdFile;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        // Prepare view transformation:
        final AffineTransform viewAT = getViewTransform();

        startTests();

        if (!inputDirectory.exists()) {
            System.out.println("Invalid input directory = " + inputDirectory);
            System.exit(1);
        }

        System.out.println("Loading maps from = " + inputDirectory.getAbsolutePath());

        final File[] dataFiles = inputDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(testMatcher);
            }
        });

        // Sort file names:
        Arrays.sort(dataFiles);

        final StringBuilder sbWarm = new StringBuilder(8 * 1024);
        sbWarm.append(Result.toStringHeader()).append('\n');

        final StringBuilder sbRes = new StringBuilder(16 * 1024);
        sbRes.append(Result.toStringHeader()).append('\n');

        final StringBuilder sbScore = new StringBuilder(1024);

        System.out.println("Results format: \n" + Result.toStringHeader());

        // global score:
        int nTest = 0;
        double totalTest = 0d;
        // thread score:
        int nThPass = 0;
        int threads = 1;
        while (threads <= MAX_THREADS) {
            threads *= 2;
            nThPass++;
        }
        final int nThScores = nThPass;
        final int[] nThTest = new int[nThScores];
        final double[] nThTotal = new double[nThScores];

        double initialTime;
        int testLoops;

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        try {
            Result res;
            String sRes;
            boolean first = true;
            DrawingCommands commands;

            if (doWarmup) {
                commands = new DrawingCommands(800, 600, new ArrayList<DrawingCommand>(0));
                commands.fileName = "[calibration]";

                System.out.println("\nCalibrating up with " + MAX_THREADS + " threads and " + CALIBRATE_LOOPS + " loops");
                res = new MapBench(executor, commands, MAX_THREADS, CALIBRATE_LOOPS).timedExecute();
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

                    cmdFile = file;

                    System.out.println("drawing[" + file.getName() + "][width = " + commands.getWidth()
                            + ", height = " + commands.getHeight() + "] ...");

                    // run a warm up
                    if (doWarmup && first) {
                        first = false;

                        for (int nWarmup = WARMUP_LOOPS_MIN, i = 1; nWarmup <= WARMUP_LOOPS_MAX; nWarmup *= 2, i++) {
                            System.out.println("\nWarming up with " + MAX_THREADS + " threads and " + nWarmup + " loops on " + file.getAbsolutePath());
                            res = new MapBench(executor, commands, MAX_THREADS, nWarmup).timedExecute();
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
                        // Estimate number of loops based on median time given by 3 loops:
                        res = new MapBench(executor, commands, 1, 3).timedExecute();
                        initialTime = Result.toMillis(res.nsPerOpMed);

                        System.out.println("Initial test: " + initialTime + " ms.");

                        initialTime *= 0.95d; // 5% margin
                        testLoops = Math.max(WARMUP_BEFORE_TEST_MIN_LOOPS, (int) (WARMUP_BEFORE_TEST_MIN_DURATION / initialTime));

                        System.out.println("\nWarming up with " + WARMUP_BEFORE_TEST_THREADS + " threads and "
                                + testLoops + " loops on " + file.getAbsolutePath());
                        res = new MapBench(executor, commands, WARMUP_BEFORE_TEST_THREADS, testLoops).timedExecute();
                        System.out.println("Warm up took " + res.totalTime + " ms");
                        sRes = res.toString();

                        System.out.println(sRes);
                        sbWarm.append(sRes).append('\n');

                        // Marlin stats:
                        dumpRendererStats();
                    }

                    // Estimate number of loops based on median time given by 3 loops:
                    res = new MapBench(executor, commands, 1, 3).timedExecute();
                    initialTime = Result.toMillis(res.nsPerOpMed);

                    System.out.println("Initial test: " + initialTime + " ms.");

                    initialTime *= 0.95d; // 5% margin
                    testLoops = Math.max(MIN_LOOPS, (int) (MIN_DURATION / initialTime));

                    System.out.println("Testing file " + file.getAbsolutePath() + " for " + testLoops + " loops ...");

                    nThPass = 0;
                    threads = 1;
                    while (threads <= MAX_THREADS) {
                        res = new MapBench(executor, commands, threads, testLoops).timedExecute();
                        System.out.println(threads + " threads and " + testLoops + " loops per thread, time: " + res.totalTime + " ms");
                        sRes = res.toString();

                        nTest++;
                        totalTest += res.nsPerOpMed95;

                        nThTest[nThPass]++;
                        nThTotal[nThPass] += res.nsPerOpMed95;

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

                sbScore.append("\nPct95\t");
                sbScore.append(String.format("%.3f",
                        Result.toMillis(totalTest / (double) nTest))).append('\t');

                nThPass = 0;
                threads = 1;
                while (threads <= MAX_THREADS) {
                    sbScore.append(String.format("%.3f",
                            Result.toMillis(nThTotal[nThPass] / (double) nThTest[nThPass]))).append('\t');
                    threads *= 2;
                    nThPass++;
                }
                sbScore.append('\n');

                System.out.println("Scores:");
                System.out.println(sbScore.toString());

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

    public Result timedExecute() throws InterruptedException, ExecutionException, IOException {
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

    public void execute(final long[] opss, final long[] nanoss) throws InterruptedException, ExecutionException, IOException {

        final int _numThreads = numThreads;
        final ExecutorService _executor = executor;

        commands.prepareCommands(MapConst.doClip, MapConst.doUseWingRuleEvenOdd, PathIterator.WIND_EVEN_ODD);

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
                MapDisplay.showImage("TH" + i + " " + commands.fileName, cmdFile, image, false); // do not copy
            }
        }

        commands.dispose();

        // Marlin stats:
        dumpRendererStats();
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

                if (showImageIntermediate) {
                    if ((i % 10) == 1) {
                        MapDisplay.showImage("Int. TH" + _nThread + " " + _commands.fileName, cmdFile, _image, true); // do copy
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
        final long freeBefore = Runtime.getRuntime().freeMemory();
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
        final long freeAfter = Runtime.getRuntime().freeMemory();
        System.out.println(String.format("cleanup (explicit Full GC): %,d / %,d bytes free.", freeBefore, freeAfter));
    }

    /**
     * Test result calculator/formatter
     */
    public static final class Result {

        double totalTime = 0d;
        public final String testName;
        public final int param;
        public final int nOps;
        public final double nsPerOpAvg, nsPerOpSigma, nsPerOpMed, nsPerOpMed95;
        public final double nsPerOpMin, nsPerOpMax;
        private final long[] opss;
        private final double nsPerOps[];
        private final long opsSum;

        // TODO: rename threads to N or N elements:
        public Result(String testName, int param, int nops, long[] opss, long[] nanoss) {
            this.testName = testName;
            this.param = param;
            this.nOps = nops;
            this.opss = opss;

            long _opsSum = 0L;
            long _nanosSum = 0L;
            final double[] _nsPerOps = new double[nops];

            for (int i = 0; i < nops; i++) {
                _nsPerOps[i] = ((double) nanoss[i]) / (double) opss[i];
                _opsSum += opss[i];
                _nanosSum += nanoss[i];
            }
            this.nsPerOps = _nsPerOps;
            this.opsSum = _opsSum;

            final double _nsPerOpAvg = ((double) _nanosSum) / (double) _opsSum;
            this.nsPerOpAvg = _nsPerOpAvg;

            // stddev:
            double nsPerOpVar = 0d;
            double nsPerOpDiff;

            for (int i = 0; i < nops; i++) {
                nsPerOpDiff = _nsPerOpAvg - _nsPerOps[i]; // distance to mean
                nsPerOpVar += nsPerOpDiff * nsPerOpDiff;
            }

            nsPerOpVar /= (double) nops;
            this.nsPerOpSigma = Math.sqrt(nsPerOpVar);

            // extrema (outliers):
            double _nsPerOpMin = Double.MAX_VALUE;
            double _nsPerOpMax = -0d;

            for (int i = 0; i < nops; i++) {
                if (_nsPerOpMin > _nsPerOps[i]) {
                    _nsPerOpMin = _nsPerOps[i];
                }
                if (_nsPerOpMax < _nsPerOps[i]) {
                    _nsPerOpMax = _nsPerOps[i];
                }
            }
            this.nsPerOpMin = _nsPerOpMin;
            this.nsPerOpMax = _nsPerOpMax;

            // percentiles (median & 95% ie 2 sigma) :
            Arrays.sort(_nsPerOps);
            this.nsPerOpMed = percentile(0.5, nops, _nsPerOps); // 50%
            this.nsPerOpMed95 = percentile(0.95, nops, _nsPerOps); // 95%
        }

        private static double percentile(final double percent, final int nops, final double[] nsPerOps) {
            final double idx = Math.max(percent * (nops - 1), 0);
            final int low = (int) Math.floor(idx);
            final int high = (int) Math.ceil(idx);
            if (low == high) {
                return nsPerOps[low];
            }
            return nsPerOps[low] * (high - idx) + nsPerOps[high] * (idx - low);
        }

        @Override
        public String toString() {
            return toString(false);
        }
        private final static String separator = "\t";

        public static String toStringHeader() {
            return String.format("%-45s%sThreads%sOps%sMed%sPct95%sAvg%sStdDev%sMin%sMax%sTotalOps%s[ms/op]",
                    "Test",
                    separator, separator, separator, separator, separator, separator, separator, separator, separator, separator, separator);
        }

        public String toString(boolean dumpIndividualThreads) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(String.format("%-45s%s%d%s%d%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%d",
                    testName, separator, param, separator, nOps, separator,
                    toMillis(nsPerOpMed), separator, toMillis(nsPerOpMed95), separator,
                    toMillis(nsPerOpAvg), separator, toMillis(nsPerOpSigma), separator,
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
        System.out.println("# Min duration per test = " + MIN_DURATION + " ms.");
        System.out.printf("##############################################################\n");
    }
}
