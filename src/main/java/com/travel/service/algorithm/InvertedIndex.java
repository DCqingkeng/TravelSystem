package com.travel.service.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 倒排索引实现全文检索
 * 核心数据结构：Map<词, Map<文档ID, 词频>>
 * 支持TF-IDF排序
 */
@Component
public class InvertedIndex {

    // 改为 ConcurrentHashMap 支持并发更新
    private final Map<String, Map<Long, Integer>> index = new ConcurrentHashMap<>();
    private int totalDocs = 0;

    /**
     * 添加文档到索引（只传入需要索引的文本，如日记内容）
     */
    public void addDocument(Long docId, String text) {
        if (text == null || text.isEmpty()) return;
        totalDocs++;

        List<String> terms = tokenize(text);
        for (String term : terms) {
            index.computeIfAbsent(term, k -> new ConcurrentHashMap<>())
                    .merge(docId, 1, Integer::sum);
        }
    }

    /**
     * 检索并返回按 TF-IDF 排序的文档ID列表
     */
    public List<Long> search(String query, int topK) {
        if (query == null || query.isEmpty()) return Collections.emptyList();

        List<String> queryTerms = tokenize(query);
        Map<Long, Double> scores = new HashMap<>();

        for (String term : queryTerms) {
            Map<Long, Integer> docFreqs = index.get(term);
            if (docFreqs == null || docFreqs.isEmpty()) continue;

            double idf = Math.log((double) Math.max(totalDocs, 1) / docFreqs.size());

            for (Map.Entry<Long, Integer> entry : docFreqs.entrySet()) {
                Long docId = entry.getKey();
                int tf = entry.getValue();
                scores.merge(docId, tf * idf, Double::sum);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void clear() {
        index.clear();
        totalDocs = 0;
    }

    /**
     * 简单分词：2-gram + 单字，适用于中文
     */
    private List<String> tokenize(String text) {
        List<String> terms = new ArrayList<>();
        String cleaned = text.toLowerCase().replaceAll("[^\\u4e00-\\u9fa5a-z0-9]", "");

        for (char c : cleaned.toCharArray()) {
            terms.add(String.valueOf(c));
        }
        for (int i = 0; i < cleaned.length() - 1; i++) {
            terms.add(cleaned.substring(i, i + 2));
        }
        return terms;
    }
}