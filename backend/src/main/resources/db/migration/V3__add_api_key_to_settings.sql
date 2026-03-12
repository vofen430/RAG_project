-- Add api_key column to user_settings for per-user SiliconFlow API key
ALTER TABLE user_settings ADD COLUMN api_key VARCHAR(256) DEFAULT '';
