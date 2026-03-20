import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;


public class JumzysNotes {

    private static final Path NOTES_DIR = Path.of(System.getProperty("user.home"), ".notes");

    /**
     * Initialize the notes application.
     * Creates the notes directory if it doesn't exist.
     */
    private static Path setup() {
        // Define the notes directory in HOME
        Path notesDir = NOTES_DIR;
        Path notesSubdir = notesDir.resolve("notes");

        // Create ~/.notes if it doesn't exist
        if (!Files.exists(notesDir)) {
            try {
                Files.createDirectories(notesDir);
                System.out.println("Created notes directory: " + notesDir);
            } catch (IOException e) {
                System.err.println("Error creating notes directory: " + e.getMessage());
                System.exit(1);
            }
        }

        // Create ~/.notes/notes if it doesn't exist
        if (!Files.exists(notesSubdir)) {
            try {
                Files.createDirectories(notesSubdir);
                System.out.println("Created notes subdirectory: " + notesSubdir);
            } catch (IOException e) {
                System.err.println("Error creating notes subdirectory: " + e.getMessage());
                System.exit(1);
            }
        }

        return notesDir;
    }

    /**
     * Parse YAML front matter from a note file.
     * Returns a map with metadata.
     */
    private static Map<String, String> parseYamlHeader(Path filePath) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("file", filePath.getFileName().toString());

        try {
            List<String> lines = Files.readAllLines(filePath);

            // Check if file starts with YAML front matter
            if (lines.isEmpty() || !lines.get(0).trim().equals("---")) {
                metadata.put("title", filePath.getFileName().toString());
                return metadata;
            }

            // Find the closing ---
            int yamlEnd = -1;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("---")) {
                    yamlEnd = i;
                    break;
                }
            }

            if (yamlEnd == -1) {
                metadata.put("title", filePath.getFileName().toString());
                return metadata;
            }

            // Parse YAML lines (simple parsing for basic key: value pairs)
            for (int i = 1; i < yamlEnd; i++) {
                String line = lines.get(i).trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    metadata.put(key, value);
                }
            }

        } catch (IOException e) {
            metadata.put("error", e.getMessage());
        }

        return metadata;
    }

    /**
     * List all notes in the notes directory.
     */
    private static boolean listNotes(Path notesDir) {
        // Check if notes directory exists
        if (!Files.exists(notesDir)) {
            System.err.println("Error: Notes directory does not exist: " + notesDir);
            System.err.println("Create it with: mkdir -p ~/.notes/notes");
            System.err.println("Then copy test notes: cp test-notes/*.md ~/.notes/notes/");
            return false;
        }

        // Look for notes in the notes directory (or directly in .notes)
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        // Find all note files (*.md, *.note, *.txt)
        List<Path> noteFiles;
        try (Stream<Path> paths = Files.walk(searchDir, 1)) {
            noteFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading notes directory: " + e.getMessage());
            return false;
        }

        if (noteFiles.isEmpty()) {
            System.out.println("No notes found in " + notesDir);
            System.err.println("Copy test notes with: cp test-notes/*.md ~/.notes/");
            return true;
        }

        // Parse and display notes
        System.out.println("Notes in " + notesDir + ":");
        System.out.println("=".repeat(60));

        for (Path noteFile : noteFiles) {
            // this should probably be a private method to be re-used
            Map<String, String> metadata = parseYamlHeader(noteFile);
            String title = metadata.getOrDefault("title", noteFile.getFileName().toString());
            String created = metadata.getOrDefault("created", "N/A");
            String tags = metadata.getOrDefault("tags", "");

            System.out.println("\n" + noteFile.getFileName());
            System.out.println("  Title: " + title);
            if (!created.equals("N/A")) {
                System.out.println("  Created: " + created);
            }
            if (!tags.isEmpty()) {
                System.out.println("  Tags: " + tags);
            }
        }

        System.out.println("\n" + noteFiles.size() + " note(s) found.");
        return true;
    }

    /**
     * Read and display the full contents of a note file.
     */
    private static boolean readNote(Path notesDir, String filename) {
        // Find the notes subdirectory
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        // Build the full path to the note
        Path notePath = searchDir.resolve(filename);

        // Check if the file exists
        if (!Files.exists(notePath)) {
            System.err.println("Error: Note not found: " + filename);
            return false;
        }

        // Read and print the file contents
        try {
            String content = Files.readString(notePath);
            System.out.println("=".repeat(60));
            System.out.println(content);
            System.out.println("=".repeat(60));
            return true;
        } catch (IOException e) {
            System.err.println("Error reading note: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a note file after confirmation
     */
    private static boolean deleteNote(Path notesDir, String filename) {
        // Find the notes Subdirectory
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        // Build the full path to the note
        Path notePath = searchDir.resolve(filename);

        // Check if file exists
        if (!Files.exists(notePath)) {
            System.err.println("Error: Note not found: " + filename);
            return false;
        }

        // Ask for confirmation before deleting
        System.out.println("Are you sure you want to delete '" + filename + "'? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes")) {
            try {
                Files.delete(notePath);
                System.out.println("Note deleted: " + filename);
                return true;
            } catch (IOException e) {
                System.err.println("Error deleting note: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("Deletion cancelled.");
            return true;
        }
    }

    /**
     * Generate a safe filename from a note title and current timestamp.
     */
    private static String generateFilename(String title) {
        String safe = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        // Add timestamp for uniqueness
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        return safe + "-" + timestamp + ".md";
    }

    /**
     * Create a new note interactively and save it to the notes directory.
     */
    private static boolean createNote(Path notesDir) {
        // Find the notes subdirectory
        Path notesSubdir = notesDir.resolve("notes");
        Path saveDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        Scanner scanner = new Scanner(System.in);

        // Get title from user
        System.out.println("Enter note title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.err.println("Error: Title cannot be empty.");
            return false;
        }

        // Get tags from user (optional)
        System.out.print("Enter tags (comma-separated, or press Enter to skip): ");
        String tagsInput = scanner.nextLine().trim();

        // Get content from user
        System.out.println("Enter note content (type END on a new line when done):");
        StringBuilder content = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("END")) {
                break;
            }
            content.append(line).append("\n");
        }
        String timestamp = java.time.Instant.now().toString();
        StringBuilder fileContent = new StringBuilder();
        fileContent.append("---\n");
        fileContent.append("title: ").append(title).append("\n");
        fileContent.append("created: ").append(timestamp).append("\n");
        fileContent.append("modified: ").append(timestamp).append("\n");
        if (!tagsInput.isEmpty()) {
            fileContent.append("tags: [").append(tagsInput).append("]\n");
        }
        fileContent.append("---\n\n");
        fileContent.append(content);

        // Generate filename and save
        String filename = generateFilename(title);
        Path notePath = saveDir.resolve(filename);

        try {
            Files.writeString(notePath, fileContent.toString());
            System.out.println("Note created: " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving note: " + e.getMessage());
            return false;
        }
    }

    /**
     * Edit the content of an Existing note.
     */
    private static boolean editNote(Path notesDir, String filename) {
        // Find the notes subdirectory
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        // Build the full path to the note
        Path notePath = searchDir.resolve(filename);

        // Check if the file exists
        if (!Files.exists(notePath)) {
            System.err.println("Error: Note not found: " + filename);
            return false;
        }

        // Load existing metadata so we can preserve it
        Map<String, String> metadata = parseYamlHeader(notePath);
        String title = metadata.getOrDefault("title", filename);
        String created = metadata.getOrDefault("created", java.time.Instant.now().toString());
        String tags = metadata.getOrDefault("tags", "");
        String author = metadata.getOrDefault("author", "");

        // Show current content to the user
        System.out.println("Editing: " + title);
        System.out.println("Current tags: " + (tags.isEmpty() ? "(none)" : tags));
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        // Let user update title (optional)
        System.out.print("Enter new title (or press Enter to keep current): ");
        String newTitle = scanner.nextLine().trim();
        if (newTitle.isEmpty()) {
            newTitle = title;
        }

        // Let user update tags (optional)
        System.out.print("Enter new tags (or press Enter to keep current): ");
        String newTags = scanner.nextLine().trim();
        if (newTags.isEmpty()) {
            newTags = tags;
        }

        // Get new content
        System.out.println("Enter new content (type END on a new line when done):");
        StringBuilder content = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("END")) {
                break;
            }
            content.append(line).append("\n");
        }

        // Build updated file with preserved created and new modified timestamp
        String modifiedTimestamp = java.time.Instant.now().toString();
        StringBuilder fileContent = new StringBuilder();
        fileContent.append("---\n");
        fileContent.append("title: ").append(newTitle).append("\n");
        fileContent.append("created: ").append(created).append("\n");
        fileContent.append("modified: ").append(modifiedTimestamp).append("\n");
        if (!newTags.isEmpty()) {
            fileContent.append("tags: [").append(newTags).append("]\n");
        }
        if (!author.isEmpty()) {
            fileContent.append("author: ").append(author).append("\n");
        }
        fileContent.append("---\n\n");
        fileContent.append(content);

        // Write back to the same file
        try {
            Files.writeString(notePath, fileContent.toString());
            System.out.println("Note updated: " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving note: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search all notes for a keyword in title, tags, or content.
     */
    private static boolean searchNotes(Path notesDir, String keyword) {
        // Find the notes subdirectory
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        // Check directory exists
        if (!Files.exists(searchDir)) {
            System.err.println("Error: Notes directory does not exist: " + searchDir);
            return false;
        }

        // Get all note files
        List<Path> noteFiles;
        try (Stream<Path> paths = Files.walk(searchDir, 1)) {
            noteFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading notes directory: " + e.getMessage());
            return false;
        }

        // Search each note
        String lowerKeyword = keyword.toLowerCase();
        int matchCount = 0;

        System.out.println("Searching for: '" + keyword + "'");
        System.out.println("=".repeat(60));

        for (Path noteFile : noteFiles) {
            try {
                String content = Files.readString(noteFile);
                Map<String, String> metadata = parseYamlHeader(noteFile);
                String title = metadata.getOrDefault("title", noteFile.getFileName().toString());
                String tags = metadata.getOrDefault("tags", "");

                // Check if keyword appears in title, tags, or content
                if (title.toLowerCase().contains(lowerKeyword)
                        || tags.toLowerCase().contains(lowerKeyword)
                        || content.toLowerCase().contains(lowerKeyword)) {
                    System.out.println("\n" + noteFile.getFileName());
                    System.out.println("  Title: " + title);
                    if (!tags.isEmpty()) {
                        System.out.println("  Tags: " + tags);
                    }
                    matchCount++;
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + noteFile.getFileName());
            }
        }

        System.out.println("\n" + matchCount + " note(s) found matching '" + keyword + "'");
        return true;
    }

    /**
     * Run an interactive menu when no command is provided.
     */
    private static void runInteractiveMenu(Path notesDir) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================");
        System.out.println("    Welcome to JumzysNotes  ");
        System.out.println("============================");

        while (true) {
            System.out.println();
            System.out.println("1. List notes");
            System.out.println("2. Read a note");
            System.out.println("3. Create a note");
            System.out.println("4. Edit a note");
            System.out.println("5. Delete a note");
            System.out.println("6. Search notes");
            System.out.println("7. Stats");
            System.out.println("8. Help");
            System.out.println("0. Quit");
            System.out.println();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listNotes(notesDir);
                    break;
                case "2":
                    System.out.print("Enter filename: ");
                    String readFile = scanner.nextLine().trim();
                    readNote(notesDir, readFile);
                    break;
                case "3":
                    createNote(notesDir);
                    break;
                case "4":
                    System.out.print("Enter filename to edit: ");
                    String editFile = scanner.nextLine().trim();
                    editNote(notesDir, editFile);
                    break;
                case "5":
                    System.out.print("Enter filename to delete: ");
                    String deleteFile = scanner.nextLine().trim();
                    deleteNote(notesDir, deleteFile);
                    break;
                case "6":
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine().trim();
                    searchNotes(notesDir, keyword);
                    break;
                case "7":
                    showStats(notesDir);
                    break;
                case "8":
                    showHelp();
                    break;
                case "0":
                    System.out.println("\nGoodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number 0-7.");
            }
        }
    }

    /**
     * Display statistics about the notes collection.
     */
    private static boolean showStats(Path notesDir) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        if (!Files.exists(searchDir)) {
            System.err.println("Error: Notes directory does not exist: " + searchDir);
            return false;
        }

        List<Path> noteFiles;
        try (Stream<Path> paths = Files.walk(searchDir, 1)) {
            noteFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
                    })
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading notes directory: " + e.getMessage());
            return false;
        }

        int totalNotes = noteFiles.size();
        int totalWords = 0;
        Set<String> uniqueTags = new HashSet<>();

        for (Path noteFile : noteFiles) {
            try {
                String content = Files.readString(noteFile);

                // Count words in content only (skip YAML header)
                String[] parts = content.split("---", 3);
                String body = parts.length >= 3 ? parts[2] : content;
                String[] words = body.trim().split("\\s+");
                if (!body.trim().isEmpty()) {
                    totalWords += words.length;
                }

                // Collect tags
                Map<String, String> metadata = parseYamlHeader(noteFile);
                String tags = metadata.getOrDefault("tags", "");
                if (!tags.isEmpty()) {
                    String cleanTags = tags.replaceAll("[\\[\\]]", "");
                    for (String tag : cleanTags.split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty()) {
                            uniqueTags.add(t.toLowerCase());
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Warning: Could not read " + noteFile.getFileName());
            }
        }

        int avgWords = totalNotes > 0 ? totalWords / totalNotes : 0;

        System.out.println("=".repeat(40));
        System.out.println("        JumzysNotes Statistics");
        System.out.println("=".repeat(40));
        System.out.println("  Total notes:       " + totalNotes);
        System.out.println("  Total words:       " + totalWords);
        System.out.println("  Avg words/note:    " + avgWords);
        System.out.println("  Unique tags:       " + uniqueTags.size());
        if (!uniqueTags.isEmpty()) {
            System.out.println("  Tags: " + String.join(", ", uniqueTags));
        }
        System.out.println("=".repeat(40));
        return true;
    }

    /**
     * Display help information.
     */
    private static void showHelp() {
        String helpText = String.format("""
                JumzysNotes v1.0 - Personal Notes Manager

                Usage: java JumzysNotes [command]

                Available commands:
                help    - Display this help information
                list    - List all notes in the notes directory
                read <filename>     - Display a note
                create      - Create a new note
                edit <filename>     - Edit a note
                delete <filename>       - Delete a note
                search <keyword>        - Search notes by keyword in title, tags, or content
                stats                   - Show statistics about your notes
                
                Examples:
                java JumzysNotes list
                java JumzysNotes read sample-note-1.md
                java JumzysNotes create
                java JumzysNotes edit sample-note-1.md
                java JumzysNotes delete sample-note-1.md
                java JumzysNotes search algorithms

                Notes directory: %s

                Setup:
                To test the 'list' command, copy sample notes:
                    mkdir -p ~/.notes/notes
                    cp test-notes/*.md ~/.notes/notes/
                """, NOTES_DIR);
        System.out.println(helpText.trim());
    }

    /**
     * Clean up and exit the application.
     */
    private static void finish(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * Main entry point for the notes CLI application.
     */
    public static void main(String[] args) {
        // Setup
        Path notesDir = setup();

        // Parse command-line arguments
        if (args.length < 1) {
            runInteractiveMenu(notesDir);
            finish(0);
        }

        String command = args[0].toLowerCase();

        // Process command
        switch (command) {
            case "help":
                showHelp();
                finish(0);
                break;
            case "list":
                boolean success = listNotes(notesDir);
                finish(success ? 0 : 1);
                break;
            case "read":
                if (args.length < 2) {
                    System.err.println("Error: Please specify a filename.");
                    System.err.println("Usage: java JumzysNotes read <filename>");
                    finish(1);
                }
                boolean readSuccess = readNote(notesDir, args[1]);
                finish(readSuccess ? 0 : 1);
                break;
            case "delete":
                if (args.length < 2) {
                    System.err.println("Error: Please specify a filename.");
                    System.err.println("Usage: java JumzysNotes delete <filename>");
                    finish(1);
                }
                boolean deleteSuccess = deleteNote(notesDir, args[1]);
                finish(deleteSuccess ? 0 : 1);
                break;
            case "create":
                boolean createSuccess = createNote(notesDir);
                finish(createSuccess ? 0 : 1);
                break;
            case "edit":
                if (args.length < 2) {
                    System.err.println("Error: Please specify a filename.");
                    System.err.println("Usage: java JumzysNotes edit <filename>");
                    finish(1);
                }
                boolean editSuccess = editNote(notesDir, args[1]);
                finish(editSuccess ? 0 : 1);
                break;
            case "search":
                if (args.length < 2) {
                    System.err.println("Error: Please specify a search keyword.");
                    System.err.println("Usage: java JumzysNotes search <keyword>");
                    finish(1);
                }
                boolean searchSuccess = searchNotes(notesDir, args[1]);
                finish(searchSuccess ? 0 : 1);
                break;
            case "stats":
                boolean statsSuccess = showStats(notesDir);
                finish(statsSuccess ? 0 : 1);
                break;
            default:
                System.err.println("Error: Unknown command '" + command + "'");
                System.err.println("Try 'java JumzysNotes help' for more information.");
                finish(1);
        }
    }
}
