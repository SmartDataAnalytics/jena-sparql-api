package org.aksw.jena_sparql_api.rx;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;

/**
 * A subscriber that performs local ordering of the items by their sequence id.
 * Local ordering means, that ordering is accomplished in a streaming fashion
 * without the need of a global view of *all* items.
 * This is the case when items arrive "mostly" sequentially, with some "outliers" arriving out of order,
 * as it can happen e.g. due to network delay.
 * 
 * This implementation uses a 'extractSeqId' lambda to obtain an item's sequence id,
 * and 'incrementSeqId' in order to find out next id to expect.
 * This class then caches all arriving items in memory until
 * an item with the expected id arrives. In this case that item and all consecutive
 * ones are emitted and removed from the cache.
 * 
 * Example Usage:
 * <pre>{@code
 * flowable
 *   .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), Maps::immutableEntry)
 *   .map(...)
 *   .compose(FlowableTransformerLocalOrdering.transformer(0l, i -> i + 1, Entry::getValue))
 * }</pre>
 * 
 * @author raven May 12, 2018
 *
 * @param <T>
 * @param <S>
 */
public class FlowableTransformerLocalOrdering<T, S extends Comparable<S>>
	implements Subscriber<T>
{
	private static final Logger logger = LoggerFactory.getLogger(FlowableTransformerLocalOrdering.class);
	
	protected Emitter<? super T> delegate; //Consumer<? super T> delegate;
	
	protected Function<? super T, ? extends S> extractSeqId;
	protected Function<? super S, ? extends S> incrementSeqId;

	//protected DiscreteDomain<S> discreteDomain;

	protected S expectedSeqId;
	protected boolean isComplete = false;
	
	protected NavigableMap<S, T> seqIdToValue = new TreeMap<>();

	
	public FlowableTransformerLocalOrdering(
			S expectedSeqId,
			Function<? super S, ? extends S> incrementSeqId,
			Function<? super T, ? extends S> extractSeqId,
			Emitter<? super T> delegate) {
		super();
		this.extractSeqId = extractSeqId;
		this.incrementSeqId = incrementSeqId;
		this.expectedSeqId = expectedSeqId;
		this.delegate = delegate;		
	}

	public synchronized void onError(Throwable throwable) {
		delegate.onError(throwable);
	
		//throw new RuntimeException(throwable);
	}
		
	public synchronized void onComplete() {
		isComplete = true;
		
		// If there are no more entries in the map, complete the delegate immediately
		if(seqIdToValue.isEmpty()) {
			delegate.onComplete();
		}
		
		// otherwise, the onNext method has to handle completion
	}

	public synchronized void onNext(T value) {
		S seqId = extractSeqId.apply(value);

		// If complete, the seqId must not be higher than the latest seen one
		if(isComplete) {
			if(seqIdToValue.isEmpty()) {
				onError(new RuntimeException("Sanity check failed: Call to onNext encountered after completion."));
			}

			
			S highestSeqId = seqIdToValue.descendingKeySet().first();
			
			if(Objects.compare(seqId, highestSeqId, Comparator.naturalOrder()) > 0) {
				onError(new RuntimeException("Sequence was marked as complete with id " + highestSeqId + " but a higher id was encountered " + seqId));
			}
		}

		boolean checkForExistingKeys = true;
		if(checkForExistingKeys) {
			if(seqIdToValue.containsKey(seqId)) {
				onError(new RuntimeException("Already seen an item with id " + seqId));
			}
		}

		// Add item to the map
		seqIdToValue.put(seqId, value);
		
		// Consume consecutive items from the map
		Iterator<Entry<S, T>> it = seqIdToValue.entrySet().iterator();
		while(it.hasNext()) {
			Entry<S, T> e = it.next();
			S s = e.getKey();
			T v = e.getValue();
			
			int d = Objects.compare(s, expectedSeqId, Comparator.naturalOrder());
			if(d == 0) {
				it.remove();
				delegate.onNext(v);				
				expectedSeqId = incrementSeqId.apply(expectedSeqId);
				//System.out.println("expecting seq id " + expectedSeqId);
			} else if(d < 0) {
				// Skip values with a lower id
				// TODO Add a flag to emit onError event
				logger.warn("Should not happen: received id " + s + " which is lower than the expected id " + expectedSeqId);
				it.remove();
			} else { // if d > 0
				// Wait for the next sequence id
				logger.trace("Received id " + s + " while waiting for expected id " + expectedSeqId);
				break;
			}			
		}
		
		// If the completion mark was set and all items have been emitted, we are done
		if(isComplete && seqIdToValue.isEmpty()) {
			delegate.onComplete();
		}
	}	

	@Override
	public synchronized void onSubscribe(Subscription s) {
		// TODO Auto-generated method stub		
	}


	public static <T> Subscriber<T> forLong(long initiallyExpectedId, Function<? super T, ? extends Long> extractSeqId, Emitter<? super T> delegate) {
		return new FlowableTransformerLocalOrdering<T, Long>(initiallyExpectedId, id -> Long.valueOf(id.longValue() + 1l), extractSeqId, delegate);
	}
	
	public static <T, S extends Comparable<S>> Subscriber<T> wrap(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, Function<? super T, ? extends S> extractSeqId, Emitter<? super T> delegate) {
		return new FlowableTransformerLocalOrdering<T, S>(initiallyExpectedId, incrementSeqId, extractSeqId, delegate);
	}
	
	public static <T, S extends Comparable<S>> FlowableTransformer<T, T> transformer(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, Function<? super T, ? extends S> extractSeqId) {		
		return upstream -> {
			Flowable<T> result = Flowable.create(new FlowableOnSubscribe<T>() {				
				@Override
				public void subscribe(FlowableEmitter<T> e) throws Exception {
					Subscriber<T> tmp = wrap(initiallyExpectedId, incrementSeqId, extractSeqId, e);
					upstream.subscribe(tmp::onNext, tmp::onError, tmp::onComplete);
				}
			}, BackpressureStrategy.LATEST);
			
			return result;
		};
	}
}

