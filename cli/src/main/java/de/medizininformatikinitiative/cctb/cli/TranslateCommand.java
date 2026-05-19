package de.medizininformatikinitiative.cctb.cli;

import de.medizininformatikinitiative.cctb.Translator;
import de.medizininformatikinitiative.cctb.model.Mapping;
import de.medizininformatikinitiative.cctb.model.MappingContext;
import de.medizininformatikinitiative.cctb.model.MappingTreeBase;
import de.medizininformatikinitiative.cctb.model.MappingTreeModuleRoot;
import de.medizininformatikinitiative.cctb.model.structured_query.ContextualTermCode;
import de.medizininformatikinitiative.cctb.model.structured_query.StructuredQuery;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(
        name = "translate",
        mixinStandardHelpOptions = true,
        description = "Translates a CCDL query to a target language"
)
public class TranslateCommand implements Callable<Integer> {

    private static final ObjectMapper JSON_UTIL = new ObjectMapper();
    private static final TypeReference<HashMap<String, String>> TYPE_REF = new TypeReference<>() {};

    public enum TargetLanguage {
        CQL
    }

    @Parameters(
            index = "0",
            paramLabel = "<TARGET_LANGUAGE>",
            description = "Language to map tge CDDL query to. Supported: ${COMPLETION-CANDIDATES}"
    )
    private TargetLanguage language;

    @Parameters(
            index = "1",
            arity = "0..1",
            paramLabel = "<INPUT_FILE>",
            description = "(Optional) Path to CCDL query file. Reads from STDIN if missing"
    )
    private Optional<File> inputFile = Optional.empty();

    @Parameters(
            index = "2",
            arity = "0..1",
            paramLabel = "<OUTPUT_FILE>",
            description = "(Optional) Path to target language query file. Prints to STDOUT if missing"
    )
    private Optional<File> outputFile = Optional.empty();

    @Option(
            names = {"-m", "--mapping"},
            description = "Path to mapping file",
            required = true
    )
    private File mappingFile;

    @Option(
            names = {"-ct", "--concept-tree"},
            description = "Path to concept tree file",
            required = true
    )
    private File conceptTreeFile;

    @Option(
            names = {"-csa", "--code-system-alias"},
            description = "(Optional) path to code system aliases file"
    )
    private Optional<File> codeSystemAliasesFile = Optional.empty();

    @Override
    public Integer call() {
        try {
            if (outputFile.map(File::exists).orElse(false)) {
                throw new FileAlreadyExistsException("Output file already exists");
            }

            // Use dedicated parsing methods due to `picoli`s handling of Map<K, V>-typed options and parameters
            var mappingContext = MappingContext.of(
                    readMapping(),
                    readConceptTree(),
                    readCodeSystemAliases()
            );
            var structuredQuery = inputFile.map(v -> JSON_UTIL.readValue(v, StructuredQuery.class))
                    .orElseGet(() -> {
                        try {
                            return JSON_UTIL.readValue(System.in.readAllBytes(), StructuredQuery.class);
                        } catch (IOException exc) {
                            throw new RuntimeException(exc.getMessage());
                        }
                    });

            var result = switch (language) {
                case CQL -> Translator.of(mappingContext).toCql(structuredQuery).print();
            };

            writeResult(result);
            return 0;
        } catch (Exception exc) {
            System.err.printf("Translation failed: %s", exc.getMessage());
            return 1;
        }
    }

    private void writeResult(String result) throws IOException {
        if (outputFile.isEmpty()) {
            System.out.print(result);
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.get()))) {
                writer.write(result);
            }
        }
    }

    private Map<ContextualTermCode, Mapping> readMapping() {
        return Stream.of(JSON_UTIL.readValue(mappingFile, Mapping[].class))
                .collect(Collectors.toMap(Mapping::key, Function.identity(), (a, b) -> a));
    }

    private MappingTreeBase readConceptTree() {
        return new MappingTreeBase(Arrays.stream(JSON_UTIL.readValue(conceptTreeFile, MappingTreeModuleRoot[].class)).toList());
    }

    private Map<String, String> readCodeSystemAliases() {
        return codeSystemAliasesFile.map(v -> JSON_UTIL.readValue(v, TYPE_REF)).orElseGet(HashMap::new);
    }
}
