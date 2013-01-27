/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Collection;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;
import javolution.util.FastTable;

/**
 * A view using a custom comparator for object comparison and sorting.
 */
public final class CustomComparatorTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    private final FastComparator<E> comparator;

    public CustomComparatorTableImpl(AbstractTable<E> that, FastComparator<E> comparator) {
        this.that = that;
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        return that.get(index);
    }

    @Override
    public E set(int index, E element) {
        return that.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        that.add(index, element);
    }

    @Override
    public E remove(int index) {
        return that.remove(index);
    }

    @Override
    public CustomComparatorTableImpl<E> copy() {
        return new CustomComparatorTableImpl<E>(that.copy(), comparator.copy());
    }

    //
    // Non-abstract methods should forward to the actual table (unless impacted).
    //
    @Override
    public void clear() {
        that.clear();
    }

    @Override
    public E getFirst() {
        return that.getFirst();
    }

    @Override
    public E getLast() {
        return that.getLast();
    }

    @Override
    public boolean add(E element) {
        return that.add(element);
    }

    @Override
    public void addFirst(E element) {
        that.addFirst(element);
    }

    @Override
    public void addLast(E element) {
        that.addLast(element);
    }

    @Override
    public E removeFirst() {
        return that.removeFirst();
    }

    @Override
    public E removeLast() {
        return that.removeLast();
    }

    @Override
    public E pollFirst() {
        return that.pollFirst();
    }

    @Override
    public E pollLast() {
        return that.pollLast();
    }

    @Override
    public E peekFirst() {
        return that.peekFirst();
    }

    @Override
    public E peekLast() {
        return that.peekLast();
    }

    @Override
    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        return that.forEach(functor);
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return that.removeAll(predicate);
    }

    @Override
    public boolean addAll(final Collection<? extends E> elements) {
        return that.addAll(elements);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> elements) {
        return that.addAll(index, elements);
    }

    @Override
    public boolean contains(E element) {
        return super.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return super.remove(element);
    }

    @Override
    public int indexOf(E element) {
        return super.indexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return super.lastIndexOf(element);
    }

    @Override
    public void sort() {
        super.sort();
    }

    @Override
    public FastComparator<E> comparator() {
        return comparator;
    }        
}
