package clean_code;

import java.text.ParseException;
import java.util.*;

public class Args {
  private String schema;
  private String[] args;
  private boolean valid;
  private Set<Character> unexpectedArguments = new TreeSet<>();
  private Map<Character, Boolean> booleanArgs = new HashMap<>();
  private Map<Character, String> stringArgs = new HashMap<>();
  private List<Character> argsFound = new ArrayList<>();
  private int currentArgument;
  private char errorArgument = '\0';
  private ErrorCode errorCode = ErrorCode.OK;

  public Args(String schema, String[] args) throws ParseException {
    this.schema = schema;
    this.args = args;
    valid = parse();
  }

  private boolean parse() throws ParseException {
    if (schema.length() == 0 && args.length == 0) {
      return true;
    }
    parseSchema();
    parseArguments();
    return unexpectedArguments.size() == 0;
  }

  private boolean parseSchema() throws ParseException {
    for (String element : schema.split(",")) {
      if (element.length() > 0) {
        String trimmedElement = element.trim();
        parseSchemaElement(trimmedElement);
      }
    }
    return true;
  }

  private void parseSchemaElement(String element) throws ParseException {
    char elementId = element.charAt(0);
    validateSchemaElementId(elementId);
    // 按照规则截取字符串，判断单个schema类型，l:布尔,p#:整数,d*:字符串
    String elementTail = element.substring(1);
    if (isBooleanSchemaElement(elementTail)) {
      parseBooleanSchemaElement(elementId);
    } else if (isStringSchemaElement(elementTail)) {
      parseStringSchemaElement(elementId);
    }
  }

  // 解析错误向上传递
  private void validateSchemaElementId(char elementId) throws ParseException {
    if (!Character.isLetter(elementId)) {
      throw new ParseException("Bad character: " + elementId + "in Args format: " + schema, 0);
    }
  }

  private boolean isBooleanSchemaElement(String elementTail) {
    return elementTail.length() == 0;
  }

  private void parseBooleanSchemaElement(char elementId) {
    booleanArgs.put(elementId, false);
  }

  private boolean isStringSchemaElement(String elementTail) {
    return elementTail.equals("*");
  }

  private void parseStringSchemaElement(char elementId) {
    stringArgs.put(elementId, "");
  }

  private boolean parseArguments() {
    for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
      String arg = args[currentArgument];
      parseArgument(arg);
    }
    return true;
  }

  private void parseElement(char argChar) {
    if (setArgument(argChar)) {
      argsFound.add(argChar);
    } else {
      unexpectedArguments.add(argChar);
      valid = false;
    }
  }

  private void parseArgument(String arg) {
    if (arg.startsWith("-")) {
      parseElements(arg);
    }
  }

  private void parseElements(String arg) {
    for (int i = 1; i < arg.length(); i++) {
      parseElement(arg.charAt(i));
    }
  }

  private boolean setArgument(char argChar) {
    boolean setSuccess = true;
    if (isBoolean(argChar)) {
      setBooleanArg(argChar, true);
    } else if (isString(argChar)) {
      setStringArg(argChar, "");
    } else {
      setSuccess = false;
    }

    return setSuccess;
  }

  private void setBooleanArg(char argChar, boolean value) {
    booleanArgs.put(argChar, value);
  }

  private boolean isBoolean(char argChar) {
    return booleanArgs.containsKey(argChar);
  }

  private boolean isString(char argChar) {
    return stringArgs.containsKey(argChar);
  }

  private void setStringArg(char argChar, String value) {
    // -d与-l不同，-d后可以追加字符串，不追加默认字符串为空
    // 定义全部变量是为了跳过[追加的字符串]，进入下一个命令
    currentArgument++;
    try {
      stringArgs.put(argChar, args[currentArgument]);
      // 当出现数组越界异常时，说明命令后没有追加字符串，记录起来
    } catch (ArrayIndexOutOfBoundsException e) {
      valid = false;
      errorArgument = argChar;
      errorCode = ErrorCode.MISSING_STRING;
    }
  }

  public int cardinality() {
    return argsFound.size();
  }

  public String errorMessage() throws Exception {
    if (unexpectedArguments.size() > 0) {
      return unexpectedArgumentMessage();
    } else {
      switch (errorCode) {
        case MISSING_STRING:
          return String.format("Could not find string parameter for -%c.", errorArgument);
        case OK:
          throw new Exception("TILT: should not get here.");
      }
      return "";
    }
  }

  public String usage() {
    if (schema.length() > 0) {
      return "-[" + schema + "]";
    } else {
      return "";
    }
  }

  public boolean getBoolean(char arg) {
    return falseIfNull(booleanArgs.get(arg));
  }

  private String unexpectedArgumentMessage() {
    StringBuilder message = new StringBuilder("Argument(s) -");
    for (char c : unexpectedArguments) {
      message.append(c);
    }
    message.append(" unexpected.");

    return message.toString();
  }

  private boolean falseIfNull(Boolean b) {
    return b == null ? false : b;
  }

  public String getString(char arg) {
    return blankIfNull(stringArgs.get(arg));
  }

  private String blankIfNull(String s) {
    return s == null ? "" : s;
  }

  public boolean has(char arg) {
    return argsFound.contains(arg);
  }

  enum ErrorCode {
    OK, MISSING_STRING
  }

  public boolean isValid() {
    return valid;
  }
}
