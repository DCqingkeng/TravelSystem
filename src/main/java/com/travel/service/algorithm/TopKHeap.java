package com.travel.service.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Top-K最小堆工具类
 * 用于高效获取前K个最大元素而不需要完全排序
 *
 * 原理：
 * - 使用最小堆（PriorityQueue）维护当前最大的K个元素
 * - 堆顶元素是这K个元素中最小的（即第K大的元素）
 * - 新元素比堆顶大时，替换堆顶，保持堆大小为K
 * - 最终堆中即为Top-K元素
 *
 * 复杂度分析：
 * - 时间：O(n log k) - 每个元素最多经过一次插入或替换操作
 * - 空间：O(k) - 只存储K个元素
 *
 * 适用场景：
 * - 数据量大但只需前K个结果（n >> k）
 * - 流式数据实时Top-K计算
 * - 内存有限不能存储全部排序结果
 */
public class TopKHeap<E> {

    private final int capacity;
    private final PriorityQueue<E> minHeap;
    private final Comparator<? super E> comparator;

    /**
     * 构造Top-K堆
     *
     * @param k 需要保留的Top-K数量
     * @param comparator 元素比较器（升序，堆顶为最小）
     */
    public TopKHeap(int k, Comparator<? super E> comparator) {
        if (k <= 0) {
            throw new IllegalArgumentException("Top-K数量必须大于0");
        }
        this.capacity = k;
        this.comparator = comparator;
        // 初始容量设为k，避免扩容；使用给定的比较器
        this.minHeap = new PriorityQueue<>(k, comparator);
    }

    /**
     * 向堆中添加元素
     * 策略：
     * 1. 堆未满时，直接添加
     * 2. 堆已满时，仅当新元素大于堆顶才替换（维持Top-K性质）
     *
     * @param element 待添加元素
     * @return true 如果元素被接受（进入Top-K），false 如果被丢弃
     */
    public boolean offer(E element) {
        if (element == null) {
            return false;
        }

        if (minHeap.size() < capacity) {
            // 堆未满，直接添加
            minHeap.offer(element);
            return true;
        } else {
            // 堆已满，比较新元素与堆顶
            E peek = minHeap.peek();
            if (comparator.compare(element, peek) > 0) {
                // 新元素更大，替换堆顶（移除当前第K名，加入新元素）
                minHeap.poll();
                minHeap.offer(element);
                return true;
            }
            // 新元素不够大，丢弃
            return false;
        }
    }

    /**
     * 批量添加元素
     */
    public void addAll(List<E> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        for (E element : elements) {
            offer(element);
        }
    }

    /**
     * 获取排序后的Top-K结果（降序排列）
     * 注意：此方法会将堆转换为列表并排序，时间复杂度O(k log k)
     *
     * @return 按比较器降序排列的Top-K元素列表
     */
    public List<E> getSortedResults() {
        List<E> result = new ArrayList<>(minHeap);
        // 降序排列（与堆的升序相反，得到从大到小的Top-K）
        result.sort(comparator.reversed());
        return result;
    }

    /**
     * 获取当前堆中的元素数量（可能小于K）
     */
    public int size() {
        return minHeap.size();
    }

    /**
     * 检查是否已有K个元素
     */
    public boolean isFull() {
        return minHeap.size() >= capacity;
    }

    /**
     * 清空堆
     */
    public void clear() {
        minHeap.clear();
    }

    /**
     * 查看当前堆顶元素（第K大的元素）
     */
    public E peek() {
        return minHeap.peek();
    }
}
