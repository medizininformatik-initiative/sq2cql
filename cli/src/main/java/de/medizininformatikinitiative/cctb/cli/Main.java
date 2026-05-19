package de.medizininformatikinitiative.cctb.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "cctb-cli",
        description = "Clinical Cohort Toolbox command line interface",
        mixinStandardHelpOptions = true,
        subcommands = { TranslateCommand.class }
)
public class Main implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        var commandLine = new CommandLine(new Main()).setCaseInsensitiveEnumValuesAllowed(true);
        System.exit(commandLine.execute(args));
    }
}
