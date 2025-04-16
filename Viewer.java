import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class Viewer extends JFrame {

  private JTable leftFileTable;
  private JTable rightFileTable;
  private JLabel leftStatusBar;
  private JLabel rightStatusBar;
  private JLabel commandLine;
  private JPanel functionKeysPanel;

  private File leftCurrentDirectory;
  private File rightCurrentDirectory;
  private JTable activeTable;
  private JLabel activeStatusBar;

  private final String[] columnNames = {"Name", "Size", "Date", "Attr"};
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private final Color panelBgColor = new Color(0, 0, 128);
  private final Color panelTextColor = new Color(192, 192, 192);
  private final Color activePanelColor = new Color(0, 0, 255);

  public Viewer() {
    setTitle("Dual Pane File Manager | AS");
    setSize(1024, 768);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    leftCurrentDirectory = new File(System.getProperty("user.home"));
    rightCurrentDirectory = new File(System.getProperty("user.home"));

    createUI();

    refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
    refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);

    setActivePanel(leftFileTable, leftStatusBar);

    setupKeyboardShortcuts();

    setVisible(true);
  }

  private final Color selectedRowColor = new Color(80, 80, 160);

  private void createUI() {
    JPanel mainPanel = new JPanel(new BorderLayout());

    JPanel panelsArea = new JPanel(new GridLayout(1, 2, 2, 0));

    JPanel leftPanel = createFilePanel();
    leftFileTable = (JTable) ((JScrollPane) leftPanel.getComponent(0)).getViewport().getView();
    leftStatusBar = (JLabel) leftPanel.getComponent(1);

    JPanel rightPanel = createFilePanel();
    rightFileTable = (JTable) ((JScrollPane) rightPanel.getComponent(0)).getViewport().getView();
    rightStatusBar = (JLabel) rightPanel.getComponent(1);

    panelsArea.add(leftPanel);
    panelsArea.add(rightPanel);

    JPanel commandArea = new JPanel(new BorderLayout());
    commandArea.setBackground(panelBgColor);
    commandLine = new JLabel(" ");
    commandLine.setForeground(panelTextColor);
    commandArea.add(commandLine, BorderLayout.CENTER);

    functionKeysPanel = createFunctionKeysPanel();

    mainPanel.add(panelsArea, BorderLayout.CENTER);
    mainPanel.add(commandArea, BorderLayout.NORTH);
    mainPanel.add(functionKeysPanel, BorderLayout.SOUTH);

    setContentPane(mainPanel);
  }

  private JPanel createFilePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    JTable fileTable = new JTable(model);
    fileTable.setShowGrid(false);
    fileTable.setIntercellSpacing(new Dimension(0, 0));
    fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fileTable.setBackground(panelBgColor);
    fileTable.setForeground(panelTextColor);
    fileTable.setSelectionBackground(selectedRowColor);
    fileTable.setSelectionForeground(Color.WHITE);
    fileTable.getTableHeader().setReorderingAllowed(false);

    fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
    fileTable.getColumnModel().getColumn(1).setPreferredWidth(80);
    fileTable.getColumnModel().getColumn(2).setPreferredWidth(120);
    fileTable.getColumnModel().getColumn(3).setPreferredWidth(50);

    fileTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          JTable target = (JTable) e.getSource();
          int row = target.getSelectedRow();
          handleFileSelection(target, row);
        }
      }
    });

    JLabel statusBar = new JLabel(" ");
    statusBar.setBackground(panelBgColor);
    statusBar.setForeground(panelTextColor);
    statusBar.setOpaque(true);

    panel.add(new JScrollPane(fileTable), BorderLayout.CENTER);
    panel.add(statusBar, BorderLayout.SOUTH);

    return panel;
  }

  private JPanel createFunctionKeysPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 10, 2, 0));
    panel.setBackground(panelBgColor);

    String[] functionLabels = {
        "F1 Help", "F2 Menu", "F3 View", "F4 Edit", "F5 Copy",
        "F6 Move", "F7 Mkdir", "F8 Delete", "F9 Menu", "F10 Quit"
    };

    for (String label : functionLabels) {
      JLabel functionKey = new JLabel(label, JLabel.CENTER);
      functionKey.setForeground(panelTextColor);
      functionKey.setBorder(new LineBorder(Color.GRAY, 1));
      panel.add(functionKey);
    }

    return panel;
  }

  private void refreshDirectoryListing(JTable fileTable, File directory, JLabel statusBar) {
    DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
    model.setRowCount(0);

    if (directory == null || !directory.exists() || !directory.isDirectory()) {
      statusBar.setText(" Invalid directory");
      return;
    }

    if (directory.getParentFile() != null) {
      model.addRow(new Object[]{"[..]", "", "", ""});
    }

    try {
      File[] dirs = directory.listFiles(File::isDirectory);
      if (dirs != null) {
        Arrays.sort(dirs);
        for (File dir : dirs) {
          String dirName = "[" + dir.getName() + "]";
          String date = dateFormat.format(new Date(dir.lastModified()));
          String attr = dir.canWrite() ? "d" : "dr";

          model.addRow(new Object[]{dirName, "<DIR>", date, attr});
        }
      }

      File[] files = directory.listFiles(File::isFile);
      if (files != null) {
        Arrays.sort(files);
        for (File file : files) {
          String fileName = file.getName();
          String size = String.format("%,d", file.length());
          String date = dateFormat.format(new Date(file.lastModified()));

          String attr = "";
          attr += file.canRead() ? "r" : "-";
          attr += file.canWrite() ? "w" : "-";
          attr += file.isHidden() ? "h" : "-";

          model.addRow(new Object[]{fileName, size, date, attr});
        }
      }

      long freeSpace = directory.getFreeSpace();
      String freeSpaceStr = String.format("%,d bytes free", freeSpace);
      statusBar.setText(" " + directory.getAbsolutePath() + " | " + freeSpaceStr);

    } catch (Exception e) {
      statusBar.setText(" Error reading directory: " + e.getMessage());
    }
  }

  private void setActivePanel(JTable table, JLabel statusBar) {
    if (activeTable != null) {
      activeTable.getTableHeader().setBackground(panelBgColor);
      activeTable.getTableHeader().setForeground(panelTextColor);
    }

    activeTable = table;
    activeStatusBar = statusBar;

    table.getTableHeader().setBackground(activePanelColor);
    table.getTableHeader().setForeground(Color.WHITE);

    if (table == leftFileTable) {
      commandLine.setText(" " + leftCurrentDirectory.getAbsolutePath());
    } else {
      commandLine.setText(" " + rightCurrentDirectory.getAbsolutePath());
    }
  }

  private void handleFileSelection(JTable table, int row) {
    if (row < 0) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) table.getModel();
    String filename = (String) model.getValueAt(row, 0);

    File currentDir = (table == leftFileTable) ? leftCurrentDirectory : rightCurrentDirectory;

    if (filename.equals("[..]")) {
      File parentDir = currentDir.getParentFile();
      if (parentDir != null) {
        if (table == leftFileTable) {
          leftCurrentDirectory = parentDir;
          refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
        } else {
          rightCurrentDirectory = parentDir;
          refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);
        }
      }
    } else if (filename.startsWith("[") && filename.endsWith("]")) {
      String dirName = filename.substring(1, filename.length() - 1);
      File newDir = new File(currentDir, dirName);

      if (newDir.exists() && newDir.isDirectory()) {
        if (table == leftFileTable) {
          leftCurrentDirectory = newDir;
          refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
        } else {
          rightCurrentDirectory = newDir;
          refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);
        }
      }
    } else {
      File selectedFile = new File(currentDir, filename);
      commandLine.setText(" Selected file: " + selectedFile.getAbsolutePath());
    }
  }

  private void setupKeyboardShortcuts() {
    JRootPane rootPane = getRootPane();

    rootPane.registerKeyboardAction(
        e -> {
          if (activeTable == leftFileTable) {
            setActivePanel(rightFileTable, rightStatusBar);
          } else {
            setActivePanel(leftFileTable, leftStatusBar);
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> {
          int row = activeTable.getSelectedRow();
          if (row >= 0) {
            handleFileSelection(activeTable, row);
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> System.exit(0),
        KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> copySelectedFile(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> moveSelectedFile(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> createDirectory(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );

    rootPane.registerKeyboardAction(
        e -> deleteSelectedFile(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW
    );
  }

  private void copySelectedFile() {
    int row = activeTable.getSelectedRow();
    if (row < 0) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) activeTable.getModel();
    String filename = (String) model.getValueAt(row, 0);

    if (filename.equals("[..]")) {
      return;
    }

    File sourceDir = (activeTable == leftFileTable) ? leftCurrentDirectory : rightCurrentDirectory;
    File destDir = (activeTable == leftFileTable) ? rightCurrentDirectory : leftCurrentDirectory;

    if (filename.startsWith("[") && filename.endsWith("]")) {
      filename = filename.substring(1, filename.length() - 1);
    }

    File sourceFile = new File(sourceDir, filename);
    File destFile = new File(destDir, filename);

    if (!sourceFile.exists()) {
      JOptionPane.showMessageDialog(this, "Source file does not exist.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    int result = JOptionPane.showConfirmDialog(this,
        "Copy " + sourceFile.getName() + " to " + destDir.getAbsolutePath() + "?",
        "Confirm Copy", JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      try {
        if (sourceFile.isDirectory()) {
          copyDirectory(sourceFile, destFile);
        } else {
          Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Refresh destination panel
        if (activeTable == leftFileTable) {
          refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);
        } else {
          refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
        }

        commandLine.setText(" Copied: " + sourceFile.getName());
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error copying file: " + ex.getMessage(),
            "Copy Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void copyDirectory(File sourceDir, File destDir) throws IOException {
    if (!destDir.exists()) {
      destDir.mkdir();
    }

    for (File file : sourceDir.listFiles()) {
      File destFile = new File(destDir, file.getName());
      if (file.isDirectory()) {
        copyDirectory(file, destFile);
      } else {
        Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private void moveSelectedFile() {
    int row = activeTable.getSelectedRow();
    if (row < 0) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) activeTable.getModel();
    String filename = (String) model.getValueAt(row, 0);

    if (filename.equals("[..]")) {
      return;
    }

    File sourceDir = (activeTable == leftFileTable) ? leftCurrentDirectory : rightCurrentDirectory;
    File destDir = (activeTable == leftFileTable) ? rightCurrentDirectory : leftCurrentDirectory;

    if (filename.startsWith("[") && filename.endsWith("]")) {
      filename = filename.substring(1, filename.length() - 1);
    }

    File sourceFile = new File(sourceDir, filename);
    File destFile = new File(destDir, filename);

    if (!sourceFile.exists()) {
      JOptionPane.showMessageDialog(this, "Source file does not exist.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    int result = JOptionPane.showConfirmDialog(this,
        "Move " + sourceFile.getName() + " to " + destDir.getAbsolutePath() + "?",
        "Confirm Move", JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      try {
        Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
        refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);

        commandLine.setText(" Moved: " + sourceFile.getName());
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error moving file: " + ex.getMessage(),
            "Move Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void createDirectory() {
    File currentDir = (activeTable == leftFileTable) ? leftCurrentDirectory : rightCurrentDirectory;

    String dirName = JOptionPane.showInputDialog(this, "Enter directory name:");
    if (dirName == null || dirName.trim().isEmpty()) {
      return;
    }

    File newDir = new File(currentDir, dirName);
    if (newDir.exists()) {
      JOptionPane.showMessageDialog(this, "Directory already exists.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (newDir.mkdir()) {
      if (activeTable == leftFileTable) {
        refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
      } else {
        refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);
      }
      commandLine.setText(" Created directory: " + dirName);
    } else {
      JOptionPane.showMessageDialog(this, "Failed to create directory.", "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void deleteSelectedFile() {
    int row = activeTable.getSelectedRow();
    if (row < 0) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) activeTable.getModel();
    String filename = (String) model.getValueAt(row, 0);

    if (filename.equals("[..]")) {
      return;
    }

    File currentDir = (activeTable == leftFileTable) ? leftCurrentDirectory : rightCurrentDirectory;

    if (filename.startsWith("[") && filename.endsWith("]")) {
      filename = filename.substring(1, filename.length() - 1);
    }

    File fileToDelete = new File(currentDir, filename);

    if (!fileToDelete.exists()) {
      JOptionPane.showMessageDialog(this, "File does not exist.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    String confirmMsg = fileToDelete.isDirectory()
        ? "Delete directory " + fileToDelete.getName() + " and all its contents?"
        : "Delete file " + fileToDelete.getName() + "?";

    int result = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirm Delete",
        JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      try {
        if (fileToDelete.isDirectory()) {
          deleteDirectory(fileToDelete);
        } else {
          fileToDelete.delete();
        }

        if (activeTable == leftFileTable) {
          refreshDirectoryListing(leftFileTable, leftCurrentDirectory, leftStatusBar);
        } else {
          refreshDirectoryListing(rightFileTable, rightCurrentDirectory, rightStatusBar);
        }

        commandLine.setText(" Deleted: " + fileToDelete.getName());
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error deleting file: " + ex.getMessage(),
            "Delete Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void deleteDirectory(File dir) throws IOException {
    File[] contents = dir.listFiles();
    if (contents != null) {
      for (File file : contents) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    dir.delete();
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    SwingUtilities.invokeLater(() -> new Viewer());
  }
}