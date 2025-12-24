import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class UI extends JFrame {
  private RecordingManager recordingManager;
  private PlaybackManager playbackManager;
  private FileManager fileManager;

  private JButton recordButton;
  private JButton playButton;
  private JButton stopButton;
  private JButton saveButton;
  private JButton loadButton;
  private JButton clearButton;
  private JLabel statusLabel;
  private JTable actionTable;
  private DefaultTableModel tableModel;
  private JScrollPane scrollPane;

  private Timer updateTimer;

  public UI() {
    this.recordingManager = new RecordingManager();
    this.playbackManager = new PlaybackManager();
    this.fileManager = new FileManager();

    setupUI();
    startActionListUpdater();
  }

  private void setupUI() {
    setTitle("Mouse Recorder & Replayer");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    // Add window listener to cleanup on close
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent e) {
        recordingManager.cleanup();
        System.exit(0);
      }
    });

    // Top panel with buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

    recordButton = new JButton("Start Recording");
    playButton = new JButton("Play");
    stopButton = new JButton("Stop");
    saveButton = new JButton("Save");
    loadButton = new JButton("Load");
    clearButton = new JButton("Clear");
    statusLabel = new JLabel("Ready");

    // Add keyboard shortcuts (mnemonics)
    recordButton.setMnemonic(KeyEvent.VK_R);
    playButton.setMnemonic(KeyEvent.VK_P);
    stopButton.setMnemonic(KeyEvent.VK_S);
    clearButton.setMnemonic(KeyEvent.VK_C);

    // Add tooltips showing shortcuts
    recordButton.setToolTipText("Alt+R or Ctrl+R to toggle recording");
    playButton.setToolTipText("Alt+P or Ctrl+P to play recording");
    stopButton.setToolTipText("Alt+S or Ctrl+S to play recording");
    clearButton.setToolTipText("Alt+C or Ctrl+D to clear actions");

    recordButton.addActionListener(e -> toggleRecording());
    playButton.addActionListener(e -> playRecording());
    stopButton.addActionListener(e -> stopRecording());
    saveButton.addActionListener(e -> saveRecording());
    loadButton.addActionListener(e -> loadRecording());
    clearButton.addActionListener(e -> clearActions());

    // Add global keyboard shortcuts (Ctrl/Cmd + key)
    setupGlobalShortcuts();

    buttonPanel.add(recordButton);
    buttonPanel.add(playButton);
    buttonPanel.add(stopButton);
    buttonPanel.add(saveButton);
    buttonPanel.add(loadButton);
    buttonPanel.add(clearButton);

    // Bottom panel with status
    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    statusPanel.add(statusLabel);

    // Center panel with action list
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(BorderFactory.createTitledBorder("Recorded Actions"));

    // Create table model
    String[] columnNames = { "#", "Type", "Position", "Time (ms)", "Button" };
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Make table read-only
      }
    };

    actionTable = new JTable(tableModel);
    actionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    actionTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    actionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
    actionTable.getColumnModel().getColumn(2).setPreferredWidth(120);
    actionTable.getColumnModel().getColumn(3).setPreferredWidth(80);
    actionTable.getColumnModel().getColumn(4).setPreferredWidth(80);

    scrollPane = new JScrollPane(actionTable);
    scrollPane.setPreferredSize(new Dimension(500, 300));
    centerPanel.add(scrollPane, BorderLayout.CENTER);

    // Add panels to frame
    add(buttonPanel, BorderLayout.NORTH);
    add(centerPanel, BorderLayout.CENTER);
    add(statusPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);
  }

  private void setupGlobalShortcuts() {
    JRootPane rootPane = getRootPane();

    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke("control R"), "record");
    rootPane.getActionMap().put("record", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        toggleRecording();
      }
    });

    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke("control P"), "play");
    rootPane.getActionMap().put("play", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        playRecording();
      }
    });

    // Ctrl+S for Save
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke("control S"), "stop");
    rootPane.getActionMap().put("stop", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        stopRecording();
      }
    });

    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke("control D"), "clear");
    rootPane.getActionMap().put("clear", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        clearActions();
      }
    });
  }

  private void startActionListUpdater() {
    updateTimer = new Timer(100, e -> updateActionList());
    updateTimer.start();
  }

  private void updateActionList() {
    List<MouseAction> actions = recordingManager.getActions();

    if (tableModel.getRowCount() != actions.size()) {
      tableModel.setRowCount(0);

      for (int i = 0; i < actions.size(); i++) {
        MouseAction action = actions.get(i);
        Object[] row = new Object[5];
        row[0] = i + 1;
        row[1] = action.getType().toString();
        row[2] = String.format("(%d, %d)", action.getX(), action.getY());
        row[3] = action.getTimestamp();
        row[4] = action.getButton() == 0 ? "-" : "Button " + action.getButton();
        tableModel.addRow(row);
      }

      if (actionTable.getRowCount() > 0) {
        actionTable.scrollRectToVisible(
            actionTable.getCellRect(actionTable.getRowCount() - 1, 0, true));
      }
    }
  }

  private void clearActions() {
    recordingManager.clear();
    tableModel.setRowCount(0);
    statusLabel.setText("Actions cleared");
  }

  private void toggleRecording() {
    if (!recordingManager.isRecording()) {
      recordingManager.startRecording();
      recordButton.setText("Stop Recording");
      statusLabel.setText("Recording...");
      playButton.setEnabled(false);
      stopButton.setEnabled(false);
      saveButton.setEnabled(false);
      loadButton.setEnabled(false);
    } else {
      recordingManager.stopRecording();
      recordButton.setText("Start Recording");
      int count = recordingManager.getActionCount();
      statusLabel.setText("Recorded " + count + " actions");
      playButton.setEnabled(true);
      stopButton.setEnabled(true);
      saveButton.setEnabled(true);
      loadButton.setEnabled(true);
    }
  }

  private void playRecording() {
    if (recordingManager.getActionCount() == 0) {
      statusLabel.setText("No recording to play");
      return;
    }

    if (playbackManager.isPlaying()) {
      return;
    }

    playbackManager.play(recordingManager.getActions(), new PlaybackManager.PlaybackListener() {
      @Override
      public void onPlaybackStarted() {
        SwingUtilities.invokeLater(() -> {
          statusLabel.setText("Playing...");
          recordButton.setEnabled(false);
          playButton.setEnabled(false);
          stopButton.setEnabled(true);
          saveButton.setEnabled(false);
          loadButton.setEnabled(false);
        });
      }

      @Override
      public void onPlaybackCompleted() {
        SwingUtilities.invokeLater(() -> {
          statusLabel.setText("Playback complete");
          recordButton.setEnabled(true);
          playButton.setEnabled(true);
          stopButton.setEnabled(true);
          saveButton.setEnabled(true);
          loadButton.setEnabled(true);
        });
      }

      @Override
      public void onPlaybackActionExecute(MouseAction action) {
      }

      @Override
      public void onPlaybackError(String error) {
        SwingUtilities.invokeLater(() -> {
          statusLabel.setText("Error: " + error);
          recordButton.setEnabled(true);
          playButton.setEnabled(true);
          stopButton.setEnabled(true);
          saveButton.setEnabled(true);
          loadButton.setEnabled(true);
        });
      }
    });
  }

  private void stopRecording() {
    playbackManager.stop();
  }

  private void saveRecording() {
    if (recordingManager.getActionCount() == 0) {
      statusLabel.setText("No recording to save");
      return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Recording");

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      try {
        fileManager.save(recordingManager.getActions(), file);
        statusLabel.setText("Saved to " + file.getName());
      } catch (Exception e) {
        statusLabel.setText("Error saving file: " + e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Failed to save recording: " + e.getMessage(),
            "Save Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void loadRecording() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Load Recording");

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      try {
        recordingManager.setActions(fileManager.load(file));
        updateActionList(); // Refresh the table immediately
        int count = recordingManager.getActionCount();
        statusLabel.setText("Loaded " + count + " actions from " + file.getName());
      } catch (Exception e) {
        statusLabel.setText("Error loading file: " + e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Failed to load recording: " + e.getMessage(),
            "Load Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void display() {
    setVisible(true);
  }

}
