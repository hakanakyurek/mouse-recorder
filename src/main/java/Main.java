import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      UI ui = new UI();
      ui.display();
    });
  }
}
