package http;

public class User {

	public String name, surname;
	public final String email, password;

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User(String name, String surname, String email, String password) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
	}

}
