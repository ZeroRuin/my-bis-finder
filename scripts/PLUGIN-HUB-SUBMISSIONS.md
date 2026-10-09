# Automatic Plugin Hub submissions

After you merge a PR created by the weekly data updater, `submit-plugin-hub.yml` tests that exact merged commit and opens a PR in runelite/plugin-hub from ZeroRuin/plugin-hub. It changes only `plugins/my-bis-finder`. Hub maintainers still review and merge the submission; no automatic approval or merge is performed.

The workflow accepts only merged same-repository PRs authored by github-actions[bot], from automation/wiki-data-update, targeting main, with changes restricted to bundled Wiki data and WIKI-DATA-NOTES.txt. Ordinary code merges and the workflow setup commit are skipped.

Set repository Actions secret HUB_SUBMISSION_TOKEN to a ZeroRuin classic PAT with public_repo. The credential is exposed only to the submission step after mechanics tests, not to compilation. Renew the secret before the token expires. The submitter uses gh's credential helper; it never puts the token into URLs, source files or PR bodies.

An existing open ZeroRuin Plugin Hub PR touching this plugin is preserved and a new submission is skipped. After that PR is resolved, use Actions → Submit merged data update to Plugin Hub → Run workflow, entering the merged automated data PR number. The same retry handles a previous token/network failure. The submitter reuses a previously pushed branch only if its descriptor is exactly as expected.

A no-data-change week creates neither a source PR nor a Hub PR. Data still ships bundled through Plugin Hub updates; installed clients do not download JSON at login. Tests run before submission in this workflow, independently of whether GitHub runs separate checks on the bot-created source PR.
