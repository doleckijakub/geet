package pl.doleckijakub.geet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Geet {

    public static void main(String[] args) {
        System.out.println("Starting Geet!");
        SpringApplication.run(Geet.class, args);
    }

}