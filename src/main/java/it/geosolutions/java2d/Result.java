/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package it.geosolutions.java2d;

import java.util.Arrays;

/**
 * Test result calculator/formatter
 */
public final class Result {

    double totalTime = 0d;
    public final String testName;
    public final int param;
    public final int nOps;
    public final double nsPerOpAvg;
    public final double nsPerOpSigma;
    public final double nsPerOpMed;
    public final double nsPerOpPct95;
    public final double nsPerOpMin;
    public final double nsPerOpMax;
    private final long[] opss;
    private final double[] nsPerOps;
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
        this.nsPerOpPct95 = percentile(0.95, nops, _nsPerOps); // 95%
    }

    public double getFpsMed() {
        return 1.0 / toSecond(nsPerOpMed);
    }

    public double getFpsPct95() {
        return 1.0 / toSecond(nsPerOpPct95);
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
    private static final String separator = "\t";

    public static String toStringHeader() {
        return String.format("%-45s%sThreads%sOps%sMed%sPct95%sAvg%sStdDev%sMin%sMax%sFPS(med)%sTotalOps%s[ms/op]", "Test",
                separator, separator, separator, separator, separator, separator, separator, separator, separator, separator, separator, separator);
    }

    public String toString(boolean dumpIndividualThreads) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(String.format("%-45s%s%d%s%d%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%d",
                testName, separator, param, separator, nOps, separator, toMillis(nsPerOpMed), separator,
                toMillis(nsPerOpPct95), separator, toMillis(nsPerOpAvg), separator, toMillis(nsPerOpSigma), separator,
                toMillis(nsPerOpMin), separator, toMillis(nsPerOpMax), separator, getFpsMed(), separator, opsSum));
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

    public static double toSecond(final double ns) {
        return ns / 1e9d;
    }

}
