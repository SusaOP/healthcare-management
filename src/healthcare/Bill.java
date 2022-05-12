package healthcare;

public class Bill {
	String date;
	String time;
	String providerNum;
	String memberNum;
	String serviceCode;
	float fee;
	String comments;
	
	// Constructors
	
	// Default Constructor
	public Bill () {
		this.date = "";
		this.time = "";
		this.providerNum = "";
		this.memberNum = "";
		this.serviceCode = "";
		this.fee = (float) (-1.00);		// change to double ?
		this.comments = "";
	}
	
	// Parameter Constructor
	public Bill (String date, String time, String providerNum, String memberNum, String serviceCode,float fee, String comments) {
		this.date = date;
		this.time = time;
		this.providerNum = providerNum;
		this.memberNum = memberNum;
		this.serviceCode = serviceCode;
		this.fee = fee;
		this.comments = comments;
	}
	 
	// set date billed
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getDate()
	{
		return date;
	}
	
	// set time billed
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getTime()
	{
		return time;
	}
	
	// set provider of bill
	public void setProviderNum(String providerNum) {
		this.providerNum = providerNum;
	}
	
	public String getProviderNum()
	{
		return providerNum;
	}
	
	// set member of bill
	public void setMemberNum(String memberNum) {
		this.memberNum= memberNum;
	}
	
	public String getMemberNum()
	{
		return memberNum;
	}
	
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	
	public String getServiceCode()
	{
		return serviceCode;
	}
	
	// set provider comments on bill
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getComments()
	{
		return comments;
	}
	
	// set the fee for services
	public void setFee(float fee) {
		this.fee = fee;
	}
	public float getFee() {
		return fee;
	}
}
