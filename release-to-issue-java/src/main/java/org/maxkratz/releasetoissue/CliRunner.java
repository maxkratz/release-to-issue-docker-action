package org.maxkratz.releasetoissue;

import org.apache.commons.cli.*;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class CliRunner {

    private final Logger logger = Logger.getLogger(CliRunner.class.getName());

    public CliRunner() {
        // Configure logging
        logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogEntryFormatter());
        logger.addHandler(handler);
    }

    public static void main(final String[] args) {
        final CliRunner runner = new CliRunner();
        final Configuration config = runner.parseArgs(args);
        new ReleaseToIssue().run(config);
    }

    private Configuration parseArgs(final String[] args) {
        final Options options = new Options();

        // Source repository name
        final Option sourceRepoNameOption = new Option("s", "sourcerepo", true, "Source repository name");
        sourceRepoNameOption.setRequired(true);
        options.addOption(sourceRepoNameOption);

        // Target repository name
        final Option targetRepoNameOption = new Option("t", "targetrepo", true, "Target repository name");
        targetRepoNameOption.setRequired(true);
        options.addOption(targetRepoNameOption);

        // Date limit
        final Option dateLimitOption = new Option("l", "datelimit", true, "Date limit");
        dateLimitOption.setRequired(false);
        options.addOption(dateLimitOption);

        // Target issue assignee
        final Option assigneeOption = new Option("a", "assignee", true, "Target issue assignee");
        assigneeOption.setRequired(true);
        options.addOption(assigneeOption);

        // Dry run
        final Option dryRunOption = new Option("d", "dryrun", false, "Dry run option");
        dryRunOption.setRequired(false);
        options.addOption(dryRunOption);

        // Properties file path
        final Option propertiesFilePathOption = new Option("p", "properties", true, "Properties file path");
        dryRunOption.setRequired(false);
        options.addOption(propertiesFilePathOption);

        // Parse arguments
        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException ex) {
            logger.warning("Argument parsing failed.");
            formatter.printHelp("CLI parameters", options);
            System.exit(1);
        }

        // Get and save values as configuration
        return new Configuration( //
                cmd.getOptionValue("sourcerepo"), //
                cmd.getOptionValue("targetrepo"), //
                cmd.getOptionValue("datelimit"), //
                cmd.getOptionValue("assignee"), //
                cmd.hasOption("dryrun"), //
                cmd.hasOption("properties") ? cmd.getOptionValue("properties") : null //
        );
    }

}
