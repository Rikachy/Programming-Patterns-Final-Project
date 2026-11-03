
--Registration table(Ilsa)
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

--Waiting List Table (Ilsa)
CREATE TABLE IF NOT EXISTS WaitingList (
    waiting_id INTEGER PRIMARY KEY AUTOINCREMENT,
    registration_id INTEGER NOT NULL UNIQUE,
    joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (registration_id)
    REFERENCES Registrations(registration_id)
    ON DELETE CASCADE
    );