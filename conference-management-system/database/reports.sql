-- Report 1: Conference Registration Summary

SELECT
    c.conference_id,
    c.capacity,
    COUNT(CASE
              WHEN r.status = 'CONFIRMED' THEN 1
        END) AS confirmed_registrations,
    c.capacity - COUNT(CASE
                           WHEN r.status = 'CONFIRMED' THEN 1
        END) AS available_seats
FROM Conferences c
         LEFT JOIN Registrations r
                   ON c.conference_id = r.conference_id
GROUP BY c.conference_id, c.capacity
ORDER BY c.conference_id;

-- Report 2: Waiting List Summary

SELECT
    c.conference_id,
    COUNT(w.waiting_id) AS waiting_count
FROM Conferences c
         LEFT JOIN Registrations r
                   ON c.conference_id = r.conference_id
                       AND r.status = 'WAITLISTED'
         LEFT JOIN WaitingList w
                   ON r.registration_id = w.registration_id
GROUP BY c.conference_id
ORDER BY waiting_count DESC, c.conference_id;

-- Report 3: Registration Status Summary

SELECT
    c.conference_id,

    COUNT(CASE
              WHEN r.status = 'CONFIRMED' THEN 1
        END) AS confirmed_count,

    COUNT(CASE
              WHEN r.status = 'WAITLISTED' THEN 1
        END) AS waitlisted_count,

    COUNT(CASE
              WHEN r.status = 'CANCELLED' THEN 1
        END) AS cancelled_count

FROM Conferences c
         LEFT JOIN Registrations r
                   ON c.conference_id = r.conference_id

GROUP BY c.conference_id
ORDER BY c.conference_id;

-- Report 4: Conference Popularity Ranking

SELECT
    c.conference_id,
    COUNT(r.registration_id) AS total_confirmed
FROM Conferences c
         LEFT JOIN Registrations r
                   ON c.conference_id = r.conference_id
                       AND r.status = 'CONFIRMED'
GROUP BY c.conference_id
ORDER BY total_confirmed DESC, c.conference_id ASC;

-- Report 5: Participant Registration History

SELECT
    r.user_id,
    r.conference_id,
    r.status,
    r.registered_at
FROM Registrations r
ORDER BY r.user_id ASC, r.registered_at DESC;

-- Report 6: Cancellation Summary

SELECT
    c.conference_id,
    COUNT(r.registration_id) AS cancelled_registrations
FROM Conferences c
         LEFT JOIN Registrations r
                   ON c.conference_id = r.conference_id
                       AND r.status = 'CANCELLED'
GROUP BY c.conference_id
ORDER BY cancelled_registrations DESC,
         c.conference_id ASC;