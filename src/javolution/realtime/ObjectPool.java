/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.JavolutionError;

/**
 * This abstract class represents an object pool managed by a 
 * {@link PoolContext}. {@link #isLocal Local} pools are safe to use 
 * without synchronization. The real-time framework guarantees that 
 * no more than one thread can have access to any local pool at any
 * given time. As for operations upon non-local pools, synchronization
 * has to be performed on the pool itself to guarantee thread-safety.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public abstract class ObjectPool {

    /**
     * Holds a pool returning <code>null</code> values.
     */
    public static final ObjectPool NULL = new ObjectPool() {
        public Object next() {
            return null;
        }
        public void recycle(Object obj) {}
        protected void recycleAll() {}
        protected void clearAll() {}
    };
    

    /**
     * Holds the outer pool of this pool or <code>null</code> if none.
     */
    ObjectPool outer;

    /**
     * Holds the current user of this pool.  
     */
    Thread user;

    /**
     * Indicates if this pool is in use by this context or an inner context.  
     */
    boolean inUse;

    /**
     * Default constructor.
     */
    protected ObjectPool() {
    }

    /**
     * Returns the current user of this pool or <code>null</code> if none.
     * 
     * @return the pool current user.  
     */
    public final Thread getUser() {
        return user;
    }

    /**
     * Indicates if this pool is actively used by the current thread.
     * The framework ensures that local pools are visible to their users only
     * (concurrent executions are performed in an inner pool context).
     * Operations on local pools are therefore thread-safe without requiring
     * synchronization.
     * 
     * @return <code>true</code> if this pool is local for the current thread;
     *         <code>false</code> otherwise.
     * @throws JavolutionError if this operation is called upon a pool 
     *         not currently {@link #inUse in use}.
     */
    public final boolean isLocal() {
        if (inUse) {
            if (user == null) {
                return false; // Outer pool.
            } else {
                if (user == Thread.currentThread()) {
                    return true; // Local pool.
                } else {
                    throw new JavolutionError(
                            "Concurrent access to local pool detected");
                }
            }
        } else {
            throw new JavolutionError(
                    "Access to inner pool or unused pool detected");
        }
    }

    /**
     * Indicates if this pool is in use. A pool can be in use and not having a
     * current user if the user has entered an inner pool.
     * 
     * @return <code>true</code> if at least one thread has been allocating from 
     *         this pool; <code>false</code> if this pool is not being used at 
     *         all (e.g. inner pool).
     */
    public final boolean inUse() {
        return inUse;
    }

    /**
     * Returns the outer pool of this pool.  
     * 
     * @return the outer pool or <code>null</code> if the outer is the heap.
     */
    public final ObjectPool getOuter() {
        return outer;
    }

    /**
     * Returns the next available object from this pool. If there is none,
     * a new object from the {@link #getOuter outer} pool is moved to this pool
     * and returned.  
     * 
     * @return the next available object from this pool.
     */
    public abstract Object next();

    /**
     * Recycles the specified object. Callers should make sure that the recycled
     * object is not going to be referenced anymore (in a heap context it would
     * be garbage collected).
     * 
     * @param obj the object to recycle to this pool.
     * @throws JavolutionError if the specified object do not belong to the pool.
     */
    public abstract void recycle(Object obj);

    /////////////////////
    // Control Methods //
    /////////////////////

    /**
     * Recycles all the objects of this pool (all used objects become new).
     * 
     * <p> Note: This method is called upon {@link PoolContext#exit exit}
     *           of a pool context for which this pool has been used.</p>
     */
    protected abstract void recycleAll();

    /**
     * Removes all objects (used and new) from this pool.
     * 
     * <p> Note: This method is called upon {@link PoolContext#clear 
     *           clearing} of the pool context this pool belongs to.
     */
    protected abstract void clearAll();

}