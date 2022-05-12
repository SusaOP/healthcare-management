package healthcare;

public class Human {
	
	// Class Variables
	String name;
	String address;
	String city;
	String state;
	String zipcode;
	
	// Default Constructor
	public Human() {
		this.name = "";
        this.address = "";
        this.city = "";
        this.state = "";
        this.zipcode = "";
	}
		
	// Parameter Constructor
    public Human(String name, String address, String city, 
    			 String state, String zipcode) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getAddress()
    {
        return address;
    }
    
    public String getCity()
    {
        return city;
    }
    
    public void setState(String state) {
    	this.state = state;
    }
    
    public String getState() {
    	return state;
    }
    
    
    public String getZipcode()
    {
        return zipcode;
    }
}
