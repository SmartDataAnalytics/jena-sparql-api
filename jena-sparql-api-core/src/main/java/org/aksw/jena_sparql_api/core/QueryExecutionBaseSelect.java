package org.aksw.jena_sparql_api.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.jena_sparql_api.utils.CloseableQueryExecution;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class IteratorWrapperClose<T>
    extends SinglePrefetchIterator<T>
{
    private Iterator<T> it;

    public IteratorWrapperClose(Iterator<T> it) {
        this.it = it;
    }

    @Override
    protected T prefetch() throws Exception {
        if(!it.hasNext()) {
            return finish();
        } else {
            T result = it.next();
            return result;
        }
    }
}

class TestQueryExecutionBaseSelect
    extends QueryExecutionBaseSelect
{

    public TestQueryExecutionBaseSelect(Query query) {
        super(query, null);
    }

    @Override
    protected QueryExecution executeCoreSelectX(Query query) {
        System.out.println("Got a query string: " + query);
        return null;
    }

    public static void main(String[] args) {
        Query query = QueryFactory.create("Describe ?x <http://aaaa> {?x a <http://blah> .}");
        query = QueryFactory.create("Describe <http://aaaa>");
        query = QueryFactory.create("Describe");
        TestQueryExecutionBaseSelect x = new TestQueryExecutionBaseSelect(query);

        x.execDescribe();

    }
}


/**
 * A Sparqler-class that implements ask, describe, and construct
 * based on the executeCoreSelect(Query) method.
 *
 * Also, works on String and Query level.
 *
 * Some of the code has been taken from
 * org.apache.jena.sparql.engine.QueryExecutionBase, which is a
 * class with a similar purpose but not as reusable as this one
 * (This class reduces all operations to a single executeCoreSelect call)
 *
 * NOTE: executeCoreSelect will close this query execution once the ResultSet is consumed.
 *
 * @author raven
 *
 */
public abstract class QueryExecutionBaseSelect
        extends QueryExecutionDecorator
        implements QueryExecution
{
    private static final Logger logger = LoggerFactory
            .getLogger(QueryExecutionBaseSelect.class);

    protected Query query;


    // Describe queries are sent as multiple individual queries, therefore we require a
    // back reference to the corresponding QueryExecutionFactory
    protected QueryExecutionFactory parentFactory;


    // TODO Move these two utility methods to a utility class
    // Either the whole Sparql API should go to the jena module
    // or it needs a dependency on that module...
    public static Model createModel(Iterator<Triple> it) {
        return createModel(ModelFactory.createDefaultModel(), it);
    }

    public static Model createModel(Model result, Iterator<Triple> it) {

        while(it.hasNext()) {
            Triple t = it.next();
            Statement stmt = org.apache.jena.sparql.util.ModelUtils.tripleToStatement(result, t);
            if (stmt != null) {
                result.add(stmt);
            }
        }

        return result;
    }



    public QueryExecutionBaseSelect(Query query, QueryExecutionFactory subFactory) {
        super(null);
        this.query = query;
        this.parentFactory = subFactory;
    }

    //private QueryExecution running = null;

    abstract protected QueryExecution executeCoreSelectX(Query query);

    protected ResultSetCloseable executeCoreSelect(Query query) {
        if(this.decoratee != null) {
            throw new RuntimeException("A query is already running");
        }

        this.decoratee = executeCoreSelectX(query);

        if(this.decoratee == null) {
            throw new RuntimeException("Failed to obtain a QueryExecution for query: " + query);
        }

        //return decoratee.execSelect();

        ResultSet tmp = decoratee.execSelect();
        final QueryExecution self = this;
        ResultSetCloseable result = new ResultSetCloseable(tmp, new CloseableQueryExecution(self));

        return result;


    }

// Note: The super class already closes the decoratee
//    @Override
//    public void close() {
//        decoratee.close();
//    }

    @Override
    public boolean execAsk() {
        if (!query.isAskType()) {
            throw new RuntimeException("ASK query expected. Got: ["
                    + query.toString() + "]");
        }

        Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
        selectQuery.setLimit(1);

        ResultSet rs = executeCoreSelect(selectQuery);

        long rowCount = 0;
        while(rs.hasNext()) {
            rs.next();
            ++rowCount;
        }

        if (rowCount > 1) {
            logger.warn("Received " + rowCount + " rows for the query ["
                    + query.toString() + "]");
        }

        return rowCount > 0;
    }

    @Override
    public Model execDescribe() {
        Model model = ModelFactory.createDefaultModel();
        return execDescribe(model);
    }


    public static Node extractDescribeNode(Query query) {
        if (!query.isDescribeType()) {
            throw new RuntimeException("DESCRIBE query expected. Got: ["
                    + query.toString() + "]");
        }

        // TODO Right now we only support describe with a single constant.

        //Element queryPattern = query.getQueryPattern();
        if(query.getQueryPattern() != null || !query.getResultVars().isEmpty() || query.getResultURIs().size() > 1) {
            throw new RuntimeException("Sorry, DESCRIBE is only implemented for a single resource argument");
        }

        Node result = query.getResultURIs().get(0);

        return result;
    }


    /**
     * We use this query execution for retrieving the result set of the
     * where clause, but we neet the subFactory to describe the individual
     * resources then.
     *
     * @return
     */
    @Override
    public Iterator<Triple> execDescribeTriples() {


        ResultSetCloseable rs = null;
        if ( query.getQueryPattern() != null ) {
            Query q = new Query();
            q.setQuerySelectType();
            q.setResultVars();
            for(String v : query.getResultVars()) {
                q.addResultVar(v);
            }
            q.setQueryPattern(query.getQueryPattern());

            rs = this.executeCoreSelect(q);
        }

        // Note: We need to close the connection when we are done

        Describer tmp = Describer.create(query.getResultURIs(), query.getResultVars(), rs, parentFactory);


        final QueryExecution self = this;

        Iterator<Triple> result = new IteratorWrapperClose<Triple>(tmp) {
            @Override
            public void close() {
                self.close();
            }
        };

        return result;
    }

    /**
     * A describe query is translated into a construct query.
     *
     *
     *
     * Lets see...
     * Describe ?a ?b ... &lt;x&gt;&lt;y&gt; Where Pattern { ... } becomes ...?
     *
     * Construct { ?a ?ap ?ao . ?b ?bp ?bo . } Where Pattern {  } Union {}
     * Ah, lets just query every resource individually for now
     *
     *
     * TODO Add support for concise bounded descriptions...
     *
     * @param result
     * @return
     */
    @Override
    public Model execDescribe(Model result) {
        createModel(result, execDescribeTriples());
        return result;


        /*
        Generator generator = Gensym.create("xx_generated_var_");

        Element queryPattern = query.getQueryPattern();
        ElementPathBlock pathBlock;

        if(queryPattern == null) {
            ElementGroup elementGroup = new ElementGroup();

            pathBlock = new ElementPathBlock();
            elementGroup.addElement(pathBlock);
        } else {

            ElementGroup elementGroup = (ElementGroup)queryPattern;

            pathBlock = (ElementPathBlock)elementGroup.getElements().get(0);
        }

        //Template template = new Template();
        //template.

        BasicPattern basicPattern = new BasicPattern();

        System.out.println(queryPattern.getClass());

        for(Node node : query.getResultURIs()) {
            Var p = Var.alloc(generator.next());
            Var o = Var.alloc(generator.next());

            Triple triple = new Triple(node, p, o);

            basicPattern.add();
            //queryPattern.
        }

        for(String var : query.getResultVars()) {

        }


        Template template = new Template(basicPattern);


        Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());

        ResultSet rs = executeCoreSelect(selectQuery);


        //throw new RuntimeException("Sorry, DESCRIBE is not implemted yet.");
        */
    }

    private Iterator<Triple> executeConstructStreaming(Query query) {
        if (!query.isConstructType()) {
            throw new RuntimeException("CONSTRUCT query expected. Got: ["
                    + query.toString() + "]");
        }

        Template template = query.getConstructTemplate();
        Set<Var> projectVars = QuadPatternUtils.getVarsMentioned(template.getQuads());
        
        Query clone = query.cloneQuery();
        clone.setQuerySelectType();

        //Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
        
    	clone.getProject().clear();
    	if(projectVars.isEmpty()) {
        	// If the template is variable free then project the first variable of the query pattern
    		// If the query pattern is variable free then just use the result star
        	Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(query.getQueryPattern()));
        	if(patternVars.isEmpty()) {
        		clone.setQueryResultStar(true);
        	} else {
        		Var v = patternVars.iterator().next();
            	clone.setQueryResultStar(false);
            	clone.getProject().add(v);        		
        	}
        } else {
        	clone.setQueryResultStar(false);
        	clone.addProjectVars(projectVars);
        }

        ResultSetCloseable rs = executeCoreSelect(clone);

        //System.out.println("Executing query as: " + clone);

        // insertPrefixesInto(result) ;

        Iterator<Triple> result = new ConstructIterator(template, rs);
        return result;
    }

    private Model executeConstruct(Query query, Model result) {
        createModel(result, executeConstructStreaming(query));
        return result;
    }

    @Override
    public Model execConstruct(Model result) {
        return executeConstruct(this.query, result);
    }

    @Override
    public Model execConstruct() {
        Model result = ModelFactory.createDefaultModel();
        execConstruct(result);
        return result;
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        return executeConstructStreaming(this.query);
    }

    @Override
    public ResultSet execSelect() {
        if (!query.isSelectType()) {
            throw new RuntimeException("SELECT query expected. Got: ["
                    + query.toString() + "]");
        }

        return executeCoreSelect(query);
    }

    @Override
    public Query getQuery() {
        return query;
    }

    //@Override
    public void executeUpdate(UpdateRequest updateRequest)
    {
        throw new RuntimeException("Not implemented");
    }
    
	@Override
	public JsonArray execJson() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<JsonObject> execJsonItems() {
		throw new UnsupportedOperationException();
	}
}
