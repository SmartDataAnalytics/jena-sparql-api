package org.aksw.jena_sparql_api.rdf_stream.processors;

import java.io.StringWriter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.aksw.commons.util.function.TriConsumer;
import org.aksw.jena_sparql_api.vocabs.LSQ;
import org.aksw.jena_sparql_api.vocabs.OWLTIME;
import org.aksw.jena_sparql_api.vocabs.PROV;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * A dispatcher reads tasks from a source and passes them on to an executor.
 * Thereby, the time of the task execution is measured. Implements Runnable so
 * it can be easily run in a thread.
 *
 * A lambda is used to construct an IRI string for the task execution from the
 * task description. Furthermore, a function can be provided, that turns a task
 * description into a Java entity that gets passed to the executor. This
 * conversion does not count towards the execution.
 *
 * @author raven
 *
 * @param <T>
 *            The type of the task object
 * @param <R>
 *            The type of the task result
 */
public class PerformanceEvaluator<T>
	implements BiConsumer<Resource, T>
{
	private static final Logger logger = LoggerFactory.getLogger(PerformanceEvaluator.class);

	protected BiConsumer<Resource, T> taskExecutor;
	protected TriConsumer<Resource, T, Exception> exceptionHandler;
	protected BiConsumer<Resource, T> reportConsumer;

	public PerformanceEvaluator(BiConsumer<Resource, T> taskExecutor) {
		this(taskExecutor, (r, t, e) -> {}, (r, t) -> {});
	}

	public PerformanceEvaluator(BiConsumer<Resource, T> taskExecutor, TriConsumer<Resource, T, Exception> exceptionHandler,
			BiConsumer<Resource, T> reportConsumer) {
		super();
		this.taskExecutor = taskExecutor;
		this.exceptionHandler = exceptionHandler;
		this.reportConsumer = reportConsumer;
	}

	public BiConsumer<Resource, T> getTaskExecutor() {
		return taskExecutor;
	}

	public PerformanceEvaluator<T> setTaskExector(BiConsumer<Resource, T> taskExecutor) {
		this.taskExecutor = taskExecutor;
		return this;
	}

	public TriConsumer<Resource, T, Exception> getExceptionHandler() {
		return exceptionHandler;
	}

	public PerformanceEvaluator<T> setExceptionHandler(TriConsumer<Resource, T, Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}

	public BiConsumer<Resource, T> getReportConsumer() {
		return reportConsumer;
	}

	public PerformanceEvaluator<T> setReportConsumer(BiConsumer<Resource, T> reportConsumer) {
		this.reportConsumer = reportConsumer;
		return this;
	}

	public static Logger getLogger() {
		return logger;
	}

	@Override
	public void accept(Resource r, T t) {

		if (r.getProperty(OWLTIME.numericDuration) != null) {
			StringWriter tmp = new StringWriter();
			ResourceUtils.reachableClosure(r).write(tmp, "TTL");
			throw new RuntimeException("Resource " + r + " already has a numeric duration assigned: " + tmp);
		}

		Calendar startInstant = new GregorianCalendar();

		r.addLiteral(PROV.startedAtTime, startInstant);

		Stopwatch sw = Stopwatch.createStarted();

		try {
			taskExecutor.accept(r, t);
		} catch (Exception e) {
			// ex = e;
			logger.warn("Reporting failed task execution", e);
			r.addLiteral(LSQ.executionError, "" + e);

			exceptionHandler.accept(r, t, e);
		}

		sw.stop();
		Calendar stopInstant = new GregorianCalendar();
		Duration duration = Duration.ofNanos(sw.elapsed(TimeUnit.NANOSECONDS));

		r.addLiteral(PROV.endAtTime, stopInstant);
		r.addLiteral(OWLTIME.numericDuration, duration.get(ChronoUnit.NANOS) / 1000000000.0);

		try {
			reportConsumer.accept(r, t);
		} catch (Exception e) {
			logger.error("Failed to send report to consumer", e);
			throw new RuntimeException(e);
		}
	}

}
