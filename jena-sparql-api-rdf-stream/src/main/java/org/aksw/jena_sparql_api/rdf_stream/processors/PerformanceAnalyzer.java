package org.aksw.jena_sparql_api.rdf_stream.processors;

import java.io.StringWriter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.vocabs.OWLTIME;
import org.aksw.jena_sparql_api.vocabs.PROV;
import org.aksw.simba.lsq.vocab.LSQ;
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
public class PerformanceAnalyzer
//	implements BiConsumer<Resource, Runnable>
{
	private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalyzer.class);

	protected BiConsumer<Resource, Exception> exceptionHandler;
	protected Consumer<Resource> reportConsumer;

	public PerformanceAnalyzer() {
		this((r, e) -> {}, (r) -> {});
	}

	public PerformanceAnalyzer(BiConsumer<Resource, Exception> exceptionHandler,
			Consumer<Resource> reportConsumer) {
		super();
		this.exceptionHandler = exceptionHandler;
		this.reportConsumer = reportConsumer;
	}

	public BiConsumer<Resource, Exception> getExceptionHandler() {
		return exceptionHandler;
	}

	public PerformanceAnalyzer setExceptionHandler(BiConsumer<Resource, Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}

	public Consumer<Resource> getReportConsumer() {
		return reportConsumer;
	}

	public PerformanceAnalyzer setReportConsumer(Consumer<Resource> reportConsumer) {
		this.reportConsumer = reportConsumer;
		return this;
	}

	public static Logger getLogger() {
		return logger;
	}

//	@Override
//	public void accept(Resource r, Runnable t) {
//	}

	public static PerformanceAnalyzer start() {
		return new PerformanceAnalyzer();
	}

//	public static void analyze(Resource r, Runnable t) {
//		start().create().accept(r, t);
//	}

	public static void analyze(Resource r, RunnableWithException t) {
		Runnable wrapper = () -> { try {
			t.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} };

		start().create().accept(r, wrapper);
	}

	public BiConsumer<Resource, Runnable> create() {
		BiConsumer<Resource, Runnable> result = (r, t) -> {
			if (r.getProperty(OWLTIME.numericDuration) != null) {
				StringWriter tmp = new StringWriter();
				ResourceUtils.reachableClosure(r).write(tmp, "TTL");
				throw new RuntimeException("Resource " + r + " already has a numeric duration assigned: " + tmp);
			}

			Calendar startInstant = new GregorianCalendar();

			r.addLiteral(PROV.startedAtTime, startInstant);

			Stopwatch sw = Stopwatch.createStarted();

			try {
				t.run();
			} catch (Exception e) {
				// ex = e;
				logger.warn("Reporting failed task execution", e);
				r.addLiteral(LSQ.executionError, "" + e);

				exceptionHandler.accept(r, e);
			}

			sw.stop();
			Calendar stopInstant = new GregorianCalendar();
			Duration duration = Duration.ofNanos(sw.elapsed(TimeUnit.NANOSECONDS));

			r.addLiteral(PROV.endAtTime, stopInstant);
			r.addLiteral(OWLTIME.numericDuration, duration.get(ChronoUnit.NANOS) / 1000000000.0);

			try {
				reportConsumer.accept(r);
			} catch (Exception e) {
				logger.error("Failed to send report to consumer", e);
				throw new RuntimeException(e);
			}
		};

		return result;
	}

}
