/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import static javolution.internal.osgi.JavolutionActivator.TEXT_CONTEXT_TRACKER;
import javolution.context.AbstractContext;
import javolution.context.FormatContext;
import javolution.internal.text.TextContextImpl;
import javolution.lang.Configurable;

/**
 * <p> A context for plain text parsing/formatting. The default text 
 *     format for any class is given by the 
 *     {@link javolution.text.DefaultTextFormat DefaultTextFormat} 
 *     inheritable annotation.</p>
 * 
 * <p> A default format exists for the following predefined types:
 *     <code><ul>
 *       <li>java.lang.String</li>
 *       <li>java.lang.Boolean</li>
 *       <li>java.lang.Character</li>
 *       <li>java.lang.Byte</li>
 *       <li>java.lang.Short</li>
 *       <li>java.lang.Integer</li>
 *       <li>java.lang.Long</li>
 *       <li>java.lang.Float</li>
 *       <li>java.lang.Double</li>
 *       <li>java.lang.Class</li>
 *    </ul></code></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class TextContext extends FormatContext {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false);

    /**
     * Default constructor.
     */
    protected TextContext() {}

    /**
     * Enters a new text context instance.
     * 
     * @return the new text context implementation entered.
     */
    public static TextContext enter() {
        return (TextContext) TextContext.currentTextContext().enterInner();
    }

    /**
     * Returns the plain text format for the specified type or <code>null</code> 
     * if none defined.
     */
    public static <T> TextFormat<T> getFormat(Class<? extends T> type) {
        return TextContext.currentTextContext().getFormatInContext(type);
    }

    /**
     * Sets the plain text format for the specified type (and its sub-types).
     */
    public abstract <T> void setFormat(Class<? extends T> type,
            TextFormat<T> format);

    /**
     * Returns the plain text format for the specified type.
     */
    protected abstract <T> TextFormat<T> getFormatInContext(
            Class<? extends T> type);

    /**
     * Returns the current text context.
     */
    protected static TextContext currentTextContext() {
        TextContext ctx = AbstractContext.current(TextContext.class);
        if (ctx != null)
            return ctx;
        return TEXT_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get(), DEFAULT);
    }

    private static final TextContextImpl DEFAULT = new TextContextImpl();
}