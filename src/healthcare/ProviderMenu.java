package healthcare;

import java.util.ListIterator;
import java.util.Scanner;

public class ProviderMenu {
	
	Terminal terminal;
	Provider curProvider;
	Member validatedMember;
	boolean validated = false;

	/**
	 * Pass in terminal object to give access to ArrayLists
	 * @param terminal
	 * @param curProvider
	 */
	public ProviderMenu(Terminal terminal, Provider curProvider) {
		this.terminal = terminal;
		this.curProvider = curProvider;
		this.validatedMember = null;
		this.validated = false;
	}

	/**
	 * Search for a member and validate their membership status
	 * @param cardNum
	 * @return
	 */
	public String validateMember(String cardNum) {
		//Have user Enter the Members card number
		System.out.print("Enter Member's card number: ");
		
		//search members ArrayList for entered number
		ListIterator<Member> memberIt = terminal.members.listIterator();
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			if(curMember.memberNum.equals(cardNum)) {
				System.out.println("Membership Status: " + curMember.status);
				
				//update validatedMember if the member found is Validated
				if (curMember.getStatus().equals("Validated")) {
					System.out.println("set validatedMember to curMember " + curMember.status);
					validated = true;
					validatedMember = curMember;
				} else if (curMember.getStatus().equals("Member suspended")) {
					validated = false;
					return ("Member suspended");
				}
				
				return curMember.status;
			}
	     }
		System.out.println("Invalid number");
		return ("Invalid number");
	}

	/**
	 *
	 * @param bill		Bill for a specific member
	 * @param memNum	Member Number for billed member
	 */
	public void billHealthcare(Bill bill, String memNum) {	// PASS PROVIDER AND MEMBER AS PARAMETERS?
		
		if (!validateMember(bill.getMemberNum()).equals("Validated")) {
			System.out.println("FROM billHealthcare -> ERROR NOT VALIDATED: bill.getMemberNum() == " + bill.getMemberNum());
			System.out.println("FROM billHealthcare -> ERROR NOT VALIDATED: validateMember(bill.getMemberNum()) returns: " + validateMember(bill.getMemberNum()));
			return;
		}
		//Collect info for a new Bill object and add it the the curProvider and validatedMember
		
			//get current date and time
		DateAndTime currentDT = new DateAndTime();
		String date = currentDT.getDate();
		String time = currentDT.getTime();
		bill.setDate(date);
		bill.setTime(time);

		//display info
		//validatedMember = new Member(mem);
		
		System.out.println("Printing Bill that is being added to provider ("+curProvider.getProviderNum()+") and member ("+validatedMember.getMemberNum()+"):");
		
		System.out.println("Time: " + bill.getTime());
		System.out.println("Date: " + bill.getDate());
		System.out.println("Provider Number: " + bill.getProviderNum());
		System.out.println("Member Number: " + bill.getMemberNum());
		System.out.println("Service Code: " + bill.getServiceCode() );
		System.out.println("Service Fee: " + bill.getFee());
		System.out.println("Comments: " + bill.getComments());
		
		//Create new Bill object and add it to terminal objects
		curProvider.addService(bill);
		terminal.memberFromNum(memNum).addService(bill);
		
	}
	
	//Prints Provider Directory
	//name - code - fee    <-on each line
	public void printProviderDirectory() {
		ListIterator<Service> serviceIt = terminal.getProviderDirectory().listIterator();
		
		
		while (serviceIt.hasNext()) {
			Service curService = serviceIt.next();
			System.out.println(curService.name + " - " + curService.code + " - " + curService.fee);
        }	
	}
	
	// returns provider directory as string	 -- FORMAT SERVICE NAME AND CODE DIFFERENTLY?
	public String getProviderDirectory() {
		ListIterator<Service> serviceIt = terminal.getProviderDirectory().listIterator();
		
		String provDirStr = "";
		
		while (serviceIt.hasNext()) {
			Service curService = serviceIt.next();
			
			provDirStr += (curService.name + " - " + curService.code + " - $" + String.format("%.2f",curService.fee) + "\n");
			
        }	
		return provDirStr;
	}

}
