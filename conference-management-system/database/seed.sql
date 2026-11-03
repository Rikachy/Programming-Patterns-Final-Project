-- Sample registrations (Ilsa)

INSERT INTO Registrations
(registration_id, user_id, conference_id, status)
VALUES
    (1, 2, 1, 'CONFIRMED'),
    (2, 3, 1, 'CONFIRMED'),
    (3, 4, 1, 'WAITLISTED'),
    (4, 5, 1, 'WAITLISTED'),
    (5, 2, 2, 'CONFIRMED');

-- Sample waiting list (Ilsa)

INSERT INTO WaitingList
(waiting_id, registration_id)
VALUES
    (1, 3),
    (2, 4);