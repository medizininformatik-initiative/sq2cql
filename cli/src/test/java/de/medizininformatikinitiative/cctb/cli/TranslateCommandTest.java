package de.medizininformatikinitiative.cctb.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.medizininformatikinitiative.cctb.cli.Util.*;
import static org.assertj.core.api.Assertions.assertThat;

class TranslateCommandTest {

    private static final String EXPECTED_CQL = """
            library Retrieve version '1.0.0'
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'

            codesystem codeSystem1: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'

            context Patient

            define Criterion:
              exists [Condition: Code 'C71.1' from codeSystem1]

            define InInitialPopulation:
              Criterion
            """;

    private static final String EXPECTED_CQL_WITH_ALIAS = """
            library Retrieve version '1.0.0'
            using FHIR version '4.0.0'
            include FHIRHelpers version '4.0.0'

            codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'

            context Patient

            define Criterion:
              exists [Condition: Code 'C71.1' from icd10]

            define InInitialPopulation:
              Criterion
            """;

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @TempDir
    private Path tempDir;

    @BeforeEach
    void redirectStreams() {
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private static void stdin(String s) {
        System.setIn(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void readsFromStdInIfNoInputFileIsProvided() throws Exception {
        stdin(slurp("structured-query.json"));

        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL);
    }

    @Test
    void readsInputFileIfProvided() throws Exception {
        System.setIn(InputStream.nullInputStream());

        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL);
    }

    @Test
    void translatesStructuredQueryToCqlOnStdout() throws Exception {
        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL);
        assertThat(stderr()).isEmpty();
    }

    @Test
    void targetLanguageIsMatchedCaseInsensitively() throws Exception {
        var exitCode = execute("translate", "CQL",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL);
    }

    @Test
    void appliesCodeSystemAliases() throws Exception {
        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                "-csa", resource("code-system-aliases.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL_WITH_ALIAS);
    }

    @Test
    void writesResultToStdOutIfNoOutputFileWasProvided() throws Exception {
        var outputFile = tempDir.resolve("out.cql");

        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isZero();
        assertThat(outputFile).doesNotExist();
        assertThat(stdout()).isEqualTo(EXPECTED_CQL);
    }

    @Test
    void writesResultToOutputFileIfProvided() throws Exception {
        var outputFile = tempDir.resolve("out.cql");

        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString(),
                outputFile.toString());

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEmpty();
        assertThat(Files.readString(outputFile)).isEqualTo(EXPECTED_CQL);
    }

    @Test
    void failsIfOutputFileExists() throws Exception {
        var outputFile = tempDir.resolve("out.cql");
        Files.writeString(outputFile, "existing content");

        var exitCode = execute("translate", "cql",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString(),
                outputFile.toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(stderr()).isEqualTo("Translation failed: Output file already exists");
        assertThat(Files.readString(outputFile)).isEqualTo("existing content");
    }

    @Test
    void rejectsUnknownTargetLanguage() throws Exception {
        var exitCode = execute("translate", "xml",
                "-m", resource("mapping.json").toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains(
                "Invalid value for positional parameter at index 0 (<TARGET_LANGUAGE>): expected one of [CQL] (case-insensitive) but was 'xml'");
    }

    @Test
    void requiresMappingAndConceptTreeOptions() throws Exception {
        var exitCode = execute("translate", "cql", resource("structured-query.json").toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains("Missing required options: '--mapping=<mappingFile>', '--concept-tree=<conceptTreeFile>'");
    }

    @Test
    void reportsUnreadableMappingFileAsTranslationFailure() throws Exception {
        var missingFile = tempDir.resolve("does-not-exist.json");

        var exitCode = execute("translate", "cql",
                "-m", missingFile.toString(),
                "-ct", resource("concept-tree.json").toString(),
                resource("structured-query.json").toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(stderr()).startsWith("Translation failed:");
    }
}
