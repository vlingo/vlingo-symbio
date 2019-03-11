// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.journal.inmemory;

import java.util.List;
import java.util.ListIterator;

import io.vlingo.actors.Actor;
import io.vlingo.common.Completes;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.store.journal.JournalReader;

public class InMemoryJournalReaderActor<T> extends Actor implements JournalReader<T> {
  private final InMemoryJournalReader<T> reader;

  public InMemoryJournalReaderActor(final ListIterator<Entry<T>> journalView, final String name) {
    this.reader = new InMemoryJournalReader<>(journalView, name);
  }

  @Override
  public Completes<String> name() {
    return reader.name();
  }

  @Override
  public Completes<Entry<T>> readNext() {
    return reader.readNext();
  }

  @Override
  public Completes<List<Entry<T>>> readNext(final int maximumEntries) {
    return reader.readNext(maximumEntries);
  }

  @Override
  public void rewind() {
    reader.rewind();
  }

  @Override
  public Completes<String> seekTo(final String id) {
    return reader.seekTo(id);
  }
}
