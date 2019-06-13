/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */

public class User {

    private String username;
    private String type;
    public User(String username, String type) {
        this.username = username;
        this.type = type;
    }

    public String getUsername() { return this.username; }
    public String getUserType() { return type; }
}
