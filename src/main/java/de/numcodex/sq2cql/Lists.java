package de.numcodex.sq2cql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List utils.
 *
 * @author Alexander Kiel
 */
public interface Lists {

    static <T> List<T> append(Collection<T> a, T b) {
        var newList = new ArrayList<>(a);
        newList.add(b);
        return List.copyOf(newList);
    }

    static <T> List<T> concat(Collection<T> a, Collection<T> b) {
        var newList = new ArrayList<>(a);
        newList.addAll(b);
        return List.copyOf(newList);
    }
}
