package com.rado.pq;

public class Application {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(new Integer[]{5, 9, 1, 2, 3, 12, 11, 19, 0});

        System.out.println(pq);
        pq.poll();
        System.out.println(pq);
        pq.offer(63);
        pq.offer(5);
        pq.offer(0);
        System.out.println(pq);
        pq.removeAt(4);
        System.out.println(pq);
    }
}
