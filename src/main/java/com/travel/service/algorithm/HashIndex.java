package com.travel.service.algorithm;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日记名称哈希索引
 * 核心算法：哈希表查找，平均时间复杂度 O(1)
 * 适用于日记数量大、变化快的精确查询场景
 */
@Component
public class HashIndex {

    // 日记标题 -> 日记ID（Java 内部使用哈希桶+链表/红黑树解决冲突）
    private final Map<String, Long> index = new ConcurrentHashMap<>();

    /**
     * 添加或更新索引（创建/修改日记标题时调用）
     */
    public void put(String title, Long diaryId) {
        if (title != null && diaryId != null) {
            index.put(title, diaryId);
        }
    }

    /**
     * 精确查找：O(1)
     * @return 日记ID，未找到返回 null
     */
    public Long get(String title) {
        if (title == null) return null;
        return index.get(title);
    }

    /**
     * 删除索引（日记删除或改名时调用）
     */
    public void remove(String title) {
        if (title != null) {
            index.remove(title);
        }
    }

    public void clear() {
        index.clear();
    }

    public int size() {
        return index.size();
    }
}
