package healthcare;

import java.util.LinkedList;

public class Member extends SystemUser {
	String memberNum;
	String status;
	private LinkedList<Bill> servicesReceived;
	
	// Default Constructor
	public Member() {
		super();
		this.memberNum = "";
		this.status = "";
		this.setServicesReceived(new LinkedList<Bill>());
	}
	
	// Copy Constructor
	public Member(Member otherMem) {
		super(otherMem.getName(), otherMem.getAddress(), otherMem.getCity(), otherMem.getState(), otherMem.getZipcode(),otherMem.getEmail(), otherMem.getStatus());
		this.memberNum = otherMem.getMemberNum();
		this.status = otherMem.getStatus();
		this.setServicesReceived(new LinkedList<Bill>());
	}
	
	// Parameterized Constructor
	public Member(String name, String address, String city, String state, String zipcode, String email, String password, String memberNum, String status) {
		super(name, address, city, state, zipcode, email, password);
		this.memberNum = memberNum;
		this.status = status;
		this.setServicesReceived(new LinkedList<Bill>());
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
    
    public String getMemberNum()
    {
    	return memberNum;
    }
    
    public String getStatus()
    {
    	return status;
    }
    
    //updates status with newStatus
    public void updateStatus(String newStatus)
    {
    	this.status = newStatus;
    }

    //add a new Bill to the end of the list
    public void addService(Bill bill)
    {
    	getServicesReceived().add(bill);
    }

    /**
     * Fetches the services that a Member has received
     * @return LinkedList of Bills for the Member
     */
	public LinkedList<Bill> getServicesReceived() {
		return this.servicesReceived;
	}

	public void setServicesReceived(LinkedList<Bill> servicesReceived) {
		this.servicesReceived = servicesReceived;
	}
}
