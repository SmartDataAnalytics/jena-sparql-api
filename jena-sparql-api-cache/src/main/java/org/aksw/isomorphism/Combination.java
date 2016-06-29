package org.aksw.isomorphism;

import java.util.Stack;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
     * @author raven
     *
     * @param <A>
     * @param <B>
     * @param <S>
     */
    public class Combination<A, B, S>
        extends SimpleEntry<Stack<Entry<A, B>>, S>
    {
        private static final long serialVersionUID = 1L;

        public Combination(Stack<Entry<A, B>> key, S value) {
            super(key, value);
        }
//        public Combination(Entry<? extends Stack<Entry<A, B>>, ? extends S> entry) {
//            super(entry);
//        }
    }