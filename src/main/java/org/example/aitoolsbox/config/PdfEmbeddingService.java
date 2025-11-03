package org.example.aitoolsbox.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

//@Configuration
@Slf4j
public class PdfEmbeddingService {
    private final VectorStore vectorStore;

    public PdfEmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    //    @PostConstruct
    public void processPDFAndStoreToRedis() {
        Resource pdfResource = new ClassPathResource("document/深入理解C (3rd Edition).pdf");
        try {
            // 1. 读取PDF文档
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder().withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)//如果设置为0，则表示所有页都变成一个文档
                    .build();

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    pdfResource, config);

            List<Document> documents = pdfReader.get();

            // 2. 文本分割
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            // 分批处理，每批不超过10个文档，避免超出嵌入模型的批量大小限制
            int batchSize = 10;
            for (int i = 0; i < splitDocuments.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, splitDocuments.size());
                List<Document> batch = splitDocuments.subList(i, endIndex);
                // 3. 存储到Milvus向量数据库（分批处理）
                vectorStore.add(batch);
                log.info("Processed batch {} to {}", i / batchSize + 1, (endIndex - 1) / batchSize + 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF and store to Redis", e);
        }
    }
}
