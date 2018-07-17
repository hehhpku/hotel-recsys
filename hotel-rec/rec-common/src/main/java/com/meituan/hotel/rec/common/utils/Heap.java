package com.meituan.hotel.rec.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 12/21/15
 */

//固定容量的优先队列，模拟大顶堆，用于解决求topN小的问题
public class Heap<T> {
    public PriorityQueue<T> queue;
    private int maxSize; // 堆的最大容量
    private Comparator comparator;
    public boolean isFull = false;  //是否满了

    /**
     * 堆的构造函数
     * @param maxSize 堆的最大容量
     * @param comparator
     * 比较函数：若要获取N个元中与用户最近的n个元
     * 那么o1.compareTo(o2) == 1 if o1.distance > o2.distance
     */
    public Heap(int maxSize, Comparator<T> comparator) {
        if (maxSize <= 0)
            throw new IllegalArgumentException();
        this.maxSize = maxSize;
        this.comparator = comparator;
        this.queue = new PriorityQueue<T>(maxSize, Collections.reverseOrder(comparator));
    }

    /**
     * 添加新元
     * @param e
     */
    public void add(T e) {
        if (queue.size() < maxSize) { // 未达到最大容量，直接添加
            queue.add(e);
        } else { // 队列已满
            T peek = queue.peek();
            if (comparator.compare(e,peek) < 0) { // 将新元素与当前堆顶元素比较，保留较小的元素
                queue.poll();
                queue.add(e);
            }
        }
        if (!isFull && queue.size() == maxSize)
            isFull = true;
    }

    /**
     * 剔除堆顶元
     * @return
     */
    public T poll() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    /**
     * 取堆顶元
     * @return
     */
    public T peek(){
        if (queue.isEmpty())
            return null;
        return queue.peek();
    }

    /**
     * 取堆中所有元素，并返回有序的List
     * @return
     */
    public List<T> sortedList() {
        List<T> list = new ArrayList<T>(queue);
        Collections.sort(list,comparator); // PriorityQueue本身的遍历是无序的，最终需要对队列中的元素进行排序
        return list;
    }
}

