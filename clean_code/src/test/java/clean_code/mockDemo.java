package clean_code;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

public class mockDemo {

  @Test
  public void verifyBehavior() {
    //mock creation
    List mockedList = mock(List.class);

    //using mock object
    mockedList.add("one");
    mockedList.clear();

    //verification
    //mock对象会记录所有的交互操作，所以这里可以验证该对象是否发生了某个行为
    verify(mockedList).add("one");
    verify(mockedList).clear();
  }

  @Test
  public void stubbing() {
    //You can mock concrete classes, not just interfaces
    LinkedList mockedList = mock(LinkedList.class);

    //stubbing
    //简单理解：mock对象可以指定某一行为对象会作出的反应，如访问0下标就返回first
    when(mockedList.get(0)).thenReturn("first");
    when(mockedList.get(1)).thenThrow(new RuntimeException());

    //following prints "first"
    System.out.println(mockedList.get(0));

    //following throws runtime exception
    System.out.println(mockedList.get(1));

    //following prints "null" because get(999) was not stubbed
    System.out.println(mockedList.get(999));

    //Although it is possible to verify a stubbed invocation, usually it's just redundant
    //If your code cares what get(0) returns, then something else breaks (often even before verify() gets executed).
    //If your code doesn't care what get(0) returns, then it should not be stubbed.
    verify(mockedList).get(0);
  }

  @Test
  public void argumentMarcher() {
    LinkedList mockedList = mock(LinkedList.class);

    //stubbing using built-in anyInt() argument matcher
    when(mockedList.get(anyInt())).thenReturn("element");

    //stubbing using custom matcher (let's say isValid() returns your own matcher implementation):
    when(mockedList.contains(argThat(isValid()))).thenReturn(true);

    //following prints "element"
    System.out.println(mockedList.get(999));

    //you can also verify using an argument matcher
    verify(mockedList).get(anyInt());

    //argument matchers can also be written as Java 8 Lambdas
    verify(mockedList).add(argThat(someString -> ((String) someString).length() > 5));


//    verify(mockedList).someMethod(anyInt(), anyString(), eq("third argument"));
    //above is correct - eq() is also an argument matcher

//    verify(mockedList).someMethod(anyInt(), anyString(), "third argument");
    //above is incorrect - exception will be thrown because third argument is given without an argument matcher.
  }

  private ArgumentMatcher isValid() {
    // 自定义argumentMatcher
    return new ArgumentMatcher() {
      @Override
      public boolean matches(Object argument) {
        return true;
      }
    };
  }

  @Test
  public void mockTimes() {
    LinkedList mockedList = mock(LinkedList.class);
    //using mock
    mockedList.add("once");

    mockedList.add("twice");
    mockedList.add("twice");

    mockedList.add("three times");
    mockedList.add("three times");
    mockedList.add("three times");

    //following two verifications work exactly the same - times(1) is used by default
    // times(1)是verify的默认参数
    verify(mockedList).add("once");
    verify(mockedList, times(1)).add("once");

    //exact number of invocations verification
    //twice是否被调用了两次，three times是否被调用了三次
    verify(mockedList, times(2)).add("twice");
    verify(mockedList, times(3)).add("three times");

    //verification using never(). never() is an alias to times(0)
    verify(mockedList, never()).add("never happened");

    //verification using atLeast()/atMost()
    verify(mockedList, atMostOnce()).add("once");
    verify(mockedList, atLeastOnce()).add("three times");
    verify(mockedList, atLeast(2)).add("three times");
    verify(mockedList, atMost(5)).add("three times");
  }

  @Test
  public void testStubbingVoidMethodException() {
    LinkedList mockedList = mock(LinkedList.class);

    // stubbing的另一种表现形式: doThrow写在前边
    // 返回值为void的方法，stubbing抛出异常
    doThrow(new RuntimeException()).when(mockedList).clear();

    //following throws RuntimeException:
    mockedList.clear();
  }

  @Test
  public void testVerifyOrder() {
    // A. Single mock whose methods must be invoked in a particular order
    List singleMock = mock(List.class);

    //using a single mock
    singleMock.add("was added first");
    singleMock.add("was added second");

    //create an inOrder verifier for a single mock
    InOrder inOrder = inOrder(singleMock);

    //following will make sure that add is first called with "was added first", then with "was added second"
    inOrder.verify(singleMock).add("was added first");
    inOrder.verify(singleMock).add("was added second");

    // B. Multiple mocks that must be used in a particular order
    List firstMock = mock(List.class);
    List secondMock = mock(List.class);

    //using mocks
    firstMock.add("was called first");
    secondMock.add("was called second");

    //create inOrder object passing any mocks that need to be verified in order
    InOrder inOrder2 = inOrder(firstMock, secondMock);

    //following will make sure that firstMock was called before secondMock
    inOrder2.verify(firstMock).add("was called first");
    inOrder2.verify(secondMock).add("was called second");

    // Oh, and A + B can be mixed together at will
  }

  @Test
  public void testNever() {
    LinkedList mockOne = mock(LinkedList.class);
    //using mocks - only mockOne is interacted
    mockOne.add("one");

    //ordinary verification
    verify(mockOne).add("one");

    //verify that method was never called on a mock
    verify(mockOne, never()).add("two");
  }

  @Test
  public void testRedundant() {
    LinkedList mockedList = mock(LinkedList.class);
    //using mocks
    mockedList.add("one");
    mockedList.add("two");

    verify(mockedList).add("one");

    //following verification will fail
    //校验是否mock对象是否没有其他互动了，这个方法不应该经常被调用
    verifyNoMoreInteractions(mockedList);
  }
}
