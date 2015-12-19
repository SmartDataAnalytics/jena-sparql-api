package org.aksw.jena_sparql_api.batch.step;


import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import com.google.common.base.Predicate;
import com.hp.hpl.jena.sparql.core.Quad;

public class ValidatorQuadByPredicate
    implements Validator<Quad>
{
    protected Predicate<? super Quad> predicate;

    public ValidatorQuadByPredicate(Predicate<? super Quad> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void validate(Quad quad) throws ValidationException {
        boolean isValid = predicate.apply(quad);
        if(!isValid) {
            throw new ValidationException("A quad failed validation: " + quad);
        }
    }

}
