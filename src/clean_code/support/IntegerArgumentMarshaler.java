package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntegerArgumentMarshaler extends ArgumentMarshaler {

  private int intValue = 0;

  @Override
  public void set(String s) throws ArgsException {
    try {
      intValue = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new ArgsException();
    }
  }

  @Override
  public Object get() {
    return intValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    String parameter = null;
    try {
      parameter = currentArgument.next();
      // 数值类型的解析异常，下沉到了IntegerArgumentMarshaler中去处理
      set(parameter);
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_STRING;
      throw new ArgsException();
    } catch (ArgsException e) {
      errorCode = ErrorCode.INVALID_INTEGER;
      errorParameter = parameter;
      throw e;
    }
  }
}
