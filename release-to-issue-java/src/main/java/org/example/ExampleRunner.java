package org.example;

import org.kohsuke.github.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ExampleRunner {

    private final static String GITHUB_URL = "https://github.com/";
    private final String sourceRepoName = "lectureStudio/lectureStudio";
    private final String targetRepoName = "maxkratz/github-api-testing";
    private final String dateLimit = "2023-01-01";
    private final String assigneeName = "maxkratz";

    public static void main(final String[] args) {
        new ExampleRunner().run();
    }

    public void run() {
        GitHub github = null;
        try {
            github = GitHubBuilder.fromPropertyFile("./github.properties").build();
        } catch (final IOException e) {
            throw new RuntimeException("GitHub auth failed: " + e);
        }

        final Set<GHRelease> allSourceReleases = getAllReleases(github, sourceRepoName);
        final Set<GHIssue> allTargetIssues = getAllTargetIssues(github, targetRepoName);

        final Set<GHRelease> newReleases = calculateNewReleases(github, allSourceReleases, sourceRepoName, allTargetIssues, dateLimit);
        createIssuesForReleases(github, newReleases, sourceRepoName, targetRepoName);
    }

    private void createIssuesForReleases(final GitHub github, final Set<GHRelease> releases, final String sourceRepoName, final String targetRepoName) {
        releases.forEach(r -> {
            createIssueForRelease(github, r, sourceRepoName, targetRepoName);
        });
    }

    private void createIssueForRelease(final GitHub github, final GHRelease release, final String sourceRepoName, final String targetRepoName) {
        try {
            final GHRepository targetRepo = github.getRepository(targetRepoName);
            final String tagName = release.getTagName();
            final GHIssueBuilder issueBuilder = targetRepo.createIssue(constructIssueTitle(sourceRepoName, tagName));
            issueBuilder.body(constructIssueBody(sourceRepoName, tagName));
            issueBuilder.label("enhancement");
            if (assigneeName != null && !assigneeName.isBlank()) {
                issueBuilder.assignee("");
            }

            //System.out.println("Would have created issue: " + issueBuilder.toString());
            System.out.println("Create issue: " + constructIssueTitle(sourceRepoName, tagName));
            //issueBuilder.create();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<GHRelease> calculateNewReleases(final GitHub github, final Set<GHRelease> allSourceReleases, final String sourceRepoName, final Set<GHIssue> allTargetIssues, final String dateLimit) {
        final Set<GHRelease> newReleases = new HashSet<>();

        for (final GHRelease currentRelease : allSourceReleases) {
            // Check date limit
            final long epochLimit = convertDateToEpoch(dateLimit);
            final long epochRelease = currentRelease.getPublished_at().getTime();
            if (epochLimit > epochRelease) {
                continue;
            }

            boolean found = false;

            // Check if issue in target repo already exists
            for (final GHIssue currentIssue : allTargetIssues) {
                if (currentIssue.getTitle().equals(constructIssueTitle(sourceRepoName, currentRelease.getTagName()))) {
                    found = true;
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
        final Set<GHRelease> allReleases = new HashSet<>();

        try {
            final GHRepository sourceRepo = github.getRepository(sourceRepoName);
            sourceRepo.listReleases().forEach(r -> {
                if (!r.isDraft() && !r.isPrerelease()) {
                    allReleases.add(r);
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return allReleases;
    }

    private Set<GHIssue> getAllTargetIssues(final GitHub github, final String targetRepoName) {
        final Set<GHIssue> allIssues;

        try {
            final GHRepository targetRepo = github.getRepository(targetRepoName);
            allIssues = new HashSet<>(targetRepo.getIssues(GHIssueState.ALL));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return allIssues;
    }

    //
    // Utility methods.
    //

    private long convertDateToEpoch(final String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-dd-MM");
        try {
            Date d = df.parse(date);
            return d.getTime();
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String constructIssueTitle(final String repoName, final String version) {
        return "Update " + repoName.split("/")[1] + " to " + version;
    }

    private String constructIssueBody(final String repoName, final String tag) {
        return "- GitHub release: " + constructReleaseUrl(repoName, tag) //
                + System.lineSeparator() //
                + "- GitHub tag: " + constructTagUrl(repoName, tag);
    }

    private String constructTagUrl(final String repoName, final String tag) {
        return GITHUB_URL + repoName + "/tree/" + tag;
    }

    private String constructReleaseUrl(final String repoName, final String tag) {
        return GITHUB_URL + repoName + "/releases/tag/" + tag;
    }

    private void checkNotNull(final Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter was null.");
        }
    }

}