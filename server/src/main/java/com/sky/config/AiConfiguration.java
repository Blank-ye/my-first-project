package com.sky.config;

import com.sky.ChatMemoryStore.RedisChatMemoryStore;
import com.sky.rag.BM25ContentRetriever;
import com.sky.rag.HybridContentRetriever;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@Configuration
@Slf4j
public class AiConfiguration {
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /*记忆存储*/
    @Bean
    public ChatMemoryProvider getMyChatMemoryProvider() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object o) {
                return MessageWindowChatMemory.builder()
                        .id(o)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
    }

    /*定义向量存储*/
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(){
        QdrantEmbeddingStore store = new QdrantEmbeddingStore.Builder()
                .host("192.168.153.132")
                .port(6334)
                .collectionName("sky-knowledge")
                .build();
        log.info("向量存储创建完毕，{}",store);
        return store;
    }

    /*文件加载、向量化 + BM25 索引构建*/
    @EventListener(ApplicationReadyEvent.class)
    public void loadAndIngestDocuments(){
        //1.获取文件的路径
        Path pathDir;
        try {
            pathDir = new ClassPathResource("AiRag").getFile().toPath();
        } catch (IOException e) {
            log.info("文件加载失败");
            return;
        }
        //2.加载AiRag文件夹下的所有文件
        List<Document> documents = loadDocuments(
                pathDir,
                new TextDocumentParser());
        log.info("加载了{}个文件",documents.size());

        //3.确保 Qdrant collection 存在，已存在则跳过 ingest
        if (!ensureCollectionExists()) {
            log.info("Collection 已存在且有数据，跳过向量 ingest");
            return;
        }

        //4.分割文件：按段落分割，每块最多200个字符，重叠50个字符
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        //5.使用 EmbeddingStoreIngestor 完成：分割 → 向量化 → 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        log.info("文件分割了{}片段",documents.size());
    }

    /**
     * 从 AiRag 目录加载文档，构建 BM25 索引
     * 使用与向量化相同的分割策略，保证文档片段一致
     */
    private BM25ContentRetriever buildBM25Retriever() {
        Path pathDir;
        try {
            pathDir = new ClassPathResource("AiRag").getFile().toPath();
        } catch (IOException e) {
            log.warn("AiRag 文件加载失败，BM25 索引无法构建", e);
            return null;
        }

        List<Document> documents = loadDocuments(pathDir, new TextDocumentParser());
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        List<String> texts = new ArrayList<>();
        List<Metadata> metadataList = new ArrayList<>();

        for (Document doc : documents) {
            List<TextSegment> segments = splitter.split(doc);
            for (TextSegment segment : segments) {
                texts.add(segment.text());
                metadataList.add(segment.metadata());
            }
        }

        BM25ContentRetriever retriever = BM25ContentRetriever.builder()
                .maxResults(6)
                .build();
        retriever.buildIndex(texts, metadataList);
        log.info("BM25 索引构建完成，{} 个文档片段", texts.size());
        return retriever;
    }

    /** 检查 collection 是否存在，不存在则创建，返回 true 表示需要 ingest */
    private boolean ensureCollectionExists() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest check = HttpRequest.newBuilder()
                    .uri(URI.create("http://192.168.153.132:6333/collections/sky-knowledge"))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(check, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                log.info("Collection 已存在，跳过 ingest");
                return false;
            }
        } catch (Exception e) {
            log.warn("检查 collection 失败: {}", e.getMessage());
        }
        // 不存在则创建
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{\"vectors\":{\"size\":2048,\"distance\":\"Cosine\"}}";
            HttpRequest create = HttpRequest.newBuilder()
                    .uri(URI.create("http://192.168.153.132:6333/collections/sky-knowledge"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(create, HttpResponse.BodyHandlers.ofString());
            log.info("Collection 创建成功");
        } catch (Exception e) {
            log.warn("创建 collection 失败: {}", e.getMessage());
        }
        return true;
    }

    /*定义向量检索器（供 Hybrid 内部使用）*/
    @Bean
    public ContentRetriever embeddingStoreContentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(6)
                .minScore(0.6)
                .build();
    }

    /*定义混合检索器：BM25 + 向量，RRF 融合排序*/
    @Bean
    public ContentRetriever hybridContentRetriever(
            ContentRetriever embeddingStoreContentRetriever){
        BM25ContentRetriever bm25Retriever = buildBM25Retriever();
        if (bm25Retriever == null) {
            log.warn("BM25 索引构建失败，回退为纯向量检索");
            return embeddingStoreContentRetriever;
        }
        return HybridContentRetriever.builder()
                .bm25Retriever(bm25Retriever)
                .embeddingRetriever(embeddingStoreContentRetriever)
                .maxResults(6)
                .build();
    }
}
