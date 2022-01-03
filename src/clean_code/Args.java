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
  private final String schema;
  private boolean valid;
  private final Set<Character> unexpectedArguments = new TreeSet<>();
  private final Map<Character, ArgumentMarshaler> marshalers = new HashMap<>();
  private final List<Character> argsFound = new ArrayList<>();
  private final List<String> argsList;
  private Iterator<String> currentArgument;
  private char errorArgument = '\0';
  private ErrorCode errorCode = ErrorCode.OK;

  public Args(String schema, String[] args) throws ParseException, ArgsException {
    this.schema = schema;
    this.argsList = Arrays.asList(args);
    valid = parse();
  }

  private boolean parse() throws ParseException, ArgsException {
    if (schema.length() == 0 && argsList.size() == 0) {
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
    if (elementTail.length() == 0) {
      marshalers.put(elementId, new BooleanArgumentMarshaler());
    } else if (elementTail.equals("*")) {
      marshalers.put(elementId, new StringArgumentMarshaler());
    } else if (elementTail.equals("#")) {
      marshalers.put(elementId, new IntegerArgumentMarshaler());
    } else {
      throw new ParseException(String.format("Argument: %c has invalid format: %s.", elementId, elementTail), 0);
    }
  }

  // 解析错误向上传递
  private void validateSchemaElementId(char elementId) throws ParseException {
    if (!Character.isLetter(elementId)) {
      throw new ParseException("Bad character: " + elementId + "in Args format: " + schema, 0);
    }
  }

  private boolean parseArguments() throws ArgsException {
    for (currentArgument = argsList.iterator(); currentArgument.hasNext(); ) {
      String arg = currentArgument.next();
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
    if (am == null) {
      return false;
    }
    try {
      if (am instanceof BooleanArgumentMarshaler) {
        am.set(currentArgument);
      } else if (am instanceof StringArgumentMarshaler) {
        am.set(currentArgument);
      } else if (am instanceof IntegerArgumentMarshaler) {
        am.set(currentArgument);
      }
    } catch (ArgsException e) {
      valid = false;
      errorArgument = argChar;
      throw e;
    }

    return true;
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
