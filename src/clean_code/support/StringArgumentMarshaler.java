package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StringArgumentMarshaler extends ArgumentMarshaler {

  private String stringValue = "";

  @Override
  public void set(String s) {
//    stringValue = s
  }

  @Override
  public Object get() {
    return stringValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    try {
      stringValue = currentArgument.next();
      // 当出现数组越界异常时，说明命令后没有追加字符串，记录起来
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_STRING;
      throw new ArgsException();
    }
  }
}
