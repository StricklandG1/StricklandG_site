public class Item
{
	private String movieId;
    private String movieName;
    public double cost;
    public int quantity;

    // Constructors
    public Item(String movieId, String movieName) 
    {
    	this.movieId = movieId;
        this.movieName = movieName;
        this.cost = 10.00;
        this.quantity = 1;
    }
    
    // Setters
    public void updateOne()
    {	
    	++this.quantity;
    	this.cost = quantity * 10;
    }

    // Getters
    public String getMovieId()
    {
    	return this.movieId;
    }
    public String getMovieTitle()
    { 
    	return this.movieName; 
    }
    public int getQuantity()
    {
    	return this.quantity;
    }
    public void setQuantity(int count)
    {
    	this.quantity += count;
    }
    public double getCost()
    {
    	return this.cost;
    }
}
