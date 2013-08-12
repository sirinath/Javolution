/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Collection;

import javolution.util.function.Consumer;
import javolution.util.service.CollectionService;

/**
 * A sequential view over a collection.
 */
public class SequentialCollectionImpl<E> extends CollectionView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SequentialCollectionImpl(CollectionService<E> target) {
        super(target);
    }

    @Override
    public void clear() {
        target().clear();
    }

    @Override
    public boolean contains(Object obj) {
        return target().contains(obj);
    }
    
    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public void perform(Consumer<Collection<E>> action, CollectionService<E> view) {
        action.accept(view); // Executes immediately.
    }

    @Override
    public boolean remove(Object obj) {
        return target().remove(obj);
    }

    @Override
    public int size() {
        return target().size();
   }
    
    @Override
    public void update(final Consumer<Collection<E>> action, CollectionService<E> view) {
        action.accept(view); // Executes immediately.
    }
}
