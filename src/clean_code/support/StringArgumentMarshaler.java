package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StringArgumentMarshaler extends ArgumentMarshaler {

  private String stringValue = "";

  @Override
  public Object get() {
    return stringValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    try {
      stringValue = currentArgument.next();
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_INTEGER;
      throw new ArgsException();
    }
  }
}
