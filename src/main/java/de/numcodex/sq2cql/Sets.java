package de.numcodex.sq2cql;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Set utils.
 *
 * @author Alexander Kiel
 */
public interface Sets {

    static <T> Set<T> union(Collection<T> a, Collection<T> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toUnmodifiableSet());
    }
}
