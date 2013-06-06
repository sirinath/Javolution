/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Set;

import javolution.annotation.RealTime;
import javolution.annotation.RealTime.Limit;
import javolution.internal.util.set.FilteredSetImpl;
import javolution.internal.util.set.SharedSetImpl;
import javolution.internal.util.set.UnmodifiableSetImpl;
import javolution.util.function.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.SetService;

/**
 * <p> A high-performance set with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastSet<E> extends FastCollection<E> implements Set<E> {
  
    /**
     * Creates an empty set backed up by a {@link FastMap} and having  
     * the same real-time characteristics.
     */
    public FastSet() {
        this(Comparators.STANDARD);
    }
    
    /**
     * Creates an empty set backed up by a {@link FastMap} and using the 
     * specified comparator for key equality.
    */
   public FastSet(ComparatorService<? super E> comparator) {
       super(new FastMap<E, Void>(comparator).keySet().getService());
   }   
   
   /**
     * Creates a set backed up by the specified implementation.
     */
    protected FastSet(SetService<E> implementation) {
        super(implementation);        
    }
    
    /**
     * Returns the service implementation of this set.
     */
    protected SetService<E> getService() {
        return (SetService<E>) super.getService();
    }

    /**
     * Returns the number of elements in this set.
     */
    @RealTime(Limit.CONSTANT)
    public int size() {
       return getService().size(); 
    }

    /**
     * Removes all of the elements from this set.
     */
    @RealTime(Limit.CONSTANT)
    public void clear() {
        getService().clear();
    }
    
    /**
     * Indicates if this set contains the specified element.
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.CONSTANT)
    public boolean contains(Object obj) {
        return getService().contains((E)obj);
    }

    /**
     * Removes the specified element from this set. More formally,
     * removes an element {@code elem} such that
     * {@code getComparator().areEquals(elem, obj))}.
     */  
    @SuppressWarnings("unchecked")
    @RealTime(Limit.CONSTANT)
    public boolean remove(Object obj) {
        return getService().remove((E) obj);
    }

    //
    // Overrides views returning a set.
    // 
  
    @Override
    public FastSet<E> unmodifiable() {
        return new FastSet<E>(new UnmodifiableSetImpl<E>(getService()));
    }

    @Override
    public FastSet<E> shared() {
        return new FastSet<E>(new SharedSetImpl<E>(getService()));
    }

    @Override
    public FastSet<E> filter(final Predicate<? super E> filter) {
        return new FastSet<E>(new FilteredSetImpl<E>(getService(), filter));
    }
    
    @Override
    public FastSet<E> distinct() {
        return this; // Elements already distinct.
    }

    private static final long serialVersionUID = 6416925552876467301L;
}