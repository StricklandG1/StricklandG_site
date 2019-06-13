
public class Movie {
	private String id;
	private int year;
	private String director;
	
	Movie()
	{
		id = "";
		year = 0;
		director = "";
	}
	
	Movie(String id, int year, String director)
	{
		this.id = id;
		this.year = year;
		this.director = director;
	}
	
	public String getId() {return id;}
	public int getYear() {return year;}
	public String getDirector() {return director;}
	
	public void setId(String id) {this.id = id;}
	public void setYear(int year) {this.year = year;}
	public void setDirector(String director) {this.director = director;}
	
	boolean equals(Movie rhs)
	{
		return (this.year == rhs.year && this.director.equalsIgnoreCase(rhs.director));
	}
	
	public String toString()
	{
		return id + " " + Integer.toString(year) + " " + director;
	}
}
