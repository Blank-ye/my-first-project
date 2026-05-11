package com.sky.rag;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 混合检索器：BM25 关键词检索 + 向量语义检索
 *
 * 使用 RRF (Reciprocal Rank Fusion) 算法合并两路检索结果。
 *
 * RRF 公式：score(d) = Σ 1 / (k + rank_i(d))
 * - k 通常取 60（Lucene 默认值），控制排名融合的平滑度
 * - rank_i(d) 是文档 d 在第 i 路检索中的排名（从 1 开始）
 *
 * 优势：
 * - BM25 擅长精确关键词匹配（如订单号、菜品名）
 * - 向量检索擅长语义理解（如"退单"匹配"退款流程"）
 * - 两者互补，整体检索质量优于单一方式
 */
@Slf4j
public class HybridContentRetriever implements ContentRetriever {

    private final ContentRetriever bm25Retriever;
    private final ContentRetriever embeddingRetriever;
    private final int maxResults;
    // RRF 常数 k，控制排名融合的平滑度
    private static final int RRF_K = 60;

    public HybridContentRetriever(ContentRetriever bm25Retriever,
                                   ContentRetriever embeddingRetriever,
                                   int maxResults) {
        this.bm25Retriever = bm25Retriever;
        this.embeddingRetriever = embeddingRetriever;
        this.maxResults = maxResults;
    }

    /**
     * 混合检索入口
     *
     * 1. 分别执行 BM25 检索和向量检索
     * 2. 用 RRF 算法合并排名
     * 3. 去重后返回 Top N
     */
    @Override
    public List<Content> retrieve(Query query) {
        log.info("混合检索开始: {}", query.text());

        // 1. 两路并行检索
        List<Content> bm25Results = bm25Retriever.retrieve(query);
        List<Content> embeddingResults;
        try {
            embeddingResults = embeddingRetriever.retrieve(query);
        } catch (Exception e) {
            log.warn("向量检索失败，回退为纯 BM25 检索: {}", e.getMessage());
            embeddingResults = List.of();
        }

        log.info("BM25 返回 {} 个结果，向量检索返回 {} 个结果",
                bm25Results.size(), embeddingResults.size());

        // 2. RRF 排名融合
        // key: 文档文本内容, value: RRF 累计分数
        Map<String, Double> rrfScores = new LinkedHashMap<>();
        // key: 文档文本内容, value: Content 对象（保留第一个遇到的）
        Map<String, Content> contentMap = new LinkedHashMap<>();

        // BM25 结果排名计分（排名从 1 开始）
        for (int i = 0; i < bm25Results.size(); i++) {
            Content content = bm25Results.get(i);
            String text = content.textSegment().text();
            double rrfScore = 1.0 / (RRF_K + i + 1);
            rrfScores.merge(text, rrfScore, Double::sum);
            contentMap.putIfAbsent(text, content);
        }

        // 向量检索结果排名计分
        for (int i = 0; i < embeddingResults.size(); i++) {
            Content content = embeddingResults.get(i);
            String text = content.textSegment().text();
            double rrfScore = 1.0 / (RRF_K + i + 1);
            rrfScores.merge(text, rrfScore, Double::sum);
            contentMap.putIfAbsent(text, content);
        }

        // 3. 按 RRF 分数降序排序，取 Top N
        List<Content> merged = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> contentMap.get(entry.getKey()))
                .toList();

        log.info("混合检索完成，返回 {} 个结果", merged.size());
        return merged;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ContentRetriever bm25Retriever;
        private ContentRetriever embeddingRetriever;
        private int maxResults = 3;

        public Builder bm25Retriever(ContentRetriever bm25Retriever) {
            this.bm25Retriever = bm25Retriever;
            return this;
        }

        public Builder embeddingRetriever(ContentRetriever embeddingRetriever) {
            this.embeddingRetriever = embeddingRetriever;
            return this;
        }

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public HybridContentRetriever build() {
            return new HybridContentRetriever(bm25Retriever, embeddingRetriever, maxResults);
        }
    }
}
