package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoubleArgumentMarshaler extends ArgumentMarshaler {

  private double doubleValue;

  @Override
  public Object get() {
    return doubleValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    String parameter = null;
    try {
      parameter = currentArgument.next();
      doubleValue = Double.parseDouble(parameter);
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_DOUBLE;
      throw new ArgsException();
    } catch (NumberFormatException e) {
      errorCode = ErrorCode.INVALID_DOUBLE;
      errorParameter = parameter;
      throw new ArgsException();
    }
  }
}
