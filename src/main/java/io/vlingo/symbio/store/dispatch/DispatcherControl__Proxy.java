// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.dispatch;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

import java.util.function.Consumer;

public class DispatcherControl__Proxy implements DispatcherControl {

  private static final String dispatchUnconfirmedRepresentation1 = "dispatchUnconfirmed()";
  private static final String confirmDispatchedRepresentation2 = "confirmDispatched(java.lang.String, io.vlingo.symbio.store.state.ConfirmDispatchedResultInterest)";

  private final Actor actor;
  private final Mailbox mailbox;

  public DispatcherControl__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void dispatchUnconfirmed() {
    final java.util.function.Consumer<DispatcherControl> consumer = (actor) -> actor.dispatchUnconfirmed();
    send(consumer, DispatcherControl__Proxy.dispatchUnconfirmedRepresentation1);
  }

  public void confirmDispatched(java.lang.String arg0, ConfirmDispatchedResultInterest arg1) {
    final java.util.function.Consumer<DispatcherControl> consumer = (actor) -> actor.confirmDispatched(arg0, arg1);
    send(consumer, DispatcherControl__Proxy.confirmDispatchedRepresentation2);
  }

  public void stop() {
    final java.util.function.Consumer<DispatcherControl> consumer = (actor) -> actor.stop();
    send(consumer, DispatcherControl__Proxy.dispatchUnconfirmedRepresentation1);
  }

  private void send(Consumer<DispatcherControl> consumer, String representation) {
    if (!actor.isStopped()) {
      if (mailbox.isPreallocated()) {
        mailbox.send(actor, DispatcherControl.class, consumer, null, representation);
      } else {
        mailbox.send(new LocalMessage<>(actor, DispatcherControl.class, consumer, representation));
      }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representation));
    }
  }
}
