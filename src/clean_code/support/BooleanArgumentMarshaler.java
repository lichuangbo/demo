package clean_code.support;

import clean_code.exception.ArgsException;

import java.util.Iterator;

public class BooleanArgumentMarshaler extends ArgumentMarshaler {

  private boolean booleanValue = false;

  @Override
  public void set(String s) {
//    booleanValue = true;
  }

  @Override
  public Object get() {
    return booleanValue;
  }

  @Override
  public void set(Iterator<String> currentArgument) throws ArgsException {
    booleanValue = true;
  }
}
