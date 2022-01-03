package clean_code;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;
import clean_code.support.ArgumentMarshaler;
import clean_code.support.BooleanArgumentMarshaler;
import clean_code.support.IntegerArgumentMarshaler;
import clean_code.support.StringArgumentMarshaler;

import java.text.ParseException;
import java.util.*;


public class Args {
  private String schema;
  private String[] args;
  private boolean valid;
  private Set<Character> unexpectedArguments = new TreeSet<>();
  private Map<Character, ArgumentMarshaler> marshalers = new HashMap<>();
  private List<Character> argsFound = new ArrayList<>();
  private int currentArgument;
  private char errorArgument = '\0';
  private ErrorCode errorCode = ErrorCode.OK;

  public Args(String schema, String[] args) throws ParseException, ArgsException {
    this.schema = schema;
    this.args = args;
    valid = parse();
  }

  private boolean parse() throws ParseException, ArgsException {
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
    } else if (isIntegerSchemaElement(elementTail)) {
      parseIntegerSchemaElement(elementId);
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
    marshalers.put(elementId, new BooleanArgumentMarshaler());
  }

  private boolean isStringSchemaElement(String elementTail) {
    return elementTail.equals("*");
  }

  private void parseStringSchemaElement(char elementId) {
    marshalers.put(elementId, new StringArgumentMarshaler());
  }

  private boolean isIntegerSchemaElement(String elementTail) {
    return elementTail.equals("#");
  }

  private void parseIntegerSchemaElement(char elementId) {
    marshalers.put(elementId, new IntegerArgumentMarshaler());
  }

  private boolean parseArguments() throws ArgsException {
    for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
      String arg = args[currentArgument];
      parseArgument(arg);
    }
    return true;
  }

  private void parseArgument(String arg) throws ArgsException {
    if (arg.startsWith("-")) {
      parseElements(arg);
    }
  }

  private void parseElements(String arg) throws ArgsException {
    for (int i = 1; i < arg.length(); i++) {
      parseElement(arg.charAt(i));
    }
  }

  private void parseElement(char argChar) throws ArgsException {
    if (setArgument(argChar)) {
      argsFound.add(argChar);
    } else {
      unexpectedArguments.add(argChar);
      valid = false;
    }
  }

  private boolean setArgument(char argChar) throws ArgsException {
    ArgumentMarshaler am = marshalers.get(argChar);
    boolean setSuccess = true;
    try {
      if (am instanceof BooleanArgumentMarshaler) {
        setBooleanArg(am);
      } else if (am instanceof StringArgumentMarshaler) {
        setStringArg(am);
      } else if (am instanceof IntegerArgumentMarshaler) {
        setIntArg(am);
      } else {
        setSuccess = false;
      }
    } catch (ArgsException e) {
      valid = false;
      errorArgument = argChar;
      throw e;
    }

    return setSuccess;
  }

  private void setBooleanArg(ArgumentMarshaler am) {
    try {
      am.set("true");
    } catch (ArgsException e) {
      e.printStackTrace();
    }
  }

  private void setStringArg(ArgumentMarshaler am) {
    // -d与-l不同，-d后可以追加字符串，不追加默认字符串为空
    // 定义全部变量是为了跳过[追加的字符串]，进入下一个命令
    currentArgument++;
    try {
      am.set(args[currentArgument]);
      // 当出现数组越界异常时，说明命令后没有追加字符串，记录起来
    } catch (ArrayIndexOutOfBoundsException e) {
      errorCode = ErrorCode.MISSING_STRING;
    } catch (ArgsException e) {
      e.printStackTrace();
    }
  }

  private void setIntArg(ArgumentMarshaler am) throws ArgsException {
    currentArgument++;
    String parameter = null;
    try {
      parameter = args[currentArgument];
      // 数值类型的解析异常，下沉到了IntegerArgumentMarshaler中去处理
      am.set(parameter);
    } catch (ArrayIndexOutOfBoundsException e) {
      errorCode = ErrorCode.MISSING_STRING;
      throw new ArgsException();
    } catch (ArgsException e) {
      errorCode = ErrorCode.INVALID_INTEGER;
      throw e;
    }
  }

  public int cardinality() {
    return argsFound.size();
  }

  public String usage() {
    if (schema.length() > 0) {
      return "-[" + schema + "]";
    } else {
      return "";
    }
  }

  public boolean getBoolean(char arg) {
    ArgumentMarshaler argumentMarshaler = marshalers.get(arg);
    return argumentMarshaler != null && (Boolean) argumentMarshaler.get();
  }

  public String getString(char arg) {
    ArgumentMarshaler argumentMarshaler = marshalers.get(arg);
    return argumentMarshaler == null ? "" : (String) argumentMarshaler.get();
  }

  public int getInt(char arg) {
    ArgumentMarshaler argumentMarshaler = marshalers.get(arg);
    return argumentMarshaler == null ? 0 : (Integer) argumentMarshaler.get();
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

  private String unexpectedArgumentMessage() {
    StringBuilder message = new StringBuilder("Argument(s) -");
    for (char c : unexpectedArguments) {
      message.append(c);
    }
    message.append(" unexpected.");

    return message.toString();
  }

  public boolean has(char arg) {
    return argsFound.contains(arg);
  }

  public boolean isValid() {
    return valid;
  }
}
