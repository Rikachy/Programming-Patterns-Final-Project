
INSERT INTO Users (user_id, full_name, email, age, role)
VALUES
    (1, 'System Admin', 'admin@example.com', 30, 'ADMIN'),
    (2, 'Ilsa Rehmat', 'ilsa@example.com', 25, 'PARTICIPANT'),
    (3, 'Jesse Oliver', 'jesse@example.com', 35, 'PARTICIPANT'),
    (4, 'Manal Fayyaz', 'manal@example.com', 28, 'PARTICIPANT'),
    (5, 'Richard Qing', 'richard@example.com', 32, 'PARTICIPANT');

INSERT INTO Credentials (user_id, password_hash)
VALUES
    (1, 'pbkdf2_sha256$210000$yYOr3s2I14HNJPn+MN89Ag==$Zyf3O3f65V7/r1Q/tw4V98fMpAa/e/xbuWnNT/FsR5E='),

    (2, 'pbkdf2_sha256$210000$8RAxnsa76ojYbTTx2Ar4QQ==$uxpBrthcOwor+cr4GhlD074EgQFa+VeLFXjsFxbzLJs='),

    (3, 'pbkdf2_sha256$210000$yiJA4H6vmJj33Kt27Ircgw==$jz66Oc9wv8uVZHu/8/O3LYXiF1wAYKcaf6hR2K6yy+Q='),

    (4, 'pbkdf2_sha256$210000$fule2Q+HoqoVg6Ew0HCjiA==$tkSwBj1vHV7Q4YRB/YtKu9NAIeSz0omfBynNdSLRGZ8='),

    (5, 'pbkdf2_sha256$210000$0Ezqk0NnSU0Zh5bPrpIOwg==$SZYgWOGEkI2Zk0apNUysaSvWjdLutLNE1tCnB+n/pZ4=');

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