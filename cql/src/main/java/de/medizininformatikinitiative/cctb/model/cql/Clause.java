package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

public interface Clause {

    String print(PrintContext printContext);

    Clause withIncrementedSuffixes(Map<String, Integer> increments);
}
