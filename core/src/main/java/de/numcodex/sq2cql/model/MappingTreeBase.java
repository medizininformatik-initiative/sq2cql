package de.numcodex.sq2cql.model;


import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;

import java.util.List;
import java.util.stream.Stream;

public record MappingTreeBase(List<MappingTreeModuleRoot> moduleRoots) {

    public Stream<ContextualTermCode> expand(ContextualTermCode termCode) {
        var key = termCode.termCode().code();

        return moduleRoots.stream().flatMap(moduleRoot ->
                moduleRoot.isModuleMatching(termCode) ? moduleRoot.expand(key) : Stream.empty());
    }
}
