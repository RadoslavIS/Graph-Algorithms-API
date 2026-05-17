package com.rado.pq;

import java.util.Arrays;

/** Priority Queue implementation using a binary MinHeap **/
public class PriorityQueue<E extends Comparable<? super E>> {
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /** Array of elements inside the PQ. <br>
     * Cast the elements to E when needed.**/
    private Object[] queue;

    private int size;

    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public PriorityQueue(int initialCapacity) {
        if (initialCapacity <= 0) throw new IllegalArgumentException();
        queue = new Object[initialCapacity];
    }

    public PriorityQueue(E[] arr) {
        if (arr == null) throw new IllegalArgumentException();
        size = arr.length;
        queue = new Object[Math.max(DEFAULT_INITIAL_CAPACITY,  size)];
        System.arraycopy(arr, 0, queue, 0, size);
        heapify();
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    /** Adds to the tail of the queue **/
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException("Null elements are not allowed!");
        if (size >= queue.length) grow(size + 1);
        queue[size] = e;
        siftUp(size++);
        return true;
    }

    /** Returns the head of the queue **/
    @SuppressWarnings("unchecked")
    public E peek() {
        return isEmpty() ? null : (E) queue[0];
    }

    /** Removes the head of the queue **/
    public E poll() {
        return isEmpty() ? null : removeAt(0);
    }

    @SuppressWarnings("unchecked")
    public E removeAt(int i) {
        if (i < 0 || i >= size || isEmpty()) throw new IndexOutOfBoundsException();
        E removed = (E) queue[i];
        queue[i] = queue[--size];
        queue[size] = null;
        if (size > 0) siftDown(i);
        return removed;
    }

    /** Enforces the heap invariant.
     * Begins from the first leaf node and iterates up to the start,
     * calling siftDown(i) on each, which marks the current node's subtree as a heap.
     * O(n) Time complexity **/
    public void heapify() {
        for (int i = (size >>> 1) - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) >> 1;
            if (less(parent, i)) break;
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (i < size >>> 1) {
            int left = (i << 1) + 1;
            int right = left + 1;
            int smallest = left;
            if (right < size && !less(left, right)) smallest = right;
            if (left > size || less(i, left)) break;
            swap(i, smallest);
            i = smallest;

        }
    }

    /** Swaps 2 array elements **/
    private void swap(int i, int j) {
        Object temp = queue[i];
        queue[i] = queue[j];
        queue[j] = temp;
    }

    /** Compares 2 elements given their index
     * @return if left element is bigger **/
    @SuppressWarnings("unchecked")
    private boolean less(int i, int j) {
        return ((E) queue[i]).compareTo(((E) queue[j])) <= 0;
    }

    public void grow(int minCap) {
        int oldCap = queue.length;
        int newCap = oldCap + (oldCap >> 2);
        if (newCap < 0) newCap = Integer.MAX_VALUE;
        if (newCap < minCap) newCap = minCap;
        queue = Arrays.copyOf(queue, newCap);
    }

    public String toString() {
        return Arrays.toString(queue);
    }
}

