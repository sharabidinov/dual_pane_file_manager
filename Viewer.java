import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


public class Viewer extends JFrame {

  private final Model model;
  private Controller controller;

  private JPanel leftPanel, rightPanel;
  private JTable leftTable, rightTable;
  private DefaultTableModel leftTableModel, rightTableModel;
  private JLabel leftStatusBar, rightStatusBar;
  private JTextField leftPathField, rightPathField;

  private String leftCurrentPath;
  private String rightCurrentPath;

  private final String[] columnNames = {"Name", "Size", "Date Modified", "Type"};
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private boolean leftPanelActive = true;

  public Viewer() {
    this(new Model());
  }

  public Viewer(Model model) {
    this.model = model;
    initUI();

    String userHome = System.getProperty("user.home");
    updateLeftPanel(userHome);
    updateRightPanel(userHome);
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  private void initUI() {
    setTitle("Dual Pane File Manager");
    setSize(1000, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(Color.BLUE);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    splitPane.setBackground(Color.BLUE);

    leftPanel = createFilePanel(true);
    rightPanel = createFilePanel(false);

    splitPane.setLeftComponent(leftPanel);
    splitPane.setRightComponent(rightPanel);

    mainPanel.add(splitPane, BorderLayout.CENTER);

    JPanel functionKeyPanel = createFunctionKeyPanel();
    functionKeyPanel.setBackground(Color.BLUE);
    mainPanel.add(functionKeyPanel, BorderLayout.SOUTH);

    setupKeyboardShortcuts();

    add(mainPanel);
    getContentPane().setBackground(Color.BLUE);
  }

  private JPanel createFilePanel(boolean isLeft) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    panel.setBackground(new Color(228, 243, 250, 255));

    JTextField pathField = new JTextField();
    pathField.setEditable(false);
    pathField.setBackground(Color.BLUE);
    pathField.setForeground(Color.WHITE);
    panel.add(pathField, BorderLayout.NORTH);

    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    JTable table = new JTable(tableModel);
    table.setShowGrid(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setRowHeight(20);
    table.setBackground(Color.BLUE);
    table.setForeground(Color.WHITE);
    table.setSelectionBackground(new Color(0, 0, 128));
    table.setSelectionForeground(Color.WHITE);

    table.getColumnModel().getColumn(0).setCellRenderer(new FileTableCellRenderer());

    table.getColumnModel().getColumn(0).setPreferredWidth(200);
    table.getColumnModel().getColumn(1).setPreferredWidth(80);
    table.getColumnModel().getColumn(2).setPreferredWidth(150);
    table.getColumnModel().getColumn(3).setPreferredWidth(80);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.BLUE);
    scrollPane.setBackground(Color.BLUE);
    panel.add(scrollPane, BorderLayout.CENTER);

    JLabel statusBar = new JLabel(" ");
    statusBar.setBorder(new EmptyBorder(3, 5, 3, 5));
    statusBar.setBackground(Color.BLUE);
    statusBar.setForeground(Color.WHITE);
    statusBar.setOpaque(true);
    panel.add(statusBar, BorderLayout.SOUTH);

    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        leftPanelActive = isLeft;
        highlightActivePanel();
        table.requestFocusInWindow();
      }
    });

    if (isLeft) {
      leftTable = table;
      leftTableModel = tableModel;
      leftPathField = pathField;
      leftStatusBar = statusBar;

      table.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          leftPanelActive = true;
          highlightActivePanel();
          table.requestFocusInWindow();

          if (e.getClickCount() == 2) {
            int row = leftTable.getSelectedRow();
            if (row != -1) {
              handleDoubleClick(true, row);
            }
          }
        }
      });

      table.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          leftPanelActive = true;
          highlightActivePanel();
        }
      });
    } else {
      rightTable = table;
      rightTableModel = tableModel;
      rightPathField = pathField;
      rightStatusBar = statusBar;

      table.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          leftPanelActive = false;
          highlightActivePanel();
          table.requestFocusInWindow();

          if (e.getClickCount() == 2) {
            int row = rightTable.getSelectedRow();
            if (row != -1) {
              handleDoubleClick(false, row);
            }
          }
        }
      });

      table.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          leftPanelActive = false;
          highlightActivePanel();
        }
      });
    }

    return panel;
  }

  private JPanel createFunctionKeyPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 10, 5, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setBackground(Color.BLUE);

    JButton f5Button = new JButton("F5 Copy");
    JButton f6Button = new JButton("F6 Move");
    JButton shiftF6Button = new JButton("Sh+F6 Rename");
    JButton f7Button = new JButton("F7 Mkdir");
    JButton f8Button = new JButton("F8 Delete");

    JButton[] buttons = {f5Button, f6Button, shiftF6Button, f7Button, f8Button};
    for (JButton btn : buttons) {
      btn.setBackground(Color.BLUE);
      btn.setForeground(Color.WHITE);
      btn.setOpaque(true);
      btn.setBorderPainted(false);
      btn.setFocusable(false);
    }

    f5Button.addActionListener(e -> {
      if (controller != null) {
        controller.handleCopy();
      }
    });

    f6Button.addActionListener(e -> {
      if (controller != null) {
        controller.handleMove();
      }
    });

    shiftF6Button.addActionListener(e -> {
      if (controller != null) {
        controller.handleRename();
      }
    });

    f7Button.addActionListener(e -> {
      if (controller != null) {
        controller.handleMkdir();
      }
    });

    f8Button.addActionListener(e -> {
      if (controller != null) {
        controller.handleDelete();
      }
    });

    JLabel[] labels = {
        new JLabel("Tab - Switch"),
        new JLabel("Enter - Open"),
        new JLabel("Backspace - Up"),
        new JLabel("Cmd+O - Terminal"),
        new JLabel("Esc - Exit")
    };
    for (JLabel lbl : labels) {
      lbl.setForeground(Color.WHITE);
    }

    panel.add(f5Button);
    panel.add(f6Button);
    panel.add(shiftF6Button);
    panel.add(f7Button);
    panel.add(f8Button);
    for (JLabel lbl : labels) {
      panel.add(lbl);
    }
    return panel;
  }

  private void setupKeyboardShortcuts() {
    JRootPane rootPane = getRootPane();
    InputMap rootInputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap rootActionMap = rootPane.getActionMap();

    AbstractAction switchPanelAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        leftPanelActive = !leftPanelActive;
        highlightActivePanel();
        if (leftPanelActive) {
          leftTable.requestFocusInWindow();
        } else {
          rightTable.requestFocusInWindow();
        }
      }
    };

    InputMap leftTableInputMap = leftTable.getInputMap(JComponent.WHEN_FOCUSED);
    InputMap rightTableInputMap = rightTable.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap tableActionMap = leftTable.getActionMap();

    leftTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "switchPanel");
    rightTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "switchPanel");
    tableActionMap.put("switchPanel", switchPanelAction);

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "switchPanel");
    rootActionMap.put("switchPanel", switchPanelAction);

    leftTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
    rightTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
    leftTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    rightTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "copy");
    rootActionMap.put("copy", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller != null) {
          controller.handleCopy();
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "move");
    rootActionMap.put("move", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller != null) {
          controller.handleMove();
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_DOWN_MASK), "rename");
    rootActionMap.put("rename", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller != null) {
          controller.handleRename();
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "mkdir");
    rootActionMap.put("mkdir", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller != null) {
          controller.handleMkdir();
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "delete");
    rootActionMap.put("delete", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller != null) {
          controller.handleDelete();
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
    rootActionMap.put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (leftPanelActive) {
          int row = leftTable.getSelectedRow();
          if (row != -1) {
            handleDoubleClick(true, row);
          }
        } else {
          int row = rightTable.getSelectedRow();
          if (row != -1) {
            handleDoubleClick(false, row);
          }
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace");
    rootActionMap.put("backspace", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (leftPanelActive) {
          String parentPath = model.getParentDirectory(leftCurrentPath);
          updateLeftPanel(parentPath);
        } else {
          String parentPath = model.getParentDirectory(rightCurrentPath);
          updateRightPanel(parentPath);
        }
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "openTerminal");
    rootActionMap.put("openTerminal", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        openTerminalInCurrentDirectory();
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
    rootActionMap.put("escape", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "quit");
    rootActionMap.put("quit", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
  }

  private void openTerminalInCurrentDirectory() {
    String currentDir = getActiveDirectoryPath();

    try {
      String osName = System.getProperty("os.name").toLowerCase();
      ProcessBuilder pb;

      if (osName.contains("mac")) {
        pb = new ProcessBuilder("open", "-a", "Terminal", currentDir);
      } else if (osName.contains("win")) {
        pb = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", "cd", "/d", currentDir);
      } else {
        String[] terminals = {"gnome-terminal", "konsole", "xterm", "terminator"};
        boolean terminalOpened = false;

        for (String terminal : terminals) {
          try {
            if (terminal.equals("gnome-terminal")) {
              pb = new ProcessBuilder(terminal, "--working-directory=" + currentDir);
            } else if (terminal.equals("konsole")) {
              pb = new ProcessBuilder(terminal, "--workdir", currentDir);
            } else {
              pb = new ProcessBuilder(terminal);
              pb.directory(new File(currentDir));
            }

            pb.start();
            terminalOpened = true;
            break;
          } catch (Exception ex) {
            System.err.println("Failed to open " + terminal + ": " + ex.getMessage());
          }
        }

        if (!terminalOpened) {
          throw new Exception("No suitable terminal found");
        }
        return;
      }

      pb.start();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
          "Failed to open terminal: " + ex.getMessage(),
          "Terminal Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void handleDoubleClick(boolean isLeft, int row) {
    JTable table = isLeft ? leftTable : rightTable;
    String currentPath = isLeft ? leftCurrentPath : rightCurrentPath;

    String fileName = (String) table.getValueAt(row, 0);
    String fileType = (String) table.getValueAt(row, 3);

    if (fileType.equals("Directory")) {
      String newPath = currentPath + File.separator + fileName;
      if (isLeft) {
        updateLeftPanel(newPath);
      } else {
        updateRightPanel(newPath);
      }
    } else {
      JOptionPane.showMessageDialog(this,
          "Opening file: " + fileName,
          "File Action",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  public void updateLeftPanel(String path) {
    leftCurrentPath = path;
    leftPathField.setText(path);
    updateTableContents(leftTableModel, path);
    leftTable.getSelectionModel().setSelectionInterval(0, 0);
    updateStatusBar(leftStatusBar, path);
  }

  public void updateRightPanel(String path) {
    rightCurrentPath = path;
    rightPathField.setText(path);
    updateTableContents(rightTableModel, path);
    rightTable.getSelectionModel().setSelectionInterval(0, 0);
    updateStatusBar(rightStatusBar, path);
  }

  private void updateTableContents(DefaultTableModel tableModel, String path) {
    tableModel.setRowCount(0);
    List<File> files = model.getDirectoryContents(path);

    for (File file : files) {
      String name = file.getName();
      String size = file.isDirectory() ? "<DIR>" : formatFileSize(file.length());
      String date = dateFormat.format(new Date(file.lastModified()));
      String type = file.isDirectory() ? "Directory" : getFileExtension(name);

      tableModel.addRow(new Object[]{name, size, date, type});
    }
  }

  private void updateStatusBar(JLabel statusBar, String path) {
    File directory = new File(path);
    if (directory.exists() && directory.isDirectory()) {
      File[] files = directory.listFiles();
      int fileCount = 0;
      int dirCount = 0;

      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            dirCount++;
          } else {
            fileCount++;
          }
        }
      }

      statusBar.setText(dirCount + " directories, " + fileCount + " files");
    } else {
      statusBar.setText("Invalid directory");
    }
  }

  private void highlightActivePanel() {
    if (leftPanelActive) {
      leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
      rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    } else {
      rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
      leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
  }

  private String formatFileSize(long size) {
    if (size < 1024) {
      return size + " B";
    } else if (size < 1024 * 1024) {
      return String.format("%.1f KB", size / 1024.0);
    } else if (size < 1024 * 1024 * 1024) {
      return String.format("%.1f MB", size / (1024.0 * 1024));
    } else {
      return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
  }

  private String getFileExtension(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot > 0 && lastDot < fileName.length() - 1) {
      return fileName.substring(lastDot + 1).toUpperCase() + " File";
    }
    return "File";
  }

  public String getActiveDirectoryPath() {
    return leftPanelActive ? leftCurrentPath : rightCurrentPath;
  }

  public String getInactiveDirectoryPath() {
    return leftPanelActive ? rightCurrentPath : leftCurrentPath;
  }

  public String getSelectedFileName() {
    JTable activeTable = leftPanelActive ? leftTable : rightTable;
    int selectedRow = activeTable.getSelectedRow();
    if (selectedRow != -1) {
      return (String) activeTable.getValueAt(selectedRow, 0);
    }
    return null;
  }

  public void refreshPanels() {
    updateLeftPanel(leftCurrentPath);
    updateRightPanel(rightCurrentPath);
  }

  private class FileTableCellRenderer extends DefaultListCellRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          new JList<>(), value, row, isSelected, hasFocus);

      if (column == 0) {
        String type = (String) table.getValueAt(row, 3);
        if ("Directory".equals(type)) {
          label.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        } else {
          label.setIcon(UIManager.getIcon("FileView.fileIcon"));
        }
      }

      if (isSelected) {
        label.setBackground(new Color(0, 0, 128));
        label.setForeground(Color.WHITE);
      } else {
        label.setBackground(Color.BLUE);
        label.setForeground(Color.WHITE);
      }

      return label;
    }
  }
}