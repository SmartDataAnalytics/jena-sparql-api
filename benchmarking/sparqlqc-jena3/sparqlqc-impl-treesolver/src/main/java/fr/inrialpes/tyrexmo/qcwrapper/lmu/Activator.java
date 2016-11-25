package fr.inrialpes.tyrexmo.qcwrapper.lmu;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.inrialpes.tyrexmo.testqc.LegacyContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public class Activator implements BundleActivator
{
    public void start(BundleContext context)
    {
        TreeSolverWrapper service = new TreeSolverWrapper();
        context.registerService(SimpleContainmentSolver.class, service, new Hashtable<>());
        context.registerService(LegacyContainmentSolver.class, service, new Hashtable<>());
    }

    public void stop(BundleContext context)
    {
        // NOTE: The service is automatically unregistered.
    }
}
