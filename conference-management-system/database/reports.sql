-- Report 1: Conference Overview
SELECT c.conference_id, c.title, c.location, c.start_time, c.end_time, c.capacity,
       COUNT(r.registration_id) AS confirmed,
       c.capacity - COUNT(r.registration_id) AS available_seats
FROM Conferences c
         LEFT JOIN Registrations r
                   ON r.conference_id = c.conference_id AND r.status = 'CONFIRMED'
GROUP BY c.conference_id
ORDER BY c.start_time;

-- Report 2: Conference Participant Report (? = conference_id)
SELECT u.user_id, u.full_name, u.email, r.registered_at
FROM Registrations r
         JOIN Users u ON u.user_id = r.user_id
WHERE r.conference_id = ? AND r.status = 'CONFIRMED'
ORDER BY u.full_name;

-- Report 3: Waiting-List Report (? = conference_id)
SELECT ROW_NUMBER() OVER (ORDER BY w.waiting_id) AS queue_position,
    u.full_name, u.email, w.joined_at
FROM WaitingList w
         JOIN Registrations r ON r.registration_id = w.registration_id
         JOIN Users u ON u.user_id = r.user_id
WHERE r.conference_id = ? AND r.status = 'WAITLISTED'
ORDER BY w.waiting_id;

-- Report 4: Participant Registration Report (? = user_id)
SELECT c.conference_id, c.title, c.start_time, c.end_time, r.status
FROM Registrations r
         JOIN Conferences c ON c.conference_id = r.conference_id
WHERE r.user_id = ? AND r.status IN ('CONFIRMED', 'WAITLISTED')
ORDER BY c.start_time;

-- Report 5: Conference Occupancy Report
SELECT c.conference_id, c.title, c.capacity,
       COUNT(r.registration_id) AS seats_occupied,
       ROUND(100.0 * COUNT(r.registration_id) / c.capacity, 1) AS occupancy_percent
FROM Conferences c
         LEFT JOIN Registrations r
                   ON r.conference_id = c.conference_id AND r.status = 'CONFIRMED'
GROUP BY c.conference_id
ORDER BY c.conference_id;

-- Report 6: Unregistered Conferences Report
SELECT c.conference_id, c.title, c.start_time
FROM Conferences c
WHERE NOT EXISTS (
    SELECT 1 FROM Registrations r
    WHERE r.conference_id = c.conference_id
      AND r.status IN ('CONFIRMED', 'WAITLISTED')
)
ORDER BY c.conference_id;