import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * GUI designed to match the specific "Simple Chat GUI" layout from the project screenshot.
 * Includes FTP requirements (Browse, Save, File List, Download).
 */
public class ClientGUIConsole extends JFrame implements ChatIF
{
  final public static int DEFAULT_PORT = 5555;

  private ChatClient client;
  private File currentFile; // The file selected by Browse

  // --- UI Components matching the Screenshot ---
  
  // Text Fields
  private JTextField hostField = new JTextField("localhost");
  private JTextField portField = new JTextField("5555");
  private JTextField userIdField = new JTextField("guest");
  private JTextField messageField = new JTextField();
  
  // Chat Output (Placed at bottom to keep the top looking like the screenshot)
  private JTextArea chatArea = new JTextArea(5, 20);

  // Buttons & Combo Box
  private JButton userListBtn = new JButton("User List");
  private JComboBox<String> fileListCombo = new JComboBox<>(); // Using the combo box for Files
  
  private JButton pmBtn = new JButton("PM");
  private JButton sendBtn = new JButton("Send");
  
  private JButton loginBtn = new JButton("Login");
  private JButton logoffBtn = new JButton("Logoff");
  
  private JButton browseBtn = new JButton("Browse"); // Opens JFileChooser
  private JButton saveBtn = new JButton("Save");     // Performs Upload
  
  private JButton quitBtn = new JButton("Quit");
  private JButton downloadBtn = new JButton("Download"); // Added to fulfill requirement

  public ClientGUIConsole()
  {
    super("Simple Chat GUI");
    setSize(400, 500); // Sized to look like the screenshot
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(5, 5));

    // --- MAIN CONTROL PANEL (The Screenshot Layout) ---
    // We use a GridBagLayout or GridLayout to match the screenshot's 2-column look
    JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // 2 Columns, Auto-rows
    controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Row 1: Host
    controlPanel.add(new JLabel("Host:", SwingConstants.RIGHT));
    controlPanel.add(hostField);

    // Row 2: Port
    controlPanel.add(new JLabel("Port:", SwingConstants.RIGHT));
    controlPanel.add(portField);

    // Row 3: User Id
    controlPanel.add(new JLabel("User Id:", SwingConstants.RIGHT));
    controlPanel.add(userIdField);

    // Row 4: Message
    controlPanel.add(new JLabel("Message:", SwingConstants.RIGHT));
    controlPanel.add(messageField);

    // Row 5: User List | Combo Box (File List)
    controlPanel.add(userListBtn);
    controlPanel.add(fileListCombo);

    // Row 6: PM | Send
    controlPanel.add(pmBtn);
    controlPanel.add(sendBtn);

    // Row 7: Login | Logoff
    controlPanel.add(loginBtn);
    controlPanel.add(logoffBtn);

    // Row 8: Browse | Save
    controlPanel.add(browseBtn);
    controlPanel.add(saveBtn);

    // Row 9: Quit | Download (Added Download here to fit grid)
    controlPanel.add(quitBtn);
    controlPanel.add(downloadBtn);

    // Add the control panel to the top
    add(controlPanel, BorderLayout.NORTH);

    // --- CHAT OUTPUT AREA ---
    // Needed to see server responses, placed at bottom
    chatArea.setEditable(false);
    chatArea.setLineWrap(true);
    chatArea.setBorder(BorderFactory.createTitledBorder("Chat Output"));
    add(new JScrollPane(chatArea), BorderLayout.CENTER);

    // --- BUTTON ACTIONS ---

    // 1. Connection / Login
    loginBtn.addActionListener(e -> login());
    logoffBtn.addActionListener(e -> {
        sendCommand("#logoff");
        display("Logged off.");
    });
    quitBtn.addActionListener(e -> {
        sendCommand("#quit");
        System.exit(0);
    });

    // 2. Messaging
    sendBtn.addActionListener(e -> send());
    messageField.addActionListener(e -> send());
    pmBtn.addActionListener(e -> sendPM());
    userListBtn.addActionListener(e -> sendCommand("#who"));

    // 3. FTP Actions (The Project Requirements)
    
    // BROWSE: Opens the JFileChooser (Right side of your screenshot) [cite: 13]
    browseBtn.addActionListener(e -> {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            display("Selected file: " + currentFile.getName());
        }
    });

    // SAVE: Uploads the file [cite: 15, 19]
    saveBtn.addActionListener(e -> uploadFile());

    // DOWNLOAD: Downloads selected file from Combo Box 
    downloadBtn.addActionListener(e -> downloadFile());

    setVisible(true);
  }

  /* ================= LOGIC METHODS ================= */

  private void login()
  {
    String host = hostField.getText().trim();
    int port;
    try {
        port = Integer.parseInt(portField.getText().trim());
    } catch (NumberFormatException e) {
        port = DEFAULT_PORT;
    }
    String userId = userIdField.getText().trim();

    try {
        // Create client and connect
        client = new ChatClient(host, port, this);
        // Login command
        client.handleMessageFromClientUI("#login " + userId);
        
        // Auto-fetch file list for the combo box 
        client.handleMessageFromClientUI("#ftplist"); 
        
    } catch (IOException e) {
        display("Error: Could not connect to server.");
    }
  }

  private void send() {
    if (client == null) return;
    String msg = messageField.getText();
    if (!msg.isEmpty()) {
        client.handleMessageFromClientUI(msg);
        messageField.setText("");
    }
  }

  private void sendPM() {
      // Simple PM logic: assumes user typed "#pm target message" OR we prompt them
      String msg = messageField.getText();
      if (msg.startsWith("#pm")) {
          client.handleMessageFromClientUI(msg);
      } else {
          display("To PM, type: #pm <user> <message> in the message box, then click PM or Send.");
      }
  }

  private void sendCommand(String cmd) {
      if (client != null) client.handleMessageFromClientUI(cmd);
  }

  // --- FTP IMPLEMENTATION ---

  private void uploadFile() {
      if (currentFile == null) {
          display("Error: No file selected. Click Browse first.");
          return;
      }
      try {
          // Convert file to byte array [cite: 16]
          byte[] fileBytes = Files.readAllBytes(currentFile.toPath());
          String fileName = currentFile.getName();
          
          // Send #ftpUpload envelope [cite: 20]
          Envelope env = new Envelope("#ftpUpload", fileName, fileBytes);
          client.sendToServer(env);
          display("Uploading: " + fileName);
      } catch (IOException e) {
          display("Error reading file.");
      }
  }

  private void downloadFile() {
      String selected = (String) fileListCombo.getSelectedItem();
      if (selected != null) {
          // Send #ftpget command [cite: 26]
          sendCommand("#ftpget " + selected);
      }
  }
  
  // Updates the Combo Box (Called by ChatClient when server sends list) 
  public void updateFileBox(ArrayList<String> files) {
      fileListCombo.removeAllItems();
      for (String f : files) {
          fileListCombo.addItem(f);
      }
      display("File list updated.");
  }

  @Override
  public void display(String message) {
    chatArea.append(message + "\n");
    // Auto-scroll
    chatArea.setCaretPosition(chatArea.getDocument().getLength());
  }

  public static void main(String[] args) {
    new ClientGUIConsole();
  }
}