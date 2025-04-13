package org.maxkratz.releasetoissue;

import org.kohsuke.github.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ExampleRunner {

    private final static String GITHUB_URL = "https://github.com/";
    private final String sourceRepoName = "lectureStudio/lectureStudio";
    private final String targetRepoName = "maxkratz/github-api-testing";
    private final String dateLimit = "2023-01-01";
    private final String assigneeName = "maxkratz";
    private final boolean dryRun = false;
    private final String propertiesFilePath = "./github.properties";

    private final Logger logger = Logger.getLogger(ExampleRunner.class.getName());

    public ExampleRunner() {
        // Configure logging
        logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogEntryFormatter());
        logger.addHandler(handler);
    }

    public static void main(final String[] args) {
        new ExampleRunner().run();
    }

    public void run() {
        GitHub github = null;
        try {
            github = GitHubBuilder.fromPropertyFile(propertiesFilePath).build();
        } catch (final IOException e) {
            logger.warning("GitHub auth failed.");
            throw new RuntimeException(e);
        }

        checkNotNull(github);

        // Get all releases of the source repository
        final Set<GHRelease> allSourceReleases = getAllReleases(github, sourceRepoName);

        // Get all issues of the target repository
        final Set<GHIssue> allTargetIssues = getAllTargetIssues(github, targetRepoName);

        // Determine which releases are new, i.e., which releases do not have a corresponding issue within the target repository
        final Set<GHRelease> newReleases = calculateNewReleases(github, allSourceReleases, sourceRepoName, allTargetIssues, dateLimit);

        // Create a new issue for every new release
        createIssuesForReleases(github, newReleases, sourceRepoName, targetRepoName);
    }

    private void createIssuesForReleases(final GitHub github, final Set<GHRelease> releases, final String sourceRepoName, final String targetRepoName) {
        checkNotNull(github, releases, sourceRepoName, targetRepoName);
        releases.forEach(r -> {
            createIssueForRelease(github, r, sourceRepoName, targetRepoName);
        });
    }

    private void createIssueForRelease(final GitHub github, final GHRelease release, final String sourceRepoName, final String targetRepoName) {
        checkNotNull(github, release, sourceRepoName, targetRepoName);
        try {
            final GHRepository targetRepo = github.getRepository(targetRepoName);
            final String tagName = release.getTagName();
            final GHIssueBuilder issueBuilder = targetRepo.createIssue(constructIssueTitle(sourceRepoName, tagName));
            issueBuilder.body(constructIssueBody(sourceRepoName, tagName));
            issueBuilder.label("enhancement");
            if (assigneeName != null && !assigneeName.isBlank()) {
                issueBuilder.assignee("");
            }

            logger.info("Create issue: " + constructIssueTitle(sourceRepoName, tagName));
            if (!dryRun) {
                issueBuilder.create();
            }
        } catch (final IOException e) {
            logger.warning("Caught an exception while creating a new issue within the target repository.");
            throw new RuntimeException(e);
        }
    }

    private Set<GHRelease> calculateNewReleases(final GitHub github, final Set<GHRelease> allSourceReleases, final String sourceRepoName, final Set<GHIssue> allTargetIssues, final String dateLimit) {
        checkNotNull(github, allSourceReleases, sourceRepoName, allTargetIssues, dateLimit);
        final Set<GHRelease> newReleases = new HashSet<>();

        for (final GHRelease currentRelease : allSourceReleases) {
            // Check date limit
            final long epochLimit = convertDateToEpoch(dateLimit);
            final long epochRelease = currentRelease.getPublished_at().getTime();
            if (epochLimit > epochRelease) {
                logger.info("Release with tag <" + currentRelease.getTagName() + "> violates specified date limit.");
                continue;
            }

            boolean found = false;

            // Check if issue in target repo already exists
            for (final GHIssue currentIssue : allTargetIssues) {
                if (currentIssue.getTitle().equals(constructIssueTitle(sourceRepoName, currentRelease.getTagName()))) {
                    found = true;
                    logger.info("Target issue <" + currentIssue.getTitle() + "> already exists.");
                    break;
                }
            }

            if (!found) {
                newReleases.add(currentRelease);
            }
        }
        return newReleases;
    }

    private Set<GHRelease> getAllReleases(final GitHub github, final String sourceRepoName) {
        checkNotNull(github, sourceRepoName);
        final Set<GHRelease> allReleases = new HashSet<>();

        try {
            final GHRepository sourceRepo = github.getRepository(sourceRepoName);
            sourceRepo.listReleases().forEach(r -> {
                if (!r.isDraft() && !r.isPrerelease()) {
                    allReleases.add(r);
                }
            });
        } catch (final IOException e) {
            logger.warning("Exception caught while getting releases of the source repository.");
            throw new RuntimeException(e);
        }

        return allReleases;
    }

    private Set<GHIssue> getAllTargetIssues(final GitHub github, final String targetRepoName) {
        checkNotNull(github, targetRepoName);
        final Set<GHIssue> allIssues;

        try {
            final GHRepository targetRepo = github.getRepository(targetRepoName);
            allIssues = new HashSet<>(targetRepo.getIssues(GHIssueState.ALL));
        } catch (final IOException e) {
            logger.warning("Exception caught while getting issues of the target repository.");
            throw new RuntimeException(e);
        }

        return allIssues;
    }

    //
    // Utility methods.
    //

    private long convertDateToEpoch(final String date) {
        checkNotNull(date);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-dd-MM");
        try {
            final Date d = df.parse(date);
            checkNotNull(d);
            return d.getTime();
        } catch (final ParseException e) {
            logger.warning("ParseException caught while parsing the date <" + date + ">.");
            throw new RuntimeException(e);
        }
    }

    private String constructIssueTitle(final String repoName, final String version) {
        checkNotNull(repoName, version);
        return "Update " + repoName.split("/")[1] + " to " + version;
    }

    private String constructIssueBody(final String repoName, final String tag) {
        checkNotNull(repoName, tag);
        return "- GitHub release: " + constructReleaseUrl(repoName, tag) //
                + System.lineSeparator() //
                + "- GitHub tag: " + constructTagUrl(repoName, tag);
    }

    private String constructTagUrl(final String repoName, final String tag) {
        checkNotNull(repoName, tag);
        return GITHUB_URL + repoName + "/tree/" + tag;
    }

    private String constructReleaseUrl(final String repoName, final String tag) {
        checkNotNull(repoName, tag);
        return GITHUB_URL + repoName + "/releases/tag/" + tag;
    }

    private void checkNotNull(final Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("Parameter was null.");
        }

        if (objects.length > 0) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) {
                    throw new IllegalArgumentException("Parameter <" + i + "> was null.");
                }
            }
        }
    }

    private static class LogEntryFormatter extends Formatter {

        @Override
        public String format(final LogRecord record) {
            if (record == null) {
                throw new IllegalArgumentException("Given log entry was null.");
            }
            return record.getMessage() + System.lineSeparator();
        }

    }

}