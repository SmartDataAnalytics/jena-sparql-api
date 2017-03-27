package org.aksw.jena_sparql_api.core.utils;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.modify.request.UpdateWithUsing;
import org.apache.jena.update.Update;

public class UpdateUtils {

	public static Update clone(Update update) {
		Update result;
		if(update instanceof UpdateDataInsert) {
			result = clone((UpdateDataInsert)update);
		} else if(update instanceof UpdateDataDelete) {
			result = clone((UpdateDataDelete)update);
		} else if(update instanceof UpdateDeleteInsert) {
			result = clone((UpdateDeleteInsert)update);
		} else {
			throw new IllegalArgumentException("Unsupported argument type: " + update.getClass());
		}

		return result;
	}

	public static UpdateDataInsert clone(UpdateDataInsert update) {
		UpdateDataInsert result = new UpdateDataInsert(new QuadDataAcc(update.getQuads()));
		return result;
	}

	public static UpdateDataDelete clone(UpdateDataDelete update) {
		UpdateDataDelete result = new UpdateDataDelete(new QuadDataAcc(update.getQuads()));
		return result;
	}

	public static UpdateDeleteInsert clone(UpdateDeleteInsert update) {
		UpdateDeleteInsert result = new UpdateDeleteInsert();
		result.setElement(update.getWherePattern());
		result.setWithIRI(update.getWithIRI());

		for(Quad quad : update.getDeleteQuads()) {
			result.getDeleteAcc().addQuad(quad);
		}

		for(Quad quad : update.getInsertQuads()) {
			result.getInsertAcc().addQuad(quad);
		}

		for(Node node : update.getUsing()) {
			result.addUsing(node);
		}

		for(Node node : update.getUsingNamed()) {
			result.addUsingNamed(node);
		}

		return result;
	}

//	public static boolean hasWithIri(Update update) {
//		boolean result = update instanceof UpdateWithUsing;
//		return result;
//	}

    public static String getWithIri(Update update) {
        Node with = update instanceof UpdateWithUsing
                ? ((UpdateWithUsing)update).getWithIRI()
                : null;

        String result = with == null ? null : with.toString();

        return result;
    }

//    public static void setWithIri(Update update, Node withIri) {
//    	UpdateWithUsing x = (UpdateWithUsing)update;
//    	x.setWithIRI(withIri);
//    }
    public static void applyWithIriIfApplicable(Update update, String withIri) {
    	Node node = NodeFactory.createURI(withIri);
    	applyWithIriIfApplicable(update, node);
    }

    public static void applyWithIriIfApplicable(Update update, Node withIri) {
    	if(update instanceof UpdateWithUsing) {
    		UpdateWithUsing x = (UpdateWithUsing)update;
    		boolean hasWithIri = x.getWithIRI() != null;
    		if(!hasWithIri) {
    			x.setWithIRI(withIri);
    		}
    	}
    }

    public static boolean applyDatasetDescriptionIfApplicable(Update update, DatasetDescription dg) {
    	boolean result;
    	if(update instanceof UpdateWithUsing) {
    		UpdateWithUsing x = (UpdateWithUsing)update;
    		// We only apply the change if there is no dataset description
    		result = !hasDatasetDescription(x);

    		if(result) {
    			applyDatasetDescription(x, dg);
    		}
    	} else {
    		result = false;
    	}

    	return result;
    }

    public static boolean hasDatasetDescription(UpdateWithUsing update) {
    	boolean result = update.getUsing() != null && !update.getUsing().isEmpty();
    	result = result || update.getUsingNamed() != null && !update.getUsingNamed().isEmpty();

    	return result;
    }

    public static void applyDatasetDescription(UpdateWithUsing update, DatasetDescription dg) {
    	if(dg != null) {
    		List<String> dgus = dg.getDefaultGraphURIs();
    		if(dgus != null) {
    			for(String dgu : dgus) {
    				Node node = NodeFactory.createURI(dgu);
    				update.addUsing(node);
    			}
    		}

    		List<String> ngus = dg.getDefaultGraphURIs();
    		if(ngus != null) {
    			for(String ngu : ngus) {
    				Node node = NodeFactory.createURI(ngu);
    				update.addUsing(node);
    			}
    		}
    	}
    }
}
