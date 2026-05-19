# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`cctb` is a Java library that translates **Structured Query (SQ / CCDL)** — a JSON-based clinical query format used by 
the FDPG (Forschungsdatenportal für Gesundheit) — into other query languages (such as **HL7 CQL \[Clinical Quality 
Language\]**) for retrieval of clinical data.

## Maven Multi-Module Layout

```
cctb/            ← parent POM (de.medizininformatik-initiative:cctb)
  cql/           ← library module for CQL translation (artifact: cctb-cql), published to GitHub Packages
  cli/           ← CLI module (artifact: cctb-cli), released as a fat JAR
```

- **cql** is the deployable library; `maven.deploy.skip=false` is set explicitly there.
- **cli** wraps project logic with a CLI tool, bundles all dependencies via `maven-assembly-plugin`, and exposes `de.medizininformatikinitiative.cctb.cli.Main` as the entry point.

## Build & Test Commands

```bash
# Compile
mvn compile

# Run unit tests
mvn test

# Run all unit + integration tests
mvn verify

# Build only the CLI fat JAR
mvn -pl cli package
```

Java 17 is required. CI uses Temurin 17.

## Architecture

### Translation Pipeline

```
StructuredQuery (JSON) → Criterion[] → Container<Expression> → CQL text
```

1. **`StructuredQuery`** — top-level input. Contains `inclusionCriteria` (CNF) and `exclusionCriteria` (DNF), each a list-of-lists of `Criterion`.
2. **`Criterion`** (interface, `model.structured_query` package) — deserialized from JSON via a `@JsonCreator` factory on the interface itself. Concrete types: `ConceptCriterion`, `NumericCriterion`, `RangeCriterion`, `ValueSetCriterion`. Each calls `toCql(MappingContext)`.
3. **`MappingContext`** — holds the mapping table (`ContextualTermCode → Mapping`), the concept expansion tree (`MappingTreeBase`), and code-system alias definitions. Passed into every `toCql` call.
4. **`Container<T extends Expression>`** — the central monadic wrapper. Carries an expression together with the `CodeSystemDefinition`s and named `ExpressionDefinition`s it depends on. Combiners (`AND`, `OR`, `AND_NOT`, `UNION`) are static `BinaryOperator` fields on `Container`. Suffix collision resolution happens inside the combiner to guarantee unique CQL identifiers when the same base name appears multiple times.
5. **`Translator`** — stateless entry point. Calls `inclusionExpr` (CNF → fold with `AND`) and `exclusionExpr` (DNF → fold with `OR`), then `moveToPatientContext("InInitialPopulation")` on the result.
6. **`Container.print()`** — serialises the full CQL library (header, code-system declarations, Unfiltered context definitions, Patient context definitions).

### Mapping Data

`Mapping` is deserialized from a JSON array (one entry per term code). Key fields:
- `resourceType` — FHIR resource (e.g. `"Condition"`, `"Observation"`)
- `value` (`PathMapping`) — FHIR path + supported types for value filtering
- `termCode` (`PathMapping`) — FHIR path used for terminology filtering (controls whether a FHIR `retrieve` filter or a `where` clause is emitted)
- `fixedCriteria` / `attributes` — pre-baked and user-selectable modifiers

`MappingTreeBase` wraps `MappingTreeModuleRoot[]` and supports hierarchical concept expansion (child term codes resolved upward).

The build downloads the ontology (`mapping.zip`) from GitHub releases of `fhir-ontology-generator` at version `${ontology.version}` during `generate-resources`. This zip is used by integration tests (`KdsTestDataTest`, `EvaluationIT`).

### CQL AST (`model/cql` package)

Every CQL construct is an immutable record/class implementing `Expression<T>`. `withIncrementedSuffixes(Map)` propagates alias renaming through the tree. `PrintContext` carries indentation level for pretty-printing.

### CLI (`cli` module)

`Main` (`de.medizininformatikinitiative.cctb.cli`) is a picocli root `@Command` (`cctb`, with `-h`/`-V` mixins) that dispatches to the `translate` subcommand; running with no subcommand prints usage.

`TranslateCommand` (a record implementing `Callable<Integer>`) translates a structured query into a target query language:

```
cctb translate <TARGET_LANGUAGE> -m <FILE> -ct <FILE> [-csa <FILE>] <INPUT_FILE> [<OUTPUT_FILE>]
```

- `<TARGET_LANGUAGE>` — positional, `TranslateCommand.TargetLanguage` enum (currently only `CQL`), matched case-insensitively (`Main` enables `setCaseInsensitiveEnumValuesAllowed`).
- `-m`/`--mapping` (required) — mapping JSON, converted to `Map<ContextualTermCode, Mapping>` via `MappingConverter`.
- `-ct`/`--concept-tree` (required) — concept tree JSON, converted to `MappingTreeBase` via `ConceptTreeConverter`.
- `-csa`/`--code-system-alias` (optional) — code-system alias JSON, converted to `Map<String, String>` via `CodeSystemAliasesConverter`.
- `<INPUT_FILE>` (required positional) — structured query JSON.
- `<OUTPUT_FILE>` (optional positional) — defaults to stdout; refuses to overwrite an existing file (throws `FileAlreadyExistsException`).

Adding a new target language means adding an enum constant and a `switch` arm in `TranslateCommand.call()`.

## Key Conventions

- All model classes use static factory methods (`of(...)`) rather than public constructors.
- `Container` is the monadic unit — prefer `map`, `flatMap`, and the combiner constants over manual construction.
- Integration tests (`*IT.java`) use Testcontainers and require Docker; they run under `maven-failsafe-plugin`.
- Jackson 3 (`tools.jackson.*`) is used — import paths differ from Jackson 2 (`com.fasterxml.jackson.*`).
