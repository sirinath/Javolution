/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map;

import java.util.Map;

import javolution.util.service.ComparatorService;

/**
 * A simple implementation of a map
 */
public final class BasicMapImpl<K, V> extends AbstractMapImpl<K, V> {

    // Emptiness level. Can be 1 (load factor 0.5), 2 (load factor 0.25) or any greater value.
    private static final int EMPTINESS_LEVEL = 2;

    // Initial block capacity, no resize until count > 2 (third entry for the block).  
    private static final int INITIAL_BLOCK_CAPACITY = 2 << EMPTINESS_LEVEL;

    private int count; // Number of entries different from null in this block.

    @SuppressWarnings("unchecked")
    private EntryImpl<K, V>[] entries = (EntryImpl<K, V>[]) new EntryImpl[INITIAL_BLOCK_CAPACITY];

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        entries = (EntryImpl<K, V>[]) new EntryImpl[INITIAL_BLOCK_CAPACITY];
        count = 0;
    }

    //
    // Implementation.
    //

    public boolean containsKey(Object key, int hash) {
        return entries[indexOfKey(key, hash)] != null;
    }

    public V get(Object key, int hash) {
        EntryImpl<K, V> entry = entries[indexOfKey(key, hash)];
        return (entry != null) ? entry.value : null;
    }

    public V put(K key, V value, int hash) {
        int i = indexOfKey(key, hash);
        EntryImpl<K, V> entry = entries[i];
        if (entry != null) { // Entry exists.
            V oldValue = entry.value;
            entry.value = value;
            return oldValue;
        }
        entries[i] = new EntryImpl<K, V>(key, value, hash);
        // Check if we need to resize.
        if ((++count << EMPTINESS_LEVEL) > entries.length) {
            resize(entries.length << 1);
        }
        return null;
    }

    public V remove(Object key, int hash) {
        int i = indexOfKey(key, hash);
        EntryImpl<K, V> oldEntry = entries[i];
        if (oldEntry == null)
            return null; // Entry does not exist.
        entries[i] = null;
        // Since we have made a hole, adjacent keys might have to shift.
        for (;;) {
            // We use a step of 1 (improve caching through memory locality).
            i = (i + 1) & (entries.length - 1);
            EntryImpl<K, V> entry = entries[i];
            if (entry == null)
                break; // Done.
            int correctIndex = indexOfKey(entry.key, entry.hash);
            if (correctIndex != i) { // Misplaced.
                entries[correctIndex] = entries[i];
                entries[i] = null;
            }
        }
        // Check if we need to resize.
        if (((--count << (EMPTINESS_LEVEL + 1)) <= entries.length)
                && (entries.length > INITIAL_BLOCK_CAPACITY)) {
            resize(entries.length >> 1);
        }
        return oldEntry.value;
    }

    // The capacity is a power of two such as: 
    //    (count * 2**EMPTINESS_LEVEL) <=  capacity < (count * 2**(EMPTINESS_LEVEL+1))
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        EntryImpl<K, V>[] newEntries = (EntryImpl<K, V>[]) new EntryImpl[newCapacity];
        int newMask = newEntries.length - 1;
        for (int i = 0, n = entries.length; i < n; i++) {
            EntryImpl<K, V> entry = entries[i];
            if (entry == null)
                continue;
            int newIndex = entry.hash & newMask;
            while (newEntries[newIndex] != null) { // Find empty slot.
                newIndex = (newIndex + 1) & newMask;
            }
            newEntries[newIndex] = entry;
        }
        entries = newEntries;
    }

    // Returns the index of the specified key in the map (points to a null key if key not present).
    private int indexOfKey(Object key, int hash) {
        int mask = entries.length - 1;
        int i = hash & mask;
        while (true) {
            EntryImpl<K, V> entry = entries[i];
            if (entry == null)
                return i;
            if ((entry.hash == hash) && key.equals(entry.key))
                return i;
            i = (i + 1) & mask;
        }
    }

   
}
