package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntegerArgumentMarshaler extends ArgumentMarshaler {

  private int intValue = 0;

  @Override
  public Object get() {
    return intValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    String parameter = null;
    try {
      parameter = currentArgument.next();
      intValue = Integer.parseInt(parameter);
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_STRING;
      throw new ArgsException();
    } catch (NumberFormatException e) {
      errorCode = ErrorCode.INVALID_INTEGER;
      errorParameter = parameter;
      throw new ArgsException();
    }
  }
}
