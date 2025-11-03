package org.example.aitoolsbox;

import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
class AIToolsBoxApplicationTests {

    @Test
    void contextLoads() throws IOException, InterruptedException {
        Map<String, String> map = new HashMap<>();
        map.put("username", "admin");
        map.put("password", "admin");
        String json = JsonUtils.toJson(map);
        log.info(json);

    }

}
