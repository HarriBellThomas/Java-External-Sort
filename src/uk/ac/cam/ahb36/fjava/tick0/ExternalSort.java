/**
 * ExternalSort.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * This is the main class for Further Java Tick 0.
 * It implements the external sort function required by the task.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class ExternalSort {

    // region Class Attributes

    // Controls whether debug info is printed to console
    private static boolean DEBUG = false;

    private static RandomAccessFile A_1, A_2, B_1;

    private static int BIN_SIZE_IN_BYTES;
    private static int BIN_SIZE_IN_INTS;

    // endregion

    // region Main Sort Function

    /**
     * Main External Sort Function.
     * @param f1 Path to the file to be sorted. Result stored in this file.
     * @param f2 Path of auxiliary file that can be used for partitioning.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void sort(String f1, String f2) throws FileNotFoundException, IOException {

        // PHASE 0 //

        // Estimate the amount of memory we have to play with.
        // Note we divide by 3 to calculate the bin size as we need roughly double the bin size in contiguous
        // storage at any one time during the first phase.
        long availableMemory = Runtime.getRuntime().freeMemory() - 2000;
        BIN_SIZE_IN_BYTES = (int)(availableMemory / 3);

        // Init file access objects.
        A_1 = new RandomAccessFile(f1, "rw");
        A_2 = new RandomAccessFile(f1, "rw");
        B_1 = new RandomAccessFile(f2, "rw");

        // Calculate how many ints we have to sort. 0 or 1 are already implicitly sorted.
        long numInts = (A_1.length() >> 2);
        if(numInts < 2) return;

        // Calculate the number of bins required and their sizes.
        long numBins = (long)Math.ceil((numInts << 2) / (double)(BIN_SIZE_IN_BYTES));
        BIN_SIZE_IN_INTS = (int)(numInts / numBins);
        int leftOver = (int)(numInts % numBins);

        // Debug output.
        if(DEBUG) {
            System.out.println("Available Mem:  " + availableMemory);
            System.out.println("Num. ints:      " + numInts);
            System.out.println("Num. bins:      " + numBins);
            System.out.println("Bin size:       " + BIN_SIZE_IN_INTS);
            System.out.println("Left over:      " + leftOver);
            System.out.println("Bin Size Bytes: " + BIN_SIZE_IN_BYTES);
        }


        // PHASE 1 //
        // Partition, sort and write bins out to the auxiliary file.

        byte[] buf = new byte[1];
        int offset;
        int standardBinLength = BIN_SIZE_IN_INTS << 2;

        List<BinInfo> binInfos = new ArrayList<>();

        for(int i = 0; i < numBins; i++) {

            // Init bin byte arrays and calculate their offset in the file.
            // Include the extra ints in the first bin.
            if(i == 0) {
                buf = new byte[standardBinLength + (leftOver << 2)];
                offset = 0;
            }
            else if(i == 1) {
                buf = new byte[standardBinLength];
                offset = (leftOver << 2) + (i * standardBinLength);
            }
            else {
                offset = (leftOver << 2) + (i * standardBinLength);
            }

            // Read in bin segment.
            A_1.seek(offset);
            A_1.read(buf);

            try {
                // 1. Convert to integers.
                // 2. Sort using Java's inbuilt concurrent dual pivot quicksort implementation.
                // 3. Convert back to a byte array.
                // 4. If multiple bins write out to the auxiliary file, else write immediately back to original file.
                // 5. Save info about the bin in a BinInfo object and store.
                int[] iBuf = pack(buf);
                Arrays.parallelSort(iBuf);

                if(numBins > 1) {
                    B_1.seek(offset);
                    B_1.write(unpack(iBuf));
                    binInfos.add(new BinInfo(offset, buf.length, B_1, i));
                }
                else {
                    // Write straight back to original file.
                    A_2.seek(0);
                    A_2.write(unpack(iBuf));
                    return;
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }



        // PHASE 2 //
        // Auxiliary file has been partitioned and sorted. Now read in and merge.

        // Again calculate how much memory we have to play with.
        // Need to accommodate for all the bins buffers and the BufferedFileOutput buffer.
        availableMemory = Runtime.getRuntime().freeMemory() - 2000;
        int binInfoBufferSize = (int)(availableMemory / ((numBins + 1) << 2));

        // Debug output.
        if(DEBUG) {
            System.out.println("----------------");
            System.out.println("Avail. Mem 2:   " + availableMemory);
            System.out.println("Bin Info Size:  " + binInfoBufferSize);
        }


        // Assert: We have multiple bins, needs to merge and write out.
        // All cases with 0 or 1 bins have already returned by this point.

        // Use a heap to pop the min items from all bins.
        Heap h = new Heap((int) numBins);
        BufferedFileOutput bfo = new BufferedFileOutput(binInfoBufferSize, A_2);

        // Loop through each bin and add to the heap.
        // Use the bin's min item as the key.
        int head;
        for(BinInfo entry : binInfos) {
            try {
                head = entry.getHeadAndIncrement(binInfoBufferSize);
                h.push(new Pair(head, entry.getBinID()));
            } catch (BinEmptyException bee) {
                // Bin is now empty, don't re-add to the heap.
            }
        }

        Pair top;
        int i;

        // Loop through all the numbers we have, extracting the min and re-adding the bin's new min.
        // Write this extracted min to the BufferedFileOutput instance.
        for (long counter = 0; counter < numInts; counter++) {
            top = h.pop();
            if (top == null) break;

            bfo.writeInteger(top.getLabel());

            try {
                i = binInfos.get(top.getBin()).getHeadAndIncrement(binInfoBufferSize);
                while(top.getLabel() == i) {
                    bfo.writeInteger(i);
                    i = binInfos.get(top.getBin()).getHeadAndIncrement(binInfoBufferSize);
                }
                top.setLabel(i);
                h.push(top);
            } catch (BinEmptyException bee) {
                // Bin is now empty, don't re-add to the heap.
            }
        }

        // Looped through all the numbers, flush the buffer.
        bfo.finalWrite();


        // Close all our open files.
        A_1.close();
        A_2.close();
        B_1.close();
    }

    // endregion

    // region Array Conversions

    /**
     * Convert byte array to the int array it represents.
     * @param bytes Array of bytes to convert.
     * @return Converted array of ints.
     */
    public static int[] pack(byte[] bytes) {
        int n = bytes.length >> 2;
        int[] packed = new int[n];
        int i2;
        for (int i = 0; i < n; i++) {
            i2 = i << 2;
            packed[i] = (bytes[i2 + 0x03] & 0xFF)       |
                        (bytes[i2 + 0x02] & 0xFF) << 8  |
                        (bytes[i2 + 0x01] & 0xFF) << 16 |
                        (bytes[i2]        & 0xFF) << 24;
        }
        return packed;
    }


    /**
     * Convert int array to the byte array that represents it.
     * @param ints Array of ints to convert.
     * @return Converted array of bytes.
     */
    public static byte[] unpack(int[] ints) {
        int n = ints.length << 2;
        byte[] unpacked = new byte[n];
        int i2;
        for(int i = 0; i < ints.length; i++) {
            i2 = i << 2;
            unpacked[i2]        = (byte)(ints[i] >>> 24);
            unpacked[i2 + 0x01] = (byte)(ints[i] >>> 16);
            unpacked[i2 + 0x02] = (byte)(ints[i] >>> 8);
            unpacked[i2 + 0x03] = (byte)(ints[i]);
        }
        return unpacked;
    }

    // endregion

    // region Default Tick Code

    /**
     * Convert a byte to its String hex representation.
     * @param b Byte to represent.
     * @return String representation.
     */
    private static String byteToHex(byte b) {
        String r = Integer.toHexString(b);
        if (r.length() == 8) {
            return r.substring(6);
        }
        return r;
    }


    /**
     * Calculates the checksum for a file.
     * @param f File path to calculate the checksum for.
     * @return String checksum.
     */
    public static String checkSum(String f) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream ds = new DigestInputStream(
                    new FileInputStream(f), md);
            byte[] b = new byte[512];
            while (ds.read(b) != -1)
                ;

            String computed = "";
            for(byte v : md.digest())
                computed += byteToHex(v);

            return computed;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<error computing checksum>";
    }


    /**
     * Static class invocation.
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String f1 = args[0];
        String f2 = args[1];
        sort(f1, f2);
        System.out.println("The checksum is: "+checkSum(f1));
    }

    // endregion
}