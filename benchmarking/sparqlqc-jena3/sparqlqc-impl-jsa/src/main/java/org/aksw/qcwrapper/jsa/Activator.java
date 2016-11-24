package org.aksw.qcwrapper.jsa;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public class Activator implements BundleActivator
{
    public void start(BundleContext context)
    {
        context.registerService(SimpleContainmentSolver.class, new ContainmentSolverWrapperJsaVarMapper(), new Hashtable<>());
    }

    public void stop(BundleContext context)
    {
        // NOTE: The service is automatically unregistered.
    }
}
