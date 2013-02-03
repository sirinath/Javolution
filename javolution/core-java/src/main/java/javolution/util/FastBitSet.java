/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;
import java.util.Set;

import javolution.internal.util.FastBitSetImpl;
import javolution.internal.util.BitSetIteratorImpl;
import javolution.internal.util.SharedBitSetImpl;
import javolution.internal.util.UnmodifiableBitSetImpl;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Predicate;

/**
 * <p> A table of bits equivalent to a packed set of non-negative numbers.</p>
 * 
 * <p> This class is integrated with the collection framework (as 
 *     a set of {@link Index indices} and obeys the collection semantic
 *     for methods such as {@link #size} (cardinality) or {@link #equals}
 *     (same set of indices).</p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastBitSet extends FastCollection<Index> implements Set<Index>,
        Copyable<FastBitSet> {

    /**
     * The actual implementation.
     */
    private final AbstractBitSet impl;

    /**
     * Creates an empty bit set whose capacity increments/decrements smoothly
     * without large resize operations to best fit the set current size.
     */
    public FastBitSet() {
        impl = new FastBitSetImpl(0);
    }

    /**
     * Creates a bit set of specified initial capacity (in bits). 
     * All bits are initially {@code false}.  This
     * constructor reserves enough space to represent the indices 
     * from {@code 0} to {@code bitSize-1}.
     * 
     * @param bitSize the initial capacity in bits.
     */
    public FastBitSet(int bitSize) {
        impl = new FastBitSetImpl(bitSize);
    }

    /**
     * Creates a bit set using the specified implementation.
     */
    protected FastBitSet(AbstractBitSet impl) {
        this.impl = impl;
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void and(FastBitSet that) {
        impl.and(that.impl);
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    public void andNot(FastBitSet that) {
        impl.andNot(that.impl);
    }

    /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public int cardinality() {
        return impl.cardinality();
    }

    /**
     * Sets all bits in the set to {@code false} (empty the set).
     */
    @Override
    public void clear() {
        impl.clear();
    }

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public void clear(int bitIndex) {
        impl.clear(bitIndex);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared.
     * @param  toIndex index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void clear(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        impl.clear(fromIndex, toIndex);
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void flip(int bitIndex) {
        impl.flip(bitIndex);
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void flip(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        impl.flip(fromIndex, toIndex);
    }

    /**
     * Returns {@code true } if the specified integer is in 
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public boolean get(int bitIndex) {
        return impl.get(bitIndex);
    }

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public FastBitSet get(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        return new FastBitSet(impl.get(fromIndex, toIndex));
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    public boolean intersects(FastBitSet that) {
        return impl.intersects(that.impl);
    }

    /**
     * Returns the logical number of bits actually used by this bit
     * set.  It returns the index of the highest set bit plus one.
     * 
     * <p> Note: This method does not return the number of set bits
     *           which is returned by {@link #size} </p>
     *
     * @return the index of the highest set bit plus one.
     */
    public int length() {
        return impl.length();
    }

    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextClearBit(int fromIndex) {
        return impl.nextClearBit(fromIndex);
    }

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *    for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
     *         ...
     *    }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextSetBit(int fromIndex) {
        return impl.nextSetBit(fromIndex);
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void or(FastBitSet that) {
        impl.or(that.impl);
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex) {
        impl.set(bitIndex);
    }

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex, boolean value) {
        impl.set(bitIndex, value);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set.
     * @param  toIndex index after the last bit to be set.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        impl.set(fromIndex, toIndex);
    }

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex the end range (exclusive).
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        impl.set(fromIndex, toIndex, value);
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void xor(FastBitSet that) {
        impl.xor(that.impl);
    }

    //
    // FastCollection<Index> Methods
    //

    /**
     * Returns the cardinality of this bit set (number of bits set).
     * 
     * <P>Note: Unlike {@code java.util.BitSet} this method does not 
     *          returns an approximation of the number of bits of space 
     *          actually in use. This method is compliant with 
     *          java.util.Collection meaning for size().</p>
     *
     * @return the cardinality of this bit set.
     */
    @Override
    public int size() {
        return cardinality();
    }

    /**
     * Adds the specified index to this set. This method is equivalent 
     * to <code>set(index.intValue())</code>.
     * 
     * @param index the object to be added to this set.
     * @return {@code true} if this set did not contains the specified
     *         index; {@code false} otherwise.
     */
    @Override
    public boolean add(Index index) {
        return !impl.getAndSet(index.intValue(), true);
    }

    /**
     * Indicates if the specified object/index is in this set. 
     * This method is equivalent to <code>get(index.intValue())</code>.
     * 
     * @param index the object to be tested.
     * @return {@code true} if this set contains the specified
     *         index; {@code false} otherwise.
     */
    @Override
    public boolean contains(Object index) {
        if (!(index instanceof Index)) return false;
        return impl.get(((Index)index).intValue());        
    }
    
    /**
     * Removes the specified index from this set. This method is equivalent 
     * to <code>clear(index.intValue())</code>.
     * 
     * @param index the object to be removed from this set.
     * @return {@code true} if this set contained the specified
     *         index; {@code false} otherwise.
     */
    @Override
    public boolean remove(Object index) {
        if (!(index instanceof Index)) return false;
        return impl.getAndSet(((Index)index).intValue(), false);
    }
    
    @Override
    public FastBitSet copy() {
        return new FastBitSet(impl.copy());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FastBitSet)
            return impl.equals(((FastBitSet) obj).impl);
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    @Override
    public FastBitSet unmodifiable() {
        return new FastBitSet(new UnmodifiableBitSetImpl(impl));
    }

    @Override
    public FastBitSet shared() {
        return new FastBitSet(new SharedBitSetImpl(impl));
    }

    @Override
    public <R> FastCollection<R> forEach(Functor<Index, R> functor) {
        return impl.forEach(functor);
    }

    @Override
    public void doWhile(Predicate<Index> predicate) {
        impl.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<Index> predicate) {
        return impl.removeAll(predicate);
    }

    @Override
    public Iterator<Index> iterator() {
        return new BitSetIteratorImpl(impl, 0);
    }

    @Override
    public FastCollection<Index> usingComparator(
            FastComparator<Index> comparator) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private static final long serialVersionUID = 6452172317804380908L;
}
