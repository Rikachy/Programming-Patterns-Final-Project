
INSERT INTO Users (user_id, full_name, email, age, role)
VALUES
    (1, 'System Admin', 'admin@example.com', 30, 'ADMIN'),
    (2, 'Ilsa Rehmat', 'ilsa@example.com', 25, 'PARTICIPANT'),
    (3, 'Jesse Oliver', 'jesse@example.com', 35, 'PARTICIPANT'),
    (4, 'Manal Fayyaz', 'manal@example.com', 28, 'PARTICIPANT'),
    (5, 'Richard Qing', 'richard@example.com', 32, 'PARTICIPANT');

INSERT INTO Credentials (user_id, password_hash) --used terminal to hash the password
VALUES
    (1, '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'),

    (2, '0b0aff78745b0e9d584be970efeb71fe6cc53179d04b69ba79c8e9e6d2f6475c'),

    (3, '2205b638d26672154d21f6cf243bfe99c061d5505935b2940236aef54057d5b5'),

    (4, '8c42e5addbc5a9a77b9110e880ccac6d7fcb67d066ba91c7627fa02b892f0e85'),

    (5, '61bffea9215f65164ad18b45aff1436c0c165d0d5dd2087ef61b4232ba6d2c1a');

INSERT INTO Conferences
(conference_id, title, description, location,
 start_time, end_time, capacity)
VALUES
    (1, 'Java Programming Workshop',
     'A practical workshop covering Java programming.',
     'Montreal',
     '2026-12-10 09:00:00',
     '2026-12-10 12:00:00',
     2),

    (2, 'AI Conference',
     'A conference about artificial intelligence.',
     'Toronto',
     '2026-12-11 10:00:00',
     '2026-12-11 16:00:00',
     3),

    (3, 'Cybersecurity Class',
     'A class about cybersecurity and online safety.',
     'Ottawa',
     '2026-12-12 09:00:00',
     '2026-12-12 15:00:00',
     2);

INSERT INTO Registrations
(registration_id, user_id, conference_id, status)
VALUES
    (1, 2, 1, 'CONFIRMED'),
    (2, 3, 1, 'CONFIRMED'),
    (3, 4, 1, 'WAITLISTED'),
    (4, 5, 1, 'WAITLISTED'),
    (5, 2, 2, 'CONFIRMED');

INSERT INTO WaitingList
(waiting_id, registration_id)
VALUES
    (1, 3),
    (2, 4);