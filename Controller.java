import java.io.File;
import javax.swing.JOptionPane;

public class Controller {

  private final Model model;
  private final Viewer viewer;

  public Controller(Model model, Viewer viewer) {
    this.model = model;
    this.viewer = viewer;

    viewer.setController(this);
  }

  public void handleCopy() {
    String sourceFileName = viewer.getSelectedFileName();
    if (sourceFileName == null) {
      showMessage("No file selected", "Error");
      return;
    }

    String sourceDir = viewer.getActiveDirectoryPath();
    String targetDir = viewer.getInactiveDirectoryPath();

    String sourcePath = sourceDir + File.separator + sourceFileName;

    int result = JOptionPane.showConfirmDialog(
        viewer,
        "Copy '" + sourceFileName + "' to '" + targetDir + "'?",
        "Confirm Copy",
        JOptionPane.YES_NO_OPTION
    );

    if (result == JOptionPane.YES_OPTION) {
      boolean success = model.copyFile(sourcePath, targetDir);
      if (success) {
        showMessage("File copied successfully", "Copy Complete");
        viewer.refreshPanels();
      } else {
        showMessage("Failed to copy file", "Error");
      }
    }
  }

  public void handleMove() {
    String sourceFileName = viewer.getSelectedFileName();
    if (sourceFileName == null) {
      showMessage("No file selected", "Error");
      return;
    }

    String sourceDir = viewer.getActiveDirectoryPath();
    String targetDir = viewer.getInactiveDirectoryPath();

    String sourcePath = sourceDir + File.separator + sourceFileName;

    int result = JOptionPane.showConfirmDialog(
        viewer,
        "Move '" + sourceFileName + "' to '" + targetDir + "'?",
        "Confirm Move",
        JOptionPane.YES_NO_OPTION
    );

    if (result == JOptionPane.YES_OPTION) {
      boolean success = model.moveFile(sourcePath, targetDir);
      if (success) {
        showMessage("File moved successfully", "Move Complete");
        viewer.refreshPanels();
      } else {
        showMessage("Failed to move file", "Error");
      }
    }
  }

  public void handleMkdir() {
    String directoryName = JOptionPane.showInputDialog(
        viewer,
        "Enter new directory name:",
        "Create Directory",
        JOptionPane.QUESTION_MESSAGE
    );

    if (directoryName != null && !directoryName.trim().isEmpty()) {
      String currentDir = viewer.getActiveDirectoryPath();
      boolean success = model.createDirectory(currentDir, directoryName);

      if (success) {
        showMessage("Directory created successfully", "Create Directory");
        viewer.refreshPanels();
      } else {
        showMessage("Failed to create directory", "Error");
      }
    }
  }

  public void handleDelete() {
    String fileName = viewer.getSelectedFileName();
    if (fileName == null) {
      showMessage("No file selected", "Error");
      return;
    }

    String currentDir = viewer.getActiveDirectoryPath();
    String filePath = currentDir + File.separator + fileName;

    int result = JOptionPane.showConfirmDialog(
        viewer,
        "Delete '" + fileName + "'?",
        "Confirm Delete",
        JOptionPane.YES_NO_OPTION
    );

    if (result == JOptionPane.YES_OPTION) {
      boolean success = model.deleteFile(filePath);
      if (success) {
        showMessage("File deleted successfully", "Delete Complete");
        viewer.refreshPanels();
      } else {
        showMessage("Failed to delete file", "Error");
      }
    }
  }

  public void handleRename() {
    String fileName = viewer.getSelectedFileName();
    if (fileName == null) {
      showMessage("No file selected", "Error");
      return;
    }

    String currentDir = viewer.getActiveDirectoryPath();
    String filePath = currentDir + File.separator + fileName;

    String newName = JOptionPane.showInputDialog(
        viewer,
        "Enter new name:",
        "Rename",
        JOptionPane.QUESTION_MESSAGE
    );

    if (newName != null && !newName.trim().isEmpty()) {
      boolean success = model.renameFile(filePath, newName);
      if (success) {
        showMessage("File renamed successfully", "Rename Complete");
        viewer.refreshPanels();
      } else {
        showMessage("Failed to rename file", "Error");
      }
    }
  }

  private void showMessage(String message, String title) {
    JOptionPane.showMessageDialog(viewer, message, title, JOptionPane.INFORMATION_MESSAGE);
  }
}
