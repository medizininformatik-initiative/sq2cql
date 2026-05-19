package de.medizininformatikinitiative.cctb.model.cql;

import java.util.Map;

public interface QueryInclusionClause extends Clause {

    @Override
    QueryInclusionClause withIncrementedSuffixes(Map<String, Integer> increments);
}
