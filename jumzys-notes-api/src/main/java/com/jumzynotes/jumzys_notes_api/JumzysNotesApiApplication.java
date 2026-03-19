package com.jumzynotes.jumzys_notes_api;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JumzysNotesApiApplication {

    public static void main(String[] args) throws Exception {
    SpringApplication.run(JumzysNotesApiApplication.class, args);

    // Wait for server to fully start then open browser
    Thread.sleep(1500);
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    }
}

}