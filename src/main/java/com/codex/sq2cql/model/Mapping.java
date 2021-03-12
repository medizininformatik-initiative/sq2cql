package com.codex.sq2cql.model;

import com.codex.sq2cql.model.common.TermCode;

import java.util.Objects;

/**
 * @author Alexander Kiel
 */
public final class Mapping {

    private final TermCode concept;
    private final String resourceType;

    private Mapping(TermCode concept, String resourceType) {
        this.concept = Objects.requireNonNull(concept);
        this.resourceType = Objects.requireNonNull(resourceType);
    }

    public static Mapping of(TermCode concept, String resourceType) {
        return new Mapping(concept, resourceType);
    }

    public TermCode getConcept() {
        return concept;
    }

    public String getResourceType() {
        return resourceType;
    }
}
