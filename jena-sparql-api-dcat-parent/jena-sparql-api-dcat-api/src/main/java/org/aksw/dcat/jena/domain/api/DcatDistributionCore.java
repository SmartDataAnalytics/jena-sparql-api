package org.aksw.dcat.jena.domain.api;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;

public interface DcatDistributionCore
	extends DcatEntityCore
{
	String getFormat();
	void setFormat(String format);

	Set<String> getAccessURLs();
	Set<String> getDownloadURLs();

//	default void setAccessUrls(Collection<String> urls) {
//		replace(getAccessUrls(), urls);
//	}
//
//	default void setDownloadUrls(Collection<String> urls) {
//		replace(getAccessUrls(), urls);
//	}
	
	default String getAccessURL() {
		Set<String> c = getAccessURLs();
		String result = Iterables.getFirst(c, null);
		return result;
	}
	
	default void setAccessURL(String url) {
		Set<String> c = getAccessURLs();
		replace(c, url);
	}

	default String getDownloadURL() {
		Set<String> c = getDownloadURLs();
		String result = Iterables.getFirst(c, null);
		return result;
	}
	
	default void setDownloadURL(String url) {
		Set<String> c = getDownloadURLs();
		replace(c, url);
	}

	public static <T, C extends Collection<T>> C replace(C c, T item) {
		c.clear();
		c.add(item);
		return c;
	}

	public static <T, C extends Collection<T>> C replace(C c, Collection<T> items) {
		c.clear();
		c.addAll(items);
		return c;
	}
}
