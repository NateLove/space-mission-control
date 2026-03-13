-- =============================================================
-- Space Mission Control - Initial Schema
-- V1__initial_schema.sql
-- =============================================================

CREATE TABLE missions (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100)  NOT NULL,
    description NVARCHAR(500),
    status      NVARCHAR(30)   NOT NULL DEFAULT 'PLANNING',
    destination NVARCHAR(100)  NOT NULL,
    launch_date DATETIME2,
    created_at  DATETIME2      NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2      NOT NULL DEFAULT GETDATE()
);

CREATE TABLE crew_members (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    first_name   NVARCHAR(50)  NOT NULL,
    last_name    NVARCHAR(50)  NOT NULL,
    email        NVARCHAR(100) UNIQUE,
    role         NVARCHAR(30)  NOT NULL,
    callsign     NVARCHAR(30),
    flight_hours INT           DEFAULT 0,
    mission_id   BIGINT        REFERENCES missions(id) ON DELETE SET NULL,
    assigned_at  DATETIME2,
    created_at   DATETIME2     NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_missions_status      ON missions(status);
CREATE INDEX idx_crew_mission_id      ON crew_members(mission_id);
CREATE INDEX idx_crew_role            ON crew_members(role);
