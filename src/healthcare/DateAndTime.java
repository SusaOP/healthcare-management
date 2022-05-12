package healthcare;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DateAndTime {

	/**
	 * Date and Time variables
	 */
	private String serviceDate;
	private String serviceTime;
	
	private LocalDateTime currentTime;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:MM:SS");
	
	public DateAndTime() {
		currentTime = LocalDateTime.now();
		serviceDate = (currentTime.format(formatter)).split(" ")[0];
		serviceTime = (currentTime.format(formatter)).split(" ")[1];
	}
	
	public String getDate() {
	
		return (serviceDate);
	}
	
	public String getTime() {
	
		return (serviceTime);
	}
}
