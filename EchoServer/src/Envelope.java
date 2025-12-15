import java.io.Serializable;

/**
 * Instructor-style Envelope:
 *  id = "#login", "#join", "#pm", "#yell", "#who"
 *  arg = extra single arg (pm target)
 *  contents = username / roomName / message / ArrayList<String>
 */
public class Envelope implements Serializable
{
  private static final long serialVersionUID = 1L;

  private String id;
  private String arg;
  private Object contents;

  public Envelope() {}

  public Envelope(String id)
  {
    this.id = id;
  }

  public Envelope(String id, String arg, Object contents)
  {
    this.id = id;
    this.arg = arg;
    this.contents = contents;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getArg() { return arg; }
  public void setArg(String arg) { this.arg = arg; }

  public Object getContents() { return contents; }
  public void setContents(Object contents) { this.contents = contents; }
}
