import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class EchoServer extends AbstractServer
{
  final public static int DEFAULT_PORT = 5555;

  public EchoServer(int port)
  {
    super(port);
  }

  public void handleMessageFromClient(Object msg, ConnectionToClient client)
  {
    if (msg instanceof Envelope)
    {
      Envelope env = (Envelope) msg;
      handlecommandFromClient(env, client);
      return;
    }
    String userId = safeInfo(client, "userId", "guest");
    sendToAllClientsInRoom(userId + ": " + msg, client);
  }

  public void handlecommandFromClient(Envelope env, ConnectionToClient client)
  {
    if (env == null || env.getId() == null) return;
    String cmd = env.getId();

    // --- 1. FTP Upload ---
    if (cmd.equals("#ftpUpload"))
    {
       String filename = env.getArg();
       byte[] data = (byte[]) env.getContents();
       
       File folder = new File("uploads");
       if (!folder.exists()) folder.mkdir();
       
       try (FileOutputStream fos = new FileOutputStream(new File(folder, filename))) {
           fos.write(data);
           safeSend(client, "Server: Upload successful.");
       } catch (IOException e) {
           safeSend(client, "Server: Upload failed.");
       }
    }
    // --- 2. FTP List ---
    else if (cmd.equals("#ftplist"))
    {
        File folder = new File("uploads");
        ArrayList<String> fileNames = new ArrayList<>();
        if (folder.exists()) {
            File[] list = folder.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isFile()) fileNames.add(f.getName());
                }
            }
        }
        try {
            client.sendToClient(new Envelope("#updateFileList", "", fileNames));
        } catch (IOException e) {}
    }
    // --- 3. FTP Get (Download) ---
    else if (cmd.equals("#ftpget"))
    {
        String filename = env.getArg();
        if (filename == null && env.getContents() != null) 
             filename = env.getContents().toString().trim();
        
        File file = new File("uploads/" + filename);
        if (file.exists()) {
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                client.sendToClient(new Envelope("#fileDownloadResponse", filename, data));
            } catch (IOException e) {
                safeSend(client, "Server: Error reading file.");
            }
        } else {
            safeSend(client, "Server: File not found.");
        }
    }
    // --- Existing Commands ---
    else if (cmd.equals("#login"))
    {
      String userId = (env.getContents() == null) ? "guest" : env.getContents().toString().trim();
      client.setInfo("userId", userId);
      if (client.getInfo("roomName") == null) client.setInfo("roomName", "lobby");
      safeSend(client, "Logged in as: " + userId);
    }
    else if (cmd.equals("#join"))
    {
      String roomName = (env.getContents() == null) ? "lobby" : env.getContents().toString().trim();
      client.setInfo("roomName", roomName);
      safeSend(client, "Joined room: " + roomName);
    }
    else if (cmd.equals("#pm"))
    {
      String target = env.getArg();
      String message = (env.getContents() == null) ? "" : env.getContents().toString();
      sendToAClient(message, target, client);
    }
    else if (cmd.equals("#yell"))
    {
      String message = (env.getContents() == null) ? "" : env.getContents().toString();
      String userId = safeInfo(client, "userId", "guest");
      sendToAllClients(userId + " yells: " + message);
    }
    else if (cmd.equals("#who"))
    {
      sendRoomListToClient(client);
    }
    else
    {
      safeSend(client, "Unknown command: " + cmd);
    }
  }

  // Helper methods (unchanged logic)
  public void sendRoomListToClient(ConnectionToClient client) {
    Envelope env = new Envelope("#who", "", null);
    ArrayList<String> userList = new ArrayList<String>();
    Thread[] clientThreadList = getClientConnections();
    for (int i = 0; i < clientThreadList.length; i++) {
      ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
      userList.add(safeInfo(target, "userId", "guest") + " - " + safeInfo(target, "roomName", "lobby"));
    }
    env.setContents(userList);
    try { client.sendToClient(env); } catch (Exception ex) {}
  }

  public void sendToAClient(Object msg, String pmTarget, ConnectionToClient client) {
    Thread[] clientThreadList = getClientConnections();
    String sender = safeInfo(client, "userId", "guest");
    for (Thread t : clientThreadList) {
      ConnectionToClient target = (ConnectionToClient) t;
      if (safeInfo(target, "userId", "").equalsIgnoreCase(pmTarget)) {
        try {
          target.sendToClient("[PM] " + sender + ": " + msg);
          client.sendToClient("[PM to " + pmTarget + "] " + sender + ": " + msg);
        } catch (Exception ex) {}
        return;
      }
    }
    safeSend(client, "User not found: " + pmTarget);
  }

  public void sendToAllClientsInRoom(Object msg, ConnectionToClient client) {
    String room = safeInfo(client, "roomName", "lobby");
    for (Thread t : getClientConnections()) {
      ConnectionToClient recipient = (ConnectionToClient) t;
      if (safeInfo(recipient, "roomName", "lobby").equals(room)) {
        try { recipient.sendToClient(msg); } catch (Exception ex) {}
      }
    }
  }

  private void safeSend(ConnectionToClient client, Object msg) {
    try { client.sendToClient(msg); } catch (Exception e) { }
  }

  private String safeInfo(ConnectionToClient client, String key, String defaultValue) {
    Object val = client.getInfo(key);
    return (val == null || val.toString().trim().isEmpty()) ? defaultValue : val.toString();
  }

  protected void serverStarted() { System.out.println("Server listening on port " + getPort()); }
  protected void serverStopped() { System.out.println("Server stopped."); }
  protected void clientConnected(ConnectionToClient client) { System.out.println(client + " connected"); client.setInfo("userId", "guest"); client.setInfo("roomName", "lobby"); }
  
  public static void main(String[] args) {
    int port;
    try { port = Integer.parseInt(args[0]); } catch (Exception e) { port = DEFAULT_PORT; }
    EchoServer sv = new EchoServer(port);
    try { sv.listen(); } catch (Exception ex) { System.out.println("ERROR - Could not listen!"); }
  }
}