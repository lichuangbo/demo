package clean_code;

import clean_code.exception.ArgsException;

import java.text.ParseException;

/**
 * Argument需求的理解
 * https://gitee.com/chengkunxf/argument
 */
public class ArgsTest {
  public static void main(String[] args) {
    Args myArgs = null;
    try {
//      myArgs = new Args("l,d*", new String[]{"-l", "-d", "/usr/local"});
//      myArgs = new Args("l,d*", new String[]{"-l", "-d"});
//      myArgs = new Args("l,d*", new String[]{"-d", "/usr/local"});
//      myArgs = new Args("y", new String[]{"-d", "/usr/local"});
      myArgs = new Args("l,d*,p#", new String[]{"-l", "-d", "/usr/local", "-p", "8080"});
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (ArgsException e) {
      e.printStackTrace();
    }
    boolean p1 = myArgs.getBoolean('l');
    System.out.println("result:" + p1);
    String p2 = myArgs.getString('d');
    System.out.println("result:" + p2);
    int p3 = myArgs.getInt('p');
    System.out.println("result:" + p3);

    System.out.println("valid:" + myArgs.isValid());
    System.out.println("valid arguments:" + myArgs.cardinality());
    System.out.println("schema:" + myArgs.usage());
    try {
      System.out.println("error message:" + myArgs.errorMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
