import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ChatClient extends AbstractClient
{
  ChatIF clientUI;

  public ChatClient(String host, int port, ChatIF clientUI) throws IOException
  {
    super(host, port);
    this.clientUI = clientUI;
    openConnection();
  }

  public void handleMessageFromServer(Object msg)
  {
    if (msg instanceof Envelope)
    {
      Envelope env = (Envelope) msg;
      handleCommandFromServer(env);
      return;
    }
    clientUI.display(msg.toString());
  }

  @SuppressWarnings("unchecked")
  public void handleCommandFromServer(Envelope env)
  {
    if (env == null || env.getId() == null) return;

    String cmd = env.getId();

    // --- 1. Handle File List Update ---
    if (cmd.equals("#updateFileList"))
    {
       ArrayList<String> files = (ArrayList<String>) env.getContents();
       // If the UI is our GUI, update the dropdown box
       if (clientUI instanceof ClientGUIConsole) {
           ((ClientGUIConsole) clientUI).updateFileBox(files);
       } else {
           clientUI.display("Files on server: " + files);
       }
    }
    // --- 2. Handle File Download ---
    else if (cmd.equals("#fileDownloadResponse"))
    {
       String filename = env.getArg();
       byte[] data = (byte[]) env.getContents();
       
       try {
           // Save to 'downloads' folder as per requirements
           File folder = new File("downloads");
           if (!folder.exists()) folder.mkdir();
           
           File dst = new File(folder, filename);
           FileOutputStream fos = new FileOutputStream(dst);
           fos.write(data);
           fos.close();
           clientUI.display("File downloaded to: " + dst.getAbsolutePath());
       } catch (IOException e) {
           clientUI.display("Error saving downloaded file.");
       }
    }
    // --- Existing Commands ---
    else if (cmd.equals("#who"))
    {
      Object c = env.getContents();
      if (c instanceof ArrayList)
      {
        ArrayList<String> userList = (ArrayList<String>) c;
        clientUI.display("Users: " + userList);
      }
    }
    else
    {
      if (env.getContents() != null)
        clientUI.display(env.getContents().toString());
      else
        clientUI.display("Received command: " + env.getId());
    }
  }

  public void handleMessageFromClientUI(String message)
  {
    if (message == null) return;
    message = message.trim();
    if (message.isEmpty()) return;

    if (message.charAt(0) == '#')
    {
      handleClientCommand(message);
    }
    else
    {
      try { sendToServer(message); }
      catch (IOException e) {
        clientUI.display("Could not send message to server. Terminating client...");
        quit();
      }
    }
  }

  public void quit()
  {
    try { closeConnection(); } catch (IOException e) { }
    System.exit(0);
  }

  @Override
  protected void connectionException(Exception exception)
  {
    clientUI.display("Server shut down / connection lost. Closing client...");
    quit();
  }

  @Override
  protected void connectionClosed()
  {
    clientUI.display("Connection closed.");
  }

  public void handleClientCommand(String message)
  {
    // #quit
    if (message.equals("#quit")) { clientUI.display("Shutting Down Client"); quit(); return; }
    // #logoff
    if (message.equals("#logoff")) { try { closeConnection(); } catch (IOException e) { } return; }

    // #ftplist
    if (message.equals("#ftplist")) {
        try { sendToServer(new Envelope("#ftplist", "", null)); } catch(IOException e){}
        return;
    }
    
    // #ftpget <filename>
    if (message.startsWith("#ftpget")) {
        String filename = message.substring(7).trim();
        try { sendToServer(new Envelope("#ftpget", filename, null)); } catch(IOException e){}
        return;
    }

    // #setHost
    if (message.startsWith("#setHost")) {
      if (isConnected()) clientUI.display("Cannot change host while connected");
      else setHost(message.substring(8).trim());
      return;
    }
    // #setPort
    if (message.startsWith("#setPort")) {
      if (isConnected()) clientUI.display("Cannot change port while connected");
      else try { setPort(Integer.parseInt(message.substring(8).trim())); } catch(Exception e){}
      return;
    }
    // #login
    if (message.startsWith("#login")) {
      String userName = message.substring(6).trim();
      try {
        if (!isConnected()) openConnection();
        sendToServer(new Envelope("#login", "", userName));
      } catch (IOException e) { clientUI.display("failed to connect/login"); }
      return;
    }
    // #join
    if (message.startsWith("#join")) {
      try { sendToServer(new Envelope("#join", "", message.substring(5).trim())); } catch(IOException e){}
      return;
    }
    // #pm
    if (message.startsWith("#pm")) {
       // logic for parsing pm
       String rest = message.substring(3).trim();
       int firstSpace = rest.indexOf(" ");
       if (firstSpace > 0) {
           try { sendToServer(new Envelope("#pm", rest.substring(0, firstSpace), rest.substring(firstSpace+1))); } catch(IOException e){}
       }
       return;
    }
    // #yell
    if (message.startsWith("#yell")) {
      try { sendToServer(new Envelope("#yell", "", message.substring(5).trim())); } catch(IOException e){}
      return;
    }
    // #who
    if (message.equals("#who")) {
      try { sendToServer(new Envelope("#who", "", "")); } catch(IOException e){}
      return;
    }
  }
}