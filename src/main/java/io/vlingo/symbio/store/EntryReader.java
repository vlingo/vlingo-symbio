// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store;

import java.util.List;

import io.vlingo.actors.Actor;
import io.vlingo.common.Completes;
import io.vlingo.symbio.Entry;

/**
 * The {@code Entry<T>} reader for a given storage type. The specific storage type provides its typed instance.
 * This reads sequentially over all {@code Entry<T>} instances in the entire storage, from the
 * first written {@code Entry<T>} to the current last written {@code Entry<T>}, and is prepared to read
 * all newly appended {@code Entry<T>} instances beyond that point when they become available.
 *
 * @param <T> the concrete type of {@code Entry<T>} stored and read, which maybe be String, byte[], or Object
 */
public interface EntryReader<T extends Entry<?>> {

  /**
   * A means to seek to the first id position of the storage. This constant
   * must be honored by all {@code Entry<T>} storage implementations regardless
   *  of internal id type.
   */
  static final String Beginning = "<";

  /**
   * A means to seek past the last id position of the storage. This constant
   * must be honored by all {@code Entry<T>} storage implementations regardless
   * of internal id type.
   */
  static final String End = ">";

  /**
   * A means to query the current id position of the journal without repositioning.
   * This constant must be honored by all {@code Entry<T>} storage implementations
   * regardless of internal id type.
   */
  static final String Query = "=";

  /**
   * Eventually answers the name of this reader.
   *
   * @return the {@code Completes<String>} reader's name
   */
  Completes<String> name();

  /**
   * Eventually answers the next available {@code Entry<T>} instance or null if none is currently available.
   * The next {@code Entry<T>} instance is relative to the one previously read by the same reader
   * instance, or the first {@code Entry<T>} instance in the storage if none have previously
   * been read. Note that this is the least efficient read, because only one {@code Entry<T>} will
   * be answered, but it may be useful for test purposes or a storage that is
   * appended to slowly.
   * @return the {@code Completes<T>} next available entry or null if none
   */
  Completes<T> readNext();

  /**
   * Eventually answers the next available {@code Entry<T>} instances as a {@code List}, which may be
   * empty if none are currently available. The next {@code Entry<T>} instances are relative to the one(s)
   * previously read by the same reader instance, or the first {@code Entry<T>} instance in the storage
   * if none have previously been read. Note that this is the most efficient read, because up to
   * {@code maximumEntries} {@code Entry<T>} instances will be answered. The {@code maximumEntries} should be used to indicate
   * the total number of {@code Entry<T>} instances that can be consumed in a timely fashion by the sender,
   * which is a natural back-pressure mechanism.
   *
   * @param maximumEntries the int indicating the maximum number of {@code Entry<T>} instances to read
   *
   * @return the {@code Completes<List<T>>} of at most maximumEntries or empty if none
   */
  Completes<List<T>> readNext(final int maximumEntries);

  /**
   * Rewinds the reader so that the next available {@code Entry<T>} is the first one in the storage.
   * Sending {@code rewind()} is the same as sending {@code seekTo(Beginning)}.
   */
  void rewind();

  /**
   * Eventually answers the new position of the reader after attempting to seek to the {@code Entry<T>} of the
   * given id, such that the next available {@code Entry<T>} is the one of the given id. If the id does not (yet)
   * exist, the position is set to just beyond the last {@code Entry<T>} instance in the journal (see {@code End}), or to
   * being the first (see {@code Beginning}) if none currently exist. For example, if the storage id type is a
   * long, passing {@code "-1"} will cause the position to be set just beyond the last {@code Entry<T>} instance in
   * the storage, or to the beginning if no instances exist. Passing the {@code String "="} (see {@code Query})
   * answers the current id position without attempting to seek in either direction. (Seeking relative
   * to the current id position is implementation specific and is not expected to be supported by any
   * given implementation.)
   *
   * @param id the String id of the {@code Entry<T>} instance to which the seek prepares to next read
   *
   * @return {@code Completes<String>}
   */
  Completes<String> seekTo(final String id);

  /**
   * Provides advice for the specific implementation.
   */
  public static final class Advice {
    public final Object configuration;
    public final Class<? extends Actor> entryReaderClass;
    public final String queryEntryBatchExpression;
    public final String queryEntryExpression;

    public Advice(
            final Object configuration,
            final Class<? extends Actor> entryReaderClass,
            final String queryEntryBatchExpression,
            final String queryEntryExpression) {

      this.configuration = configuration;
      this.entryReaderClass = entryReaderClass;
      this.queryEntryBatchExpression = queryEntryBatchExpression;
      this.queryEntryExpression = queryEntryExpression;
    }

    @SuppressWarnings("unchecked")
    public <C> C specificConfiguration() {
      return (C) configuration;
    }
  }
}