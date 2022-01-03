package clean_code;

public class ArgsTest {
  public static void main(String[] args) {
    args = new String[]{"-l", "-m", "-l"};
    Args myArgs = new Args("l", args);
    boolean p1 = myArgs.getBoolean('l');
    System.out.println("result:" + p1);
//    boolean p2 = myArgs.getBoolean('m');
//    System.out.println("result:" + p2);

    System.out.println("valid:" + myArgs.isValid());
    System.out.println("valid arguments:" + myArgs.cardinality());
    System.out.println("schema:" + myArgs.usage());
    System.out.println("error message:" + myArgs.errorMessage());
  }
}
