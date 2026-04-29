package com.sky.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

/**
 * RAG (Retrieval-Augmented Generation) 配置类
 *
 * RAG 核心流程：
 * 1. 文档加载 (DocumentLoader) - 从文件系统加载文档
 * 2. 文档分割 (DocumentSplitter) - 将长文档分割成小块
 * 3. 文本向量化 (EmbeddingModel) - 将文本块转为向量
 * 4. 向量存储 (EmbeddingStore) - 存储向量用于检索
 * 5. 内容检索 (ContentRetriever) - 根据用户问题检索相关文档
 */
@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    /**
     * 定义向量存储 Bean
     * InMemoryEmbeddingStore：内存存储，适合学习和小规模数据
     * 生产环境可替换为 Milvus、Qdrant、Chroma 等持久化向量数据库
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        log.info("创建 InMemoryEmbeddingStore 成功");
        return store;
    }

    /**
     * 定义文档加载和向量化 Bean
     * 启动时自动加载 knowledge 目录下的所有 TXT 文件，分割后向量化存入 EmbeddingStore
     */
    @Bean
    public boolean loadAndIngestDocuments(
            EmbeddingStore<TextSegment> embeddingStore,
            @Autowired EmbeddingModel embeddingModel) {

        // 1. 加载文档：从 classpath:/knowledge/ 目录加载所有 TXT 文件
        List<Document> documents = loadDocuments(
                "knowledge",
                new TextDocumentParser()
        );
        log.info("加载了 {} 个文档", documents.size());

        // 2. 文档分割：按段落分割，每块最多 200 字符，重叠 50 字符
        DocumentSplitter splitter = DocumentSplitters.recursive(200, 50);

        // 3. 使用 EmbeddingStoreIngestor 完成：分割 → 向量化 → 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
        log.info("文档向量化并存入 EmbeddingStore 完成，共 {} 个文档片段", documents.size());

        return true;
    }

    /**
     * 定义内容检索器 Bean
     * EmbeddingStoreContentRetriever：根据用户问题的向量，在 EmbeddingStore 中检索最相似的文档片段
     *
     * 参数说明：
     * - maxResults(3)：最多返回 3 个相关文档片段
     * - minScore(0.7)：相似度阈值，只返回相似度 >= 0.7 的片段
     */
    @Bean
    public ContentRetriever embeddingStoreContentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            @Autowired EmbeddingModel embeddingModel) {

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();

        log.info("创建 EmbeddingStoreContentRetriever 成功，maxResults=3, minScore=0.7");
        return retriever;
    }
}
