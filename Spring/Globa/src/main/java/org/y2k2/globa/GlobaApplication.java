package org.y2k2.globa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GlobaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlobaApplication.class, args);
    }

}
