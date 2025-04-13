package org.maxkratz.releasetoissue;

/**
 * Configuration record that should be filled via the CLI runner.
 *
 * @param sourceRepoName     Source repository name.
 * @param targetRepoName     Target repository name.
 * @param dateLimit          Date limit.
 * @param assigneeName       Assignee name.
 * @param dryRun             If true, no new issues will be created.
 * @param propertiesFilePath File path of the GitHub properties file.
 */
public record Configuration( //
                             String sourceRepoName, //
                             String targetRepoName, //
                             String dateLimit, //
                             String assigneeName, //
                             boolean dryRun, //
                             String propertiesFilePath //
) {
}
