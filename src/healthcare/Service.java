package healthcare;

public class Service {
	String code;
	String name;
	float fee;

	// Default Constructor
	public Service () {
		this.code = "";
		this.name = "NO_SERVICE_NAME";
		this.fee = (float)0.00;
	}
	
	// Parameter Constructor
	public Service(String code, String name, float fee) {
		this.code = code;
		this.name = name;
		this.fee = fee;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String getName()
	{
		return name;
	}
	
	public float getFee()
	{
		return fee;
	}

}
