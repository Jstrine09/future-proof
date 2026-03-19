package com.jumzynotes.jumzys_notes_api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class NotesController {

    private static final Path NOTES_DIR = Path.of(
        System.getProperty("user.home"), ".notes", "notes"
    );

    /**
     * GET /api/notes - List all notes
     */
    @GetMapping("/notes")
    public List<Map<String, String>> listNotes() throws IOException {
        if (!Files.exists(NOTES_DIR)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .sorted()
                .map(p -> {
                    Map<String, String> note = new HashMap<>();
                    note.put("filename", p.getFileName().toString());
                    try {
                        note.put("content", Files.readString(p));
                    } catch (IOException e) {
                        note.put("content", "");
                    }
                    return note;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * GET /api/notes/{filename} - Get a specific note
     */
    @GetMapping("/notes/{filename}")
    public Map<String, String> getNote(@PathVariable String filename) throws IOException {
        Path notePath = NOTES_DIR.resolve(filename);

        if (!Files.exists(notePath)) {
            throw new RuntimeException("Note not found: " + filename);
        }

        Map<String, String> note = new HashMap<>();
        note.put("filename", filename);
        note.put("content", Files.readString(notePath));
        return note;
    }

    @PostMapping("/notes")
    public ResponseEntity<Map<String, String>> createNote(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "").trim();
        String content =body.getOrDefault("content", "").trim();
        String tags = body.getOrDefault("tags", "").trim();
    
        if (title.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title cannot be empty"));
        }

        String safe = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();

        
    }

    /**
     * GET /api/health - Health check
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("notesDir", NOTES_DIR.toString());
        return status;
    }
}