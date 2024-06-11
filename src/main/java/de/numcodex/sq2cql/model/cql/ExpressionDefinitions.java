package de.numcodex.sq2cql.model.cql;

import java.util.ArrayList;
import java.util.List;

public interface ExpressionDefinitions {

    static List<ExpressionDefinition> appendByUniqueName(List<ExpressionDefinition> a, ExpressionDefinition b) {
        if (a.stream().noneMatch(d -> d.name().equals(b.name()))) {
            var newList = new ArrayList<>(a);
            newList.add(b);
            return List.copyOf(newList);
        }
        return a;
    }

    static List<ExpressionDefinition> unionByName(List<ExpressionDefinition> a, List<ExpressionDefinition> b) {
        var newList = new ArrayList<>(a);
        for (var def : b) {
            if (newList.stream().noneMatch(d -> d.name().equals(def.name()))) {
                newList.add(def);
            }
        }
        return List.copyOf(newList);
    }
}
