/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import it.geosolutions.java2d.ResultInterpreter;
import java.io.IOException;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author bourgesl
 */
public class ResultInterpreterTest {

    private final static String PATH = "/home/bourgesl/libs/marlin/bourgesl.github.io/fosdem-2016/data/";
//    private final static String PATH = "/home/bourgesl/Documents/OSUG/benchmarks/";

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void hello() {
        try {

            if (false) {
                new ResultInterpreter("WITH_MARLIN", Arrays.asList(new String[]{
                    PATH + "ductus_20160122.log",
                    PATH + "pisces_20160122.log",
                    PATH + "marlin_20160122.log"
                })).showAndSavePlot();
            }

            /*
            new ResultInterpreter("BEFORE", Arrays.asList(new String[]{
                PATH + "ductus_20160122.log",
                PATH + "pisces_20160122.log"
            })).showAndSavePlot();

            new ResultInterpreter("WITH_MARLIN", Arrays.asList(new String[]{
                PATH + "ductus_20160122.log",
                PATH + "pisces_20160122.log",
                PATH + "marlin_20160122.log"
            })).showAndSavePlot();

            new ResultInterpreter("WITH_MARLIN_ZOOM", Arrays.asList(new String[]{
                PATH + "ductus_20160122.log",
                PATH + "pisces_20160122.log",
                PATH + "marlin_20160122.log"
            })).showAndSavePlot();
             */
            if (false) {
                new ResultInterpreter("MARLIN_RATIO", Arrays.asList(new String[]{
                    PATH + "ductus_20160122.log",
                    PATH + "pisces_20160122.log",
                    PATH + "marlin_20160122.log"
                })).showAndSavePlot();

                new ResultInterpreter("DEFAULT", Arrays.asList(new String[]{
                    PATH + "jmmc/jmmc_marlin.log",
                    PATH + "oidb-beta/oidb-beta_marlin.log",
                    PATH + "osug-test/osug-test_marlin.log"
                })).showAndSavePlot();

                new ResultInterpreter("DEFAULT", Arrays.asList(new String[]{
                    PATH + "osug-test/bench_marlin.log",
                    PATH + "osug-test/bench_marlin_noHT.log",
                    PATH + "osug-test/bench_marlin_2801.log"
                })).showAndSavePlot();
            }
                new ResultInterpreter("DEFAULT", Arrays.asList(new String[]{
                    PATH + "ductus_demo.log",
                    PATH + "pisces_demo.log",
                    PATH + "marlin_demo.log"
                })).showAndSavePlot();

                new ResultInterpreter("VOLATILE", Arrays.asList(new String[]{
                    PATH + "marlin_20160122_soft_tile6_last.log",
                    PATH + "marlin_20160122_soft_tile6_accel.log"
                })).showAndSavePlot();

        } catch (IOException ex) {
            fail("IO");
        }

    }
}
