CREATE TABLE IF NOT EXISTS paper_tags (ID INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(200) NOT NULL);
CREATE TABLE IF NOT EXISTS paper_to_tags (ID INT AUTO_INCREMENT PRIMARY KEY, PAPER_ID INT NOT NULL, TAG_ID INT NOT NULL, CONSTRAINT FK_PAPER_TO_TAGS_PAPER_ID_ID FOREIGN KEY (PAPER_ID) REFERENCES papers(ID) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT FK_PAPER_TO_TAGS_TAG_ID_ID FOREIGN KEY (TAG_ID) REFERENCES paper_tags(ID) ON DELETE CASCADE ON UPDATE RESTRICT);

