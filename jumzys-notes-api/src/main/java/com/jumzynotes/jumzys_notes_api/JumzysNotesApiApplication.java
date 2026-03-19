package com.jumzynotes.jumzys_notes_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JumzysNotesApiApplication {

    public static void main(String[] args) throws Exception {
    SpringApplication.run(JumzysNotesApiApplication.class, args);

    // Open browser using Mac's open command
    Thread.sleep(1500);
    Runtime.getRuntime().exec(new String[]{"open", "http://localhost:8080"});
	}
}

