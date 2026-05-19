package de.numcodex.sq2cql;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Set utils.
 *
 * @author Alexander Kiel
 */
public interface Sets {

    static <T> Set<T> append(Collection<T> a, T b) {
        var newList = new ArrayList<>(a);
        newList.add(b);
        return Set.copyOf(newList);
    }

    static <T> Set<T> union(Collection<T> a, Collection<T> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toUnmodifiableSet());
    }
}
