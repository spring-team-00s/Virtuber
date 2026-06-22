package org.example.virtuber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VirtuberApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtuberApplication.class, args);
    }

}
