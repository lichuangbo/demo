package clean_code.support;

import clean_code.exception.ArgsException;

public abstract class ArgumentMarshaler {

  public abstract void set(String s) throws ArgsException;

  public abstract Object get();
}
