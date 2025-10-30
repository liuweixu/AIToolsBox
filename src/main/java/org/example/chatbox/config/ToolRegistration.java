package org.example.chatbox.config;

import org.example.chatbox.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    /**
     * 工厂模式
     *
     * @return
     */
    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapperTool webScrapperTool = new WebScrapperTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();

        // TODO 涉及到适配器模式的应用
        return ToolCallbacks.from(
                fileOperationTool,
                webScrapperTool,
                terminalOperationTool,
                resourceDownloadTool,
                pdfGenerationTool,
                terminateTool
        );
    }
}
