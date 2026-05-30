package com.sky.rag;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.*;

/**
 * 基于 Lucene 的 BM25 检索器
 *
 * BM25 (Best Matching 25) 是一种基于词频-逆文档频率的经典检索算法，
 * 擅长精确关键词匹配，与向量检索的语义理解能力形成互补。
 *
 * 核心公式：
 * score(D, Q) = Σ IDF(qi) × (f(qi,D) × (k1+1)) / (f(qi,D) + k1 × (1-b+b × |D|/avgdl))
 *
 * - f(qi,D): 词 qi 在文档 D 中的词频
 * - |D|: 文档长度, avgdl: 平均文档长度
 * - k1: 词频饱和参数 (Lucene 默认 1.2)
 * - b: 长度归一化参数 (Lucene 默认 0.75)
 * - IDF: 逆文档频率，稀有词权重更高
 */
@Slf4j
public class BM25ContentRetriever implements ContentRetriever {

    // Lucene 内存索引目录（不写磁盘，纯内存）
    private final ByteBuffersDirectory indexDir;
    // 中文分词器
    private final Analyzer analyzer;
    // 返回的最大结果数
    private final int maxResults;

    /**
     * 私有构造，通过 Builder 创建
     */
    private BM25ContentRetriever(int maxResults) {
        this.indexDir = new ByteBuffersDirectory();
        this.analyzer = new SmartChineseAnalyzer();
        this.maxResults = maxResults;
    }

    /**
     * 构建 BM25 索引
     *
     * @param texts 文档文本列表，每个元素是一个文档片段
     * @param metadataList 对应的元数据列表（包含来源等信息）
     */
    public void buildIndex(List<String> texts, List<Metadata> metadataList) {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            // 相似度默认就是 BM25Similarity，无需额外配置
            IndexWriter writer = new IndexWriter(indexDir, config);

            for (int i = 0; i < texts.size(); i++) {
                Document doc = new Document();
                // 存储原始文本（TextField 会分词并存储）
                doc.add(new TextField("content", texts.get(i), Field.Store.YES));
                // 存储元数据（如文件来源）
                if (metadataList != null && i < metadataList.size()) {
                    Metadata meta = metadataList.get(i);
                    for (var entry : meta.toMap().entrySet()) {
                        doc.add(new TextField(entry.getKey(), entry.getValue().toString(), Field.Store.YES));
                    }
                }
                writer.addDocument(doc);
            }

            writer.commit();
            writer.close();
            log.info("BM25 索引构建完成，共 {} 个文档片段", texts.size());
        } catch (IOException e) {
            log.error("BM25 索引构建失败", e);
        }
    }

    /**
     * BM25 检索入口
     *
     * LangChain4j 的 ContentRetriever 接口方法，
     * 接收用户查询，返回匹配的 Content 列表。
     */
    @Override
    public List<Content> retrieve(Query query) {
        String queryText = query.text();
        log.info("BM25 检索: {}", queryText);

        try {
            DirectoryReader reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            // searcher 默认使用 BM25Similarity

            // 多字段查询（这里只用 content 字段，可扩展）
            Map<String, Float> boosts = new HashMap<>();
            boosts.put("content", 1.0f);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                    new String[]{"content"},
                    analyzer,
                    boosts
            );
            // 设置默认操作为 OR，提高召回率
            parser.setDefaultOperator(QueryParser.Operator.OR);

            // 转义特殊字符，避免 Lucene 语法错误
            String escaped = QueryParser.escape(queryText);
            org.apache.lucene.search.Query luceneQuery = parser.parse(escaped);

            // 执行搜索
            TopDocs topDocs = searcher.search(luceneQuery, maxResults);

            List<Content> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String text = doc.get("content");

                // 构建元数据
                Metadata metadata = new Metadata();
                for (var field : doc.getFields()) {
                    if (!"content".equals(field.name())) {
                        metadata.put(field.name(), field.stringValue());
                    }
                }

                // 包装成 LangChain4j 的 Content
                TextSegment segment = TextSegment.from(text, metadata);
                results.add(Content.from(segment));
            }

            reader.close();
            log.info("BM25 检索到 {} 个结果", results.size());
            return results;

        } catch (Exception e) {
            log.error("BM25 检索失败", e);
            return List.of();
        }
    }

    /**
     * Builder 模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxResults = 3;

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public BM25ContentRetriever build() {
            return new BM25ContentRetriever(maxResults);
        }
    }
}
