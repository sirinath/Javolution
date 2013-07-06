/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

import javolution.internal.util.ReadWriteLockImpl;
import javolution.internal.util.SharedIteratorImpl;
import javolution.util.FastCollection;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * A shared view over a collection allowing concurrent access and sequential updates.
 */
public final class SharedCollectionImpl<E> extends FastCollection<E> implements
        CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private final ReadWriteLockImpl rwLock;
    private CollectionService<E> target;
    
    /**
     * Splits the specified collection into sub-collections all of them 
     * sharing the same read/write locks.
     */
    @SuppressWarnings("unchecked")
    public static <E> SharedCollectionImpl<E>[] splitOf(
            CollectionService<E> target, int n, ReadWriteLockImpl rwLock) {
        CollectionService<E>[] tmp;
        rwLock.readLock().lock();
        try {
            tmp = target.trySplit(n);
        } finally {
            rwLock.readLock().unlock();
        }
        SharedCollectionImpl<E>[] shareds = new SharedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            shareds[i] = new SharedCollectionImpl<E>(tmp[i], rwLock);
        }
        return shareds;
    }

    public SharedCollectionImpl(CollectionService<E> target) {
        this(target, new ReadWriteLockImpl());
    }

    public SharedCollectionImpl(CollectionService<E> target, ReadWriteLockImpl rwLock) {
        this.target = target;
        this.rwLock= rwLock;
    }

    @Override
    public boolean add(E element) {
        rwLock.writeLock().lock();
        try {
            return target.add(element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        rwLock.readLock().lock();
        try {
            target.forEach(consumer, controller);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public ReadWriteLockImpl getLock() {
        return rwLock;
    }

   @Override
    @Deprecated
    public Iterator<E> iterator() {
        return new SharedIteratorImpl<E>(target.iterator(), rwLock);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        rwLock.writeLock().lock();
        try {
            return target.removeIf(filter, controller);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    protected SharedCollectionImpl<E> service() {
        return this;
    }

    @Override
    public SharedCollectionImpl<E>[] trySplit(int n) {
        return SharedCollectionImpl.splitOf(target, n, rwLock);
    }
}
