CREATE TABLE IF NOT EXISTS articles (
                                        doi           TEXT PRIMARY KEY,
                                        title         TEXT,
                                        authors       TEXT,
                                        published     TEXT,
                                        peer_reviewed BOOLEAN
);
