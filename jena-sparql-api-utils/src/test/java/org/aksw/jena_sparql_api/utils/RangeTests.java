package org.aksw.jena_sparql_api.utils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;


public class RangeTests {
    @Test
    public void testRangeToLimit() {
        Assert.assertEquals(1l, QueryUtils.rangeToLimit(Range.closed(5l, 5l)));
        Assert.assertEquals(0l, QueryUtils.rangeToLimit(Range.closedOpen(5l, 5l)));
    }

    @Test
    public void testRangeOfZeroLength() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o } LIMIT 0 OFFSET 5");
        Range<Long> range = QueryUtils.toRange(query);
        Assert.assertEquals(Range.closedOpen(5l, 5l), range);
        //System.out.println(QueryUtils.makeClosedOpen(range, DiscreteDomain.longs()));
    }

    @Test
    public void testSubRangeOfZeroLength() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o } LIMIT 10 OFFSET 5");
        Range<Long> range = QueryUtils.toRange(query);
        Assert.assertEquals(Range.closedOpen(5l, 15l), range);

        Range<Long> effectiveRange = QueryUtils.subRange(range, Range.closedOpen(0l, 10l));
        Assert.assertEquals(Range.closedOpen(5l, 15l), effectiveRange);
    }

}
