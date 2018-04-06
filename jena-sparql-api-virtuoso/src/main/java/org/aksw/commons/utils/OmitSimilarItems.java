package org.aksw.commons.utils;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

public class OmitSimilarItems<T>
    implements Consumer<T>
{
    protected BiPredicate<? super T, ? super T> isTooSimilar;

    protected Consumer<? super T> itemDelegate;
    protected Consumer<Long> skipCountDelegate;

    // TODO Turn these into Optionals
    protected T firstDistinguishedItem = null;
    protected T recentlySkippedItem = null;
    protected long numSkippedItems = 0;

    public OmitSimilarItems(Consumer<? super T> itemDelegate, Consumer<Long> skipCountDelegate, BiPredicate<? super T, ? super T> isTooSimilar) {
        super();
        this.itemDelegate = itemDelegate;
        this.skipCountDelegate = skipCountDelegate;
        this.isTooSimilar = isTooSimilar;
    }


    @Override
    public void accept(T item) {

        boolean skip = isTooSimilar.test(firstDistinguishedItem, item);
        if(skip) {
            recentlySkippedItem = item;
            ++numSkippedItems;
        } else {

            if(numSkippedItems > 0) { // implies recentlySkippedItem != null
                boolean recentlySkippedItemDiffersFromCurrentOne = !isTooSimilar.test(recentlySkippedItem, item);
                if(recentlySkippedItemDiffersFromCurrentOne) {
                    // We are going to pass on the prior item after all, so decrement the skip count
                    --numSkippedItems;
                }

                if(numSkippedItems > 0) {
                    skipCountDelegate.accept(numSkippedItems);
                }

                // Send out the prior item if it differs significantly from the current one
                if(recentlySkippedItemDiffersFromCurrentOne) {
                    itemDelegate.accept(recentlySkippedItem);
                }
            }

            itemDelegate.accept(item);
            firstDistinguishedItem = item;

            recentlySkippedItem = null;
            numSkippedItems = 0;
        }
    }

//    public static int tmp(String a, String b) {
//        int result = StringUtils.getLevenshteinDistance(a, b);
//        System.out.println("  | " + a);
//        System.out.println("  | " + b);
//        System.out.println("  | " + "---------------------------------------");
//        System.out.println("  | " + result);
//        return result;
//
//    }
    public static Consumer<String> forStrings(int maxLevenshteinDistance, Consumer<String> delegate) {
        BiPredicate<String, String> predicate =
            (a, b) -> a == null || b == null
                ? false
                : StringUtils.getLevenshteinDistance(a, b) <= maxLevenshteinDistance;

        Consumer<String> result = new OmitSimilarItems<>(
            delegate,
            (itemSkipCount) -> delegate.accept("  ... " + itemSkipCount + " similar lines omitted ..."),
            predicate
        );

        return result;
    }
}
