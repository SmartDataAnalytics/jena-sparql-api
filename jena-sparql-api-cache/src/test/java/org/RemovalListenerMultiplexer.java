package org;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;

public class RemovalListenerMultiplexer<K, V>
	implements RemovalListener<K, V>
{
	protected Set<RemovalListener<K, V>> clients = Sets.newIdentityHashSet();

	public RemovalListenerMultiplexer() {
		super();
	}

	public Set<RemovalListener<K, V>> getClients() {
		return clients;
	}

	@Override
	public void onRemoval(RemovalNotification<K, V> notification) {
		List<Exception> exceptions = new ArrayList<>();
		for(RemovalListener<K, V> client : clients) {
			try {
				client.onRemoval(notification);
			} catch(Exception e) {
				exceptions.add(e);
			}
		}

		if(!exceptions.isEmpty()) {
			// TODO Also throw the remaining the exceptions...
			throw new RuntimeException(exceptions.iterator().next());
		}
	}

}
