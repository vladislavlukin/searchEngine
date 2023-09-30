INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO page (path, site_id, code, content)
VALUES
    ('/page1', @site_id, 200, " "),
    ('/page2', @site_id, 200, " "),
    ('/page3', @site_id, 200, " ");

INSERT INTO lemma (lemma, frequency, site_id)
VALUES
    ('house', 2, @site_id),
    ('home', 2, @site_id),
    ('window', 2, @site_id);

SET @lemma_id_house = (SELECT id FROM lemma WHERE lemma = 'house' AND site_id = @site_id);
SET @lemma_id_home = (SELECT id FROM lemma WHERE lemma = 'home' AND site_id = @site_id);
SET @lemma_id_window = (SELECT id FROM lemma WHERE lemma = 'window' AND site_id = @site_id);

SET @page_id_page1 = (SELECT id FROM page WHERE path = '/page1' AND site_id = @site_id);
SET @page_id_page2 = (SELECT id FROM page WHERE path = '/page2' AND site_id = @site_id);
SET @page_id_page3 = (SELECT id FROM page WHERE path = '/page3' AND site_id = @site_id);

INSERT INTO identifier (number, lemma_id, page_id)
VALUES
    (1, @lemma_id_house, @page_id_page1),
    (1, @lemma_id_home, @page_id_page1),
    (1, @lemma_id_window, @page_id_page1),
    (1, @lemma_id_house, @page_id_page2),
    (3, @lemma_id_home, @page_id_page2),
    (10, @lemma_id_window, @page_id_page3);


INSERT INTO site (status, creation_time, error, url, name)
VALUES ('INDEXING', NOW(), NULL, 'https://example_2.com', 'Example Site');

SET @site_id = LAST_INSERT_ID();

INSERT INTO page (path, site_id, code, content)
VALUES ('/page1', @site_id, 200, " ");

SET @page_id_page1 = LAST_INSERT_ID();

INSERT INTO lemma (lemma, frequency, site_id)
VALUES ('house', 1, @site_id),
       ('home', 1, @site_id),
       ('window', 1, @site_id);

SET @lemma_id_house = (SELECT id FROM lemma WHERE lemma = 'house' AND site_id = @site_id);
SET @lemma_id_home = (SELECT id FROM lemma WHERE lemma = 'home' AND site_id = @site_id);
SET @lemma_id_window = (SELECT id FROM lemma WHERE lemma = 'window' AND site_id = @site_id);

INSERT INTO identifier (number, lemma_id, page_id)
VALUES
    (1, @lemma_id_house, @page_id_page1),
    (1, @lemma_id_home, @page_id_page1),
    (1, @lemma_id_window, @page_id_page1);

