CREATE SEQUENCE event_id;
CREATE TYPE element_type AS ENUM ('unknown', 'noninteractive', 'clickable', 'writable', 'terminal');
DROP TABLE handles;
CREATE TABLE IF NOT EXISTS handles
(
  url VARCHAR,
  xpath VARCHAR,
  eltype element_type,
  URL_access BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS urls
(
  url VARCHAR,
  hash VARCHAR,
  last_update timestamp DEFAULT current_timestamp
);