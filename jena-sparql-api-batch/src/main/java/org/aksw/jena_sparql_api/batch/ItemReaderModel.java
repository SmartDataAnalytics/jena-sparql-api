package org.aksw.jena_sparql_api.batch;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Item reader that reads a SPARQL SELECT query using pagination
 *
 * @author raven
 *
 * @param <T>
 */
public class ItemReaderModel
    extends AbstractPaginatedDataItemReader<Entry<Resource, Model>>
{
    private QueryExecutionFactory qef;

    private Concept concept;
    private ListService<Concept, Entry<Resource, Model>> listService;


    public ItemReaderModel() {
        setName(this.getClass().getName());
    }
    @Override
    protected Iterator<Entry<Resource, Model>> doPageRead() {

        ListServiceConcept ls = new ListServiceConcept(qef);
        List<Node> nodes = ls.fetchData(concept, (long)this.pageSize, (long)page);

        /*
        Map<Resource, Model> chunk = new HashMap<Resource, Model>();
        for(Node node : nodes) {
            Model model = ModelFactory.createDefaultModel();
            RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(node, model);
            Resource r = (Resource)rdfNode;
            chunk.put(r, model);
        }
        */
        long limit = (long)this.pageSize;
        long offset = this.page * this.pageSize;

        List<Entry<Resource, Model>> chunk = listService.fetchData(concept, limit, offset);
        Iterator<Entry<Resource, Model>> result = chunk.iterator();
        return result;
    }
}


//
//for(Entry<Resource, Model> entry : chunk.entrySet()) {
//
//}


/*
if (results == null) {
  results = new CopyOnWriteArrayList<T>();
}
else {
  results.clear();
}
*/

//PagingQuery pagingQuery = new PagingQuery(this.pageSize, this.query);
//Iterator<Query> itQuery = pagingQuery.createQueryIterator(this.page * this.pageSize);
//
//Query query = itQuery.next();
//
//if(query == null) {
//  Collection<T> tmp = Collections.emptyList();
//  return tmp.iterator();
//}
//
////Query query = queryIterator.next();
//QueryExecution qe = qef.createQueryExecution(query);
//ResultSet rs = qe.execSelect();
//
//
//List<T> items = new ArrayList<T>();
//long rowId = 0;
//while(rs.hasNext()) {
//  ++rowId;
//  Binding binding = rs.nextBinding();
//
//  T item = bindingMapper.map(binding, rowId);
//
//  items.add(item);
//  //results.add(item);
//}
//
//return items.iterator();
//return null;