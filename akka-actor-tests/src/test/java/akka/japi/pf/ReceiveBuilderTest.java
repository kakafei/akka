/**
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.japi.pf;

import org.junit.Test;
import org.junit.Before;
import org.scalatest.junit.JUnitSuite;

import akka.actor.AbstractActor.Receive;

import static org.junit.Assert.*;

@SuppressWarnings("serial")
public class ReceiveBuilderTest extends JUnitSuite {

  public static interface Msg {}
  public static class Msg1 implements Msg {
    @Override
    public String toString() {
      return "Msg1";
    }
  }
  public static class Msg2 implements Msg {
    public final String value;

    public Msg2(String value) {
      this.value = value;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Msg2 other = (Msg2) obj;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "Msg2 [value=" + value + "]";
    }

  }

  // using instance variable, because lambdas can only modify final fields
  private String result = "";

  private String result() {
    String r = result;
    result = "";
    return r;
  }

  private void result(String r) {
    result = r;
  }


  @Before
  public void beforeEach() {
    result = "";
   }

  @Test
  public void shouldNotMatchWhenEmpty() {
    Receive rcv = ReceiveBuilder.create().build();
    assertFalse(rcv.onMessage().isDefinedAt("hello"));
    assertFalse(rcv.onMessage().isDefinedAt(42));
  }

  @Test
  public void shouldMatchByClass() {
    Receive rcv = ReceiveBuilder.create()
        .match(Msg1.class, m -> result("match Msg1"))
        .build();
    assertTrue(rcv.onMessage().isDefinedAt(new Msg1()));
    rcv.onMessage().apply(new Msg1());
    assertEquals("match Msg1", result());

    assertFalse(rcv.onMessage().isDefinedAt(new Msg2("foo")));
    assertFalse(rcv.onMessage().isDefinedAt("hello"));
    assertFalse(rcv.onMessage().isDefinedAt(42));
  }

  @Test
  public void shouldMatchBySubclass() {
    Receive rcv = ReceiveBuilder.create()
        .match(Msg.class, m -> result("match Msg"))
        .build();
    assertTrue(rcv.onMessage().isDefinedAt(new Msg1()));
    rcv.onMessage().apply(new Msg1());
    assertEquals("match Msg", result());

    assertTrue(rcv.onMessage().isDefinedAt(new Msg2("foo")));
    rcv.onMessage().apply(new Msg2("foo"));
    assertEquals("match Msg", result());

    assertFalse(rcv.onMessage().isDefinedAt("hello"));
    assertFalse(rcv.onMessage().isDefinedAt(42));
  }

  @Test
  public void shouldMatchByPredicate() {
    Receive rcv = ReceiveBuilder.create()
        .match(Msg1.class, m -> true, m -> result("match Msg1"))
        .match(Msg2.class, m -> m.value.equals("foo"), m -> result("match Msg2"))
        .build();
    assertTrue(rcv.onMessage().isDefinedAt(new Msg1()));
    rcv.onMessage().apply(new Msg1());
    assertEquals("match Msg1", result());

    assertTrue(rcv.onMessage().isDefinedAt(new Msg2("foo")));
    rcv.onMessage().apply(new Msg2("foo"));
    assertEquals("match Msg2", result());

    assertFalse(rcv.onMessage().isDefinedAt(new Msg2("bar")));

    assertFalse(rcv.onMessage().isDefinedAt("hello"));
    assertFalse(rcv.onMessage().isDefinedAt(42));
  }

  @Test
  public void shouldMatchEquals() {
    Msg2 msg2 = new Msg2("foo");
    Receive rcv = ReceiveBuilder.create()
        .matchEquals(msg2, m -> result("match msg2"))
        .matchEquals("foo", m -> result("match foo"))
        .matchEquals(17, m -> result("match 17"))
        .build();
    assertTrue(rcv.onMessage().isDefinedAt(new Msg2("foo")));
    rcv.onMessage().apply(new Msg2("foo"));
    assertEquals("match msg2", result());

    assertTrue(rcv.onMessage().isDefinedAt("foo"));
    rcv.onMessage().apply("foo");
    assertEquals("match foo", result());

    assertTrue(rcv.onMessage().isDefinedAt(17));
    rcv.onMessage().apply(17);
    assertEquals("match 17", result());

    assertFalse(rcv.onMessage().isDefinedAt(new Msg2("bar")));
    assertFalse(rcv.onMessage().isDefinedAt("hello"));
    assertFalse(rcv.onMessage().isDefinedAt(42));
  }

  @Test
  public void shouldMatchAny() {
    Receive rcv = ReceiveBuilder.create()
        .match(Msg1.class, m -> result("match Msg1"))
        .matchAny(m -> result("match any"))
        .build();
    assertTrue(rcv.onMessage().isDefinedAt(new Msg1()));
    rcv.onMessage().apply(new Msg1());
    assertEquals("match Msg1", result());

    assertTrue(rcv.onMessage().isDefinedAt(new Msg2("foo")));
    rcv.onMessage().apply(new Msg2("foo"));
    assertEquals("match any", result());

    assertTrue(rcv.onMessage().isDefinedAt("hello"));
    assertTrue(rcv.onMessage().isDefinedAt(42));
  }
}
