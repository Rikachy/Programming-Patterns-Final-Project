-- Users table


CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    role TEXT NOT NULL DEFAULT 'PARTICIPANT',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (age > 0),
    CHECK (role IN ('ADMIN', 'PARTICIPANT'))
    );

-- Credentials table

CREATE TABLE IF NOT EXISTS Credentials (
    user_id INTEGER PRIMARY KEY,
    password_hash TEXT NOT NULL,

    FOREIGN KEY (user_id)
    REFERENCES Users(user_id)
    ON DELETE CASCADE
);

-- Conferences table

CREATE TABLE IF NOT EXISTS Conferences (
    conference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    location TEXT, --Location is optional
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (capacity > 0),
    CHECK (end_time > start_time)
);



--Registration table
CREATE TABLE IF NOT EXISTS Registrations (
    registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    conference_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'CONFIRMED',
    registered_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (conference_id) REFERENCES Conferences(conference_id),

    UNIQUE (user_id, conference_id),

    CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED'))
);

--Waiting List
CREATE TABLE IF NOT EXISTS WaitingList (
    waiting_id INTEGER PRIMARY KEY AUTOINCREMENT,
    registration_id INTEGER NOT NULL UNIQUE,
    joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (registration_id)
    REFERENCES Registrations(registration_id)
    ON DELETE CASCADE
    );