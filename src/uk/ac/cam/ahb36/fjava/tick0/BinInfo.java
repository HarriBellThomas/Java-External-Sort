/**
 * BinInfo.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * A BinInfo object is used to retrieve items sequentially from a sector in a file using buffering.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class BinInfo {

    // region Class Attributes

    private int binStartOffset;
    private int binLength;
    private int internalReadOffset = 0;
    private int maximumBufferSize;
    private int bufferSize;
    private int bufferPosition = 0;
    private int binID;
    private int bytesToRead;

    private byte[] buffer;

    private RandomAccessFile dataSource;

    private boolean binEmpty;

    // endregion


    /**
     * Constructor.
     * @param offset Offset of the start of the bin in the file.
     * @param length Length of the bin.
     * @param input File object.
     * @throws IOException
     */
    public BinInfo(int offset, int length, RandomAccessFile input, int id) throws IOException {
        this.binLength = length;
        this.binStartOffset = offset;
        this.dataSource = input;
        this.binID = id;
    }


    /**
     * Read and return the first item and increment the pointer.
     * @param bs Maximum size of the buffer.
     * @return Head item from the bin.
     * @throws BinEmptyException
     */
    public int getHeadAndIncrement(int bs) throws BinEmptyException {

        // Check for empty bin.
        if(this.internalReadOffset == this.binLength || this.binEmpty) throw new BinEmptyException();

        try {
            return this.readInteger(bs);
        }

        catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BinEmptyException(); // On error report bin empty
        }
    }


    /**
     * Populates the internal buffer.
     * @throws IOException
     */
    private void fread() throws IOException {
        this.bufferPosition = 0;
        this.bytesToRead = Math.min(this.maximumBufferSize, ((this.binLength - this.internalReadOffset) << 2));
        if(this.bytesToRead == 0) this.binEmpty = true;
        else this.binEmpty = false;
        this.dataSource.seek(this.binStartOffset + this.internalReadOffset);
        this.bufferSize = this.dataSource.read(this.buffer, 0, this.bytesToRead);
    }


    /**
     * Reads a byte from the current pointer location.
     * @return Value at the current position.
     * @throws IOException
     */
    private int readByte() throws IOException {
        if (bufferPosition >= bufferSize) this.fread();
        this.internalReadOffset++;
        return (this.buffer[bufferPosition++] & 0xFF);
    }


    /**
     * Read the integer at the current position in the bin.
     * @param bs Maximum size of the buffer.
     * @return The integer retrieved.
     * @throws IOException
     */
    private int readInteger(int bs) throws IOException {
        this.maximumBufferSize = bs;
        if(this.buffer == null) this.buffer = new byte[this.maximumBufferSize];
        return (this.readByte() << 24) | (this.readByte() << 16) | (this.readByte() << 8) | this.readByte();
    }


    public int getBinID() {
        return this.binID;
    }
}
