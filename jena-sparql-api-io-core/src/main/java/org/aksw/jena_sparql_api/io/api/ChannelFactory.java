package org.aksw.jena_sparql_api.io.api;

import java.nio.channels.Channel;

public interface ChannelFactory<T extends Channel>
    extends AutoCloseable
{
    T newChannel();
}