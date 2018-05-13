package org.aksw.commons.service.core;

import java.util.function.Consumer;

import com.google.common.util.concurrent.Service;

/**
 * A system service is a service based on an underlying system process.
 * It enables redirecting the process output,
 * such as to System.out::println or logger::debug.
 *
 * API may be extended to cater for handling stderr and stdout separately
 *
 * @author Claus Stadler
 *
 */
public interface SystemService
    extends Service
{
    /**
     * Sets the target for both stderr and stdout
     *
     * @param sink
     * @return
     */
    SystemService setOutputSink(Consumer<String> sink);
}
