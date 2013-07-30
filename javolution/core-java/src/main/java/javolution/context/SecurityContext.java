/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Permission;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A high-level security context integrated with OSGi.</p>
 *     
 * <p> When granting/revoking permission the order is important. 
 *     For example, the following code revokes all configurable permissions 
 *     except for concurrency settings.</p>
 * [code]
 * SecurityContext ctx = SecurityContext.enter(); 
 * try {
 *     ctx.revoke(Configurable.RECONFIGURE_PERMISSION);
 *     ctx.grant(ConcurrentContext.CONCURRENCY.getReconfigurePermission());
 *     ...
 *     // Disables concurrency (global) 
 *     ConcurrentContext.CONCURRENCY.reconfigure(0); // Ok (permission specifically granted).
 *     ...
 *  } finally {
 *     ctx.exit(); // Back to previous security settings. 
 *  }
 *  [/code]
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class SecurityContext extends AbstractContext {

    /**
     * Default constructor.
     */
    protected SecurityContext() {}

    /**
     * Enters and returns a new security context instance.
     * 
     * @return the new security context implementation entered. 
     */
    public static SecurityContext enter() {
        return (SecurityContext) SecurityContext.currentSecurityContext().enterInner();
    }

    /**
     * Checks if the specified permission is granted. 
     *
     * @param permission the permission to check.
     * @throws SecurityException if the specified permission is not granted.
     */
    public static void check(Permission<?> permission) {
        if (!SecurityContext.currentSecurityContext().isGranted(permission))
            throw new SecurityException(permission + " is not granted.");
    }

    /**
     * Indicates if the specified permission is granted.
     *
     * @param permission the permission to check.
     */
    public abstract boolean isGranted(Permission<?> permission);

    /**
     * Grants the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public abstract void grant(Permission<?> permission, Object certificate);

    /**
     * Revokes the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public abstract void revoke(Permission<?> permission, Object certificate);

    /**
     * Grants the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public final void grant(Permission<?> permission) {
        grant(permission, null);
    }

    /**
     * Revokes the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public final void revoke(Permission<?> permission) {
        revoke(permission, null);
    }

    /**
     * Returns the current security context. 
     */
    private static SecurityContext currentSecurityContext() {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getSecurityContext();
    }
}
