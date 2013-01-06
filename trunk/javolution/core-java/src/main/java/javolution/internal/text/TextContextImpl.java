/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.text;

import javolution.annotation.Format;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.FastMap;

/**
 * Holds the default implementation of TextContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class TextContextImpl extends TextContext {

    private final FastMap<Class, TextFormat> formats = new FastMap();

    @Override
    protected TextContext inner() {
        TextContextImpl ctx = new TextContextImpl();
        ctx.formats.putAll(formats);
        return ctx;
    }

    @Override
    protected <T> TextFormat<T> getFormatInContext(Class<T> type) {
        TextFormat tf = formats.get(type);
        if (tf != null) return tf;
        Format format = type.getAnnotation(Format.class);
        if ((format == null) || (format.text() == Format.UnsupportedTextFormat.class))
            return null;
        Class<? extends TextFormat> formatClass = format.text();
        try {
            tf = formatClass.newInstance();
            synchronized (formats) { // Required since possible concurrent use 
                // (getFormatInContext is not a configuration method).
                formats.put(type, tf);
            }
            return tf;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> void setFormat(Class<T> type, TextFormat<T> format) {
        formats.put(type, format);
    }

}
