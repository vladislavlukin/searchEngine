INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO lemma (lemma, frequency, site_id)
VALUES
    ('house', 1, @site_id),
    ('home', 1, @site_id),
    ('window', 1, @site_id);

INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example_2.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO lemma (lemma, frequency, site_id)
VALUES ('house', 1, @site_id),
       ('home', 1, @site_id),
       ('window', 1, @site_id);