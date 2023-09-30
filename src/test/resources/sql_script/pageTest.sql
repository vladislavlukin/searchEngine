INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO page (path, site_id, code, content)
VALUES
    ('/page1', @site_id, 200, " "),
    ('/page2', @site_id, 200, " "),
    ('/page3', @site_id, 200, " ");

INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example_2.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO page (path, site_id, code, content)
VALUES ('/page1', @site_id, 200, " ");