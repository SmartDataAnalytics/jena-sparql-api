package org.aksw.jena_sparql_api.batch.processor;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateServiceSparql
    implements UpdateService
{
    private UpdateRequest updateRequest;
    private Var conceptVar;
    private UpdateExecutionFactory uef;
    
    @Override
    public UpdateProcessor prepare(Concept filter) {
        
        UpdateRequest modifiedRequest = filterUpdateRequest(updateRequest, conceptVar, filter);
        UpdateProcessor result = uef.createUpdateProcessor(modifiedRequest);

        return result;
    }
    
    public static UpdateRequest filterUpdateRequest(UpdateRequest updateRequest, Var updateVar, Concept concept) {
        //Optimize.optimize(op, context)
        UpdateRequest result = new UpdateRequest();
        for(Update update : updateRequest) {
            Update tmp = filterUpdate(update, updateVar, concept);
            result.add(tmp);
        }
        
        return result;
    }
    
    /**
     * 
     * @param update
     * @param updateVar The varibale of the update request to use for filtering by the given concept
     * @param concept
     * @return
     */
    public static Update filterUpdate(Update update, Var updateVar, Concept concept) {
        Update result;
        if(concept == null || concept.isSubjectConcept()) {
            result = update;
        } else {
            // TODO Rename the filter.var 
            
            UnaryRelation renamed = ConceptUtils.renameVar(concept, updateVar);
            Element renamedElement = renamed.getElement();
            //ElementUtils.
            //renamedElement.
            //PatternVars.vars(element);
            //ElementWalker.
            //NodeTransform
            //renamedElement.
            //NodeTransformRenameMap
            //ElementVisitor
//            ElementWalker.walk(renamedElement, );
            //Transform
            //Rename.
            //ElementWalker
            
            //TRansform
            
            if(update instanceof UpdateDeleteWhere) {
                // TODO Convert update to UpdateModify
                throw new RuntimeException("Sorry, not implemented yet");

            }
            
            if(update instanceof UpdateModify) {
                UpdateModify updateModify = (UpdateModify)update;
                Element e = updateModify.getWherePattern();
                Element replacementElement = ElementUtils.mergeElements(e, renamedElement);

                UpdateModify tmp = cloneUpdateModify(updateModify);
                tmp.setElement(replacementElement);
                result = tmp;
            } else {
                throw new RuntimeException("Sorry, not implemented yet");
            }
        }
        
        return result;
    }
    
    // TODO This clone method should be part of jena
    public static UpdateModify cloneUpdateModify(UpdateModify update) {
        UpdateModify result = new UpdateModify();
        
        result.getUsing().addAll(update.getUsing());
        result.setWithIRI(update.getWithIRI());
        result.setElement(update.getWherePattern());
        
        for(Quad quad : update.getInsertQuads()) {
            result.getInsertAcc().addQuad(quad);
        }
        
        for(Quad quad : update.getDeleteQuads()) {
            result.getDeleteAcc().addQuad(quad);
        }

        return result;
    }
}