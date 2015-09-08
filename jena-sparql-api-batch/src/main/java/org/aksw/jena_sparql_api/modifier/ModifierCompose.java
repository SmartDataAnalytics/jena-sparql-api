package org.aksw.jena_sparql_api.modifier;

import java.util.Arrays;
import java.util.List;

public class ModifierCompose<T>
    implements Modifier<T>
{
    private List<Modifier<T>> modifiers;

    @SafeVarargs
    public ModifierCompose(Modifier<T> ... modifiers) {
        this.modifiers = Arrays.asList(modifiers);
    }

    public ModifierCompose(List<Modifier<T>> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void apply(T item) {
        for(Modifier<T> modifier : modifiers) {
            modifier.apply(item);
        }
    }

    @SafeVarargs
    public static <T> ModifierCompose<T> create(Modifier<T> ... modifiers) {
        ModifierCompose<T> result = create(Arrays.asList(modifiers));
        return result;
    }

    public static <T> ModifierCompose<T> create(List<Modifier<T>> modifiers) {
        ModifierCompose<T> result = new ModifierCompose<T>(modifiers);
        return result;
    }
}
