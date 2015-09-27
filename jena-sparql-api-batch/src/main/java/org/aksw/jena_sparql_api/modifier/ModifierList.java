package org.aksw.jena_sparql_api.modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModifierList<T>
    implements Modifier<T>
{
    private List<Modifier<? super T>> modifiers;

    @SafeVarargs
    public ModifierList(Modifier<? super T> ... modifiers) {
        this.modifiers = Arrays.asList(modifiers);
    }

    public ModifierList(List<Modifier<? super T>> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void apply(T item) {
        for(Modifier<? super T> modifier : modifiers) {
            modifier.apply(item);
        }
    }

    @SafeVarargs
    public static <T> ModifierList<T> create(Modifier<T> ... modifiers) {
        ModifierList<T> result = create(Arrays.asList(modifiers));
        return result;
    }

    public static <T> ModifierList<T> create(List<? extends Modifier<? super T>> modifiers) {
        List<Modifier<? super T>> tmps = new ArrayList<Modifier<? super T>>(modifiers.size());
        for(Modifier<? super T> modifier : modifiers) {
            tmps.add(modifier);
        }

        ModifierList<T> result = new ModifierList<T>(tmps);
        return result;
    }
}
