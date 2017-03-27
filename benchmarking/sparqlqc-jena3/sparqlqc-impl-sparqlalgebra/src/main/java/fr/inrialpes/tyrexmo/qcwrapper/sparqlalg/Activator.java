package fr.inrialpes.tyrexmo.qcwrapper.sparqlalg;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.inrialpes.tyrexmo.testqc.LegacyContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public class Activator implements BundleActivator
{
    public void start(BundleContext context)
    {
        SPARQLAlgebraWrapper service = new SPARQLAlgebraWrapper();

        Hashtable<String, String> meta = new Hashtable<>();
        meta.put("SHORT_LABEL", "SA");


        context.registerService(SimpleContainmentSolver.class, service, meta);
        context.registerService(LegacyContainmentSolver.class, service, meta);
    }

    public void stop(BundleContext context)
    {
        // NOTE: The service is automatically unregistered.
    }
}
