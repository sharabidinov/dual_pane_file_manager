import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        e.printStackTrace();
      }

      Model model = new Model();

      Viewer viewer = new Viewer(model);

      Controller controller = new Controller(model, viewer);

      viewer.setVisible(true);
    });
  }
}
