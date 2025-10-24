package org.example.chatreact.algorithms;

import org.springframework.ai.document.Document;

import java.util.*;

/**
 * RRF算法
 */
public class RRFFusionProcessor {
    private final int rankConstant; // 阻尼常数k

    public RRFFusionProcessor(int rankConstant) {
        this.rankConstant = rankConstant;
    }

    public List<Document> fuseRankings(List<List<Document>> rankedLists) {
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, Document> documentMap = new HashMap<>();

        // 遍历每个搜索系统结果
        for (List<Document> list: rankedLists) {
            for (int rank = 0; rank < list.size(); rank++) {
                Document document = list.get(rank);
                String documentId = document.getId();

                // 记录文档，避免重复创建
                documentMap.putIfAbsent(documentId, document);

                // 累加RRF分数
                double score = 1.0 / (rankConstant + rank + 1);
                scoreMap.put(documentId, scoreMap.getOrDefault(documentId, 0.0) + score);
            }
        }

        // 按 RRF 分数降序排序
        List<Document> fusedList = new ArrayList<>(documentMap.values());
        fusedList.sort((d1, d2) ->
                Double.compare(scoreMap.get(d2.getId()), scoreMap.get(d1.getId())));
        return fusedList;
    }

}
