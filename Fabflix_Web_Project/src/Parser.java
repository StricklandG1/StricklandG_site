import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import javafx.util.Pair;
import java.lang.Runtime;

import javax.annotation.Resource;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;
public class Parser extends DefaultHandler {
	@Resource(name="jdbc/moviedb")
	private DataSource dataSource;
	
	private static int movieMaxId = 0;
	private static int genreMaxId = 0;
	private static int starMaxId = 0;
	
	private static boolean movieChecked = false;
	
	private static HashMap<String, ArrayList<Movie>> movies = new HashMap<String, ArrayList<Movie>>();
	private static HashMap<String, ArrayList<Movie>> dbMovies = new HashMap<String, ArrayList<Movie>>();
	private static ArrayList<String> ratings = new ArrayList<String>();
	private static String title = "";
	private static String movieId = "";
	
	private static HashMap<String, Pair<Integer, String>> stars = new HashMap<String, Pair<Integer, String>>();
	private static HashMap<String, Pair<Integer, String>> dbStars = new HashMap<String, Pair<Integer, String>>();
	
	private static HashMap<String, Integer> genres = new HashMap<String, Integer>();
	private static HashMap<String, Integer> dbGenres = new HashMap<String, Integer>();
	
	private static ArrayList<Pair<Integer, String>> genres_in_movies = new ArrayList<Pair<Integer, String>>();
	private static ArrayList<Pair<String, String>> stars_in_movies = new ArrayList<Pair<String, String>>();
	
	static File file = new File("ErrorLog.txt");
	static FileWriter fileWriter;
	static BufferedWriter buffWriter;
	static
	{
		try
		{
			fileWriter = new FileWriter(file);
			buffWriter = new BufferedWriter(fileWriter);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	int lineNo = 1;
	
	private String tempVal;
	
	private Movie tempMovie;
	
	private String dirName = "";
	
	private String filename = "";
	
	private boolean valid = true;

	public Parser()
	{
		movies = new HashMap<String, ArrayList<Movie>>();
	}
	
	public void runParse()
	{
		parseDocument();
		//printData();
	}
	
	private void parseDocument()
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			SAXParser sp = spf.newSAXParser();
			filename = "mains243.xml";
			sp.parse(filename, this);
			filename = "actors63.xml";
			sp.parse(filename, this);
			filename = "casts124.xml";
			sp.parse(filename, this);
		}
		catch (SAXException se)
		{
			se.printStackTrace();
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
	}
	
	private void printData()
	{
		
		System.out.println("///////////////////////////////////////////////////");
		for(String name : movies.keySet())
		{
			int len = movies.get(name).size();
			for(int i = 0; i < len; ++i)
			{
				System.out.println(name + " " + movies.get(name).get(i).toString());
			}
		}
		/*
		System.out.println("///////////////////////////////////////////////////");
		for(String name: stars.keySet())
		{
			System.out.println(name + " " + stars.get(name));
		}
		System.out.println("///////////////////////////////////////////////////");
		for(String name: genres.keySet())
		{
			System.out.println(name + " " + genres.get(name));
		}
		*/
		System.out.println("///////////////////////////////////////////////////");
		for(Pair<Integer, String> p : genres_in_movies)
		{
			System.out.println(p);
		}
		/*
		System.out.println("///////////////////////////////////////////////////");
		for(Pair<String, String> p:stars_in_movies)
		{
			System.out.println(p);
		}
		*/
		
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		tempVal = "";
		if (filename.equals("mains243.xml"))
		{
			if (qName.equalsIgnoreCase("film"))
			{
				tempMovie = new Movie();
				tempMovie.setDirector(dirName);
				tempMovie.setId(generateMovieId());
				//System.out.println(tempMovie.getId());
				valid = true;
			}
		}
		else if (filename.equals("casts124.xml"))
		{
			if (qName.equalsIgnoreCase("filmc"))
			{
				valid = true;
			}
		}
		else if (filename.equals("actors63.xml"))
		{
			
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		tempVal = new String(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (valid && filename.equals("mains243.xml"))
		{
			if (qName.equalsIgnoreCase("dirname"))
			{
				dirName = tempVal;
			}
			else if (qName.equalsIgnoreCase("t"))
			{
				title = tempVal;
			}
			else if (qName.equalsIgnoreCase("year"))
			{
				if (verifyYear(tempVal))
					tempMovie.setYear(Integer.parseInt(tempVal));
				else
				{
					try {
						buffWriter.write(lineNo + " " + title + " directed by " + tempMovie.getDirector() + " contains invalid year: no addition to database");
						buffWriter.newLine();
						++lineNo;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					valid = false;
				}
			}
			else if (qName.equalsIgnoreCase("film"))
			{
				if (!checkExistingMovie(tempMovie))
				{
					++movieMaxId;
					if (movies.containsKey(title))
						movies.get(title).add(tempMovie);
					else
					{
						movies.put(title, new ArrayList<Movie>());
						movies.get(title).add(tempMovie);
					}
					ratings.add(tempMovie.getId());
					
				}
				else
				{
					try {
						buffWriter.write(lineNo + " " + title + " directed by " + tempMovie.getDirector() + " in " + tempMovie.getYear() + " already exists: not added to database");
						buffWriter.newLine();
						++lineNo;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				valid = true;
			}
			else if (qName.equalsIgnoreCase("cat"))
			{
				//System.out.println("genres: " + tempMovie.getId());
				if (checkExistingGenreDb(tempVal))
				{
					if (!checkExistingGenreMoviePair(dbGenres.get(tempVal), tempMovie.getId()))
					{
						genres_in_movies.add(new Pair<Integer,String>(dbGenres.get(tempVal), tempMovie.getId()));
					}
					else
					{
						try {
							buffWriter.write(lineNo + " " + tempVal + " already exists for " + title);
							buffWriter.newLine();
							++lineNo;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if (checkExistingGenreParsed(tempVal))
				{
					if (!checkExistingGenreMoviePair(genres.get(tempVal), tempMovie.getId()))
					{
							genres_in_movies.add(new Pair<Integer, String>(genres.get(tempVal), tempMovie.getId()));
					}
					else
					{
						try {
							buffWriter.write(lineNo + " " + tempVal + " already exists for " + title);
							buffWriter.newLine();
							++lineNo;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{
					genres.put(tempVal, genreMaxId);
					genres_in_movies.add(new Pair<Integer, String>(genreMaxId, tempMovie.getId()));
					++genreMaxId;
				}
			}
		}
		else if (filename.equals("actors63.xml"))
		{
			if (qName.equalsIgnoreCase("stagename"))
			{
				title = tempVal;
			}
			else if (qName.equalsIgnoreCase("dob"))
			{
				if (verifyYear(tempVal))
				{
					int year = Integer.parseInt(tempVal);
					if (!checkExistingStar(title))
					{
						stars.put(title, new Pair(year, generateStarId()));
					}
					else
					{
						try {
							buffWriter.write(lineNo + " " + title + " born: " + year + " already exists");
							buffWriter.newLine();
							++lineNo;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{
					try {
						buffWriter.write(lineNo + " " + title + " does not have valid birthyear. setting to NULL");
						buffWriter.newLine();
						++lineNo;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stars.put(title,  new Pair(-1, generateStarId()));
				}
			}
		}
		else if (valid && filename.equals("casts124.xml"))
		{
			if (qName.equalsIgnoreCase("is"))
			{
				dirName = tempVal;
			}
			else if (qName.equals("filmc"))
			{
				movieChecked = false;
				valid = true;
			}
			else if (!movieChecked && qName.equalsIgnoreCase("t"))
			{
				title = tempVal;
				movieId = (checkTitleDirector(title, dirName));
				if (movieId == null)
				{
					valid = false;
					//System.out.println(title + " does not exist");
				}
				movieChecked = true;
			}
			else if (qName.equalsIgnoreCase("a") && movieId != null)
			{
				if (checkExistingStar(tempVal))
				{
					if (dbStars.containsKey(tempVal))
					{
						stars_in_movies.add(new Pair(dbStars.get(tempVal).getValue(), movieId));
					}
					else if (stars.containsKey(tempVal))
					{
						stars_in_movies.add(new Pair(stars.get(tempVal).getValue(), movieId));
					}
				}
				//else
					//System.out.println(tempVal + " does not exist.");
			}
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		long start = System.currentTimeMillis();
		populateMapsFromDb(dbMovies);
		Parser spe = new Parser();
		spe.runParse();
		long end = System.currentTimeMillis() - start;
		System.out.println("Elapsed time: " + end + "ms");
		//System.out.println(Runtime.getRuntime().totalMemory() + " " + Runtime.getRuntime().freeMemory());
		
		try 
		{
			if (buffWriter != null)
				buffWriter.close();

			if (fileWriter != null)
				fileWriter.close();
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		
		try 
		{
			batchInsert();
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	public static boolean verifyYear(String year)
	{
		int strLen = year.length();
		if (strLen == 4)
		{
			boolean notDone = true;
			int i = 0;
			while (notDone && i < 4)
			{
				notDone = Character.isDigit(year.charAt(i));
				++i;
			}
			
			return notDone;
		}
		
		return false;
	}
	
	public static void populateMapsFromDb(HashMap<String, ArrayList<Movie>> dbMovies) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";
		
		try
		{
			//con = DriverManager.getConnection(jdbcURL, "root", "Cdkagpw#6");
			con = DriverManager.getConnection(jdbcURL, "mytestuser", "mypassword");
			stmt = con.createStatement();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		String query = "";
		
		try
		{
			query = "SELECT MAX(id) FROM movies;";
			rs = stmt.executeQuery(query);
			if (rs.next())
				movieMaxId = Integer.parseInt(rs.getString("MAX(id)").substring(2)) + 1;
			query = "SELECT MAX(id) FROM genres;";
			rs = stmt.executeQuery(query);
			if (rs.next())
				genreMaxId = rs.getInt("MAX(id)") + 1;
			query = "SELECT MAX(id) FROM stars;";
			rs = stmt.executeQuery(query);
			if (rs.next())
				starMaxId = Integer.parseInt(rs.getString("MAX(id)").substring(2)) + 1;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		Movie m = new Movie();
		try
		{
			query = "SELECT * FROM movies;";
			
			stmt = con.createStatement();
			
			rs = stmt.executeQuery(query);
			String id;
			String title;
			int year;
			String director;
			while(rs.next())
			{
				id = rs.getString("id");
				title = rs.getString("title");
				year = rs.getInt("year");
				director = rs.getString("director");
				if (dbMovies.containsKey(title))
				{
					dbMovies.get(title).add(new Movie(id, year, director));
				}
				else
				{
					dbMovies.put(title, new ArrayList<Movie>());
					dbMovies.get(title).add(new Movie(id, year, director));
				}
			}
			query = "SELECT * FROM genres;";
			rs = stmt.executeQuery(query);
			while (rs.next())
			{
				int genreId = rs.getInt("id");
				title = rs.getString("name");
				
				dbGenres.put(title, genreId);
			}
			query = "SELECT * FROM stars;";
			rs = stmt.executeQuery(query);
			while (rs.next())
			{
				title = rs.getString("name");
				year = rs.getInt("birthYear");
				id = rs.getString("id");
				
				dbStars.put(title, new Pair(year, id));
			}
			
			rs.close();
			stmt.close();
			con.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean checkExistingGenreDb(String genre)
	{
		return (dbGenres.containsKey(genre));
	}
	
	public static boolean checkExistingGenreParsed(String genre)
	{
		return (genres.containsKey(genre));
	}
	
	public static boolean checkExistingMovie(Movie otherMovie)
	{
		boolean same = false;
		if (dbMovies.containsKey(title))
		{
			int len = dbMovies.get(title).size();
			int i = 0;
			while (!same && i < len)
			{
				same = dbMovies.get(title).get(i).equals(otherMovie);
				++i;
			}
		}
		if (movies.containsKey(title) && !same)
		{
			int len = movies.get(title).size();
			int i = 0;
			while(!same && i < len)
			{
				same = movies.get(title).get(i).equals(otherMovie);
				++i;
			}
		}
		return same;
	}
	
	public static String checkTitleDirector(String title, String checkDir)
	{
		if (dbMovies.containsKey(title))
		{
			int len = dbMovies.get(title).size();
			boolean found = false;
			int i = 0;
			String result = "";
			while (i < len && !found)
			{
				result = dbMovies.get(title).get(i).getId();
				found = (dbMovies.get(title).get(i).getDirector().equalsIgnoreCase(checkDir));
				++i;
			}
			if (found) return result;
			else return null;
		}
		else if (movies.containsKey(title))
		{
			int len = movies.get(title).size();
			boolean found = false;
			int i = 0;
			String result = "";
			while (i < len && !found)
			{
				result = movies.get(title).get(i).getId();
				found = (movies.get(title).get(i).getDirector().equalsIgnoreCase(checkDir));
				++i;
			}
			if (found) return result;
			else return null;
		}
		else return null;
	}

	public static boolean checkExistingGenreMoviePair(int genreId, String movieId)
	{
		boolean found = false;
		Pair<Integer, String> p = new Pair<Integer, String>(genreId, movieId);
		int len = genres_in_movies.size();
		int i = 0;
		while (i < len && !found)
		{
			found = (genres_in_movies.get(i).equals(p));
			++i;
		}
		return found;
	}
	
	public static boolean checkExistingStar(String name)
	{
		return (dbStars.containsKey(name) || stars.containsKey(name));
	}
	
	
	public static String generateMovieId()
	{
		return "tt0" + Integer.toString(movieMaxId);
	}
	
	public static String generateStarId()
	{
		return "nm" + Integer.toString(++starMaxId);
	}
	
	@SuppressWarnings("restriction")
	public static void batchInsert() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		/////////////////////////////////////// Batch insert for inserting movies from movie map:
		// private static HashMap<String, ArrayList<Movie>> movies
		
		Connection dbcon = null;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";
		
		try
		{
			dbcon = DriverManager.getConnection(jdbcURL, "mytestuser", "mypassword");
			dbcon.setAutoCommit(false); 
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			String movieInsertQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?);";
			
			PreparedStatement movieStatement = dbcon.prepareStatement(movieInsertQuery);
			
			int count = 0;
			for (String key : movies.keySet())
			{
				int len = movies.get(key).size();
				for (int i = 0; i < len; ++i)
				{
					movieStatement.setString(1, movies.get(key).get(i).getId());		// movie ID
					movieStatement.setString(2, key);									// movie title
					movieStatement.setInt(3, movies.get(key).get(i).getYear());			// movie year
					movieStatement.setString(4, movies.get(key).get(i).getDirector());	// movie director
					
					movieStatement.addBatch();
					
					++count;
					
					// Execute the batch of query statements after a 1000 statements or if the count of statements to
					// 	execute reach the size of the map
					if (count % 1000 == 0 || count == len)
					{
						movieStatement.executeBatch();
						count = 0;
					}
				}
			}
			// Execute any remaining queries in the batch
			movieStatement.executeBatch();
			System.out.println("***movies batch insert complete!***");
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the movies batch insert!");
			e.printStackTrace();
		}
		
		//////////////////////////////////// Batch insert for inserting stars from stars map:
		// private static HashMap<String, Pair<Integer, String>> stars
		try
		{
			// Batch insert for inserting movies from movie map:
			String starInsertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?);";
			
			PreparedStatement starStatement = dbcon.prepareStatement(starInsertQuery);
			
			int count = 0;
			for (String key : stars.keySet())
			{
				starStatement.setString(1, stars.get(key).getValue());	// star ID
				starStatement.setString(2, key);						// star name
				
				if (stars.get(key).getKey() == -1)
				{
					starStatement.setNull(3, java.sql.Types.INTEGER);
				}
				else
				{
					starStatement.setInt(3, stars.get(key).getKey());		// star birthYear
				}
				
				starStatement.addBatch();
					
				++count;
				
				// Execute the batch of query statements after a 1000 statements or if the count of statements to
				// 	execute reach the size of the map
				if (count % 1000 == 0 || count == stars.size())
				{
					starStatement.executeBatch();
					count = 0;
				}
			}
			
			// Execute any remaining queries in the batch
			starStatement.executeBatch();
			System.out.println("***stars batch insert complete!***");
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the stars batch insert!");
			e.printStackTrace();
		}
		
		////////////////////////////////////Batch insert for inserting genres from genres map:
		// private static HashMap<String, Integer> genres
		try
		{
			// Batch insert for inserting movies from movie map:
			String genreInsertQuery = "INSERT INTO genres (id, name) VALUES (?, ?);";
			
			PreparedStatement genreStatement = dbcon.prepareStatement(genreInsertQuery);
			
			int count = 0;
			for (String key : genres.keySet())
			{
				genreStatement.setInt(1, genres.get(key));				// genre ID
				genreStatement.setString(2, key);						// genre name
					
				genreStatement.addBatch();
					
				++count;
				
				// Execute the batch of query statements after a 1000 statements or if the count of statements to
				// 	execute reach the size of the map
				if (count % 1000 == 0 || count == genres.size())
				{
					genreStatement.executeBatch();
					count = 0;
				}
			}
			
			// Execute any remaining queries in the batch
			genreStatement.executeBatch();
			System.out.println("***genres batch insert complete!***");
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the genres batch insert!");
			e.printStackTrace();
		}
		
		////////////////////////////////////Batch insert for inserting genres from genres_in_movies array list:
		// private static ArrayList<Pair<Integer, String>> genres_in_movies
		try
		{
			// Batch insert for inserting movies from genres_in_movies array list:
			String genresInMoviesInsertQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?);";
			
			PreparedStatement genresInMoviesStatement = dbcon.prepareStatement(genresInMoviesInsertQuery);
			
			int count = 0;
			for (Pair<Integer, String> item : genres_in_movies)
			{
				genresInMoviesStatement.setInt(1, item.getKey());				// genre ID
				genresInMoviesStatement.setString(2, item.getValue());			// movie ID
					
				genresInMoviesStatement.addBatch();
					
				++count;
				
				// Execute the batch of query statements after a 1000 statements or if the count of statements to
				// 	execute reach the size of the map
				if (count % 1000 == 0 || count == genres_in_movies.size())
				{
					genresInMoviesStatement.executeBatch();
					count = 0;
				}
			}
			
			// Execute any remaining queries in the batch
			genresInMoviesStatement.executeBatch();
			System.out.println("***genres_in_movies batch insert complete!***");
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the genres_in_movies batch insert!");
			e.printStackTrace();
		}
		
		////////////////////////////////////Batch insert for inserting genres from stars_in_movies array list:
		// private ArrayList<Pair<String, String>> stars_in_movies
		try
		{
			// Batch insert for inserting movies from stars_in_movies array list:
			String starsInMoviesInsertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?);";
			
			PreparedStatement starsInMoviesStatement = dbcon.prepareStatement(starsInMoviesInsertQuery);
			
			int count = 0;
			for (Pair<String, String> item : stars_in_movies)
			{
				starsInMoviesStatement.setString(1, item.getKey());				// genre ID
				starsInMoviesStatement.setString(2, item.getValue());			// genre name
					
				starsInMoviesStatement.addBatch();
					
				++count;
				
				// Execute the batch of query statements after a 1000 statements or if the count of statements to
				// 	execute reach the size of the map
				if (count % 1000 == 0 || count == stars_in_movies.size())
				{
					starsInMoviesStatement.executeBatch();
					count = 0;
				}
			}
			
			// Execute any remaining queries in the batch
			starsInMoviesStatement.executeBatch();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the stars_in_movies batch insert!");
			e.printStackTrace();
		}
		
		System.out.println("***stars_in_movies batch insert complete!***");
		
		////////////////////////////////////Batch insert for inserting genres from ratings array list:
		// private static ArrayList<String> ratings
		try
		{
			int count = 0;
			String ratingsInsertQuery = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, ?, ?);";
			PreparedStatement ratingsStatement = dbcon.prepareStatement(ratingsInsertQuery);
			
			int len = ratings.size();
			for (int i = 0; i < len; i++)
			{
				ratingsStatement.setString(1, ratings.get(i));
				ratingsStatement.setFloat(2, (float) 0.0);
				ratingsStatement.setInt(3, 0);
				
				ratingsStatement.addBatch();
				
				++count;
				
				if (count % 1000 == 0 || count == ratings.size())
				{
					ratingsStatement.executeBatch();
					count = 0;
				}
			}
			
			ratingsStatement.executeBatch();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong with the ratings batch insert!");
			e.printStackTrace();
		}
		
		dbcon.setAutoCommit(true);
		dbcon.close();
		
		System.out.println("***ratings batch insert complete!***");
	}
}
