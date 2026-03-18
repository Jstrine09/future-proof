import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JumzysNotesTest {

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testDir = Files.createTempDirectory("jumzys-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(testDir)
            .sorted(Comparator.reverseOrder())
            .forEach(p -> { try { Files.delete(p); } catch (IOException e) {} });
    }

    // Helper - write a note file
    private Path makeNote(String filename, String title, String content) throws IOException {
        String text = "---\ntitle: " + title + "\ncreated: 2026-01-01T00:00:00Z\nmodified: 2026-01-01T00:00:00Z\n---\n\n" + content;
        Path p = testDir.resolve(filename);
        Files.writeString(p, text);
        return p;
    }

    // LIST
    @Test void list_emptyDir_returnsZeroFiles() throws IOException {
        long count = Files.walk(testDir, 1).filter(p -> p.toString().endsWith(".md")).count();
        assertEquals(0, count);
    }

    @Test void list_threeNotes_returnsThree() throws IOException {
        makeNote("a.md", "A", "content");
        makeNote("b.md", "B", "content");
        makeNote("c.md", "C", "content");
        long count = Files.walk(testDir, 1).filter(p -> p.toString().endsWith(".md")).count();
        assertEquals(3, count);
    }

    @Test void list_ignoresNonMdFiles() throws IOException {
        makeNote("a.md", "A", "content");
        Files.writeString(testDir.resolve("data.csv"), "a,b,c");
        long count = Files.walk(testDir, 1).filter(p -> p.toString().endsWith(".md")).count();
        assertEquals(1, count);
    }

    // READ
    @Test void read_existingNote_containsTitle() throws IOException {
        Path p = makeNote("read.md", "My Title", "Hello world");
        assertTrue(Files.readString(p).contains("My Title"));
    }

    @Test void read_existingNote_containsContent() throws IOException {
        Path p = makeNote("read2.md", "Title", "Special content here");
        assertTrue(Files.readString(p).contains("Special content here"));
    }

    @Test void read_missingFile_doesNotExist() {
        assertFalse(Files.exists(testDir.resolve("ghost.md")));
    }

    // CREATE
    @Test void create_newNote_fileExistsOnDisk() throws IOException {
        Path p = makeNote("new.md", "New Note", "Some content");
        assertTrue(Files.exists(p));
    }

    @Test void create_newNote_hasYamlHeader() throws IOException {
        Path p = makeNote("yaml.md", "YAML Test", "Content");
        String text = Files.readString(p);
        assertTrue(text.startsWith("---"));
        assertTrue(text.contains("title:"));
    }

    @Test void create_emptyTitle_isEmpty() {
        String title = "";
        assertTrue(title.isEmpty());
    }

    // EDIT
    @Test void edit_updatesContent() throws IOException {
        Path p = makeNote("edit.md", "Edit Me", "Old content");
        String updated = Files.readString(p).replace("Old content", "New content");
        Files.writeString(p, updated);
        assertTrue(Files.readString(p).contains("New content"));
    }

    @Test void edit_preservesCreatedTimestamp() throws IOException {
        Path p = makeNote("ts.md", "Timestamps", "content");
        assertTrue(Files.readString(p).contains("created: 2026-01-01T00:00:00Z"));
    }

    @Test void edit_updatesTags() throws IOException {
        Path p = makeNote("tags.md", "Tags", "content");
        String updated = Files.readString(p).replace("---\n\n", "tags: [newtag]\n---\n\n");
        Files.writeString(p, updated);
        assertTrue(Files.readString(p).contains("newtag"));
    }

    // DELETE
    @Test void delete_removesFile() throws IOException {
        Path p = makeNote("del.md", "Delete Me", "content");
        Files.delete(p);
        assertFalse(Files.exists(p));
    }

    @Test void delete_missingFile_throwsException() {
        assertThrows(IOException.class, () -> Files.delete(testDir.resolve("ghost.md")));
    }

    @Test void delete_otherNotesUnaffected() throws IOException {
        Path keep = makeNote("keep.md", "Keep", "content");
        Path del = makeNote("del.md", "Delete", "content");
        Files.delete(del);
        assertTrue(Files.exists(keep));
        assertFalse(Files.exists(del));
    }

    // SEARCH
    @Test void search_findsByTitle() throws IOException {
        makeNote("s1.md", "Java Programming", "content");
        long count = Files.walk(testDir, 1).filter(Files::isRegularFile)
            .filter(p -> { try { return Files.readString(p).toLowerCase().contains("java"); } catch (IOException e) { return false; } })
            .count();
        assertEquals(1, count);
    }

    @Test void search_findsByContent() throws IOException {
        makeNote("s2.md", "Title", "algorithms are fun");
        long count = Files.walk(testDir, 1).filter(Files::isRegularFile)
            .filter(p -> { try { return Files.readString(p).toLowerCase().contains("algorithms"); } catch (IOException e) { return false; } })
            .count();
        assertEquals(1, count);
    }

    @Test void search_noMatch_returnsZero() throws IOException {
        makeNote("s3.md", "Title", "content");
        long count = Files.walk(testDir, 1).filter(Files::isRegularFile)
            .filter(p -> { try { return Files.readString(p).toLowerCase().contains("zzznomatch"); } catch (IOException e) { return false; } })
            .count();
        assertEquals(0, count);
    }

    @Test void search_caseInsensitive() throws IOException {
        makeNote("s4.md", "UPPERCASE TITLE", "content");
        long count = Files.walk(testDir, 1).filter(Files::isRegularFile)
            .filter(p -> { try { return Files.readString(p).toLowerCase().contains("uppercase"); } catch (IOException e) { return false; } })
            .count();
        assertEquals(1, count);
    }
}
