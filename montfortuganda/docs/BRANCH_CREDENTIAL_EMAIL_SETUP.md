# Branch Credential Email Setup

## Behavior

When Super Admin creates a branch, the application:

1. Creates the Branch Admin username as `{schoolCode}@montfort.ug`.
2. Generates a secure random 16-character temporary password.
3. Stores only the encoded password.
4. Makes the temporary password valid for 72 hours.
5. Commits the branch and user account.
6. Sends credentials asynchronously from the branch email.
7. Records `SENT` or `FAILED` against the user account.

The Super Admin **Reset Password** action generates replacement credentials,
invalidates older tokens through `credential_version`, sends the replacement
after commit, and records `RESENT` or `FAILED`.

## Required values

The public sender address is stored in:

```text
erp_branches.branch_email
```

The corresponding Google App Password must remain outside the database and
source code.

For existing branches:

```text
BRANCH_U011_MAIL_PASSWORD=<Google App Password>
BRANCH_U021_MAIL_PASSWORD=<Google App Password>
BRANCH_U031_MAIL_PASSWORD=<Google App Password>
```

For a newly onboarded branch, use the same pattern. Example for school code
`U041`:

```text
BRANCH_U041_MAIL_PASSWORD=<Google App Password>
```

Restart Spring Boot after adding or changing an environment variable.

Do not enter an App Password in the Add Branch form, store it in MySQL, put it
in `application.properties`, or commit it to Git.
