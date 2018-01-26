/**
 * BufferedFileOutput.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Class to manage buffered sequential writing to a file.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class BufferedFileOutput {

    // region Class Attributes

    private int outputBufferSize;
    private int currentElement = 0;

    private int[] buff;

    private RandomAccessFile output;

    // endregion


    /**
     * Constructor.
     * @param bufferSize Buffer size to use.
     * @param fos File to write to.
     */
    public BufferedFileOutput(int bufferSize, RandomAccessFile fos) {
        this.outputBufferSize = bufferSize;
        this.output = fos;
        this.buff = new int[this.outputBufferSize];
    }


    /**
     * Write an integer to the buffer and flush if required.
     * @param i Integer to write to the buffer.
     */
    public void writeInteger(int i) {
        if(this.currentElement >= this.outputBufferSize) {
            // Write out and refresh.
            try {
                this.output.write(ExternalSort.unpack(this.buff));
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            finally {
                this.currentElement = 0;
            }
        }

        this.buff[this.currentElement++] = i;
    }


    /**
     * Flush the current elements of the buffer to the file.
     */
    public void finalWrite() {
        try {
            this.output.write(ExternalSort.unpack(this.buff), 0, this.currentElement << 2);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            this.currentElement = 0;
            this.buff = null;
        }
    }
}
