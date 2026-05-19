package de.numcodex.sq2cql;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

public interface Maps {

    static <K, V> BinaryOperator<Map<K, V>> merge(BinaryOperator<V> valueMerger) {
        return (a, b) -> {
            var r = new HashMap<>(a);
            b.forEach((k, v) -> r.merge(k, v, valueMerger));
            return Map.copyOf(r);
        };
    }
}
