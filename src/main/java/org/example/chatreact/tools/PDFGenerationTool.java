package org.example.chatreact.tools;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfType1Font;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.example.chatreact.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PDFGenerationTool {
    @Tool(
            description = "Generate a PDF file with the given content. Use this when user asks to create, generate, or save a PDF document.",
            returnDirect = true
    )
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            Files.createDirectories(Path.of(fileDir));
            // TODO try-with-resources
            try (
                    PdfWriter pdfWriter = new PdfWriter(filePath);
                    PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                    Document document = new Document(pdfDocument)
            ) {
                // 运行后自动结束
                // 使用系统字体而不是中文字体（避免字体问题）
                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
