package com.re.busticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BusTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTicketApplication.class, args);
    }

}
