CREATE TABLE IF NOT EXISTS ES_STREAM (
  ID              UUID     PRIMARY KEY,
  VERSION         INTEGER  NOT NULL
);

CREATE TABLE IF NOT EXISTS ES_EVENT (
  ID              BIGSERIAL  PRIMARY KEY,
  TRANSACTION_ID  XID8       NOT NULL,
  STREAM_ID       UUID       NOT NULL REFERENCES ES_STREAM (ID),
  VERSION         INTEGER    NOT NULL,
  EVENT_TYPE      TEXT       NOT NULL,
  JSON_DATA       JSON       NOT NULL,
  UNIQUE (STREAM_ID, VERSION)
);

CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_TRANSACTION_ID_ID ON ES_EVENT (TRANSACTION_ID, ID);
CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_STREAM_ID ON ES_EVENT (STREAM_ID);
CREATE INDEX IF NOT EXISTS IDX_ES_EVENT_VERSION ON ES_EVENT (VERSION);
