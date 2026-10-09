"""Submit reviewed automated data merges; never merge a Plugin Hub PR."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

SOURCE = 'ZeroRuin/my-bis-finder'
HUB = 'runelite/plugin-hub'
FORK = 'ZeroRuin/plugin-hub'
PLUGIN = 'plugins/my-bis-finder'


def command(*args, cwd=None):
    return subprocess.check_output(args, cwd=cwd, text=True).strip()


def api(path):
    return json.loads(command('gh', 'api', path))


def eligible(pr, files):
    return (pr.get('merged') is True and pr['base']['ref'] == 'main'
            and pr['base']['repo']['full_name'] == SOURCE
            and pr['head']['repo'] is not None and pr['head']['repo']['full_name'] == SOURCE
            and pr['head']['ref'] == 'automation/wiki-data-update'
            and pr['user']['login'] == 'github-actions[bot]'
            and bool(files) and all(f['filename'].startswith('src/main/resources/wiki-data/')
                                   or f['filename'] == 'WIKI-DATA-NOTES.txt' for f in files))


def prepare(number):
    if not number.isdigit():
        raise ValueError('A merged data-update PR number is required')
    pr = api(f'repos/{SOURCE}/pulls/{number}')
    files = json.loads(command('gh', 'api', '--paginate', '--slurp', f'repos/{SOURCE}/pulls/{number}/files'))
    files = [f for page in files for f in page]
    if not eligible(pr, files):
        print('Not a merged automation-only data PR; no submission.')
        return
    sha = pr['merge_commit_sha']
    if not re.fullmatch('[0-9a-f]{40}', sha):
        raise ValueError('Invalid merge commit')
    with open(os.environ['GITHUB_OUTPUT'], 'a') as out:
        out.write(f'eligible=true\nsha={sha}\n')


def submit(sha, number):
    if not re.fullmatch('[0-9a-f]{40}', sha) or not number.isdigit():
        raise ValueError('Invalid submission arguments')
    # Preserve any existing submission for this plugin, including manually opened PRs.
    prs = json.loads(command('gh', 'pr', 'list', '--repo', HUB, '--author', 'ZeroRuin',
                             '--state', 'open', '--limit', '100', '--json', 'number,url'))
    for pr in prs:
        files = json.loads(command('gh', 'api', '--paginate', '--slurp', f'repos/{HUB}/pulls/{pr["number"]}/files'))
        if any(f['filename'] == PLUGIN for page in files for f in page):
            print('Existing Plugin Hub submission preserved: ' + pr['url'])
            return
    user = api('user')['login']
    if user != 'ZeroRuin':
        raise ValueError('HUB_SUBMISSION_TOKEN must belong to ZeroRuin')
    with tempfile.TemporaryDirectory() as tmp:
        command('git', 'clone', '--depth', '1', 'https://github.com/' + HUB + '.git', tmp)
        repo = Path(tmp)
        descriptor = repo / PLUGIN
        content = descriptor.read_text()
        if 'repository=https://github.com/ZeroRuin/my-bis-finder.git' not in content:
            raise ValueError('Unexpected plugin repository in descriptor')
        if f'commit={sha}' in content.splitlines():
            print('Plugin Hub already points to this commit; no PR needed.')
            return
        updated, count = re.subn(r'^commit=[0-9a-f]{40}$', 'commit=' + sha, content, flags=re.MULTILINE)
        if count != 1:
            raise ValueError('Expected exactly one commit line')
        descriptor.write_text(updated)
        branch = 'automation/my-bis-finder-' + sha[:12]
        command('git', 'switch', '-c', branch, cwd=tmp)
        command('git', 'config', 'user.name', 'ZeroRuin automation', cwd=tmp)
        command('git', 'config', 'user.email', '329067577+ZeroRuin@users.noreply.github.com', cwd=tmp)
        command('git', 'add', PLUGIN, cwd=tmp)
        command('git', 'commit', '-m', 'Update My BiS Finder Wiki data', cwd=tmp)
        command('git', 'remote', 'set-url', 'origin', 'https://github.com/' + FORK + '.git', cwd=tmp)
        # gh's credential helper reads GH_TOKEN; token is never embedded in remote URLs.
        command('gh', 'auth', 'setup-git', '--hostname', 'github.com')
        existing = command('git', 'ls-remote', 'origin', 'refs/heads/' + branch, cwd=tmp)
        if existing:
            command('git', 'fetch', '--depth', '1', 'origin', branch, cwd=tmp)
            if command('git', 'show', 'FETCH_HEAD:' + PLUGIN, cwd=tmp) != updated.strip():
                raise ValueError('Existing automation branch has unexpected content; inspect manually')
        else:
            command('git', 'push', 'origin', 'HEAD:refs/heads/' + branch, cwd=tmp)
        body = repo / 'submission.md'
        body.write_text(f'Update My BiS Finder to include reviewed bundled Wiki data changes.\n\nSource data PR: https://github.com/{SOURCE}/pull/{number}\nPlugin commit: https://github.com/{SOURCE}/commit/{sha}\n\nThe merged commit passed mechanicsSelfTest before submission. The plugin continues using bundled data; it makes no runtime Wiki requests.\n')
        url = command('gh', 'pr', 'create', '--repo', HUB, '--base', 'master', '--head', 'ZeroRuin:' + branch,
                      '--title', 'Update My BiS Finder Wiki data', '--body-file', str(body))
        print(url)
        if os.environ.get('GITHUB_STEP_SUMMARY'):
            with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as out:
                out.write('Plugin Hub submission: ' + url + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('mode', choices=('prepare', 'submit'))
    args = parser.parse_args()
    if args.mode == 'prepare': prepare(os.environ['DATA_PR_NUMBER'])
    else: submit(os.environ['PLUGIN_COMMIT'], os.environ['DATA_PR_NUMBER'])
