import java.util.ArrayList;

public class Cart 
{
	protected ArrayList<Item> cartItems;
	// New cart as empty vector of items
	public Cart()
	{
		cartItems = new ArrayList<Item>();
	}
	public ArrayList<Item> getCart()
	{
		return cartItems;
	}
	public void updateItem(Item newItem)
	{
		for (Item item : this.cartItems)
		{
			String existingId = item.getMovieId();
			String newId = newItem.getMovieId();
			if (newId.equals(existingId))
			{
				item.updateOne();
				break;
			}
		}
	}
	public void updateQuantity(String movieId, int quantity)
	{
		for (Item item : this.cartItems)
		{
			if (item.getMovieId().equals(movieId))
			{
				if (quantity == 0)
				{
					this.cartItems.remove(item);
					break;
				}
				else
				{
					item.quantity = quantity;
					item.cost = quantity * 10.00;
					break;
				}
			}
		}
	}
	public void addItem(Item newItem)
	{
		if (this.alreadyContains(newItem))
		{
			newItem.setQuantity(newItem.getQuantity() + 1);
		}
		else
		{
			cartItems.add(newItem);
		}
	}
	public void removeItem(Item existingItem)
	{
		cartItems.remove(existingItem);
	}
	public void deleteCart()
	{
		this.cartItems.removeAll(this.cartItems);
	}
	public boolean alreadyContains(Item newItem)
	{
		for (Item item : this.cartItems)
		{
			String existingId = item.getMovieId();
			String newId = newItem.getMovieId();
			
			if (existingId.equals(newId))
			{
				return true;
			}
		}
		
		return false;
	}
	public int getCartSize()
	{
		return cartItems.size();
	}
	public double getTotal()
	{
		double result = 0.0;
		for (Item item : this.cartItems) 
		{
			result += item.getCost();
		}
		
		return result;
	}
	
	
	// For debug purposes
	public void printCart()
	{
		System.out.println("MovieID " + " MovieName " + " Quantity " + " Cost "); 
		for (Item item : cartItems)
		{
			System.out.println(item.getMovieId() + " " + item.getMovieTitle() + " " + item.getQuantity() + " " + item.getCost()); 
		}
	}
}
