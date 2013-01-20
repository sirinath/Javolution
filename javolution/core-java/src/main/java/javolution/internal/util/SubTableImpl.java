/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import javolution.util.AbstractTable;
import javolution.util.FastComparator;

/**
 * A view over a portion of a table. 
 */
public final class SubTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    private int fromIndex;

    private int toIndex;

    public SubTableImpl(AbstractTable<E> that, int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException();
        this.that = that;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public int size() {
        return fromIndex - toIndex;
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size())) throw indexError(index);
        return that.get(index + fromIndex);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size())) throw indexError(index);
        return that.set(index + fromIndex, element);
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size())) throw indexError(index);
        that.add(index + fromIndex, element);
    }

    @Override
    public E remove(int index) {
        if ((index < 0) && (index >= size())) throw indexError(index);
        toIndex--;
        return that.remove(index + fromIndex);
    }
    
    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }

    @Override
    public SubTableImpl<E> copy() {
        return new SubTableImpl(that.copy(), fromIndex, toIndex);
    }

  // 
    // Overrides methods impacted.
    //
    
    @Override
    public void removeRange(int fromIndex, int toIndex) { 
        if ((fromIndex < 0) || (toIndex < 0) || (fromIndex > toIndex) || (toIndex > size())) 
            throw rangeError(fromIndex, toIndex);
        that.removeRange(this.fromIndex + fromIndex, this.fromIndex + toIndex);
    }            
    
}
