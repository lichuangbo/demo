package clean_code.support;

public class ArgumentMarshaler {
  private boolean booleanValue = false;
  private String stringValue = "";
  private int intValue = 0;

  public boolean getBoolean() {
    return booleanValue;
  }

  public void setBoolean(boolean value) {
    this.booleanValue = value;
  }

  public String getString() {
    return stringValue;
  }

  public void setString(String value) {
    this.stringValue = value;
  }

  public int getInt() {
    return intValue;
  }

  public void setInt(int value) {
    this.intValue = value;
  }
}
