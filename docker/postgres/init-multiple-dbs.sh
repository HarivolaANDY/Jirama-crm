#!/bin/bash
set -e

# This script is executed by the PostgreSQL Docker entrypoint
# to create additional databases beyond the default one.
# The default database (POSTGRES_DB / POSTGRES_USER) is created automatically.

echo "=== JIRAMA: Creating additional databases ==="

# POSTGRES_USER and POSTGRES_PASSWORD are set in docker-compose.yml
# POSTGRES_DB defaults to the value of POSTGRES_USER if not explicitly set

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create Keycloak database
    CREATE DATABASE keycloak
        WITH OWNER = "$POSTGRES_USER"
        ENCODING = 'UTF8'
        LC_COLLATE = 'en_US.utf8'
        LC_CTYPE = 'en_US.utf8'
        CONNECTION LIMIT = -1;

    -- Grant all privileges
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO "$POSTGRES_USER";
EOSQL

echo "=== JIRAMA: Databases created successfully ==="
echo "  - $POSTGRES_DB (application)"
echo "  - keycloak (IAM)"
