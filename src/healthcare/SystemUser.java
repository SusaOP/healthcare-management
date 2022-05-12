package healthcare;


public class SystemUser extends Human {
	
	//Class Variables
	String email;
	String password;

	// Default Constructor
	public SystemUser() {
		super();
        this.email = "";
        this.password = "";
	}
	
	
	// Parameter Constructor
	public SystemUser(String name, String address, String city, String state, String zipcode, String email, String password) {
		super(name, address, city, state, zipcode);
		this.email = email;
		this.password = password;
	}
	
	public String getName()
    {
        return super.getName();
    }
    
    public String getAddress()
    {
        return super.getAddress();
    }
    
    public String getCity()
    {
        return super.getCity();
    }
    
    public String getZipcode()
    {
        return super.getZipcode();
    }
    
    public String getEmail()
    {
        return email;
    }
    
    public String getPassword()
    {
        return password;
    }

}
