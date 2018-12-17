// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.journal.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.vlingo.actors.Actor;
import io.vlingo.common.Completes;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.store.journal.JournalReader;
import io.vlingo.symbio.store.journal.Stream;

public class InMemoryJournalReaderActor<T> extends Actor implements JournalReader<T> {
  private final ListIterator<Entry<T>> journalView;
  private final String name;

  public InMemoryJournalReaderActor(final ListIterator<Entry<T>> journalView, final String name) {
    this.journalView = journalView;
    this.name = name;
  }

  @Override
  public Completes<String> name() {
    return completes().with(name);
  }

  @Override
  public Completes<Entry<T>> readNext() {
    if (journalView.hasNext()) {
      return completes().with(journalView.next());
    }
    return null;
  }

  @Override
  public Completes<Stream<T>> readNext(final int maximumEntries) {
    final List<Entry<T>> entries = new ArrayList<>(maximumEntries);

    int streamVersion = journalView.nextIndex();
    for (int count = 0; count < maximumEntries; ++count) {
      if (journalView.hasNext()) {
        ++streamVersion; // 1-based
        entries.add(journalView.next());
      } else {
        count = maximumEntries + 1;
      }
    }
    return completes().with(new Stream<>(name, streamVersion, entries, null));
  }

  @Override
  public void rewind() {
    while (journalView.hasPrevious()) {
      journalView.previous();
    }
  }

  @Override
  public Completes<String> seekTo(final String id) {
    final String currentId;

    switch (id) {
    case Beginning:
      rewind();
      currentId = readCurrentId();
      break;
    case End:
      end();
      currentId = readCurrentId();
      break;
    case Query:
      currentId = readCurrentId();
      break;
    default:
      to(id);
      currentId = readCurrentId();
      break;
    }

    return completes().with(currentId);
  }

  private void end() {
    while (journalView.hasNext()) {
      journalView.next();
    }
  }

  private String readCurrentId() {
    if (journalView.hasNext()) {
      final String currentId = journalView.next().id();
      journalView.previous();
      return currentId;
    }
    return "-1";
  }

  private void to(final String id) {
    rewind();
    while (journalView.hasNext()) {
      final Entry<T> entry = journalView.next();
      if (entry.id().equals(id)) {
        return;
      }
    }
  }
}