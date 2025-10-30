package org.example.chatbox;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
@Slf4j
class ChatBoxApplicationTests {

    @Test
    void contextLoads() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("ls -l");
        log.info(process.getInputStream().toString());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//        }
//        process.waitFor();
    }

}
