package de.medizininformatikinitiative.cctb.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static de.medizininformatikinitiative.cctb.cli.Util.execute;
import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    private static final String USAGE = """
            Usage: cctb-cli [-hV] [COMMAND]
            Clinical Cohort Toolbox command line interface
              -h, --help      Show this help message and exit.
              -V, --version   Print version information and exit.
            Commands:
              translate  Translates a CCDL query to a target language
            """;

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @BeforeEach
    void redirectStreams() {
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printsUsageWhenNoSubcommandIsGiven() {
        var exitCode = execute();

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(USAGE);
    }

    @Test
    void printsUsageOnHelpOption() {
        var exitCode = execute("--help");

        assertThat(exitCode).isZero();
        assertThat(stdout()).isEqualTo(USAGE);
    }

    @Test
    void dispatchesToTranslateSubcommand() {
        var exitCode = execute("translate", "--help");

        assertThat(exitCode).isZero();
        assertThat(stdout()).startsWith("Usage: cctb-cli translate");
    }
}
