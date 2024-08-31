package com.sebscode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

@SpringBootApplication
@RestController

public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping("/greet")
    public GreetResponse greet() {
        return new GreetResponse("Hi, how u doin?",
                List.of("Carrot", "Tomato", "Olive"),
                new Person("Jose Eduardo"));
    }
    record Person(String name){}
    record GreetResponse(String greet,
                         List<String> favVegetable,
                         Person person){}
}
