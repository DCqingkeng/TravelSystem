package com.travel.service.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Huffman编码无损压缩
 * 存储格式：编码表##比特长度##Base64(字节数组)
 * 避免Java原生序列化，消除类元数据膨胀
 */
@Component
public class HuffmanCompressor {

    private static class Node implements Comparable<Node> {
        Character ch;
        int freq;
        Node left, right;

        Node(char ch, int freq) {
            this.ch = ch;
            this.freq = freq;
        }

        Node(int freq, Node left, Node right) {
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(Node o) {
            return this.freq - o.freq;
        }
    }

    /**
     * 压缩文本
     */
    public String compress(String text) {
        if (text == null || text.isEmpty()) return text;

        // 1. 统计字符频率
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            freqMap.merge(c, 1, Integer::sum);
        }

        // 2. 构建Huffman树
        PriorityQueue<Node> pq = new PriorityQueue<>();
        freqMap.forEach((ch, f) -> pq.offer(new Node(ch, f)));

        // 单字符特殊处理
        if (pq.size() == 1) {
            pq.offer(new Node('\0', 0));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            pq.offer(new Node(left.freq + right.freq, left, right));
        }
        Node root = pq.poll();

        // 3. 生成编码表
        Map<Character, String> codeMap = new HashMap<>();
        buildCodes(root, "", codeMap);

        // 4. 文本编码为比特串
        StringBuilder encodedBits = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedBits.append(codeMap.get(c));
        }

        // 5. 编码表序列化为紧凑字符串：Unicode整数值|编码,...
        StringBuilder table = new StringBuilder();
        for (Map.Entry<Character, String> entry : codeMap.entrySet()) {
            table.append((int) entry.getKey()).append('|').append(entry.getValue()).append(',');
        }

        // 6. 比特串 → 字节数组（补零对齐）
        String bitStr = encodedBits.toString();
        int bitLen = bitStr.length();
        byte[] data = new byte[(bitLen + 7) / 8];
        for (int i = 0; i < bitLen; i++) {
            if (bitStr.charAt(i) == '1') {
                data[i / 8] |= (1 << (7 - (i % 8)));
            }
        }

        // 7. 最终格式：编码表##比特长度##Base64数据
        return table + "##" + bitLen + "##" + Base64.getEncoder().encodeToString(data);
    }

    /**
     * 解压文本
     */
    public String decompress(String compressed) {
        if (compressed == null || compressed.isEmpty()) return compressed;
        // 非压缩数据直接返回（兼容未压缩的旧数据）
        if (!compressed.contains("##")) return compressed;

        String[] parts = compressed.split("##", 3);
        if (parts.length != 3) return compressed;

        String tableStr = parts[0];
        int bitLen = Integer.parseInt(parts[1]);
        byte[] data = Base64.getDecoder().decode(parts[2]);

        // 解析编码表（反转：编码 -> 字符）
        Map<String, Character> decodeMap = new HashMap<>();
        if (!tableStr.isEmpty()) {
            for (String entry : tableStr.split(",")) {
                if (entry.isEmpty()) continue;
                String[] kv = entry.split("\\|", 2);
                if (kv.length == 2) {
                    char ch = (char) Integer.parseInt(kv[0]);
                    decodeMap.put(kv[1], ch);
                }
            }
        }

        // 字节数组还原比特串
        StringBuilder bits = new StringBuilder(bitLen);
        for (int i = 0; i < bitLen; i++) {
            int byteIdx = i / 8;
            int bitIdx = 7 - (i % 8);
            bits.append(((data[byteIdx] >> bitIdx) & 1) == 1 ? '1' : '0');
        }

        // 按编码表解码
        StringBuilder result = new StringBuilder();
        StringBuilder current = new StringBuilder();
        for (char bit : bits.toString().toCharArray()) {
            current.append(bit);
            Character ch = decodeMap.get(current.toString());
            if (ch != null) {
                result.append(ch);
                current.setLength(0);
            }
        }

        return result.toString();
    }

    private void buildCodes(Node node, String code, Map<Character, String> codeMap) {
        if (node == null) return;
        if (node.isLeaf()) {
            codeMap.put(node.ch, code.isEmpty() ? "0" : code);
            return;
        }
        buildCodes(node.left, code + "0", codeMap);
        buildCodes(node.right, code + "1", codeMap);
    }
}