#!/bin/bash
# Wait for MSSQL to be ready, then create the database
# This runs inside the MSSQL container on first start

sleep 15s

/opt/mssql-tools/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "${SA_PASSWORD}" \
  -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MissionControl') CREATE DATABASE MissionControl;"

echo "✅ MissionControl database ready"
