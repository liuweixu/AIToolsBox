package org.example.aitoolsbox.tools;

import org.example.aitoolsbox.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL", returnDirect = true)
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;

        try {
            // 创建目录
            Files.createDirectories(Paths.get(fileDir));
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            // TODO 文件输出流
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(response.body());
            }
            return "Resource downloaded successfully to: " + filePath;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
