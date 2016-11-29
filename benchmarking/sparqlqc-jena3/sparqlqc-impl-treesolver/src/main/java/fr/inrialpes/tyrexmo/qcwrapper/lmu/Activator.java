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

        Hashtable<String, String> meta = new Hashtable<>();
        meta.put("SHORT_NAME", "TS");

        context.registerService(SimpleContainmentSolver.class, service, meta);
        context.registerService(LegacyContainmentSolver.class, service, meta);
    }

    public void stop(BundleContext context)
    {
        // NOTE: The service is automatically unregistered.
    }
}
