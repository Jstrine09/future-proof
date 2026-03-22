package com.jumzynotes.jumzys_notes_api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final Path MEDIA_DIR = Path.of(
        System.getProperty("user.home"), ".notes", "media"
    );

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(NOTES_DIR);
        Files.createDirectories(DATASETS_DIR);
        Files.createDirectories(MEDIA_DIR);
    }

    /**
     * GET /api/notes - List all notes
     */
    @GetMapping("/notes")
    public List<Map<String, String>> listNotes() throws IOException {
        if (!Files.exists(NOTES_DIR)) return List.of();

        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .sorted()
                .map(p -> {
                    Map<String, String> note = new HashMap<>();
                    note.put("filename", p.getFileName().toString());
                    try { note.put("content", Files.readString(p)); }
                    catch (IOException e) { note.put("content", ""); }
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
        if (!Files.exists(notePath)) throw new RuntimeException("Note not found: " + filename);

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
        if (!tags.isEmpty()) fileContent.append("tags: [").append(tags).append("]\n");
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
        if (!Files.exists(notePath)) return ResponseEntity.notFound().build();

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
            if (!tags.isEmpty()) fileContent.append("tags: [").append(tags).append("]\n");
            if (!author.isEmpty()) fileContent.append("author: ").append(author).append("\n");
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
        if (!Files.exists(notePath)) return ResponseEntity.notFound().build();

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
        if (!Files.exists(DATASETS_DIR)) return List.of();

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
                    String sidecar = p.getFileName().toString() + ".dataset.yml";
                    dataset.put("hasMeta", String.valueOf(
                        Files.exists(DATASETS_DIR.resolve(sidecar))));
                    return dataset;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * POST /api/datasets - Upload and validate a small dataset
     */
    @PostMapping("/datasets")
    public ResponseEntity<Map<String, Object>> uploadDataset(
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

        Map<String, Object> validation = filename.endsWith(".csv")
            ? validateCSV(content) : validateJSON(content);

        if (!(boolean) validation.get("valid")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed");
            errorResponse.put("details", validation.get("errors"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Files.writeString(DATASETS_DIR.resolve(filename), content);

        String nowIso = java.time.Instant.now().toString();
        int rowCount = (int) validation.get("rowCount");
        int colCount = (int) validation.get("colCount");

        String sidecarContent = String.format("""
                title: %s
                filename: %s
                created: %s
                modified: %s
                format: %s
                size: %d
                rowCount: %d
                colCount: %d
                """,
                title, filename, nowIso, nowIso,
                filename.endsWith(".csv") ? "csv" : "json",
                content.length(), rowCount, colCount);

        Files.writeString(DATASETS_DIR.resolve(filename + ".dataset.yml"), sidecarContent);

        Map<String, Object> response = new HashMap<>();
        response.put("filename", filename);
        response.put("sidecar", filename + ".dataset.yml");
        response.put("status", "uploaded");
        response.put("rowCount", rowCount);
        response.put("colCount", colCount);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/datasets/upload - Stream upload a large dataset
     */
    @PostMapping("/datasets/upload")
    public ResponseEntity<Map<String, Object>> streamUploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", defaultValue = "") String title) throws IOException {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        if (!filename.endsWith(".csv") && !filename.endsWith(".json")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only CSV and JSON files are supported"));
        }

        if (title.isEmpty()) title = filename.replace(".csv", "").replace(".json", "");

        Path datasetPath = DATASETS_DIR.resolve(filename);
        Files.copy(file.getInputStream(), datasetPath, StandardCopyOption.REPLACE_EXISTING);

        int rowCount = 0;
        int colCount = 0;

        if (filename.endsWith(".csv")) {
            try (java.io.BufferedReader reader = Files.newBufferedReader(datasetPath)) {
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    colCount = headerLine.split(",").length;
                    while (reader.readLine() != null) rowCount++;
                }
            }
        } else {
            rowCount = -1;
            colCount = -1;
        }

        String nowIso = java.time.Instant.now().toString();
        String sidecarContent = String.format("""
                title: %s
                filename: %s
                created: %s
                modified: %s
                format: %s
                size: %d
                rowCount: %d
                colCount: %d
                """,
                title, filename, nowIso, nowIso,
                filename.endsWith(".csv") ? "csv" : "json",
                file.getSize(), rowCount, colCount);

        Files.writeString(DATASETS_DIR.resolve(filename + ".dataset.yml"), sidecarContent);

        Map<String, Object> response = new HashMap<>();
        response.put("filename", filename);
        response.put("status", "uploaded");
        response.put("size", file.getSize());
        response.put("rowCount", rowCount);
        response.put("colCount", colCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate CSV content
     */
    private Map<String, Object> validateCSV(String content) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        String[] lines = content.split("\n");

        if (lines.length == 0) {
            errors.add("File is empty");
            result.put("valid", false); result.put("errors", errors);
            result.put("rowCount", 0); result.put("colCount", 0);
            return result;
        }

        String[] headers = lines[0].split(",");
        int expectedCols = headers.length;

        if (expectedCols == 0) {
            errors.add("Header row is empty");
            result.put("valid", false); result.put("errors", errors);
            result.put("rowCount", 0); result.put("colCount", 0);
            return result;
        }

        int dataRows = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",");
            if (cols.length != expectedCols) {
                errors.add("Row " + (i + 1) + " has " + cols.length +
                    " columns but header has " + expectedCols);
            }
            dataRows++;
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("rowCount", dataRows);
        result.put("colCount", expectedCols);
        return result;
    }

    /**
     * Validate JSON content
     */
    private Map<String, Object> validateJSON(String content) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        if (content.trim().isEmpty()) {
            errors.add("File is empty");
            result.put("valid", false); result.put("errors", errors);
            result.put("rowCount", 0); result.put("colCount", 0);
            return result;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(content);
            int rowCount = 0, colCount = 0;

            if (node.isArray()) {
                rowCount = node.size();
                if (rowCount > 0 && node.get(0).isObject()) colCount = node.get(0).size();
            } else if (node.isObject()) {
                colCount = node.size(); rowCount = 1;
            }

            result.put("valid", true); result.put("errors", errors);
            result.put("rowCount", rowCount); result.put("colCount", colCount);
        } catch (Exception e) {
            errors.add("Invalid JSON: " + e.getMessage());
            result.put("valid", false); result.put("errors", errors);
            result.put("rowCount", 0); result.put("colCount", 0);
        }

        return result;
    }

    /**
     * GET /api/datasets/{filename}/preview - Preview first 5 rows
     */
    @GetMapping("/datasets/{filename}/preview")
    public ResponseEntity<Map<String, Object>> previewDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);
        if (!Files.exists(datasetPath)) return ResponseEntity.notFound().build();

        Map<String, Object> preview = new HashMap<>();
        preview.put("filename", filename);

        if (filename.endsWith(".csv")) {
            List<String> lines = Files.readAllLines(datasetPath);
            preview.put("headers", lines.isEmpty() ? List.of() :
                List.of(lines.get(0).split(",")));
            preview.put("rows", lines.stream().skip(1).limit(5)
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
     * GET /api/datasets/{filename}/full - Get full dataset
     */
    @GetMapping("/datasets/{filename}/full")
    public ResponseEntity<Map<String, Object>> fullDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);
        if (!Files.exists(datasetPath)) return ResponseEntity.notFound().build();

        Map<String, Object> data = new HashMap<>();
        data.put("filename", filename);

        if (filename.endsWith(".csv")) {
            List<String> lines = Files.readAllLines(datasetPath);
            data.put("headers", lines.isEmpty() ? List.of() :
                List.of(lines.get(0).split(",")));
            data.put("rows", lines.stream().skip(1)
                .map(line -> List.of(line.split(",")))
                .collect(Collectors.toList()));
            data.put("totalRows", Math.max(0, lines.size() - 1));
        } else {
            data.put("content", Files.readString(datasetPath));
        }

        return ResponseEntity.ok(data);
    }

    /**
     * DELETE /api/datasets/{filename} - Delete a dataset and sidecar
     */
    @DeleteMapping("/datasets/{filename}")
    public ResponseEntity<Map<String, String>> deleteDataset(
            @PathVariable String filename) throws IOException {

        Path datasetPath = DATASETS_DIR.resolve(filename);
        if (!Files.exists(datasetPath)) return ResponseEntity.notFound().build();

        Files.delete(datasetPath);
        Path sidecarPath = DATASETS_DIR.resolve(filename + ".dataset.yml");
        if (Files.exists(sidecarPath)) Files.delete(sidecarPath);

        return ResponseEntity.ok(Map.of("filename", filename, "status", "deleted"));
    }

    /**
     * POST /api/media/{noteId} - Upload media for a note
     */
    @PostMapping("/media/{noteId}")
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @PathVariable String noteId,
            @RequestParam("file") MultipartFile file) throws IOException {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String lower = filename.toLowerCase();
        boolean isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp");
        boolean isVideo = lower.endsWith(".mp4") || lower.endsWith(".mov")
            || lower.endsWith(".webm") || lower.endsWith(".avi");

        if (!isImage && !isVideo) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only images and videos are supported"));
        }

        Path noteMediaDir = MEDIA_DIR.resolve(noteId);
        Files.createDirectories(noteMediaDir);

        Path filePath = noteMediaDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Map<String, Object> response = new HashMap<>();
        response.put("filename", filename);
        response.put("noteId", noteId);
        response.put("type", isImage ? "image" : "video");
        response.put("size", file.getSize());
        response.put("url", "/api/media/" + noteId + "/" + filename);
        response.put("status", "uploaded");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/media/{noteId} - List all media for a note
     */
    @GetMapping("/media/{noteId}")
    public List<Map<String, Object>> listMedia(@PathVariable String noteId) throws IOException {
        Path noteMediaDir = MEDIA_DIR.resolve(noteId);
        if (!Files.exists(noteMediaDir)) return List.of();

        try (Stream<Path> paths = Files.walk(noteMediaDir, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .sorted()
                .map(p -> {
                    String name = p.getFileName().toString();
                    String low = name.toLowerCase();
                    boolean isImage = low.endsWith(".jpg") || low.endsWith(".jpeg")
                        || low.endsWith(".png") || low.endsWith(".gif")
                        || low.endsWith(".webp");

                    Map<String, Object> media = new HashMap<>();
                    media.put("filename", name);
                    media.put("type", isImage ? "image" : "video");
                    media.put("size", p.toFile().length());
                    media.put("url", "/api/media/" + noteId + "/" + name);
                    return media;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * GET /api/media/{noteId}/{filename} - Serve a media file
     */
    @GetMapping("/media/{noteId}/{filename}")
    public ResponseEntity<Resource> serveMedia(
            @PathVariable String noteId,
            @PathVariable String filename) throws IOException {

        Path filePath = MEDIA_DIR.resolve(noteId).resolve(filename);
        if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    /**
     * DELETE /api/media/{noteId}/{filename} - Delete a media file
     */
    @DeleteMapping("/media/{noteId}/{filename}")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @PathVariable String noteId,
            @PathVariable String filename) throws IOException {

        Path filePath = MEDIA_DIR.resolve(noteId).resolve(filename);
        if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

        Files.delete(filePath);
        return ResponseEntity.ok(Map.of("filename", filename, "status", "deleted"));
    }

    /**
     * GET /api/search?q=keyword - Search notes by keyword
     */
    @GetMapping("/search")
    public List<Map<String, Object>> searchNotes(
            @RequestParam("q") String query) throws IOException {

        if (!Files.exists(NOTES_DIR)) return List.of();

        String lowerQuery = query.toLowerCase().trim();
        List<Map<String, Object>> results = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            List<Path> noteFiles = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .toList();

            for (Path noteFile : noteFiles) {
                String content = Files.readString(noteFile);
                String lower = content.toLowerCase();

                if (lower.contains(lowerQuery)) {
                    // Parse metadata
                    Map<String, String> meta = new HashMap<>();
                    String title = noteFile.getFileName().toString();
                    String tags = "";

                    String[] lines = content.split("\n");
                    if (lines.length > 0 && lines[0].trim().equals("---")) {
                        for (String line : lines) {
                            if (line.startsWith("title:")) title = line.substring(6).trim();
                            if (line.startsWith("tags:")) tags = line.substring(5).trim();
                        }
                    }

                    // Find matching excerpt
                    int matchIdx = lower.indexOf(lowerQuery);
                    int start = Math.max(0, matchIdx - 60);
                    int end = Math.min(content.length(), matchIdx + query.length() + 60);
                    String excerpt = "..." + content.substring(start, end).trim() + "...";

                    Map<String, Object> result = new HashMap<>();
                    result.put("filename", noteFile.getFileName().toString());
                    result.put("title", title);
                    result.put("tags", tags);
                    result.put("excerpt", excerpt);
                    result.put("matchIndex", matchIdx);
                    results.add(result);
                }
            }
        }

        return results;
    }

    /**
     * GET /api/stats - Get statistics about notes
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() throws IOException {
        if (!Files.exists(NOTES_DIR)) return Map.of("totalNotes", 0);

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
            totalWords += content.split("\\s+").length;

            for (String line : Files.readAllLines(noteFile)) {
                if (line.startsWith("tags:")) {
                    String tagLine = line.substring(5).trim().replaceAll("[\\[\\]]", "");
                    for (String tag : tagLine.split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty()) tagCounts.put(t, tagCounts.getOrDefault(t, 0) + 1);
                    }
                }
            }

            long modified = noteFile.toFile().lastModified();
            if (modified > newestTime) { newestTime = modified; newestFile = noteFile.getFileName().toString(); }
            if (modified < oldestTime) { oldestTime = modified; oldestFile = noteFile.getFileName().toString(); }
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
     * GET /api/csrf - Get CSRF token
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