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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * POST /api/notes - Create a new note
     */

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
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = safe + "-" + timestamp + ".md";

        String nowIso = java.time.Instant.now().toString();
        StringBuilder fileContent = new StringBuilder();
        fileContent.append("---\n");
        fileContent.append("title: ").append(title).append("\n");
        fileContent.append("created: ").append(nowIso).append("\n");
        fileContent.append("modified: ").append(nowIso).append("\n");
        if (!tags.isEmpty()) {
            fileContent.append("tags: [").append(tags).append("]\n");
        }
        fileContent.append("---\n\n");
        fileContent.append(content);

        try {
            Files.createDirectories(NOTES_DIR);
            Files.writeString(NOTES_DIR.resolve(filename), fileContent.toString());
            return ResponseEntity.ok(Map.of("filename", filename, "status", "created"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/notes/{filename} - Update a note
     */
    @PutMapping("/notes/{filename}")
    public ResponseEntity<Map<String, String>> updateNote(
            @PathVariable String filename,
            @RequestBody Map<String, String> body) {

        Path notePath = NOTES_DIR.resolve(filename);

        if (!Files.exists(notePath)) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            List<String> lines = Files.readAllLines(notePath);
            String created = java.time.Instant.now().toString();
            String author = "";

                if (!lines.isEmpty() && lines.get(0).trim().equals("---")) {
                    for (String line : lines) {
                        if (line.startsWith("created:")) created = line.substring(8).trim();
                        if (line.startsWith("author:")) author = line.substring(7).trim();
                }
            }
        
            String title = body.getOrDefault("title", "").trim();
            String content = body.getOrDefault("content", "").trim();
            String tags = body.getOrDefault("tags", "").trim();
            String modifiedNow = java.time.Instant.now().toString();
        
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("---\n");
            fileContent.append("title: ").append(title).append("\n");
            fileContent.append("created: ").append(created).append("\n");
            fileContent.append("modified: ").append(modifiedNow).append("\n");
            if (!tags.isEmpty()) {
                fileContent.append("tags: [").append(tags).append("]\n");
            }
            if (!author.isEmpty()) {
                fileContent.append("author: ").append(author).append("\n");
            }
            fileContent.append("---\n\n");
            fileContent.append(content);

            Files.writeString(notePath, fileContent.toString());
            return ResponseEntity.ok(Map.of("filename", filename, "status", "updated"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     *  DELETE /api/notes.{filename}
     */
    @DeleteMapping("/notes/{filename}")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable String filename) {
        Path notePath = NOTES_DIR.resolve(filename);

        if (!Files.exists(notePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Files.delete(notePath);
            return ResponseEntity.ok(Map.of("filename", filename, "status", "deleted"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/csrf - Get CSRF token for logout form
     */
    @GetMapping("/csrf")
    public Map<String, String> getCsrf(jakarta.servlet.http.HttpServletRequest request) {
        var csrf = (org.springframework.security.web.csrf.CsrfToken) 
            request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        Map<String, String> token = new HashMap<>();
        token.put("token", csrf != null ? csrf.getToken() : "");
        return token;
    }
}