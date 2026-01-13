package top.smartduck.ducktodo;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {OpenAiAutoConfiguration.class})
@EnableScheduling
public class DuckTodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuckTodoApplication.class, args);
    }

}
