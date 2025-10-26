-- Wine Reviewer Database - Mock User Seed
-- Version: V2
-- Description: Inserts mock user for local testing and development
-- Purpose: Eliminates need to comment/uncomment mockUser() methods in controllers
-- User ID: d5779339-6ff7-46f2-9e5c-fcec29a5e6c6 (hardcoded for consistency across tests)

-- Insert mock user for local testing
-- This user can be used with POST /auth/login endpoint (email: lucasxferreira@gmail.com)
-- Created 1 day ago to simulate realistic timestamps
INSERT INTO app_user (id, display_name, email, google_id, created_at, updated_at)
VALUES (
    'd5779339-6ff7-46f2-9e5c-fcec29a5e6c6',
    'Lucas',
    'lucasxferreira@gmail.com',
    NULL, -- No google_id for mock user (can be added later when testing OAuth)
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
)
ON CONFLICT (id) DO NOTHING; -- Avoid errors if re-running migration
