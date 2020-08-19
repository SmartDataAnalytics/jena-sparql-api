package org.aksw.jena_sparql_api.lookup;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceTransformKey<KI, KO, V>
    implements LookupService<KI, V>
{
    private LookupService<KO, V> delegate;
    private Function<KI, KO> keyMapper;

    public LookupServiceTransformKey(LookupService<KO, V> delegate, Function<KI, KO> keyMapper) {
        super();
        this.delegate = delegate;
        this.keyMapper = keyMapper;
    }

    @Override
    public Flowable<Entry<KI, V>> apply(Iterable<KI> keys) {
        Map<KO, KI> keyMap = new LinkedHashMap<KO, KI>();
        for(KI ki : keys) {
            KO ko = keyMapper.apply(ki);
            keyMap.put(ko, ki);
        }

        Flowable<Entry<KO, V>> tmp = delegate.apply(keyMap.keySet());

        Flowable<Entry<KI, V>> result = tmp.map(entry -> {
            KO ko = entry.getKey();
            V v = entry.getValue();

            boolean isMapped = keyMap.containsKey(ko);
            if(!isMapped) {
                throw new RuntimeException("should not happen");
            }
            KI ki = keyMap.get(ko);
            //result.put(ki, v);

            return new SimpleEntry<>(ki, v);
        });

//		Map<KI, V> result = new LinkedHashMap<KI, V>();
//		for(Entry<KO, V> entry : tmp.entrySet()) {
//			KO ko = entry.getKey();
//			V v = entry.getValue();
//
//			boolean isMapped = keyMap.containsKey(ko);
//			if(!isMapped) {
//				throw new RuntimeException("should not happen");
//			}
//			KI ki = keyMap.get(ko);
//			result.put(ki, v);
//		}
//
        return result;
    }

    public static <KI, KO, V> LookupServiceTransformKey<KI, KO, V> create(LookupService<KO, V> base, Function<KI, KO> keyMapper) {
        LookupServiceTransformKey<KI, KO, V> result = new LookupServiceTransformKey<KI, KO, V>(base, keyMapper);
        return result;
    }
}
