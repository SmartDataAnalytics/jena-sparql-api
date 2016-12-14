package org.aksw.jena_sparql_api.cache.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.util.RewriteUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


public class RewriteUtilsTest {

	protected Multimap<String, String> reductions;

	@Before
	public void init() {
		reductions = LinkedHashMultimap.create();

		reductions.put("a", "b");
		reductions.put("b", "c");
		reductions.put("c", "d");
		reductions.put("a", "z");
	}

	@Test
	public void testReductionsGreedySimple() {
		Assert.assertEquals(RewriteUtils.greedyRewrite("a", (x) -> reductions.get(x).stream()).collect(Collectors.toSet()), new HashSet<>(Arrays.asList("d")));
	}

	@Test
	public void testReductionsExhaustiveSimple() {
		Assert.assertEquals(RewriteUtils.exhaustiveRewrite("a", (x) -> reductions.get(x).stream()).collect(Collectors.toSet()), new HashSet<>(Arrays.asList("d", "z")));
	}
}
