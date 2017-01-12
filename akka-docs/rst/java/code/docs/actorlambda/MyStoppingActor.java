/**
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package docs.actorlambda;

//#my-stopping-actor
import akka.actor.ActorRef;
import akka.actor.AbstractActor;

public class MyStoppingActor extends AbstractActor {

  ActorRef child = null;

  // ... creation of child ...

  @Override
  public Receive initialReceive() {
    return receiveBuilder()
      .matchEquals("interrupt-child", m ->
        context().stop(child)
      )
      .matchEquals("done", m ->
        context().stop(getSelf())
      )
      .build();
  }
}
//#my-stopping-actor

