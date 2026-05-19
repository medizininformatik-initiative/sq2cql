package de.medizininformatikinitiative.cctb.model;


import de.medizininformatikinitiative.cctb.model.structured_query.ContextualTermCode;

import java.util.List;
import java.util.stream.Stream;

public record MappingTreeBase(List<MappingTreeModuleRoot> moduleRoots) {

    public Stream<ContextualTermCode> expand(ContextualTermCode termCode) {
        var key = termCode.termCode().code();

        return moduleRoots.stream().flatMap(moduleRoot ->
                moduleRoot.isModuleMatching(termCode) ? moduleRoot.expand(key) : Stream.empty());
    }
}
