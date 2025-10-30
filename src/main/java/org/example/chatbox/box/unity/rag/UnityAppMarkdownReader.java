package org.example.chatbox.box.unity.rag;

import org.springframework.ai.document.Document;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UnityAppMarkdownReader {

    private final ResourcePatternResolver resourcePatternResolver;

    public UnityAppMarkdownReader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        return null;
    }
}
