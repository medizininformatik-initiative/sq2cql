package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.Lists;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSelector;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.ExistsExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.cql.RetrieveExpression;
import de.numcodex.sq2cql.model.cql.SourceClause;
import de.numcodex.sq2cql.model.cql.WhereClause;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
abstract class AbstractCriterion<T extends AbstractCriterion<T>> implements Criterion {

  private static final IdentifierExpression PATIENT = IdentifierExpression.of("Patient");

  final Concept concept;
  final List<AttributeFilter> attributeFilters;
  final TimeRestriction timeRestriction;

  AbstractCriterion(Concept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction) {
    this.concept = requireNonNull(concept);
    this.attributeFilters = List.copyOf(attributeFilters);
    this.timeRestriction = timeRestriction;
  }

  /**
   * Returns the code selector expression according to the given term code.
   *
   * @param mappingContext the mapping context to determine the code system definition of the
   *                       concept
   * @param termCode       the term code to use
   * @return a {@link Container} of the code selector expression together with its used {@link
   * CodeSystemDefinition}
   */
  static Container<CodeSelector> codeSelector(MappingContext mappingContext, TermCode termCode) {
    var codeSystemDefinition = mappingContext.findCodeSystemDefinition(termCode.system())
        .orElseThrow(() -> new IllegalStateException("code system alias for `%s` not found"
            .formatted(termCode.system())));
    return Container.of(CodeSelector.of(termCode.code(), codeSystemDefinition.name()),
        codeSystemDefinition);
  }

  /**
   * Returns the name of a coding as IdentifierExpression to use as reference
   */
  static IdentifierExpression referenceName(TermCode termCode) {
    return IdentifierExpression.of("%s".formatted(termCode.display() + termCode.code() + "Ref"));
  }

  /**
   * Returns the retrieve expression according to the given term code.
   * <p>
   * Uses the mapping context to determine the resource type of the retrieve expression and the code
   * system definition of the concept.
   *
   * @param mappingContext the mapping context
   * @param termCode       the term code to use
   * @return a {@link Container} of the retrieve expression together with its used {@link
   * CodeSystemDefinition}
   * @throws TranslationException if the {@link RetrieveExpression} can't be build
   */
  static Container<RetrieveExpression> retrieveExpr(MappingContext mappingContext,
      TermCode termCode) {
    var mapping = mappingContext.findMapping(termCode)
        .orElseThrow(() -> new MappingNotFoundException(termCode));
    // TODO if no primary code retrieve all resources
    return codeSelector(mappingContext, mapping.primaryCode()).map(terminology ->
        RetrieveExpression.of(mapping.resourceType(), terminology));
  }

  static Container<BooleanExpression> modifiersExpr(List<Modifier> modifiers,
      MappingContext mappingContext,
      IdentifierExpression identifier) {
    return modifiers.stream()
        .map(m -> m.expression(mappingContext, identifier))
        .reduce(Container.empty(), Container.AND);
  }

  static ExistsExpression existsExpr(SourceClause sourceClause, BooleanExpression whereExpr) {
    return ExistsExpression.of(QueryExpression.of(sourceClause, WhereClause.of(whereExpr)));
  }

  public abstract T appendAttributeFilter(AttributeFilter attributeFilter);

  public Concept getConcept() {
    return concept;
  }

  @Override
  public Container<BooleanExpression> toCql(MappingContext mappingContext) {
    var expr = fullExpr(mappingContext);
    if (expr.isEmpty()) {
      throw new TranslationException("Failed to expand the concept %s.".formatted(concept));
    }
    return expr;
  }

  /**
   * Builds an OR-expression with an expression for each concept of the expansion of {@code
   * termCode}.
   */
  private Container<BooleanExpression> fullExpr(MappingContext mappingContext) {
    return mappingContext.expandConcept(concept)
        .map(termCode -> expr(mappingContext, termCode))
        .reduce(Container.empty(), Container.OR);
  }

  private Container<BooleanExpression> expr(MappingContext mappingContext, TermCode termCode) {
    var mapping = mappingContext.findMapping(termCode)
        .orElseThrow(() -> new MappingNotFoundException(termCode));
    if ("Patient".equals(mapping.resourceType())) {
      return valueAndModifierExpr(mappingContext, mapping, PATIENT);
    } else if ("MedicationAdministration".equals(mapping.resourceType())) {
      var retrieveExpr = RetrieveExpression.of("MedicationAdministration");
      var self = ReferenceCriterion.of(this, termCode);
      var alias = retrieveExpr.alias();
      var sourceClause = SourceClause.of(retrieveExpr, alias);
      var valueAndModifierExpr = self.valueAndModifierExpr(mappingContext, mapping, alias);
      if (valueAndModifierExpr.isEmpty()) {
        return Container.of(ExistsExpression.of(retrieveExpr));
      } else {
        return valueAndModifierExpr.map(expr -> existsExpr(sourceClause, expr));
      }
    } else {
      return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
        var alias = retrieveExpr.alias();
        var sourceClause = SourceClause.of(retrieveExpr, alias);
        var valueAndModifierExpr = valueAndModifierExpr(mappingContext, mapping, alias);
        if (valueAndModifierExpr.isEmpty()) {
          return Container.of(ExistsExpression.of(retrieveExpr));
        } else {
          return valueAndModifierExpr.map(expr -> existsExpr(sourceClause, expr));
        }
      });
    }
  }

  protected Container<BooleanExpression> valueAndModifierExpr(MappingContext mappingContext,
      Mapping mapping,
      IdentifierExpression identifier) {
    var valueExpr = valueExpr(mappingContext, mapping, identifier);
    var modifiers = Lists.concat(mapping.fixedCriteria(),
        resolveAttributeModifiers(mapping.attributeMappings()));
    if (Objects.nonNull(timeRestriction)) {
      modifiers = Lists.concat(modifiers, List.of(timeRestriction.toModifier(mapping)));
    }
    if (modifiers.isEmpty()) {
      return valueExpr;
    } else {
      return Container.AND.apply(valueExpr, modifiersExpr(modifiers, mappingContext, identifier));
    }
  }

  abstract Container<BooleanExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
      IdentifierExpression identifier);

  private List<Modifier> resolveAttributeModifiers(
      Map<TermCode, AttributeMapping> attributeMappings) {
    return attributeFilters.stream().map(attributeFilter -> {
      var key = attributeFilter.attributeCode();
      var mapping = Optional.ofNullable(attributeMappings.get(key)).orElseThrow(() ->
          new MappingNotFoundException(key));
      return attributeFilter.toModifier(mapping);
    }).toList();
  }

  public TimeRestriction timeRestriction() {
    return timeRestriction;
  }

}
