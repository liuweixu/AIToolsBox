package org.example.chatreact;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootTest
@Slf4j
class ChatReactApplicationTests {

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
