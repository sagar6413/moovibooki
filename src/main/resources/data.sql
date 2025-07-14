-- Roles
INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER'), (3, 'THEATER_OWNER') ON CONFLICT DO NOTHING;

-- Users
INSERT INTO users (id, username, email, password, enabled, email_verified, created_at, updated_at) VALUES
(1, 'admin', 'admin@moovibooki.com', '$2a$10$adminpass', true, true, NOW(), NOW()),
(2, 'user1', 'user1@moovibooki.com', '$2a$10$user1pass', true, true, NOW(), NOW()),
(3, 'owner1', 'owner1@moovibooki.com', '$2a$10$owner1pass', true, true, NOW(), NOW()) ON CONFLICT DO NOTHING;

-- User Roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3) ON CONFLICT DO NOTHING;

-- Theaters
INSERT INTO theaters (id, name, location, amenities, owner_id, created_at, updated_at, logo_url) VALUES
(1, 'Grand Cinema', 'Downtown', 'Dolby Atmos, Recliner Seats', 3, NOW(), NOW(), NULL) ON CONFLICT DO NOTHING;

-- Screens
INSERT INTO screens (id, name, capacity, layout, category, theater_id, created_at, updated_at) VALUES
(1, 'Screen 1', 100, 'Standard', 'Regular', 1, NOW(), NOW()),
(2, 'Screen 2', 80, 'Standard', 'Premium', 1, NOW(), NOW()) ON CONFLICT DO NOTHING;

-- Movies
INSERT INTO movies (id, title, genre, duration, rating, movie_cast, synopsis, poster_url, release_date, language, created_at, updated_at) VALUES
(1, 'Inception', 'Sci-Fi', 148, 'PG-13', 'Leonardo DiCaprio, Joseph Gordon-Levitt', 'A thief who steals corporate secrets through dream-sharing technology.', NULL, '2010-07-16', 'English', NOW(), NOW()),
(2, 'The Dark Knight', 'Action', 152, 'PG-13', 'Christian Bale, Heath Ledger', 'Batman faces the Joker, a criminal mastermind.', NULL, '2008-07-18', 'English', NOW(), NOW()) ON CONFLICT DO NOTHING;

-- Shows
INSERT INTO shows (id, movie_id, screen_id, start_time, end_time, price, created_at, updated_at) VALUES
(1, 1, 1, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 2 hours', 300, NOW(), NOW()),
(2, 2, 2, NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 2 hours', 350, NOW(), NOW()) ON CONFLICT DO NOTHING; 