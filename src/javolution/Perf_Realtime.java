/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.realtime.ArrayPool;
import javolution.realtime.PoolContext;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class holds {@link javolution.realtime} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Realtime extends Javolution implements Runnable {

    private volatile Object _object; 

    private volatile Object[] _objects = new Object[1000];

    /** 
     * Executes benchmark.
     */
    public void run() {
        println("-- Heap versus Stack Allocation (Pool-Context) --");
        print("Object heap creation: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new DummyObject();
            }
        }
        endTime(10000 * _objects.length);
        print("Object stack creation: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = DummyObject.newInstance();
            }
            PoolContext.exit();
        }
        endTime(10000 * _objects.length);
        print("char[128] heap creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[128];
            }
        }
        endTime(1000 * _objects.length);
        print("char[128] stack creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = ArrayPool.charArray(128).next();
            }
            PoolContext.exit();
        }
        endTime(1000 * _objects.length);
        print("char[256] heap creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[256];
            }
        }
        endTime(1000 * _objects.length);
        print("char[256] stack creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = ArrayPool.charArray(256).next();
            }
            PoolContext.exit();
        }
        endTime(1000 * _objects.length);
        println("");
    }
    

    private static final class DummyObject extends RealtimeObject {
        static final Factory FACTORY = new Factory() {
            public Object create() {
                return new DummyObject();
            }
        };

        public DummyObject() { // Heap.
        }

        public static DummyObject newInstance() { // Stack.
            return (DummyObject) FACTORY.object();
        }
    }
}