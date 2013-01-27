/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.context.LocalContext;
import javolution.context.LocalParameter;
import javolution.lang.Functor;
import javolution.lang.MultiVariable;
import javolution.text.TypeFormat;

/**
 * Utility class to measure execution time with high precision.
 */
public class Perfometer {

    volatile boolean doPerform = true;

    /**
     * Hold the measure time duration in nanosecond.
     */
    public static final LocalParameter<Long> MEASURE_DURATION_NS = new LocalParameter(1000 * 1000 * 100L) {
        @Override
        public void configure(CharSequence configuration) {
            this.setDefaultValue(TypeFormat.parseLong(configuration));
        }
    };

    /**
     * Indicates if the operation to be measured is actually performed.
     */
    public boolean doPerform() {
        return doPerform;
    }

    /**
     * Measure the execution of the specified functor critical operations in 
     * nanosecond.
     * The functor is executed first with {@link #doPerform} set to <code>false</code>,
     * then it is set to <code>true</code>, the execution time is the second execution
     * time minus the first one. Parameters of a functor can be functors themselves 
     * in which case they are evaluated recursively before each measure.
     */
    public long measure(Functor functor, Object... params) {
        measure(true, functor, params); // Class initialization.
        System.gc();
        long nopExecutionTime = measure(false, functor, params);
        System.gc();
        long performExecutionTime = measure(true, functor, params);
        return performExecutionTime - nopExecutionTime;
    }

    private long measure(boolean doPerform, Functor functor, Object... params) {
        long startTime = System.nanoTime();
        int count = 0;
        long executionTime;
        long cumulatedTime = 0;
        while (true) {
            Object param = evaluateParams(0, params);
            try {
                this.doPerform = doPerform;
                cumulatedTime -= System.nanoTime();
                functor.evaluate(param);
            } finally {
                cumulatedTime += System.nanoTime();
                this.doPerform = true;
            }
            count++;
            executionTime = System.nanoTime() - startTime;
            if (executionTime > LocalContext.getLocalValue(MEASURE_DURATION_NS))
                break;
        }
        return cumulatedTime / count;
    }

    private Object evaluateParams(int i, Object... params) {
        if (i >= params.length) return null;
        Object param = params[i];
        Object next = evaluateParams(++i, params);
        if (param instanceof Functor) {
            return ((Functor) param).evaluate(next);
        } else if (next != null) {
            return new MultiVariable(param, next);
        } else {
            return param;
        }
    }
}
