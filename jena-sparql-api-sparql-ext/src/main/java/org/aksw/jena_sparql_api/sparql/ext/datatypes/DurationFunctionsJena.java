package org.aksw.jena_sparql_api.sparql.ext.datatypes;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDuration;

import java.time.Duration;

public class DurationFunctionsJena {

	static final int HOURS_PER_DAY = 24;
	static final int MINUTES_PER_HOUR = 60;
	static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
	static final int SECONDS_PER_MINUTE = 60;
	static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
	static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L;
	static final long MICROS_PER_DAY = SECONDS_PER_DAY * 1000_000L;
	static final long NANOS_PER_MILLI = 1000_000L;
	static final long NANOS_PER_SECOND =  1000_000_000L;
	static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;
	static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR;
	static final long NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY;
	static final long DAYS_PER_WEEK = 7;

	@IriNs(JenaExtensionDuration.NS)
	public static long asWeeks(XSDDuration dur) {
		return asDays(dur) / DAYS_PER_WEEK;
	}

	@IriNs(JenaExtensionDuration.NS)
	public static long asDays(XSDDuration dur) {
		return asHours(dur) / HOURS_PER_DAY;
	}

	@IriNs(JenaExtensionDuration.NS)
	public static long asHours(XSDDuration dur) {
		return asMinutes(dur) / MINUTES_PER_HOUR;
	}

	@IriNs(JenaExtensionDuration.NS)
	public static long asMinutes(XSDDuration dur) {
		return asSeconds(dur) / SECONDS_PER_MINUTE;
	}

	@IriNs(JenaExtensionDuration.NS)
	public static long asSeconds(XSDDuration dur) {
		return dur.getFullSeconds()
				+ dur.getMinutes() * SECONDS_PER_MINUTE
				+ dur.getHours() * SECONDS_PER_HOUR
				+ dur.getDays() * SECONDS_PER_DAY;
	}

	@IriNs(JenaExtensionDuration.NS)
	public static XSDDuration simplify(XSDDuration dur) {
		long seconds = asSeconds(dur);
		Duration d = Duration.ofSeconds(seconds);

		return (XSDDuration) XSDDatatype.XSDduration.parseValidated(d.toString());
	}

}
