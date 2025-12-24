import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileManagerTest {

  private FileManager fileManager;

  @TempDir
  Path tempDir;

  @BeforeEach
  void init() {
    this.fileManager = new FileManager();
  }

  @Test
  void testSaveValid() throws IOException, ClassNotFoundException {
    ArrayList<MouseAction> actions = new ArrayList<>();
    actions.add(new MouseAction(MouseAction.Type.PRESS, 100, 200, System.currentTimeMillis(), 1));
    actions.add(new MouseAction(MouseAction.Type.MOVE, 150, 250, System.currentTimeMillis(), 0));
    File testFile = tempDir.resolve("valid_save.dat").toFile();

    fileManager.save(actions, testFile);

    assertTrue(testFile.exists());
    assertTrue(testFile.length() > 0);

    // Verify saved data can be loaded back
    ArrayList<MouseAction> loaded = fileManager.load(testFile);
    assertEquals(2, loaded.size());
  }

  @Test
  void testSaveInvalid() {
    File testFile = tempDir.resolve("invalid_save.dat").toFile();

    // Test with empty list
    ArrayList<MouseAction> emptyActions = new ArrayList<>();
    assertThrows(IllegalArgumentException.class,
        () -> fileManager.save(emptyActions, testFile));

    // Test with null list
    assertThrows(NullPointerException.class,
        () -> fileManager.save(null, testFile));

    // Test with invalid file path
    ArrayList<MouseAction> actions = new ArrayList<>();
    actions.add(new MouseAction(MouseAction.Type.PRESS, 100, 200, System.currentTimeMillis(), 1));
    File invalidFile = new File("/nonexistent/directory/file.dat");
    assertThrows(IOException.class,
        () -> fileManager.save(actions, invalidFile));
  }

  @Test
  void testLoadValid() throws IOException, ClassNotFoundException {
    ArrayList<MouseAction> originalActions = new ArrayList<>();
    originalActions.add(new MouseAction(MouseAction.Type.PRESS, 100, 200, 1000L, 1));
    originalActions.add(new MouseAction(MouseAction.Type.MOVE, 150, 250, 2000L, 0));
    File testFile = tempDir.resolve("valid_load.dat").toFile();

    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(testFile))) {
      oos.writeObject(originalActions);
    }

    ArrayList<MouseAction> loadedActions = fileManager.load(testFile);

    assertNotNull(loadedActions);
    assertEquals(originalActions.size(), loadedActions.size());
    assertEquals(originalActions.get(0), loadedActions.get(0));
    assertEquals(originalActions.get(1), loadedActions.get(1));
  }

  @Test
  void testLoadInvalid() throws IOException {
    // Test loading non-existent file
    File nonExistentFile = new File("nonexistent_file.dat");
    assertThrows(IOException.class,
        () -> fileManager.load(nonExistentFile));

    // Test loading empty file
    File emptyFile = tempDir.resolve("empty.dat").toFile();
    emptyFile.createNewFile();
    assertThrows(Exception.class,
        () -> fileManager.load(emptyFile));

    // Test loading file with non-MouseAction objects
    File invalidFile = tempDir.resolve("invalid.dat").toFile();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(invalidFile))) {
      ArrayList<String> invalidData = new ArrayList<>();
      invalidData.add("not a MouseAction");
      oos.writeObject(invalidData);
    }
    assertThrows(IOException.class,
        () -> fileManager.load(invalidFile));
  }
}
