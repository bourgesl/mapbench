package it.geosolutions.java2d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Settings holder
 * @author bourgesl
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Profile {
    /* MapConst settings (shared) */

    public final static String KEY_DO_CLIP = "doClip";
    public final static String KEY_DO_SCALE = "doScale";
    public final static String KEY_SCALE = "scale";

    /* MapBench settings */
    public final static String KEY_USE_SHARED_IMAGE = "useSharedImage";

    public final static String KEY_PASS = "pass";
    public final static String KEY_MIN_LOOPS = "minLoops";

    public final static String KEY_MAX_THREADS = "maxThreads";

    public final static String KEY_MIN_DURATION = "minDuration";

    /** result directory */
    static File profileDirectory = new File("../profiles/");

    private final static Properties defProps = new Properties();
    private static boolean resolved = false;
    private static Properties props = null;

    static {
        /* MapConst settings (shared) */
        /* true to enable shape clipping before benchmark to render only visible (even partially) shapes */
        defProps.setProperty(KEY_DO_CLIP, "false");
        /* true to perform shape scaling */
        defProps.setProperty(KEY_DO_SCALE, "false");

        /* scaling factors (only first one is used by MapBench) */
        defProps.setProperty(KEY_SCALE, "4.0");

        /* MapBench settings */
        /* use shared image for all iterations or create 1 image per iteration */
        defProps.setProperty(KEY_USE_SHARED_IMAGE, "false");

        defProps.setProperty(KEY_PASS, "1");
        defProps.setProperty(KEY_MIN_LOOPS, "25");

        defProps.setProperty(KEY_MAX_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()));

        defProps.setProperty(KEY_MIN_DURATION, "5000.0");
    }

    public static boolean getBoolean(final String key) {
        return Boolean.parseBoolean(get().getProperty(key));
    }

    public static int getInteger(final String key) {
        return Integer.parseInt(get().getProperty(key));
    }

    public static double getDouble(final String key) {
        return Double.parseDouble(get().getProperty(key));
    }

    private static Properties get() {
        if (props != null) {
            return props;
        }
        /* load properties at the first time */
        if (!resolved) {
            resolved = true;
            
            startup();

            final String profile = System.getProperty("mapbench.profile");

            if (profile == null || profile.isEmpty()) {
                System.out.println("no profile set (use -Dmapbench.profile=<profile file name>); using defaults");
            } else {
                final File profileFileName = new File(profileDirectory, profile);
                System.out.println("Loading profile file: " + profileFileName.getAbsolutePath());

                /* use default properties if some properties are missing in the profile file */
                Properties loadedProps = new Properties(defProps);
                try {
                    loadedProps.load(new FileInputStream(profileFileName));
                } catch (FileNotFoundException ex) {
                    System.out.println("Profile file not found: " + profileFileName.getAbsolutePath());
                    loadedProps = null;
                } catch (IOException ex) {
                    System.out.println("I/O failure while reading profile file: " + profileFileName.getAbsolutePath());
                    loadedProps = null;
                }
                props = loadedProps;
            }

            /* dump properties in use */
            dump();

            if (props != null) {
                return props;
            }
        }
        return defProps;
    }

    public static void startup() {
        System.out.printf("##############################################################\n");
        System.out.printf("# Java: %s\n", System.getProperty("java.runtime.version"));
        System.out.printf("#   VM: %s %s (%s)\n", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.info"));
        System.out.printf("#   OS: %s %s (%s)\n", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        System.out.printf("# CPUs: %d (virtual)\n", Runtime.getRuntime().availableProcessors());
        System.out.printf("##############################################################\n");
    }
    
    private static void dump() {
        System.out.println("Profile properties (merged with defaults):");

        /* merge and sort keys */
        final TreeSet<String> keys = new TreeSet<String>();
        for (Enumeration<Object> e = defProps.keys(); e.hasMoreElements();) {
            keys.add(e.nextElement().toString());
        }
        if (props != null) {
            for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
                keys.add(e.nextElement().toString());
            }
        }

        final Properties properties = get();

        for (String key : keys) {
            System.out.print("  ");
            System.out.print(key);
            System.out.print("=");
            System.out.println(properties.getProperty(key));
        }
        System.out.printf("##############################################################\n");
    }

    private Profile() {
        // forbidden
    }
}
