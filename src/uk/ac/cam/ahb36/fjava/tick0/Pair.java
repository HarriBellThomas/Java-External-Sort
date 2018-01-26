/**
 * Pair.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;


/**
 * Comparable Pair (Key-Value) Data Structure.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class Pair implements Comparable<Pair> {

    // region Class Attributes

    private int label;
    private int binID;

    // endregion


    /**
     * Constructor.
     * @param l Label (Key).
     * @param b Bin ID (Value).
     */
    public Pair(int l, int b) {
        this.label = l;
        this.binID = b;
    }


    /**
     * Comparison method.
     * @param p Item to compare current instance to.
     * @return Comparison result.
     */
    public int compareTo(Pair p) {
        if (this.label < p.label) return -1;
        else if (this.label > p.label) return 1;
        return 0;
    }


    /**
     * Attribute Getters.
     */
    public int getLabel() { return this.label; }
    public int getBin() { return this.binID; }


    /**
     * Attribute Setters.
     */
    public void setLabel(int l) { this.label = l; }
    public void setBin(int b) { this.binID = b; }
}
