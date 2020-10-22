package org.aksw.jena_sparql_api.rx;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;

public class AggCollection<T, COLLECTION, ITEM>
    implements Aggregator<T, COLLECTION>
{

    protected Supplier<COLLECTION> collectionSupplier;
    protected Function<T, ? extends ITEM> bindingToItem;
    protected BiConsumer<? super COLLECTION, ? super ITEM> addToCollection;

    public AggCollection(
            Supplier<COLLECTION> collector,
            Function<T, ? extends ITEM> bindingToItem,
            BiConsumer<? super COLLECTION, ? super ITEM> addToCollection
            ) {
        super();
        this.collectionSupplier = collector;
        this.bindingToItem = bindingToItem;
        this.addToCollection = addToCollection;
    }

    @Override
    public Accumulator<T, COLLECTION> createAccumulator() {
        COLLECTION collection = collectionSupplier.get();
        return new AccCollection(collection);
    }

    public class AccCollection
        implements Accumulator<T, COLLECTION>
    {
        protected COLLECTION collection;

        public AccCollection(COLLECTION collection) {
            super();
            this.collection = collection;
        }

        @Override
        public void accumulate(T binding) {
            ITEM item = bindingToItem.apply(binding);
            addToCollection.accept(collection, item);
        }

        @Override
        public COLLECTION getValue() {
            return collection;
        }
    }
}
