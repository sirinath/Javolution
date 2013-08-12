/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javolution.lang.Immutable;
import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.map.AtomicMapImpl;
import javolution.util.internal.map.FastMapImpl;
import javolution.util.internal.map.SharedMapImpl;
import javolution.util.internal.map.UnmodifiableMapImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * <p> A high-performance map with {@link Realtime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast maps support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modifications.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications.</li>
 *    <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free 
 *    and map updates (e.g. {@link #putAll}) are atomic.</li>
 *    <li>{@link #entrySet} - {@link FastSet} view over the map entries allowing 
 *                            entries to be added/removed.</li>
 *    <li>{@link #keySet} - {@link FastSet} view over the map keys allowing keys 
 *                           to be added (map entry with {@code null} value).</li>
 *    <li>{@link #values} - {@link FastCollection} view over the map values.</li>
 * </ul>      
 * <p> The iteration order over the map keys, values or entries is deterministic 
 *     (unlike {@link java.util.HashMap}). It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedMap} subclass. 
 *     This class permits {@code null} keys.</p> 
 *     
 * <p> Fast maps can advantageously replace any of the standard <code>java.util</code> maps.</p> 
 * <p>[code]
 *  FastMap<Foo, Bar> hashMap = new FastMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> concurrentHashMap = new FastMap<Foo, Bar>().shared(); // FastMap implements ConcurrentMap interface.
 *  FastMap<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>(); // Deterministic iteration order (insertion order).
 *  FastMap<Foo, Bar> linkedConcurrentHashMap = new FastMap<Foo, Bar>().shared(); // No equivalent in java.util !
 *  FastMap<Foo, Bar> treeMap = new FastSortedMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> concurrentSkipListMap = new FastSortedMap<Foo, Bar>().shared();
 *  FastMap<Foo, Bar> identityHashMap = new FastMap<Foo, Bar>(Equalities.IDENTITY);
 *  [/code]</p>
 *  <p> and adds more ... 
 * <p>[code]
 *  FastMap<Foo, Bar> atomicMap = new FastMap<Foo, Bar>().atomic(); // Mutex-free access,  all updates (e.g. putAll) atomics.
 *  FastMap<Foo, Bar> atomicTree = new FastSortedMap<Foo, Bar>().atomic(); // Mutex-free access,  all updates (e.g. putAll) atomics.
 *  FastMap<String, Bar> lexicalHashMap = new FastMap<String, Bar>(Equalities.LEXICAL);  // Allows for value retrieval using any CharSequence key.
 *  FastMap<String, Bar> fastStringHashMap = new FastMap<String, Bar>(Equalities.LEXICAL_FAST);  // Same with faster hashcode calculations.
 *  ...
 *  [/code]</p>
 *  
 *  <p> Of course all views (entry, key, values) over a fast map are fast collections 
 *      and allow parallel processing.
 * [code]
 * Consumer<Collection<String>> removeNull = new Consumer<Collection<String>>() {  
 *     public void accept(Collection<String> view) {
 *          Iterator<String> it = view.iterator();
 *          while (it.hasNext()) {
 *             if (it.next() == null) it.remove();
 *         }
 *     }
 * };
 * FastMap<Person, String> names = ...
 * names.values().update(removeNull); // Remove all entries with null values.
 * names.atomic().values().update(removeNull); // Same but performed atomically on names.
 * names.values().parallel().update(removeNull); // Same but performed in parallel.
 * [/code]</p> 
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0, July 21, 2013
 */
@Realtime
public class FastMap<K, V> implements Map<K, V>, ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the actual map service implementation.
     */
    private final MapService<K, V> service;

    /**
     * Creates an empty fast map.
     */
    public FastMap() {
        this(Equalities.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparator for keys 
     * equality.
     */
    public FastMap(Equality<? super K> keyEquality) {
        this(keyEquality, Equalities.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparators for keys 
     * equality and values equality.
     */
    public FastMap(Equality<? super K> keyEquality, 
            Equality<? super V> valueEquality) {
        service = new FastMapImpl<K, V>(keyEquality, valueEquality);
    }

    /**
     * Creates a map backed up by the specified service implementation.
     */
    protected FastMap(MapService<K, V> service) {
        this.service = service;
    }

    /***************************************************************************
     * Views.
     */

    /**
     * Returns an unmodifiable view over this map. Any attempt to 
     * modify the map through this view will result into 
     * a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastMap<K, V> unmodifiable() {
        return new FastMap<K, V>(new UnmodifiableMapImpl<K, V>(service));
    }

    /**
     * Returns an atomic view over this map. All operations that write 
     * or access multiple elements in the map (such as putAll(), 
     * keySet().retainAll(), ...) are atomic. 
     * Iterators on atomic collections are read-only (do not support element 
     * {@link Iterator#remove() removal}) except during {@link FastCollection#update updates}.   
     */
    @Parallelizable(mutexFree = true, comment = "Except for write operations, all read operations are mutex-free.")
    public FastMap<K,V> atomic() {
        return new FastMap<K, V>(new AtomicMapImpl<K, V>(service));
    }


    /**
     * Returns a thread-safe view over this map. The shared view
     * allows for concurrent read as long as there is no writer. 
     * The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
     * readers-writers locks</a> giving priority to writers. 
     */
    @Parallelizable(mutexFree = false, comment = "Use multiple-readers/single-writer lock.")
    public FastMap<K, V> shared() {
        return new FastMap<K, V>(new SharedMapImpl<K, V>(service));
    }

    /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports 
     * adding new keys for which the corresponding entry value 
     * is always {@code null}.
     */
    public FastSet<K> keySet() {
        return new FastSet<K>(service.keySet());
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. The collection
     * supports removing values (hence entries) but not adding new values. 
     */
    public FastCollection<V> values() {
        return new FastCollection<V>() {
            private static final long serialVersionUID = 0x600L; // Version.
            CollectionService<V> serviceValues = service.values();

            @Override
            protected CollectionService<V> service() {
                return serviceValues;
            }
        };
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set 
     * support adding/removing entries. As far as the set is concerned,
     * two entries are considered equals if they have the same keys regardless
     * of their values. 
     */
    public FastSet<Entry<K, V>> entrySet() {
        return new FastSet<Entry<K, V>>(service.entrySet());
    }

    /***************************************************************************
     * Map interface.
     */

    @Override
    @Realtime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return service.isEmpty();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean containsKey(Object key) {
        return service.containsKey(key);
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(Object value) {
        return service.containsValue(value);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V get(Object key) {
        return service.get(key);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V put(K key, V value) {
        return service.put(key, value);
    }

    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> map) {
        service.putAll(map);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V remove(Object key) {
        return service.remove(key);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    /***************************************************************************
     * ConcurrentMap Interface.
     */

    @Override
    @Realtime(limit = CONSTANT)
    public V putIfAbsent(K key, V value) {
        return service.putIfAbsent(key, value);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean remove(Object key, Object value) {
        return service.remove(key, value);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean replace(K key, V oldValue, V newValue) {
        return service.replace(key, oldValue, newValue);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V replace(K key, V value) {
        return service.replace(key, value);
    }

    /***************************************************************************
     * Misc.
     */

    /**
     * Returns this map with the specified map's entries added.
     */
    public FastMap<K,V> putAll(FastMap<? extends K, ? extends V> that) {   
        putAll((Map<? extends K, ? extends V>)that);
        return this;
    }

    /** 
     * Returns an immutable reference over this map. The immutable 
     * value is an {@link #unmodifiable() unmodifiable} view of this map
     * for which the caller guarantees that no change will ever be made 
     * (e.g. there is no reference left to the original map).
     */
    public <T extends Map<K,V>> Immutable<T> toImmutable() {
        return new Immutable<T>() {
            @SuppressWarnings("unchecked")
            final T value = (T) unmodifiable();
            @Override
            public T value() {
                return value;
            }            
        };
    }
    
    /**
      * Returns this map service implementation.
      */
    protected MapService<K, V> service() {
        return service;
    }

}
