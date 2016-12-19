package org.aksw.jena_sparql_api.rdf_stream.processors;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import org.aksw.commons.util.contextual.AbstractDelegated;
import org.aksw.jena_sparql_api.vocabs.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutProcessor<T>
	extends AbstractDelegated<BiConsumer<Resource, T>>
	implements BiConsumer<Resource, T>
{
	//private static final Logger logger = LoggerFactory.getLogger(TimeoutProcessor.class);

	protected CompletionService<?> completionService;

	protected long timeout;
	protected TimeUnit timeoutUnit;

	public TimeoutProcessor(long timeout, TimeUnit timeoutUnit) {
		super();
		this.completionService = new ExecutorCompletionService<>(Executors.newCachedThreadPool());
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	public TimeoutProcessor(CompletionService<?> completionService, long timeout, TimeUnit timeoutUnit) {
		super();
		this.completionService = completionService;
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	@Override
	public void accept(Resource task, T t) {
		try {
			int mode = 0;

			if (mode == 0) {
				Runnable tmp = () -> delegate.accept(task, t);
				Future<?> future = completionService.submit(tmp, null);
				future.get(timeout, timeoutUnit);
			} else {
				Thread thread = new Thread(() -> delegate.accept(task, t));
				thread.start();

				Thread.sleep(timeout);

				if (thread.isAlive()) {
					// System.out.println("TIMEOUT - Forcefully killing
					// thread");
					thread.stop();
					throw new TimeoutException();
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			task.addProperty(LSQ.processingError, "Timeout");
		}
	}
}
