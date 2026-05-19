package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

public interface Clause {

    String print(PrintContext printContext);

    Clause withIncrementedSuffixes(Map<String, Integer> increments);
}
