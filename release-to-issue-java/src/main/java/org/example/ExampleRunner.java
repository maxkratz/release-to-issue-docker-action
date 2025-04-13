package org.example;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

public class ExampleRunner {
    public static void main(final String[] args) {
        new ExampleRunner().run();
    }

    public void run() {
        try {
            final GitHub github = GitHubBuilder.fromPropertyFile("./github.properties").build();
        } catch (final IOException e) {
            throw new RuntimeException("GitHub auth failed: " + e);
        }
    }

}