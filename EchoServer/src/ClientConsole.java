import java.io.*;

/**
 * Console UI for a chat client.
 */
public class ClientConsole implements ChatIF
{
  final public static int DEFAULT_PORT = 5555;

  ChatClient client;

  public static void main(String[] args)
  {
    String host;
    int port;

    try
    {
      host = args[0];
      port = Integer.parseInt(args[1]);
    }
    catch (Exception e)
    {
      host = "localhost";
      port = DEFAULT_PORT;
    }

    ClientConsole chat = new ClientConsole(host, port);
    chat.accept();
  }

  public ClientConsole(String host, int port)
  {
    try
    {
      client = new ChatClient(host, port, this);
    }
    catch(IOException exception)
    {
      System.out.println("Error: Can't setup connection!!!! Terminating client.");
      System.exit(1);
    }
  }

  public void accept()
  {
    try
    {
      BufferedReader fromConsole =
        new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true)
      {
        message = fromConsole.readLine();
        client.handleMessageFromClientUI(message);
      }
    }
    catch (Exception ex)
    {
      System.out.println("Unexpected error while reading from console!");
    }
  }

  public void display(String message)
  {
    System.out.println("> " + message);
  }
}
