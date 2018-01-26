/**
 * Heap.java
 * Copyright 2017, Harri Bell-Thomas, All rights reserved.
 */

package uk.ac.cam.ahb36.fjava.tick0;


/**
 * Min-Heap implementation for Pair instances.
 *
 * @author Harri Bell-Thomas <ahb36@cam.ac.uk>
 */
public class Heap {

    // region Class Attributes

    private int size = 0;
    private int capacity;

    private Pair[] heap;

    // endregion


    /**
     * Constructor.
     * @param cap Maximum heap capacity.
     */
    public Heap(int cap) {
        this.capacity = cap + 1;
        this.heap = new Pair[this.capacity + 1];
    }


    /**
     * Push an item onto the heap.
     * @param x Item to add.
     */
    public void push(Pair x) {
        this.heap[++this.size] = x;
        int pos = this.size;
        while (pos > 1 && this.heap[pos >> 1].getLabel() > this.heap[pos].getLabel()) {
            Pair tmp = this.heap[pos >> 1];
            this.heap[pos >> 1] = this.heap[pos];
            this.heap[pos] = tmp;
            pos >>= 1;
        }
    }


    /**
     * Pop the minimum and fix heap.
     * @return Minimum item in the heap.
     */
    public Pair pop() {

        if(this.size < 1) return null;

        Pair top = this.heap[1];
        int position = 1;
        Pair p;

        this.heap[position] = this.heap[this.size];
        this.size--;

        while (position <= this.size) {
            int parent = position;
            int left = position << 1;
            int right = left + 1;
            if (left <= this.size && this.heap[left].getLabel() < this.heap[parent].getLabel()) parent = left;
            if (right <= this.size && this.heap[right].getLabel() < this.heap[parent].getLabel()) parent = right;
            if (parent != position) {
                p = this.heap[position];
                this.heap[position] = this.heap[parent];
                this.heap[parent] = p;
                position = parent;
            }
            else {
                break;
            }
        }
        return top;
    }
}
