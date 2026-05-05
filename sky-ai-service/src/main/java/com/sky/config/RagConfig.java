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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Path;
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

    @Autowired
    private EmbeddingModel embeddingModel;
    /**
     * 定义向量存储 Bean
     * InMemoryEmbeddingStore：内存存储，适合学习和小规模数据
     * 生产环境可替换为 Milvus、Qdrant、Chroma 等持久化向量数据库
     */
   /* @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        log.info("创建 InMemoryEmbeddingStore 成功");
        return store;
    }*/

    @Bean
    public EmbeddingStore loadAndIngestDocuments() {
        //获取knowledge目录的路径
        Path knowledgeDir;
        try {
             knowledgeDir= new ClassPathResource("knowledge").getFile().toPath();
        } catch (IOException e) {
            log.error("加载文档出错，{}", e);
            return null;
        }
        //1. 加载文档：加载knowledge 目录下的所有 TXT 文件
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(knowledgeDir);
        log.info("加载了 {} 个文档", documents.size());

        //2.创建内存嵌入式存储实例
        //TextSegment：表示文档的分块/片段
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        InMemoryEmbeddingStore<TextSegment> embeddingStore1 = new InMemoryEmbeddingStore<>();

        //3.配置嵌入模型，将文档转换为嵌入向量并存储到内存中的嵌入存储中
        //方法一：使用默认配置处理文档（直接将文档摄入到 embeddingStore 嵌入存储中）
        //EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        //方法二：使用自定义配置处理文档（将文档摄入到 embeddingStore1 嵌入存储中）
        DocumentSplitter splitter = DocumentSplitters.recursive(200, 50);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)//向量大模型
                .embeddingStore(embeddingStore1)//嵌入存储实例
                .build();
        ingestor.ingest(documents);

        return embeddingStore;
    }

    /**
     * 定义文档加载和向量化 Bean
     * 启动时自动加载 knowledge 目录下的所有 TXT 文件，分割后向量化存入 EmbeddingStore
     */
  /*  @Bean
    public boolean loadAndIngestDocuments(
            EmbeddingStore<TextSegment> embeddingStore,
            @Autowired EmbeddingModel embeddingModel) {

        // 1. 加载文档：从 classpath:/knowledge/ 目录加载所有 TXT 文件
        Path knowledgeDir;
        try {
            knowledgeDir = new ClassPathResource("knowledge").getFile().toPath();
        } catch (Exception e) {
            log.warn("knowledge 目录不存在，跳过文档加载", e);
            return false;
        }
        List<Document> documents = loadDocuments(
                knowledgeDir,
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
    }*/

    /**
     * 定义内容检索器 Bean
     * EmbeddingStoreContentRetriever：根据用户问题的向量，在 EmbeddingStore 中检索最相似的文档片段
     *
     * 参数说明：
     * - maxResults(3)：最多返回 3 个相关文档片段
     * - minScore(0.7)：相似度阈值，只返回相似度 >= 0.7 的片段
     */
    /*@Bean
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
    }*/
    //检索过程
    @Bean
    public ContentRetriever embeddingStoreContentRetriever(EmbeddingStore embeddingStore) {

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)// 配置嵌入存储，用于检索文档向量
                .minScore(0.5)// 最小相似度阈值
                .maxResults(5)// 最大返回结果数
                .build();
    }
}
