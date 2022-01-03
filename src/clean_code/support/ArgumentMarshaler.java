package clean_code.support;

import clean_code.enums.ErrorCode;
import clean_code.exception.ArgsException;

import java.util.Iterator;

public abstract class ArgumentMarshaler {

  protected ErrorCode errorCode = ErrorCode.OK;

  protected String errorParameter = "TILT";

  public abstract void set(String s) throws ArgsException;

  public abstract Object get();

  public abstract void set(Iterator<String> currentArgument) throws ArgsException;
}
