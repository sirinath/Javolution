/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import javolution.context.LogContext;
import javolution.osgi.OSGi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Holds minimalist bundle context implementation.
 */
public class BundleContextImpl implements BundleContext {

    OSGi osgi;

    BundleImpl bundle;

    /**
     * Creates a new instance.
     */
    public BundleContextImpl(OSGi osgi, BundleImpl bundle) {
        this.osgi = osgi;
        this.bundle = bundle;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public void addServiceListener(ServiceListener sl, String filter) throws InvalidSyntaxException {
        bundle.serviceListeners.add(sl);
        bundle.serviceListenerFilters.add(filter);
    }

    @Override
    public void removeServiceListener(ServiceListener sl) {
        int i = bundle.serviceListeners.indexOf(sl);
        bundle.serviceListeners.remove(i);
        bundle.serviceListenerFilters.remove(i);
    }

    @Override
    public ServiceRegistration registerService(String serviceName, Object service, Dictionary dctnr) {
        ServiceReferenceImpl serviceReference =
                new ServiceReferenceImpl(bundle, serviceName, service);
        bundle.serviceReferences.add(serviceReference);
        // Fire service event.
        ServiceEvent serviceEvent = new ServiceEvent(ServiceEvent.REGISTERED, serviceReference);
        osgi.fireServiceEvent(serviceEvent);
        return new ServiceRegistrationImpl(serviceReference);
    }

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filterExpression) throws InvalidSyntaxException {
        Filter filter = (filterExpression != null) ? this.createFilter(filterExpression) : null;

        // Searches all bundles.
        ArrayList<ServiceReference> services = new ArrayList<ServiceReference>();
        for (int i = 0; i < osgi.bundles.size(); i++) {
            BundleImpl aBundle =  (BundleImpl) osgi.bundles.get(i);
            for (int j = 0; j < aBundle.serviceReferences.size(); j++) {
                ServiceReferenceImpl serviceReference = aBundle.serviceReferences.get(j);
                if (!serviceReference.serviceName.equals(clazz))
                    continue; // No match.
                if ((filter != null) && (!filter.match(serviceReference)))
                    continue; // No match.
                services.add(serviceReference);
            }
        }

        int count = services.size();
        if (count == 0) return null;
        return services.toArray(new ServiceReference[count]);
    }

    @Override
    public Object getService(ServiceReference sr) {
        ServiceReferenceImpl sri = (ServiceReferenceImpl) sr;
        return sri.service;
    }

    @Override
    public Filter createFilter(String filter) {
        return new FilterImpl(filter);
    }

    @Override
    public boolean ungetService(ServiceReference reference) {
        return false;
    }

    @Override
    public Bundle[] getBundles() {
        return osgi.bundles.toArray(new Bundle[osgi.bundles.size()]);
    }

    @Override
    public ServiceReference getServiceReference(String clazz) {
         try {
              ServiceReference[] refs = getServiceReferences(clazz, null);
              return (refs == null) ? null : refs[0];
         } catch (InvalidSyntaxException e) {
              LogContext.error(e);
         }
         return null;
    }

    // End conversion.
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String getProperty(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Bundle installBundle(String string) throws BundleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Bundle installBundle(String string, InputStream in) throws BundleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Bundle getBundle(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addServiceListener(ServiceListener sl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addBundleListener(BundleListener bl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeBundleListener(BundleListener bl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addFrameworkListener(FrameworkListener fl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFrameworkListener(FrameworkListener fl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceRegistration registerService(String[] strings, Object o, Dictionary dctnr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceReference[] getAllServiceReferences(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getDataFile(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
