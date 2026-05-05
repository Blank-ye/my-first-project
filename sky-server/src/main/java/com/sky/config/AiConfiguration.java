package com.sky.config;

import com.sky.ChatMemoryStore.RedisChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@Configuration
@Slf4j
public class AiConfiguration {
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private EmbeddingModel embeddingModel;

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
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        log.info("向量存储创建完毕，{}",store);
        return store;
    }

    /*文件加载和向量化*/
    @Bean
    public boolean loadAndIngestDocuments(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel){
        //1.获取文件的路径
        Path pathDir;
        try {
            pathDir = new ClassPathResource("AiRag").getFile().toPath();
        } catch (IOException e) {
            log.info("文件加载失败");
            return false;
        }
        //2.加载AiRag文件夹下的所有文件
        List<Document> documents = loadDocuments(
                pathDir,
                new TextDocumentParser());
        log.info("加载了{}个文件",documents.size());
        //3.分割文件：按段落分割，每块最多200个字符，重叠50个字符
        DocumentSplitter splitter = DocumentSplitters.recursive(200, 50);
        //4.使用 EmbeddingStoreIngestor 完成：分割 → 向量化 → 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)//向量大模型
                .embeddingStore(embeddingStore)//嵌入存储实例
                .build();
        ingestor.ingest(documents);
        log.info("文件分割了{}片段",documents.size());
        return true;
    }

    /*定义内容检索器*/
    @Bean
    public ContentRetriever embeddingStoreContentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.85)
                .build();
    }
}
