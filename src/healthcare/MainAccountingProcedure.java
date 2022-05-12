package healthcare;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.ListIterator;
import java.io.IOException;

public class MainAccountingProcedure {
	//Define necessary variables to run mainAccountingProcedure
	ArrayList<Provider> providers;
	ArrayList<Member> members;
	ArrayList<Service> providerDirectory;
	Terminal term;

	/**
	 * Constructor that runs main accounting procedure via the passed terminal
	 * Places data into the ./data/MemberReports and ./data/ProviderReports folder
	 * @param terminal
	 */
	public MainAccountingProcedure(Terminal terminal) {
		//Instantiate Variables needed to run mainAccountingProcedure
		this.term=terminal;
		this.members=terminal.members;
		this.providerDirectory=terminal.getProviderDirectory();
		this.providers=terminal.providers;
		
		try {
			//Designing Provider Files
			ListIterator<Provider> providerIt = providers.listIterator();
			while (providerIt.hasNext()) {
				//Iterate Through Providers
				Provider curProvider = providerIt.next();
				DateAndTime dt = new DateAndTime();
				String date = dt.getDate();
				
				//Instantiate variable to track the total price of services provided for each provider
				float providerfee = 0;
				
				//Generate the Provider's specific file
				String pf2 = "./data/ProviderReports/"+curProvider.name+"-"+date;
				FileWriter providersWriter = new FileWriter(pf2);
				
				//add Provider information to their respective organized text files
				providersWriter.write("Weekly Provider Report\n\n");
				providersWriter.write(curProvider.name + "\n");
				providersWriter.write(curProvider.address + "\n");
				providersWriter.write(curProvider.city + "\n");
				providersWriter.write(curProvider.state + "\n");
				providersWriter.write(curProvider.zipcode + "\n");
				providersWriter.write(curProvider.email + "\n");
				providersWriter.write(curProvider.password + "\n");
				providersWriter.write(curProvider.providerNum + "\n\n");
				providersWriter.write("Performed Services\n");
				
				//Iterate through Provider's Bills and add their info to the file
				ListIterator<Bill> billIt = curProvider.servicesProvided.listIterator();
				while (billIt.hasNext()) {
					Bill curBill = billIt.next();
					providersWriter.write(curBill.date + "\n");
					providersWriter.write(curBill.time + "\n");
					providersWriter.write(curBill.providerNum + "\n");
					providersWriter.write(curBill.memberNum + "\n");
					providersWriter.write(curBill.serviceCode + "\n");
					providersWriter.write(curBill.fee+"\n");
					providerfee+=curBill.fee;// add individual bill fee to running tracker
					providersWriter.write(curBill.comments + "\n\n");
				}
				
				// Add info of total price of all services to the file
				if (providerfee <= 999.99) {
					providersWriter.write("Total Fee of Services Provided: "+String.format("$%3.2f\n", providerfee));			
				} else if (providerfee >= 1000.00) {
					providersWriter.write("Total Fee: $999.99 (surpassed weekly maximum fee)\n");
				}
				
				providersWriter.close();
	        }
			
			
			//Design Member Files
			ListIterator<Member> memberIt = members.listIterator();
			while (memberIt.hasNext()) {
				//Iterate Through Members
				Member curMember = memberIt.next();
				DateAndTime dt = new DateAndTime();
				String date = dt.getDate();
				
				//Instantiate Variable to track member total price for all services
				float memberfee = 0;
				
				//Generate Member respective files with their info
				String mf2 = "./data/MemberReports/" +curMember.name+"-"+date;
				FileWriter membersWriter = new FileWriter(mf2);
				
				//add member info to file
				membersWriter.write("Weekly Member Report\n\n");
				membersWriter.write(curMember.name + "\n");
				membersWriter.write(curMember.address + "\n");
				membersWriter.write(curMember.city + "\n");
				membersWriter.write(curMember.state + "\n");
				membersWriter.write(curMember.zipcode + "\n");
				membersWriter.write(curMember.email + "\n");
				membersWriter.write(curMember.password + "\n");
				membersWriter.write(curMember.memberNum + "\n");
				membersWriter.write(curMember.status + "\n\n");
				
				//Iterate through Member's bills and add them to their file
				ListIterator<Bill> billIt = curMember.getServicesReceived().listIterator();
				membersWriter.write("Received Services\n");
				while (billIt.hasNext()) {
					Bill curBillMem = billIt.next();
					membersWriter.write(curBillMem.date + "\n");
					membersWriter.write(curBillMem.time + "\n");
					membersWriter.write(curBillMem.providerNum + "\n");
					membersWriter.write(curBillMem.memberNum + "\n");
					membersWriter.write(curBillMem.serviceCode + "\n");
					membersWriter.write(curBillMem.fee+"\n");
					memberfee+=curBillMem.fee;// add individual bill fee to running tracker
					membersWriter.write(curBillMem.comments + "\n\n");
				}
				
				//Write total fee member needs to pay at bottom of text file
				if (memberfee <= 999.99) {
					membersWriter.write("Total Fee of Services Received: "+String.format("$%3.2f\n", memberfee));			
				} else if (memberfee >= 1000.00) {
					membersWriter.write("Total Fee: $999.99 (surpassed weekly maximum fee)\n");
				}
				
				membersWriter.close();
	        }
		}
		catch (IOException e) {
			//This catches any errors that are made in the generation and accessing of all the files in mainAccountingProcedure
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }

	}
}

