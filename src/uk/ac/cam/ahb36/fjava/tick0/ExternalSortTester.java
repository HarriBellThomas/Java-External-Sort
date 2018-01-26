/**
 * ExternalSortTester.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;


import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * For testing only, runs all test files.
 *
 * Note that running this requires the test suite files to be unzipped in the tick0/test-suite folder.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class ExternalSortTester {

    private static String[] HASHES = { "",
                                "d41d8cd98f0b24e980998ecf8427e",
                                "a54f041a9e15b5f25c463f1db7449",
                                "c2cb56f4c5bf656faca0986e7eba38",
                                "c1fa1f22fa36d331be4027e683baad6",
                                "8d79cbc9a4ecdde112fc91ba625b13c2",
                                "1e52ef3b2acef1f831f728dc2d16174d",
                                "6b15b255d36ae9c85ccd3475ec11c3",
                                "1484c15a27e48931297fb6682ff625",
                                "ad4f60f065174cf4f8b15cbb1b17a1bd",
                                "32446e5dd58ed5a5d7df2522f0240",
                                "435fe88036417d686ad8772c86622ab",
                                "c4dacdbc3c2e8ddbb94aac3115e25aa2",
                                "3d5293e89244d513abdf94be643c630",
                                "468c1c2b4c1b74ddd44ce2ce775fb35c",
                                "79d830e4c0efa93801b5d89437f9f3e",
                                "c7477d400c36fca5414e0674863ba91",
                                "cc80f01b7d2d26042f3286bdeff0d9"
                            };
    /**
     * Constructor.
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        executeCommand("rm -rf src/uk/ac/cam/ahb36/fjava/tick0/test-suite");

        int tests = 10;
        int run = 0;
        double t = 0;

        for(int i = 0; i < tests; i++) {
            try {
                executeCommand("unzip test-suite.zip -d src/uk/ac/cam/ahb36/fjava/tick0/test-suite");
                t += runTestSuite();
                run++;

            }

            catch (OutOfMemoryError oofme) {
                System.out.println("Out of memory: task aborted.");
            }

            finally {
                executeCommand("rm -rf src/uk/ac/cam/ahb36/fjava/tick0/test-suite");
            }
        }

        System.out.println("Overall Time: " + (t / run)/1000000000.0 + "s");
        executeCommand("rm -rf src/uk/ac/cam/ahb36/fjava/tick0/test-suite");
    }


    /**
     * Loops over all 17 test files, sorting and timing them.
     * @return Running time in nanoseconds.
     * @throws Exception
     */
    private static double runTestSuite() throws Exception {
        int from = 1;
        int to = 17;

        long startTime = System.nanoTime();

        for(int i = from; i <= to; i++) {
            String f1 = "src/uk/ac/cam/ahb36/fjava/tick0/test-suite/test" + i + "a.dat";//args[0];
            String f2 = "src/uk/ac/cam/ahb36/fjava/tick0/test-suite/test" + i + "b.dat";//args[1];

            String[] a = new String[2];
            a[0] = f1;
            a[1] = f2;
            ExternalSort.main(a);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        boolean correct = true;
        for(int i = from; i <= to; i++) {
            String f1 = "src/uk/ac/cam/ahb36/fjava/tick0/test-suite/test" + i + "a.dat";
            if(!ExternalSort.checkSum(f1).equals(ExternalSortTester.HASHES[i])) {
                System.out.println(i + " is wrong: " + ExternalSort.checkSum(f1) + " instead of " + ExternalSortTester.HASHES[i]);
                correct = false;
            }
        }

        System.out.println("Success: " + correct);
        return duration;
    }


    /**
     * Executes bash command.
     * @param command Command to execute.
     * @return String output.
     */
    private static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}
