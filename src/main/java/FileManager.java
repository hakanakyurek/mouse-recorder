import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class FileManager {

  public void save(ArrayList<MouseAction> actions, File filePath) throws IOException {

    if (actions.isEmpty() || actions == null) {
      throw new IllegalArgumentException("There are no actions to record.");
    }

    try (ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      dos.writeObject(actions);
    }

  }

  @SuppressWarnings("unchecked")
  public ArrayList<MouseAction> load(File filePath) throws IOException, ClassNotFoundException {

    try (ObjectInputStream ios = new ObjectInputStream(new FileInputStream(filePath))) {
      Object obj = ios.readObject();

      ArrayList<?> actions = (ArrayList<?>) obj;

      for (Object action : actions) {
        if (!(action instanceof MouseAction)) {
          throw new IOException("File contains non-MouseAction objects");
        }
      }

      return (ArrayList<MouseAction>) actions;
    }
  }
}
