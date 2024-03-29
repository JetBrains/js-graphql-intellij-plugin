/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.collect;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The standard ImmutableMap does not allow null values.  The implementation does.
 * We have cases in graphql, around arguments where a mep entry can be explicitly set to null
 * and we want immutable smart maps for these case.
 *
 * @param <K> for key
 * @param <V> for victory
 */
@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes"})
@Internal
public final class ImmutableMapWithNullValues<K, V> implements Map<K, V> {

  private final Map<K, V> delegate;

  private static final ImmutableMapWithNullValues emptyMap = new ImmutableMapWithNullValues();

  private ImmutableMapWithNullValues(Map<K, V> values) {
    this.delegate = Collections.unmodifiableMap(new LinkedHashMap<>(values));
  }

  /**
   * Only used to construct the singleton empty map
   */
  private ImmutableMapWithNullValues() {
    this(Collections.emptyMap());
  }


  public static <K, V> ImmutableMapWithNullValues<K, V> emptyMap() {
    return emptyMap;
  }

  public static <K, V> ImmutableMapWithNullValues<K, V> copyOf(Map<K, V> map) {
    Assert.assertNotNull(map);
    if (map instanceof ImmutableMapWithNullValues) {
      return (ImmutableMapWithNullValues<K, V>)map;
    }
    if (map.isEmpty()) {
      return emptyMap();
    }
    return new ImmutableMapWithNullValues<>(map);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return delegate.get(key);
  }

  @Override
  @Deprecated
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    return delegate.getOrDefault(key, defaultValue);
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    delegate.forEach(action);
  }

  @Override
  @Deprecated
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V putIfAbsent(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public boolean replace(K key, V oldValue, V newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V replace(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    throw new UnsupportedOperationException();
  }
}
