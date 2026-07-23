# Development and Production Database Setup

## Architecture

The application uses two independent persistence units:

1. `erp` is the primary read/write persistence unit.
   - Development: Hostinger `montfortug_erp_dev`
   - Production: Hostinger `montfortug_erp_prod`
2. `website` is the secondary read-only persistence unit.
   - GoDaddy website database
   - Only `WebDonationRepository` and `web_donations`

The same codebase is used in both environments. Never copy production data into
the development schema and never activate the `prod` profile on a developer
machine.

## Local development tunnel

Start the tunnel from Windows PowerShell and keep the window open:

```powershell
ssh -N `
  -L 127.0.0.1:3307:127.0.0.1:3306 `
  -o ExitOnForwardFailure=yes `
  -o ServerAliveInterval=60 `
  -p 2222 erpadmin@YOUR_VPS_IP
```

Verify it from a second PowerShell window:

```powershell
Test-NetConnection 127.0.0.1 -Port 3307
```

`TcpTestSucceeded` must be `True`.

Run Spring Boot with:

```text
SPRING_PROFILES_ACTIVE=dev
DB_HOST=127.0.0.1
DB_PORT=3307
DB_NAME=montfortug_erp_dev
DB_USERNAME=erp_dev_user
DB_PASSWORD=<local secret>
```

## VPS production

The deployed service must use:

```text
SPRING_PROFILES_ACTIVE=prod
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=montfortug_erp_prod
DB_USERNAME=erp_prod_user
DB_PASSWORD=<VPS secret>
```

MySQL port `3306` must remain closed to the public internet. Production reaches
MySQL over VPS loopback.

## GoDaddy donation reader

Create a dedicated database account that can only read the donation table.
Use the control panel's database-user permissions when direct `CREATE USER`
access is unavailable.

```sql
GRANT SELECT
ON website_database.web_donations
TO 'erp_donation_reader'@'VPS_PUBLIC_IP';
```

Do not grant this account access to the old GoDaddy ERP tables. Configure both
profiles with:

```text
WEBSITE_DB_ENABLED=true
WEBSITE_DB_HOST=<GoDaddy MySQL hostname>
WEBSITE_DB_PORT=3306
WEBSITE_DB_NAME=<website database>
WEBSITE_DB_USERNAME=erp_donation_reader
WEBSITE_DB_PASSWORD=<secret>
WEBSITE_DB_SSL_MODE=VERIFY_IDENTITY
```

Keep `WEBSITE_DB_ENABLED=false` while GoDaddy remote access is blocked or
unavailable. Donation endpoints then return HTTP `503` immediately while the
Hostinger ERP modules continue normally. After GoDaddy access is restored,
set it to `true` and restart the application.

Use the provider hostname rather than an IP address for TLS hostname
verification. If GoDaddy does not support verified remote MySQL TLS and
VPS-IP restrictions, replace the database connection with a secured website
API; do not weaken the production connection globally.

## Cross-database references

`erp_scholarship_allocations.donation_id` and
`erp_branch_fund_allocations.donation_id` store external scalar identifiers.
They are not JPA relationships and must not have foreign keys to a Hostinger
`web_donations` table.

Before dropping an old foreign key, inspect its exact name:

```sql
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'montfortug_erp_dev'
  AND COLUMN_NAME = 'donation_id'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

Repeat the inspection separately for `montfortug_erp_prod`. Never run a
development cleanup against production.

## Verification

1. Start the SSH tunnel.
2. Run `mvnw.cmd clean compile` with JDK 21.
3. Start the application with the `dev` profile.
4. Confirm login and ordinary ERP APIs use `montfortug_erp_dev`.
5. Confirm donor endpoints read GoDaddy `web_donations`.
6. Temporarily block the GoDaddy connection and confirm ordinary ERP endpoints
   still work while donation endpoints return HTTP `503`.
7. Confirm no old GoDaddy ERP table changes after creating or editing an
   employee, branch, department, or application.
