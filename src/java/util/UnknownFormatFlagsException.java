/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.util;

public class UnknownFormatFlagsException extends IllegalFormatException {
    String _flags;

    public UnknownFormatFlagsException(String flags) {
        _flags = flags;
    }

    public String getFlags() {
        return _flags;
    }
}