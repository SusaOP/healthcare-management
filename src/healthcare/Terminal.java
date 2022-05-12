package healthcare;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ListIterator;


public class Terminal {
	ArrayList<Provider> providers;
	ArrayList<Member> members;
	ArrayList<SystemUser> operators;
	ArrayList<SystemUser> managers;
	private ArrayList<Service> providerDirectory;
	

	//Constructor
	// loads HealthcareData
	// reads files and populates ArrayList variables
	public Terminal() {
		//Initialize empty Lists
		this.setProviderDirectory(new ArrayList<Service>());
		this.providers = new ArrayList<Provider>();
		this.members = new ArrayList<Member>();
		this.operators = new ArrayList<SystemUser>();
		this.managers = new ArrayList<SystemUser>();
		
		try {
			// Create File objects
			File pdFile = new File("./data/providerDirectory.txt");	
			File pFile = new File("./data/providers.txt");
			File mFile = new File("./data/members.txt");
			File oFile = new File("./data/operators.txt");
			File mgFile = new File("./data/managers.txt");
		
			// Create Scanner objects (allows us to read a file using a Delimiter)
			Scanner servicesFile = new Scanner(pdFile).useDelimiter("/");
			Scanner providersFile = new Scanner(pFile).useDelimiter("/");
			Scanner membersFile = new Scanner(mFile).useDelimiter("/");
			Scanner operatorsFile = new Scanner(oFile).useDelimiter("/");
			Scanner managersFile = new Scanner(mgFile).useDelimiter("/");
			
			//read START
			// read providerDirectory.txt into providerDirectory ArrayList
			while(servicesFile.hasNext()){  
				String num = servicesFile.next(); 
	            String name = servicesFile.next();
	            float fee = servicesFile.nextFloat();
	            Service service = new Service(num, name, fee);
	            getProviderDirectory().add(service);
	        }  
			servicesFile.close();
			//read END
			
			//read START
			// read provider.txt into providers ArrayList
			while(providersFile.hasNext()){  
	            String name = providersFile.next();
	            String address = providersFile.next();
	            String city = providersFile.next();
	            String state = providersFile.next();
	            String zipcode = providersFile.next();
	            String email = providersFile.next();
	            String password = providersFile.next();
	            String providerNum = providersFile.next();
	            
	            // create new provider object
	            Provider newProvider = new Provider(name, address, city, state, zipcode, email, password, providerNum);
	            
	            // add Bills to newProvider object
	            while(providersFile.hasNext()) {
	            	String date = providersFile.next();
	            	if(date.equals("-1")) {break;} // -1 signals that there are no more Bills to add
	            	String time = providersFile.next();
	            	String providerNum2 = providersFile.next();
	            	String memberNum = providersFile.next();
	            	String serviceCode = providersFile.next();
	            	float fee = providersFile.nextFloat();
	            	String comments = providersFile.next();
	            	Bill newBill = new Bill(date, time, providerNum2, memberNum, serviceCode, fee, comments);
	            	newProvider.addService(newBill);
	            }
	            // add provider object to providers ArrayList
	            providers.add(newProvider);
	        }  
			providersFile.close();
			// read END
			
			//read START
			// read members.txt into members ArrayList
			while(membersFile.hasNext()){  
	            String name = membersFile.next();
	            String address = membersFile.next();
	            String city = membersFile.next();
	            String state = membersFile.next();
	            String zipcode = membersFile.next();
	            String email = membersFile.next();
	            String password = membersFile.next();
	            String memberNum = membersFile.next();
	            String status = membersFile.next();
	            
	            // create new member object
	            Member newMember = new Member(name, address, city,state, zipcode, email, password, memberNum, status);
	            
	            // add Bills to member object
	            while(membersFile.hasNext()) {
	            	String date = membersFile.next();
	            	if(date.equals("-1")) {break;} // -1 signals that there are no more Bills to add
	            	String time = membersFile.next();
	            	String providerNum = membersFile.next();
	            	String memberNum2 = membersFile.next();
	            	String serviceCode = membersFile.next();
	            	float fee = membersFile.nextFloat();
	            	String comments = membersFile.next();
	            	Bill newBill = new Bill(date, time, providerNum, memberNum2, serviceCode, fee, comments);
	            	newMember.addService(newBill);
	            }
	            // add member object to members ArrayList
	            members.add(newMember);
	        }  
			membersFile.close();
			//read END
			
			
			//read START
			// read operators.txt into operators ArrayList
			while(operatorsFile.hasNext()){  
	            String name = operatorsFile.next();
	            String address = operatorsFile.next();
	            String city = operatorsFile.next();
	            String state = operatorsFile.next();
	            String zipcode = operatorsFile.next();
	            String email = operatorsFile.next();
	            String password = operatorsFile.next();
	            
	            // create new SystemUser object
	            SystemUser newOperator = new SystemUser(name, address, city, state,zipcode, email, password);
	            
	            // add SystemUser object to operators ArrayList
	            operators.add(newOperator);
	        }  
			operatorsFile.close();
			//read END
			
			//read START
			// read managers.txt into managers ArrayList
			while(managersFile.hasNext()){  
	            String name = managersFile.next();
	            String address = managersFile.next();
	            String city = managersFile.next();
	            String state = managersFile.next();
	            String zipcode = managersFile.next();
	            String email = managersFile.next();
	            String password = managersFile.next();
	            
	            // create new SystemUser object
	            SystemUser newManager = new SystemUser(name, address, city, state,zipcode, email, password);
	            
	            // add SystemUser object to managers ArrayList
	            managers.add(newManager);
	        }  
			managersFile.close();
			//read END
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}

	// Writes all the ArrayLists to the correct files in "/project4/data"
	public void SaveHealthcareData() {
		    try {
		      // Open files for writing
		      FileWriter servicesWriter = new FileWriter("./data/providerDirectory.txt");
		      FileWriter providersWriter = new FileWriter("./data/providers.txt");
		      FileWriter membersWriter = new FileWriter("./data/members.txt");
		      FileWriter operatorsWriter = new FileWriter("./data/operators.txt");
		      FileWriter managersWriter = new FileWriter("./data/managers.txt");
		      
		      // Create Iterators for each ArrayList
		      ListIterator<Service> serviceIt = getProviderDirectory().listIterator();
		      ListIterator<Provider> providerIt = providers.listIterator();
		      ListIterator<Member> memberIt = members.listIterator();
		      ListIterator<SystemUser> operatorIt = operators.listIterator();
		      ListIterator<SystemUser> managerIt = managers.listIterator();
		      
		      //write START
		      //write providerDirectory ArrayList to providerDirectory.txt
		      while (serviceIt.hasNext()) {
					Service curService = serviceIt.next();
					servicesWriter.write(curService.code + "/");
					servicesWriter.write(curService.name + "/");
					servicesWriter.write(curService.fee + "/");
		        }
		      servicesWriter.close();
		      System.out.println("Services Data Saved");
		      //write END
		      
		      //write START
		      
		      
		      // write providers ArrayList to providers.txt
		      while (providerIt.hasNext()) {
					Provider curProvider = providerIt.next();
					providersWriter.write(curProvider.name + "/");    //+ "/" used for Scanner Delimiter in Constructor
					providersWriter.write(curProvider.address + "/");
					providersWriter.write(curProvider.city + "/");
					providersWriter.write(curProvider.state + "/");
					providersWriter.write(curProvider.zipcode + "/");
					providersWriter.write(curProvider.email + "/");
					providersWriter.write(curProvider.password + "/");
					providersWriter.write(curProvider.providerNum + "/");
					ListIterator<Bill> billIt = curProvider.servicesProvided.listIterator();
					while (billIt.hasNext()) {
						Bill curBill = billIt.next();
						providersWriter.write(curBill.date + "/");
						providersWriter.write(curBill.time + "/");
						providersWriter.write(curBill.providerNum + "/");
						providersWriter.write(curBill.memberNum + "/");
						providersWriter.write(curBill.serviceCode + "/");
						providersWriter.write(curBill.fee + "/");
						providersWriter.write(curBill.comments + "/");
					}
					providersWriter.write("-1/"); // -1 signals no more bills
		        }
		      providersWriter.close();
		      System.out.println("Providers Data Saved");
		      // write END
		      
		      //write START
		      // write members ArrayList to members.txt
		      while (memberIt.hasNext()) {
					Member curMember = memberIt.next();	
					membersWriter.write(curMember.name + "/");
					membersWriter.write(curMember.address + "/");
					membersWriter.write(curMember.city + "/");
					membersWriter.write(curMember.state + "/");
					membersWriter.write(curMember.zipcode + "/");
					membersWriter.write(curMember.email + "/");
					membersWriter.write(curMember.password + "/");
					membersWriter.write(curMember.memberNum + "/");
					membersWriter.write(curMember.status + "/");
					ListIterator<Bill> billIt = curMember.getServicesReceived().listIterator();
					while (billIt.hasNext()) {
						Bill curBill = billIt.next();
						membersWriter.write(curBill.date + "/");
						membersWriter.write(curBill.time + "/");
						membersWriter.write(curBill.providerNum + "/");
						membersWriter.write(curBill.memberNum + "/");
						membersWriter.write(curBill.serviceCode + "/");
						membersWriter.write(curBill.fee + "/");
						membersWriter.write(curBill.comments + "/");
					}
					membersWriter.write("-1/"); // -1 signals no more bills
		        }
		      membersWriter.close();
		      System.out.println("Members Data Saved");
		      //write END
		      
		    //write START
		      // write operators ArrayList to operators.txt
		      while (operatorIt.hasNext()) {
					SystemUser curOperator = operatorIt.next();
					operatorsWriter.write(curOperator.name + "/");
					operatorsWriter.write(curOperator.address + "/");
					operatorsWriter.write(curOperator.city + "/");
					operatorsWriter.write(curOperator.state + "/");
					operatorsWriter.write(curOperator.zipcode + "/");
					operatorsWriter.write(curOperator.email + "/");
					operatorsWriter.write(curOperator.password + "/");
		        }
		      operatorsWriter.close();
		      System.out.println("Operators Data Saved");
		      //write END
		      
		    //write START
		      // write managers ArrayList to managers.txt
		      while (managerIt.hasNext()) {
					SystemUser curManager = managerIt.next();
					managersWriter.write(curManager.name + "/");
					managersWriter.write(curManager.address + "/");
					managersWriter.write(curManager.city + "/");
					managersWriter.write(curManager.state + "/");
					managersWriter.write(curManager.zipcode + "/");
					managersWriter.write(curManager.email + "/");
					managersWriter.write(curManager.password + "/");
		        }
		      managersWriter.close();
		      System.out.println("Managers Data Saved");
		      //write END
		      
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		    saveSummaryReport();
		}
		
	
	// Susa - Writes Summary Report for the week: (Each provider+numConsultations+totalFee for the week) 
	// and total num providers, consultations, and total fee
	public boolean saveSummaryReport() {
		String summaryReport = "";
		int totalConsults = 0;
		float provFee = (float)0.00;
		float totalFee = (float)0.00;
		
		for (Provider p : providers) {
			summaryReport += (p.getName() + "\n");
			totalConsults += p.servicesProvided.size();
			summaryReport += (p.servicesProvided.size() + " consultations\n");
			for (Bill b : p.servicesProvided) {
				provFee += b.getFee();
			}
			if (provFee <= 999.99) {
				summaryReport += (String.format("Provider Total Fee: $%3.2f", provFee) + "\n\n");
				totalFee += provFee;
			} else if (provFee >= 1000.00) {
				summaryReport += ("Provider Total Fee: $999.99 (surpassed maximum weekly fee)\n\n");	
				totalFee += 999.99;
			}
			
			provFee = (float)0.00;
		}
		
		summaryReport += ("Total number of Providers: " + providers.size() + "\n");
		summaryReport += ("Total number of Consultations: " + totalConsults + "\n");
		if (totalFee <= 99999.99) {
			summaryReport += ("Total Fee to be paid: " + String.format("$%5.2f", totalFee) + "\n");			
		} else if (totalFee >= 100000.00) {
			summaryReport += ("Total Fee to be paid: $99,999.99 (surpassed maximum weekly fee)\n");	
		}
		
		try {
			FileWriter summaryW = new FileWriter("./data/SummaryReport.txt");
			summaryW.write(summaryReport);
			summaryW.close();
			System.out.println("Successfully wrote to ./data/SummaryReport.txt");
		} catch (IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		    return false;
		}
		return true;
	}
	
	
	public int LogIn(String userEmail, String userPass) {
		int systemUserFound = -1; // -1 invalid, 1 manager, 2 operator, 3 provider
		//Get user input 
		//System.out.println("Enter Email: ");
		//System.out.println("Enter Password: ");
		
		    
		//search ArrayLists in leaner time because we didn't get paid enough for anything faster
		ListIterator<Provider> providerIt = providers.listIterator();
		ListIterator<SystemUser> operatorIt = operators.listIterator();
		ListIterator<SystemUser> managerIt = managers.listIterator();
		    
		while (managerIt.hasNext() && (systemUserFound==-1)) {
			SystemUser curManager = managerIt.next();
			if(curManager.email.equals(userEmail)) {
				if(curManager.password.equals(userPass)) {
					System.out.println("TODO access manager menu"); //TODO
					systemUserFound = 1;
				}
			}
	     }
		 while (operatorIt.hasNext() && (systemUserFound==-1)) {
			SystemUser curOperator = operatorIt.next();
			if(curOperator.email.equals(userEmail)) {
				if(curOperator.password.equals(userPass)) {
					System.out.println("TODO access operator menu"); //TODO
					systemUserFound = 2;
				}
			}
	     }
		 
		 //Access ProviderMenu
		 while (providerIt.hasNext() && (systemUserFound==-1)) {
			Provider curProvider = providerIt.next();
			if(curProvider.email.equals(userEmail)) {
				if(curProvider.password.equals(userPass)) {
					ProviderMenu pmenu = new ProviderMenu(this, curProvider);
					systemUserFound = 3;
				}
			}
	     }
		 
		 if (systemUserFound==-1) {
		    System.out.println("Email or Password not found");
		 }
		 return systemUserFound;			 
		 
	}
	
	/**
	 * Check to see if a duplicate member or provider is being added 
	 * 
	 * @param Member checkMember
	 * 
	 * @return true if already in database, false if they do not (false allows to be entered)
	 */
	public boolean memberExists(Member checkMember) {
		
		ListIterator<Member> memberIt = members.listIterator();
		    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			if(curMember.getMemberNum().equals(checkMember.getMemberNum())) {
				System.out.println("MemberNum: " + checkMember.getMemberNum() + " ALREADY EXISTS for Member " + curMember.getName() + " -- " + curMember.getMemberNum());
				return true;
			}
	     }
		 
		System.out.println("Member not in list, good to add.");
		return false;
	}
	
	/**
	 * Check to see if a duplicate provider is being added 
	 * 
	 * @param Provider checkProvider
	 * 
	 * @return true if already in database, false if they do not (false allows to be entered)
	 */
	public boolean providerExists(Provider checkProvider) {
		
		ListIterator<Provider> providerIt = providers.listIterator();
		    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			if(curProvider.getProviderNum().equals(checkProvider.getProviderNum())) {
				System.out.println("ProviderNum: " + checkProvider.getProviderNum() + " ALREADY EXISTS for Provider " + curProvider.getName() + " -- "+curProvider.getProviderNum());
				return true;
			}
	     }
		 
		System.out.println("Provider not in list, good to add.");
		return false;
	}
	
	/**
	 * 
	 * @param providerNum
	 * @return provider Name
	 */
	public String providerNameFromNum(String providerNum) {
		ListIterator<Provider> providerIt = providers.listIterator();
	    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			if(curProvider.getProviderNum().equals(providerNum)) {
				System.out.println("ProviderNum: " + curProvider.getProviderNum() + " is for Provider " + curProvider.getName());
				return curProvider.getName();
			}
	     }
		return ("NO NAME FOUND FOR " + providerNum);
	}
	
	/**
	 * 
	 * @param memberNum
	 * @return member Name
	 */
	public String memberNameFromNum(String memberNum) {
		ListIterator<Member> memberIt = members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			if(curMember.getMemberNum().equals(memberNum) ) {
				System.out.println("MemberNum: " + curMember.getMemberNum() + " is for Member " + curMember.getName());
				return curMember.getName();
			}
	     }
		return ("NO NAME FOUND FOR " + memberNum);
	}
	
	/**
	 * 
	 * @param memberNum
	 * @return member Name
	 */
	public Member memberFromNum(String memberNum) {
		ListIterator<Member> memberIt = members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			if(curMember.getMemberNum().equals(memberNum) ) {
				System.out.println("MemberNum: " + curMember.getMemberNum() + " is for Member " + curMember.getName());
				return curMember;
			}
	     }
		
		System.out.println("NO MEMBER TO RETURN (SHOULDN'T HAPPEN)");
		return new Member();
	}
	
	
	
	/**
	 * 
	 * @param serviceCode
	 * @return service name
	 */
	public String serviceNameFromCode(String serviceCode) {
		ListIterator<Service> serviceIt = getProviderDirectory().listIterator();
	    
		// check for service existence
		while (serviceIt.hasNext()) {
			Service curService = serviceIt.next();
			if(curService.getCode().equals(serviceCode)) {
				System.out.println("ServiceCode: " + curService.getCode() + " is for Service " + curService.getName());
				return curService.getName();
			}
	     }
		return ("NO NAME FOUND FOR " + serviceCode);
	}
	
	
	/**
	 * 
	 * @param Provider providerToAdd
	 */
	public void addProvider(Provider providerToAdd) {
		providers.add(providerToAdd);
		
	}
	
	/**
	 * 
	 * @param Provider providerToAdd
	 */
	public void removeProvider(Provider providerToRemove) {
		providers.remove(providerToRemove);
		
	}
	
	
	public static void main(String[] args) {
		
		System.out.println("start main terminal");
		Terminal terminal = new Terminal();
		System.out.println("initialized terminal");
		FrameBuilder GUI = new FrameBuilder(terminal);
		System.out.println("initialized gui in main");
		GUI.buildTestFrames();
		System.out.println("HERE terminal");
		MainAccountingProcedure first = new MainAccountingProcedure(terminal);
		//working test?
		
		//terminal.SaveHealthcareData(); // called on user logout, as they re-enter login terminal
	}

	public ArrayList<Service> getProviderDirectory() {
		return providerDirectory;
	}

	public void setProviderDirectory(ArrayList<Service> providerDirectory) {
		this.providerDirectory = providerDirectory;
	}

}
