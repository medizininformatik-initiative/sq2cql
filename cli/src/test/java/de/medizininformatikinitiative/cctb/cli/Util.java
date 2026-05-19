package de.medizininformatikinitiative.cctb.cli;

import picocli.CommandLine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Util {
    static int execute(String... args) {
        return new CommandLine(new Main()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
    }

    static Path resource(String name) throws URISyntaxException {
        return Path.of(TranslateCommandTest.class.getResource(name).toURI());
    }

    static String slurp(String name) throws URISyntaxException, IOException {
        return Files.readString(resource(name), StandardCharsets.UTF_8);
    }
}
