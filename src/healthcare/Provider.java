package healthcare;

import java.util.LinkedList;

public class Provider extends SystemUser {
	
	String providerNum;
	LinkedList<Bill> servicesProvided;

    /**
     * Default Constructor
     */
	public Provider() {
		super();
		this.providerNum = "";
		this.servicesProvided = new LinkedList<Bill>();
	}

	// Parameter Constructor

    /**
     * Parameter Constructor
     * @param name          Name : String
     * @param address       Address : String
     * @param city          City : String
     * @param state         State : String
     * @param zipcode       zipcode : String
     * @param email         email : String
     * @param password      password : String
     * @param providerNum   providerNum : String
     */
	public Provider(String name, String address, String city, String state, String zipcode, String email, String password, String providerNum) {
		super(name, address, city, state, zipcode, email, password);
		this.providerNum = providerNum;
		this.servicesProvided = new LinkedList<Bill>();
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
    
    public String getState() {
    	return super.getState();
    }
    
    public String getZipcode()
    {
        return super.getZipcode();
    }
    
    public String getEmail()
    {
        return super.getEmail();
    }
    
    public String getPassword()
    {
        return super.getPassword();
    }
    
    public String getProviderNum()
    {
    	return providerNum;
    }
    
    public LinkedList<Bill> getBills()
    {
    	return servicesProvided;
    }
    
    // adds a Bill to the LinkedList
    public void addService(Bill bill)
    {
    	servicesProvided.add(bill);
    }

}
