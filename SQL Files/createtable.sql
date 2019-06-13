/* CREATE Database moviedb; */
DROP DATABASE moviedb;
CREATE DATABASE moviedb;
USE moviedb;
CREATE TABLE IF NOT EXISTS movies (
	id			varchar(10) NOT NULL DEFAULT ' ',
	title		varchar(100) NOT NULL DEFAULT ' ',
	year		integer NOT NULL,
	director 	varchar(100) NOT NULL DEFAULT ' ',
	PRIMARY KEY (id) 
);

CREATE TABLE IF NOT EXISTS stars (
	id			varchar(10) NOT NULL DEFAULT ' ',
	name		varchar(100) NOT NULL DEFAULT ' ',
    birthYear	integer,
	PRIMARY KEY (id) 
);

CREATE TABLE IF NOT EXISTS stars_in_movies (
	starId		varchar(10) NOT NULL DEFAULT ' ',
    movieId		varchar(10) NOT NULL DEFAULT ' ',
    FOREIGN KEY (starId) REFERENCES stars (id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS genres (
	id			integer NOT NULL AUTO_INCREMENT,
    name		varchar(32) NOT NULL DEFAULT ' ',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS genres_in_movies (
	genreId		integer NOT NULL,
    movieId		varchar(10) NOT NULL DEFAULT ' ',
    FOREIGN KEY (genreId) REFERENCES genres (id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS creditcards (
	id			varchar(20) NOT NULL DEFAULT ' ',
    firstName	varchar(50) NOT NULL DEFAULT ' ',
    lastName	varchar(50) NOT NULL DEFAULT ' ',
    expiration	date NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS customers (
	id			integer NOT NULL AUTO_INCREMENT,
    firstName	varchar(50) NOT NULL DEFAULT ' ',
    lastName	varchar(50) NOT NULL DEFAULT ' ',
    ccId		varchar(20) NOT NULL DEFAULT ' ',
    address		varchar(200) NOT NULL DEFAULT ' ',
    email		varchar(50) NOT NULL DEFAULT ' ',
    password	varchar(20) NOT NULL DEFAULT ' ',
    PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sales (
	id			integer NOT NULL AUTO_INCREMENT,
    customerId	integer NOT NULL,
    movieId		varchar(10) NOT NULL DEFAULT ' ',
    saleDate	date NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ratings (
	movieId		varchar(10) NOT NULL DEFAULT ' ',
    rating		float NOT NULL DEFAULT 0.0,
    numVotes	integer NOT NULL DEFAULT 0,
    FOREIGN KEY (movieId) REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employees(
	email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    name VARCHAR(50)
);

INSERT INTO employees VALUES('classta@email.edu', 'classta', 'CS122B TA');

CREATE FULLTEXT INDEX ft_idx ON movies(title);

DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie(IN newTitle VARCHAR(100), IN newYear INT, IN newDirector VARCHAR(100), IN newGenre VARCHAR(32), IN newStar VARCHAR(100), OUT result VARCHAR(50))
BEGIN
	DECLARE checkNum INT;
    DECLARE movieId VARCHAR(10);
    DECLARE starId VARCHAR(10);
    DECLARE idNum INT;
    SELECT COUNT(*) INTO checkNum
    FROM movies
    WHERE movies.title = newTitle AND movies.year = newYear AND movies.director = newDirector;
    
    IF checkNum > 0 THEN # if movie exists in db
		SELECT 'Error: movie already exists in database' INTO result;
	ELSE # if no entry exists
		SELECT MAX(id) INTO movieId #get max id
        FROM movies;
        SELECT SUBSTR(movieId, 3) INTO movieId; #parse out first 2 chars
        SELECT CAST(movieId AS SIGNED) INTO idNum; #cast to int
        SET idNum = idNum + 1; #add 1
        SELECT CAST(idNum AS CHAR) INTO movieId; #cast back to string
        SELECT CONCAT("tt", movieId) INTO movieId;#add chars back
        INSERT INTO movies VALUES(movieId, newTitle, newYear, newDirector); #insert data into movies table
        INSERT INTO ratings VALUES(movieId, 0.0, 0);
        SELECT COUNT(*) INTO checkNum
        FROM genres
        WHERE genres.name = newGenre;
        IF checkNum > 0 THEN # if genre exists in db
			SELECT genres.id INTO idNum FROM genres WHERE genres.name = newGenre; # get the id
            INSERT INTO genres_in_movies VALUES(idNum, movieId); # insert genre id and previously generated movie id into db
        ELSE # if genre doesn't exist
			SELECT MAX(genres.id) INTO idNum #grab max id
            FROM genres;
            SET idNum = idNum + 1; #increment by 1
            INSERT INTO genres VALUES(idNum, newGenre); #put into genres table
            INSERT INTO genres_in_movies VALUES(idNum, movieId); # pair with newly inserted movie
        END IF;
        SELECT COUNT(*) INTO checkNum
        FROM stars
        WHERE stars.name = newStar;
        IF checkNum > 0 THEN # check for existing star
			SELECT stars.id INTO starId FROM stars WHERE stars.name = newStar; # 
            INSERT INTO stars_in_movies VALUES(starId, movieId);
        ELSE # if star doesn't exist
			SELECT MAX(stars.id) INTO starId # get max id
            FROM stars;
            SELECT SUBSTR(starId, 3) INTO starId; # get num portion of id
            SELECT CAST(starId AS SIGNED) INTO idNum; #cast to int
            SET idNum = idNum + 1; # add 1
            SELECT CAST(idNum AS CHAR) INTO starId; #cast back to string
            SELECT CONCAT("nm", starId) INTO starId;# add chars back to front
            INSERT INTO stars VALUES(starId, newStar, NULL); #insert into stars
            INSERT INTO stars_in_movies VALUES(starId, movieId);# pair with movie
        END IF;
        SELECT 'Movie successfuly added!' INTO result;
	END IF;
END $$

DELIMITER ;