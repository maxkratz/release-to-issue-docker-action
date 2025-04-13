package org.maxkratz.releasetoissue;

public record Configuration(String sourceRepoName, String targetRepoName, String dateLimit, String assigneeName,
                            boolean dryRun, String propertiesFilePath) {
}
