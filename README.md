# Release to issue GitHub Action

[![Build CI](https://github.com/maxkratz/release-to-issue-docker-action/actions/workflows/build-ci.yml/badge.svg)](https://github.com/maxkratz/release-to-issue-docker-action/actions/workflows/build-ci.yml)

This GitHub Action can be used to automatically check a GitHub repository for new releases and, in case a new release was found, automatically open a new issue in a third repository.
The newly created issue contains a link to the release, a link to the tag, and a nice title mentioning the repositories name as well as the version.
This GitHub Action also assignes a user and the tag `enhancement` to the newly created issue.

## Inputs

- `source-repo`: Which repo to check for new releases
    - Required: yes
- `target-repo`: Which repo to create new issues in
    - Required: yes
- `start-date`: Date limit to ignore older releases
    - Required: yes
- `assignee-name`: Which GitHub user should be assigned to newly created issues
    - Required: yes
- `github-username`: Which GitHub user should be used to query the GitHub API
    - Required: yes
- `github-token`: Which GitHub token should be used to query the GitHub API
    - Required: yes
- `dry-run`: If true, the Action will not create any issues but just simulate them
    - Required: no

## Outputs

None.

## Example usage

- Create a new classic access token in your GitHub preferences.
- Inside your GitHub repository (that will use this Action) create a new secret `USER_TOKEN` that contains your access token.
- Add the following snippet to your GitHub Actions configuration file:

```
uses: maxkratz/release-to-issue-docker-action@v1.0.0
with:
    source-repo: 'lectureStudio/lectureStudio'  # the repo that should be checked for new releases
    target-repo: 'maxkratz/github-api-testing'  # the repo in which you want to open an issue
    start-date: '2023-01-01'                    # all releases older than this date will be ignored
    assignee-name: 'maxkratz'                   # can be different than your username
    github-username: 'maxkratz'                 # your username (in whose name the issues will be opened)
    github-token: ${{ secrets.USER_TOKEN }}     # your secret GitHub access token
    dry-run: false                              # if set to true, no actual issues will be opened
```

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for more details.
