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

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api")
public class NotesController {

    private static final Path NOTES_DIR = Path.of(
        System.getProperty("user.home"), ".notes", "notes"
    );

    private static final Path DATASETS_DIR = Path.of(
        System.getProperty("user.home"), ".notes", "datasets"
    );

    /**
     * Create necessary directories on startup.
     */
    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(DATASETS_DIR);
    }

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
        String content = body.getOrDefault("content", "").trim();
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
     * DELETE /api/notes/{filename} - Delete a note
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
     * GET /api/datasets - List all datasets
     */
    @GetMapping("/datasets")
    public List<Map<String, String>> listDatasets() throws IOException {
        if (!Files.exists(DATASETS_DIR)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(DATASETS_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return name.endsWith(".csv") || name.endsWith(".json");
                })
                .sorted()
                .map(p -> {
                    Map<String, String> dataset = new HashMap<>();
                    dataset.put("filename", p.getFileName().toString());
                    dataset.put("size", String.valueOf(p.toFile().length()));
                    // Check for sidecar YAML
                    String sidecar = p.getFileName().toString() + ".dataset.yml";
                    Path sidecarPath = DATASETS_DIR.resolve(sidecar);
                    dataset.put("hasMeta", String.valueOf(Files.exists(sidecarPath)));
                    return dataset;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * POST /api/datasets - Upload a dataset file
     */
    @PostMapping("/datasets")
    public ResponseEntity<Map<String, String>> uploadDataset(
            @RequestBody Map<String, String> body) throws IOException {

        String filename = body.getOrDefault("filename", "").trim();
        String content = body.getOrDefault("content", "").trim();
        String title = body.getOrDefault("title", filename).trim();

        if (filename.isEmpty() || content.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Filename and content are required"));
        }

        if (!filename.endsWith(".csv") && !filename.endsWith(".json")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only CSV and JSON files are supported"));
        }

        // Save the dataset file
        Path datasetPath = DATASETS_DIR.resolve(filename);
        Files.writeString(datasetPath, content);

        // Count rows for CSV
        int rowCount = 0;
        if (filename.endsWith(".csv")) {
            String[] lines = content.split("\n");
            rowCount = Math.max(0, lines.length - 1); // subtract header row
        }

        // Create sidecar YAML metadata file
        String nowIso = java.time.Instant.now().toString();
        String sidecarContent = String.format("""
                title: %s
                filename: %s
                created: %s
                modified: %s
                format: %s
                size: %d
                rowCount: %d
                """,
                title,
                filename,
                nowIso,
                nowIso,
                filename.endsWith(".csv") ? "csv" : "json",
                content.length(),
                rowCount);

        String sidecarFilename = filename + ".dataset.yml";
        Files.writeString(DATASETS_DIR.resolve(sidecarFilename), sidecarContent);

        return ResponseEntity.ok(Map.of(
            "filename", filename,
            "sidecar", sidecarFilename,
            "status", "uploaded"
        ));
    }

    /**
     * GET /api/datasets/{filename}/preview - Preview first 5 rows of a dataset
     */
    @GetMapping("/datasets/{filename}/preview")
    public ResponseEntity<Map<String, Object>> previewDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);

        if (!Files.exists(datasetPath)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> preview = new HashMap<>();
        preview.put("filename", filename);

        if (filename.endsWith(".csv")) {
            List<String> lines = Files.readAllLines(datasetPath);
            preview.put("headers", lines.isEmpty() ? List.of() : 
                List.of(lines.get(0).split(",")));
            preview.put("rows", lines.stream()
                .skip(1)
                .limit(5)
                .map(line -> List.of(line.split(",")))
                .collect(Collectors.toList()));
            preview.put("totalRows", Math.max(0, lines.size() - 1));
        } else {
            String content = Files.readString(datasetPath);
            preview.put("content", content.substring(0, Math.min(500, content.length())));
        }

        return ResponseEntity.ok(preview);
    }

    /**
 * GET /api/datasets/{filename}/full - Get full dataset content
 */
    @GetMapping("/datasets/{filename}/full")
    public ResponseEntity<Map<String, Object>> fullDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);

        if (!Files.exists(datasetPath)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("filename", filename);

        if (filename.endsWith(".csv")) {
            List<String> lines = Files.readAllLines(datasetPath);
            data.put("headers", lines.isEmpty() ? List.of() :
                List.of(lines.get(0).split(",")));
            data.put("rows", lines.stream()
                .skip(1)
                .map(line -> List.of(line.split(",")))
                .collect(Collectors.toList()));
            data.put("totalRows", Math.max(0, lines.size() - 1));
        } else {
            String content = Files.readString(datasetPath);
            data.put("content", content);
        }

        return ResponseEntity.ok(data);
    }

    /**
     * DELETE /api/datasets/{filename} - Delete a dataset and its sidecar
     */
    @DeleteMapping("/datasets/{filename}")
    public ResponseEntity<Map<String, String>> deleteDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);

        if (!Files.exists(datasetPath)) {
            return ResponseEntity.notFound().build();
        }

        // Delete the dataset file
        Files.delete(datasetPath);

        // Delete sidecar if it exists
        Path sidecarPath = DATASETS_DIR.resolve(filename + ".dataset.yml");
        if (Files.exists(sidecarPath)) {
            Files.delete(sidecarPath);
        }

        return ResponseEntity.ok(Map.of("filename", filename, "status", "deleted"));
    }

    /**
     * GET /api/stats - Get statistics about notes
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() throws IOException {
        if (!Files.exists(NOTES_DIR)) {
            return Map.of("totalNotes", 0);
        }

        List<Path> noteFiles;
        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            noteFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .toList();
        }

        int totalNotes = noteFiles.size();
        int totalWords = 0;
        Map<String, Integer> tagCounts = new HashMap<>();
        String newestFile = "";
        String oldestFile = "";
        long newestTime = 0;
        long oldestTime = Long.MAX_VALUE;

        for (Path noteFile : noteFiles) {
            String content = Files.readString(noteFile);
            String[] words = content.split("\\s+");
            totalWords += words.length;

            List<String> lines = Files.readAllLines(noteFile);
            for (String line : lines) {
                if (line.startsWith("tags:")) {
                    String tagLine = line.substring(5).trim()
                        .replaceAll("[\\[\\]]", "");
                    for (String tag : tagLine.split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty()) {
                            tagCounts.put(t, tagCounts.getOrDefault(t, 0) + 1);
                        }
                    }
                }
            }

            long modified = noteFile.toFile().lastModified();
            if (modified > newestTime) {
                newestTime = modified;
                newestFile = noteFile.getFileName().toString();
            }
            if (modified < oldestTime) {
                oldestTime = modified;
                oldestFile = noteFile.getFileName().toString();
            }
        }

        List<Map<String, Object>> topTags = tagCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(5)
            .map(e -> {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("tag", e.getKey());
                tagMap.put("count", e.getValue());
                return tagMap;
            })
            .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotes", totalNotes);
        stats.put("totalWords", totalWords);
        stats.put("totalTags", tagCounts.size());
        stats.put("avgWordsPerNote", totalNotes > 0 ? totalWords / totalNotes : 0);
        stats.put("topTags", topTags);
        stats.put("newestNote", newestFile);
        stats.put("oldestNote", oldestFile);

        return stats;
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
