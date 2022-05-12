package healthcare;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class FrameBuilder {

	public static List<JFrame> frames = new ArrayList<JFrame>();
	private static final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int WIDTH = (int)(size.width * 0.8);
	private static final int HEIGHT = (int)(size.height * 0.8);
	
	private static Bill billToAdd = new Bill();	// Needs a default constructor // GLOBAL BILL TO ADD
	
	private static String emailInput = "";
	private static String passInput = "";
	
	private static int typeOfUser = -1;
	
	private static Provider provider = new Provider();		// Initialize to default (blank) Provider
	private static SystemUser operator = new SystemUser();	// Initialize to default (blank) Operator (SystemUser)	
	private static SystemUser manager = new SystemUser();	// Initialize to default (blank) Manager (SystemUser)
	
	private static VerifyFormat formatter = new VerifyFormat();
	
	// member
	private static Member member;
	
	private static boolean validCardNum = false;
	private static boolean billingValidate = false;	// set in frame 14 to use frame 13 (validate member) for a different case (post input of info)
	
	private static double serviceFee = -1.00;		// GLOBAL FOR FEE
	private static String serviceInfo = "";			// GLOBAL FOR SERVICE NAME/INFO
	
	private static int prevFrameNum = -1;
	private static int frameNum = 0;
	private static int nextFrame = frameNum + 1;	
	
	private static JFrame currentFrame;				// stores current frame (quick change of visibility)
	
	private static Terminal terminal;			// local instance of Bill for file management
	private static ProviderMenu provMenu;			// local instance of ProviderMenu for Provider Directory and files
	
	public FrameBuilder(Terminal terminal) {
		this.terminal = terminal;
	}

	
	public static void buildTestFrames() {
		
		// determine type of user before inits
		initAccessFrame();		      // read in user via terminal
		
		initLoggedOut();			  // frame 0 LOGS OUT / CLOSES AUTOMATICALLY (page never opened)
		initManagerMenu();		      // frame 1 (Manager Menu)   ->((4->5) , (6->7) , (8->9))
		initOperatorMenu();		      // frame 2 (Operator Menu)  ->((10->(21,22,23)) , (11->(24,25,26)))
		initProviderMenu();		      // frame 3 (Provider Menu)  ->((12) , (13) , (13*->14))	*conditionally sends to 14 if billing
		initMemberReportSelect();     // frame 4 ->(5)
		initDisplayMemberReport();    // frame 5 (display only) back->(4)
		initProviderReportSelect();   // frame 6 ->(7)
		initDisplayProviderReport();  // frame 7 (display only) back->(6)
		initSummaryReportSelect();    // frame 8 ->(9)
		initDisplaySummaryReport();	  // frame 9 (display only) back->(8)
		initMemberRecords(); 		  // frame 10
		initProviderRecords();		  // frame 11
		initProviderDirectory();	  // frame 12 (display provider directory)
		initValidateMember();		  // frame 13
		initInputBilling();			  // frame 14
		
		INSERT_FILLER_FRAME();		  // frame 15 - filler
		INSERT_FILLER_FRAME();		  // frame 16 - filler
		
		initComments();				  // frame 17
		initDisplayFee();			  // frame 18
		initDisplayService();		  // frame 19
		initPreviewReport();		  // frame 20
		// --Operator Edit Frames--
		initInputAddMember();     	  // frame 21
		initDeleteMember();			  // frame 22
		initUpdateMember();		  	  // frame 23 
		initInputAddProvider();		  // frame 24
		initDeleteProvider();		  // frame 25
		initUpdateProvider();		  // frame 26 
		INSERT_FILLER_FRAME();		  // frame 27 -> update interface for mem (takes params)
		INSERT_FILLER_FRAME();		  // frame 28 -> update interface for prov (takes params)
		//set current frame to first frame
		initErrorPage();			  // frame 29 - FORMAT ERROR FRAME -> "Back" button returns to frame that had format error (via prevFrameNum)
		
		currentFrame = frames.get(typeOfUser);	// initializes currentFrame depending on what type of user logs in
		goToFrame(typeOfUser);					// starts at available user menu
	}
	
	/**
	 * Closes current frame, sets currentFrame to frameID, opens new frame
	 * @param frameID	int that refers to the frameID
	 */
	public static void goToFrame(int frameID) {
		currentFrame.setVisible(false);
		currentFrame = frames.get(frameID);
		if (frameID == 0) {
			System.out.println("close program here -> Saving data");
			terminal.SaveHealthcareData();
		}
		System.out.println("set currentFrame at " + frameID);
		
		// if frame 0 (CLI login, uses placeholder frame), nothing visible, no frames yet built
		if (frameID == 0) {	
			currentFrame.setVisible(false);
			frames.clear();
			buildTestFrames();		// initialize and build frames
		} else {
			currentFrame.setVisible(true);		// for any non-0 frame, make visible
		}
	}
	
	/**
	 * Login Frame (CLI)
	 */
	public static void initAccessFrame() {

		// VERIFY EMAIL/PASS IN TERMINAL
		while (true) {
			Scanner userInput = new Scanner(System.in);
			System.out.println("Enter Email: ");
			emailInput = userInput.nextLine();
			if (emailInput.equals("quit") || emailInput.equals("QUIT") || emailInput.equals("Quit")) {	// hardcode in quit command in cli
				System.exit(1);
			}
			
			if (emailInput.equals("generateweeklyreports") || emailInput.equals("auto") || emailInput.equals("genreports")) {	// hardcode in quit command in cli
				MainAccountingProcedure weeklyReportGenerate = new MainAccountingProcedure(terminal);
				emailInput = "";
				initAccessFrame();
				return;
			}
			
			System.out.println("Enter Password: ");
			passInput = userInput.nextLine();

			typeOfUser = terminal.LogIn(emailInput, passInput);		// determine user permissions based upon their Login credentials (Manager,Operator,Provider)
			if (typeOfUser != -1) break;	// no user type assigned, unauthorized user
			
		}
		//System.out.println
		switch(typeOfUser) {
		case -1:
			break;
		case 1:
			for (SystemUser man : terminal.managers){
				System.out.println("\t\tFrom man.getEmail() = " + man.getEmail());
				if ((man.getEmail() == emailInput) && (man.getPassword() == passInput)) {
					// set this manager as the current user of the program
					manager = man;
					System.out.println("Assigned user to manager: " + man.getName() + " with email " + man.getEmail());
				}
			}
			break;
		case 2:
			for (SystemUser oper : terminal.operators) {
				System.out.println("\t\tFrom oper.getEmail() = " + oper.getEmail());
				if ((oper.getEmail() == emailInput) && (oper.getPassword() == passInput)) {
					// set this operator as the current user of the program
					operator = oper;
					System.out.println("Assigned user to operator: " + oper.getName() + " with email " + oper.getEmail());
				}
			}
			break;
		case 3:
			// need to find the index of individual provider name
			for (Provider prov : terminal.providers) {
				System.out.println("\t\tFrom prov.getEmail() = " + prov.getEmail());
				if ((prov.getEmail().equals(emailInput)) && (prov.getPassword().equals(passInput))) {
					// set this provider as the current user of the program
					provider = prov;
					System.out.println("Assigned user to provider: " + prov.getName() + " with num =" + prov.getProviderNum() + ", name =" + prov.getName());
				}
				// each provider's services
				for (int j = 0; j < prov.servicesProvided.size(); j++) {
					System.out.println("Date of service: " + prov.servicesProvided.get(j).getDate());	
					System.out.println("Date/Time received: " +prov.servicesProvided.get(j).getTime());
					// Member name ?? 
					System.out.println("Member num: "+prov.servicesProvided.get(j).getMemberNum());	
					System.out.println("Service Code: "+prov.servicesProvided.get(j).getServiceCode());	
					System.out.println("Fee: $"+ (String.format("%3.2f", prov.servicesProvided.get(j).getFee())));	
					System.out.println("Provider num: "+ prov.servicesProvided.get(j).getProviderNum());	
					System.out.println("Provider num (Compare): "+ (prov.getProviderNum()));	
				}
			}	// RECREATE FOR OTHER USERS
			break;
		default:
			System.out.println("DEFAULT (error)");
			break;
		}
		
		// valid user
		if (typeOfUser != -1) {
			//init frame/page number
			frameNum = typeOfUser;
			
		} else {
			//invalid user
			System.out.println("email/pass combo: " + emailInput + ", " + passInput + "   not valid");
		}
				
	}
		
	
	/**
	 * frame 0 (close GUI on logout)
	 */
	public static void initLoggedOut() {
		
		JFrame closeFrame = new JFrame("Close Program");
		
		closeFrame.setSize(WIDTH,HEIGHT);
		
		closeFrame.setVisible(false);
		closeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frames.add(closeFrame);
		
	}
	
	/**
	 * frame 1 (links to frames 4->5, 6->7, 8->9)
	 */
	public static void initManagerMenu() {

		JFrame managerF = new JFrame("Manager Menu");
				
		JPanel panelManButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelManExit= new JPanel(new FlowLayout());	//exit button panel
		
		managerF.setSize(WIDTH,HEIGHT);
		
		// button to verify email and password
		JButton memberReportB = new JButton("Member Report");
		JButton providerReportB = new JButton("Provider Report");
		JButton summaryReportB = new JButton("Summary Report");
		
		JButton logoutB = new JButton("Log Out");
		
		// LISTENERS for button pressed to move to selected page
		memberReportB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button memberReportB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(4);	//go to member report select page
				frameNum = 4;
			}
		});
		
		providerReportB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button providerReportB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(6);	//go to provider report select page
				frameNum = 6;
			}
		});
		
		summaryReportB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button summaryReportB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(8);	//go to summary report select page
				frameNum = 8;
			}
		});		
		
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(0);	//go to provider report select page
				frameNum = 0;
			}
		});
		
		panelManButtons.add(memberReportB);
		panelManButtons.add(providerReportB);
		panelManButtons.add(summaryReportB);
		panelManExit.add(logoutB);
		
		managerF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		managerF.getContentPane().add(panelManButtons, BorderLayout.CENTER);
		managerF.getContentPane().add(panelManExit, BorderLayout.SOUTH);
		managerF.setVisible(false);
		frames.add(managerF);
	}
	
	/**
	 * frame 2 (links to frames 10, 11)
	 */
	public static void initOperatorMenu() {

		JFrame operatorF = new JFrame("Operator Menu");
				
		JPanel panelOperButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperExit= new JPanel(new FlowLayout());	//exit button panel
		
		operatorF.setSize(WIDTH,HEIGHT);
		
		// button to verify email and password
		JButton memberRecordsB = new JButton("Manage Member Records");
		JButton providerRecordsB = new JButton("Manage Provider Records");
		JButton logoutB = new JButton("Log Out");
		
		// LISTENERS for button pressed to move to selected page
		memberRecordsB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button memberReportB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				frames.add(22, updateDeleteMember());	// updates info for delete member
				frames.remove(23);
				
				frames.add(23,updateUpdateMember());
				frames.remove(24);
				
				goToFrame(10);	//go to member report select page
				frameNum = 10;
			}
		});
		
		providerRecordsB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button providerReportB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				frames.add(25, updateDeleteProvider());
				frames.remove(26);
				
				frames.add(26, updateUpdateProvider());
				frames.remove(27);
				
				goToFrame(11);	//go to provider report select page
				frameNum = 11;
			}
		});
		
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(0);	// exit and await login
				frameNum = 0;
			}
		});
		
		
		panelOperButtons.add(memberRecordsB);
		panelOperButtons.add(providerRecordsB);
		panelOperExit.add(logoutB);
		
		operatorF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		operatorF.getContentPane().add(panelOperButtons, BorderLayout.CENTER);
		operatorF.getContentPane().add(panelOperExit, BorderLayout.SOUTH);
		operatorF.setVisible(false);
		frames.add(operatorF);
	}
	
	/**
	 * frame 3 (links to frames 12, 13, 14)
	 */
	public static void initProviderMenu() {

		JFrame providerF = new JFrame("Provider Menu");
		
		// initiate the provider menu class
		provMenu = new ProviderMenu(terminal, provider);
		
		JPanel panelProvButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelProvExit= new JPanel(new FlowLayout());	//exit button panel
		
		providerF.setSize(WIDTH,HEIGHT);
		
		// Provider Menu options shown
		JButton reqProvDirB = new JButton("Request Provider Directory");
		JButton validateMemberB = new JButton("Validate Member Card");
		JButton billHealthcareB = new JButton("Bill Healthcare");
		
		JButton logoutB = new JButton("Log Out");
		
		// LISTENERS for button pressed to move to selected page
		reqProvDirB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button reqProvDirB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(12);	//go to member report select page
				frameNum = 12;
			}
		});
		
		validateMemberB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button validateMemberB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				frames.add(13,updateValidateMember());	//add updated (cleared) validate member
				frames.remove(14);		// remove old frame 13 (pushed to 14)
				
				goToFrame(13);	//go to provider report select page
				frameNum = 13;
			}
		});
		
		billHealthcareB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button billHealthcareB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				billingValidate = true;
				
				frames.add(13,updateValidateMember());	//add updated (cleared) validate member
				frames.remove(14);		// remove old frame 13 (pushed to 14)
				
				goToFrame(13);	//go to ValidateMember with billingValidate TRUE, will go to provider report select page after revalidation
				frameNum = 13;
			}
		});
		
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(0);	//go to provider report select page
				frameNum = 0;
			}
		});
		
		
		panelProvButtons.add(reqProvDirB);
		panelProvButtons.add(validateMemberB);
		panelProvButtons.add(billHealthcareB);
		panelProvExit.add(logoutB);
		
		providerF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		providerF.getContentPane().add(panelProvButtons, BorderLayout.CENTER);
		providerF.getContentPane().add(panelProvExit, BorderLayout.SOUTH);
		providerF.setVisible(false);
		frames.add(providerF);
	}
	
	/**
	 * frame 4 (links to frame 5)
	 */
	public static void initMemberReportSelect() {
		// all the possible member reports are here
		JFrame selectMemReportsF = new JFrame("Select Member Report");
		
		JPanel panelMemReportsButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelMemReportSelectExit= new JPanel(new FlowLayout());	//exit button panel
		
		selectMemReportsF.setSize(WIDTH,HEIGHT);
				
		
		// for all members, produce buttons
		for (Member m : terminal.members) {
			JButton reportButton = new JButton(m.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//System.out.println("button reportButton clicked, SELECTED FILE " + terminal.members.get(I_COUNTER).getName() +" and " + frameNum);
					System.out.println("reportButton clicked for member " + m.getName() + ", frame " + frameNum);
					// OPEN FILE SELECTED -> frame 5
					frames.add(5, updateDisplayMemberReport(m)); // Bounds error ??
					frames.remove(6);	//remove 5 (pushed to 6);
					
					goToFrame(5);
					frameNum = 5;
					
				}
			});
			panelMemReportsButtons.add(reportButton);
			System.out.println(m.getName());			
		}
		
		
					
		JButton backB = new JButton("Back");
				
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button backB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(1);	//go to provider report select page
				frameNum = 1;
			}
		});
		
		
		panelMemReportSelectExit.add(backB);
		
		selectMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectMemReportsF.getContentPane().add(panelMemReportsButtons, BorderLayout.CENTER);
		selectMemReportsF.getContentPane().add(panelMemReportSelectExit, BorderLayout.SOUTH);
		selectMemReportsF.setVisible(false);
		frames.add(selectMemReportsF);
		
	}
	
	// frame 5 (selected member report from frame 4)
	public static void initDisplayMemberReport() {
		
		JFrame mrF = new JFrame("SELECTED MEMBER REPORT");
		
		JPanel panelMRButtons = new JPanel(new FlowLayout());	//button panel
		//JPanel panelMRResponse = new JPanel(new BorderLayout());	//response panel
		mrF.setSize(WIDTH,HEIGHT);
		
		
		
		// text area for user feedback/fee display
		JTextArea mrJTA = new JTextArea(80,80);
		mrJTA.setMinimumSize(new Dimension(500, 500));
		mrJTA.setMaximumSize(new Dimension(800,800));
		
		mrJTA.setText("MEMBER REPORT DATA HERE");

		JScrollPane scrollMRPanel = new JScrollPane(mrJTA); // scroll response panel
		System.out.println("MEMBER REPORT DATA HERE");
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in initDisplayMemberReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(4);
				frameNum = 4;
				
			}
		});
		
		panelMRButtons.add(closeB);
		
		mrF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//mrF.getContentPane().add(panelMRResponse, BorderLayout.CENTER);
		mrF.getContentPane().add(scrollMRPanel);
		mrF.getContentPane().add(panelMRButtons, BorderLayout.SOUTH);
		mrF.setVisible(false);
		frames.add(mrF);
	}
	
	// UPDATE frame 5 (selected member report from frame 4 ~ called by 4)
	public static JFrame updateDisplayMemberReport(Member memberReport) {
		
		JFrame mrF = new JFrame(memberReport.getName());
		
		JPanel panelMRButtons = new JPanel(new FlowLayout());	//button panel
		//JPanel panelMRResponse = new JPanel(new BorderLayout());	//response panel
		mrF.setSize(WIDTH,HEIGHT);
		
		// text area for user feedback/fee display
		JTextArea mrJTA = new JTextArea(80,80);
		mrJTA.setMinimumSize(new Dimension(500, 500));
		mrJTA.setMaximumSize(new Dimension(800,800));

		String reportText = "";
		// Read in from selected file
		System.out.println("getName is : "+memberReport.getName());
		
		reportText += (memberReport.getName() + "\n");
		reportText += (memberReport.getMemberNum() + "\n");
		reportText += (memberReport.getAddress() + "\n");
		reportText += (memberReport.getCity() + "\n");
		reportText += (memberReport.getState() + "\n");
		reportText += (memberReport.getZipcode() + "\n\nServices Received:\n");
		for (Bill bill : memberReport.getServicesReceived()) {
			reportText += (bill.getDate() + "\n");
			reportText += (terminal.providerNameFromNum(bill.getProviderNum()) + "\n");
			reportText += (terminal.serviceNameFromCode(bill.getServiceCode()) + "\n\n");
		}
		
		mrJTA.setText(reportText);
		mrJTA.setEditable(false);

		JScrollPane panelMRScroll = new JScrollPane(mrJTA);
		System.out.println(memberReport.toString());
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in updateDisplayMemberReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(4);
				frameNum = 4;
				
			}
		});
		
		panelMRButtons.add(closeB);
		
		mrF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mrF.getContentPane().add(panelMRScroll);
		mrF.getContentPane().add(panelMRButtons, BorderLayout.SOUTH);
		mrF.setVisible(false);
		return mrF;
	}
	
	// frame 6 (links to frame 7)
	public static void initProviderReportSelect() {
		// all the possible member reports are here
		JFrame selectProvReportsF = new JFrame("Select Provider Report");
		
		JPanel panelProvReportsButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelProvReportSelectExit= new JPanel(new FlowLayout());	//exit button panel
		
		selectProvReportsF.setSize(WIDTH,HEIGHT);
				
		// Create buttons for all the available provider Reports
		
		for (Provider p : terminal.providers) {
			JButton reportButton = new JButton(p.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//System.out.println("button reportButton clicked, SELECTED FILE " + terminal.providers.get(I_COUNTER).getName() +" and " + frameNum);
					System.out.println("reportButton clicked for provider " + p.getName() + ", frame " + frameNum);
					// OPEN FILE SELECTED -> frame 7
					frames.add(7, updateDisplayProviderReport(p)); // Bounds error ??
					frames.remove(8);	//remove 5 (pushed to 6);
					
					goToFrame(7);
					frameNum = 7;
					
				}
			});
			panelProvReportsButtons.add(reportButton);
			System.out.println(p.getName());			
		}
		
					
		JButton backB = new JButton("Back");
				
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button backB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(1);	//go to provider report select page
				frameNum = 1;
			}
		});
		
		
		panelProvReportSelectExit.add(backB);
		
		selectProvReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		selectProvReportsF.getContentPane().add(panelProvReportsButtons, BorderLayout.CENTER);
		selectProvReportsF.getContentPane().add(panelProvReportSelectExit, BorderLayout.SOUTH);
		selectProvReportsF.setVisible(false);
		frames.add(selectProvReportsF);
		
	}
	
	// frame 7
	public static void initDisplayProviderReport() {
		
		JFrame prF = new JFrame("SELECTED PROVIDER REPORT");
		
		JPanel panelPRButtons = new JPanel(new FlowLayout());	//button panel
		prF.setSize(WIDTH,HEIGHT);
		
		
		// text area for user feedback/fee display
		JTextArea prJTA = new JTextArea(80,80);
		prJTA.setMinimumSize(new Dimension(500, 500));
		prJTA.setMaximumSize(new Dimension(800,800));
		
		prJTA.setText("PROVIDER REPORT DATA HERE");
		System.out.println("PROVIDER REPORT DATA HERE");

		JScrollPane scrollPanel = new JScrollPane(prJTA);	// response panel with scroll
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in initDisplayProviderReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(6);
				frameNum = 6;
				
			}
		});
		
		panelPRButtons.add(closeB);
		
		prF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		prF.getContentPane().add(scrollPanel);
		prF.getContentPane().add(panelPRButtons, BorderLayout.SOUTH);
		prF.setVisible(false);
		frames.add(prF);
	}
	
	// UPDATE frame 7 (selected provider report from frame 4 ~ called by 4)
	public static JFrame updateDisplayProviderReport(Provider providerReport) {
		
		JFrame prF = new JFrame(providerReport.getName());
		
		JPanel panelPRButtons = new JPanel(new FlowLayout());	//button panel
		prF.setSize(WIDTH,HEIGHT);
		
		
		
		// text area for user feedback/fee display
		JTextArea prJTA = new JTextArea(80,80);
		prJTA.setMinimumSize(new Dimension(500, 500));
		prJTA.setMaximumSize(new Dimension(800,800));

		String reportText = "";
		float weeklyFee = (float) 0.0;
		int numConsultations = 0;
		// Read in from selected file
		System.out.println("getName is : "+providerReport.getName());
		
		reportText += (providerReport.getName() + "\n");
		reportText += (providerReport.getProviderNum() + "\n");
		reportText += (providerReport.getAddress() + "\n");
		reportText += (providerReport.getCity() + "\n");
		reportText += (providerReport.getState() + "\n");
		reportText += (providerReport.getZipcode() + "\n\nServices Provided:\n");
		for (Bill bill : providerReport.servicesProvided) {
			numConsultations++;
			reportText += (bill.getDate() + "\n");
			reportText += (bill.getTime() + "\n");
			reportText += (terminal.memberNameFromNum(bill.getMemberNum())+"\n"); // ? ? 
			reportText += (bill.getMemberNum() + "\n");
			reportText += (bill.getServiceCode() + "\n");
			reportText += (String.format("$%3.2f", bill.getFee()) + "\n\n");
			weeklyFee += bill.getFee();
		}
		reportText += ("Num consultations: " + numConsultations + "\n");
		reportText += ("Total fee for week: " + String.format("$%3.2f\n", weeklyFee));
		System.out.println(reportText);
		
				
		prJTA.setText(reportText);
		prJTA.setEditable(false);

		JScrollPane scrollPanel = new JScrollPane(prJTA);	// response panel (scroll)
		
		System.out.println(providerReport.toString());
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in updateDisplayProviderReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(6);
				frameNum = 6;
				
			}
		});
		
		panelPRButtons.add(closeB);
		
		prF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		prF.getContentPane().add(scrollPanel);
		prF.getContentPane().add(panelPRButtons, BorderLayout.SOUTH);
		prF.setVisible(false);
		return prF;
	}
	
	
	// frame 8 (links to frame 9)
	public static void initSummaryReportSelect() {		
		// all the possible member reports are here
		JFrame selectSummaryReportsF = new JFrame("Select Summary Report");
		
		JPanel panelSummaryReportsButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelSummaryReportSelectExit= new JPanel(new FlowLayout());	//exit button panel
		
		selectSummaryReportsF.setSize(WIDTH,HEIGHT);
				
		// Create buttons for summaryReport
		File f = new File("./data/SummaryReport.txt");	
		JButton reportButton = new JButton(f.getName());
		reportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button reportButton clicked, SELECTED FILE " + f.getName() +" and " + frameNum);
				// OPEN FILE 
				frames.add(9, updateDisplaySummaryReport(f));
				frames.remove(10);	//remove 9 (pushed to 10);
				
				goToFrame(9);
				frameNum = 9;
			}
		});
		panelSummaryReportsButtons.add(reportButton);
		System.out.println(f.getName());			
		
					
		JButton backB = new JButton("Back");
				
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button backB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(1);	//go to provider report select page
				frameNum = 1;
			}
		});
		
		
		panelSummaryReportSelectExit.add(backB);
		
		selectSummaryReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		selectSummaryReportsF.getContentPane().add(panelSummaryReportsButtons, BorderLayout.CENTER);
		selectSummaryReportsF.getContentPane().add(panelSummaryReportSelectExit, BorderLayout.SOUTH);
		selectSummaryReportsF.setVisible(false);
		frames.add(selectSummaryReportsF);
		
	}
		
	// frame 9
	public static void initDisplaySummaryReport() {
		
		JFrame srF = new JFrame("SELECTED SUMMARY REPORT");
		
		JPanel panelSRButtons = new JPanel(new FlowLayout());	//button panel
		
		srF.setSize(WIDTH,HEIGHT);
		
		
		
		// text area for user feedback/fee display
		JTextArea srJTA = new JTextArea(80,80);
		srJTA.setMinimumSize(new Dimension(500, 500));
		srJTA.setMaximumSize(new Dimension(800,800));
		
		srJTA.setText("SUMMARY REPORT DATA HERE");
		System.out.println("SUMMARY REPORT DATA HERE");
		JScrollPane scrollSRPanel = new JScrollPane(srJTA);
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in initDisplaySummaryReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(8);
				frameNum = 8;
				
			}
		});
		
		panelSRButtons.add(closeB);
		
		srF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		srF.getContentPane().add(scrollSRPanel);
		srF.getContentPane().add(panelSRButtons, BorderLayout.SOUTH);
		srF.setVisible(false);
		frames.add(srF);
	}
	
	// UPDATE frame 9 (selected summary report from frame 8 ~ called by 8)
	public static JFrame updateDisplaySummaryReport(File summaryReportFile) {
		
		JFrame srF = new JFrame(summaryReportFile.getName());
		
		JPanel panelSRButtons = new JPanel(new FlowLayout());	//button panel
		//JPanel panelSRResponse = new JPanel(new BorderLayout());	//response panel
		srF.setSize(WIDTH,HEIGHT);
		
		
		// text area for user feedback/fee display
		JTextArea srJTA = new JTextArea(80,80);
		srJTA.setMinimumSize(new Dimension(500, 500));
		srJTA.setMaximumSize(new Dimension(800,800));

		String fileText = "";
		// Read in from selected file
		System.out.println("getName is : "+summaryReportFile.getName());
		try {
			FileReader fr = new FileReader(summaryReportFile.toPath().toString());
			Scanner inFile = new Scanner(fr);
			while (inFile.hasNextLine()) {
				fileText += (inFile.nextLine() + "\n");
			}
			inFile.close();
		} catch (IOException e) {
			System.out.println(summaryReportFile.toPath());
			System.out.println(e + "ERROR: -- File Not Found");
			System.exit(-1);
		}
				
		srJTA.setText(fileText);
		srJTA.setEditable(false);

		JScrollPane scrollSRPanel = new JScrollPane(srJTA);
		System.out.println(summaryReportFile.toString());
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in updateDisplaySummaryReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(8);
				frameNum = 8;
				
			}
		});
		
		panelSRButtons.add(closeB);
		
		srF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		srF.getContentPane().add(scrollSRPanel);
		srF.getContentPane().add(panelSRButtons, BorderLayout.SOUTH);
		srF.setVisible(false);
		return srF;
	}
	
	
	// frame 10 
	public static void initMemberRecords() {
		// all the possible member reports are here
		JFrame memberRecordsF = new JFrame("Member Records");
		
		JPanel panelMemberRecordsButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelMemberRecordsExit= new JPanel(new FlowLayout());	//exit button panel
		
		memberRecordsF.setSize(WIDTH,HEIGHT);
		
		JButton addB = new JButton("Add Record");
		JButton deleteB = new JButton("Delete Record");
		JButton updateB = new JButton("Update Record");
										
		JButton logoutB = new JButton("Back");
		
		addB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button addB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// GO TO SELECT RECORD TO ADD
				goToFrame(21);
				frameNum = 21;
			}
		});
		
		deleteB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button deleteB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				

				// GO TO SELECT RECORD TO DELETE
				goToFrame(22);
				frameNum = 22;
			}
		});
		
		updateB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button updateB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// GO TO SELECT RECORD TO UPDATE
				goToFrame(23);
				frameNum = 23;
			}
		});
		
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		panelMemberRecordsButtons.add(addB);
		panelMemberRecordsButtons.add(deleteB);
		panelMemberRecordsButtons.add(updateB);
		
		panelMemberRecordsExit.add(logoutB);
		
		memberRecordsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		memberRecordsF.getContentPane().add(panelMemberRecordsButtons, BorderLayout.CENTER);
		memberRecordsF.getContentPane().add(panelMemberRecordsExit, BorderLayout.SOUTH);
		memberRecordsF.setVisible(false);
		frames.add(memberRecordsF);
		
	}

	
	// frame 11
	public static void initProviderRecords() {
		// all the possible provider reports are here
		JFrame providerRecordsF = new JFrame("Provider Records");
		
		JPanel panelProviderRecordsButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelProviderRecordsExit= new JPanel(new FlowLayout());	//exit button panel
		
		providerRecordsF.setSize(WIDTH,HEIGHT);
		
		JButton addB = new JButton("Add Record");
		JButton deleteB = new JButton("Delete Record");
		JButton updateB = new JButton("Update Record");
										
		JButton backB = new JButton("Back");
		
		addB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button addB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// GO TO SELECT RECORD TO ADD
				goToFrame(24);
				frameNum = 24;
			}
		});
		
		deleteB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button deleteB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// GO TO SELECT RECORD TO DELETE
				goToFrame(25);
				frameNum = 25;
			}
		});
		
		updateB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button updateB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// GO TO SELECT RECORD TO UPDATE
				goToFrame(26);
				frameNum = 26;
			}
		});
		
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
			
		panelProviderRecordsButtons.add(addB);
		panelProviderRecordsButtons.add(deleteB);
		panelProviderRecordsButtons.add(updateB);
		
		panelProviderRecordsExit.add(backB);
		
		providerRecordsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		providerRecordsF.getContentPane().add(panelProviderRecordsButtons, BorderLayout.CENTER);
		providerRecordsF.getContentPane().add(panelProviderRecordsExit, BorderLayout.SOUTH);
		providerRecordsF.setVisible(false);
		frames.add(providerRecordsF);
		
	}

	
	// frame 12 (returns to frame 3)
	public static void initProviderDirectory() {
		
		JFrame pdF = new JFrame("Provider Directory");
		
		JPanel panelPDButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelPDResponse = new JPanel(new BorderLayout());	//response panel
		pdF.setSize(WIDTH,HEIGHT);
		
		
		
		// text area for user feedback/fee display
		JTextArea pdJTA = new JTextArea(80,80);
		pdJTA.setMinimumSize(new Dimension(500, 500));
		pdJTA.setMaximumSize(new Dimension(800,800));
		
		
		// set text as providerdirectory
		pdJTA.setText(provMenu.getProviderDirectory());
		pdJTA.setEditable(false);
		
		System.out.println(provMenu.getProviderDirectory());
		
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button close clicked in initProviderDirectory, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// Return to provider menu
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelPDResponse.add(pdJTA);
		panelPDButtons.add(closeB);
		
		pdF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pdF.getContentPane().add(panelPDResponse, BorderLayout.CENTER);
		pdF.getContentPane().add(panelPDButtons, BorderLayout.SOUTH);
		pdF.setVisible(false);
		frames.add(pdF);
	}
	
	
	// frame 13
	public static void initValidateMember() {
		
		JFrame validateMemF = new JFrame("Validate Member");
		
		JPanel panelValMemButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelValMemText = new JPanel(new FlowLayout());	//text input panel
		JPanel panelValMemResponse = new JPanel(new BorderLayout());	//response panel
		validateMemF.setSize(WIDTH,HEIGHT);
		
		// text fields for input of member card
		JTextField cardNumJTF = new JTextField("Scan Member Card Number:");
		cardNumJTF.setMinimumSize(new Dimension(150,50));
		
		
		// text area for user feedback
		JTextArea responseJTA = new JTextArea("");
		responseJTA.setMinimumSize(new Dimension(500, 500));
		
		// button to verify card number
		JButton submitB = new JButton("Submit");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button submit clicked in billing, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				String cardNumInput = "";
				
				if (!cardNumJTF.getText().isEmpty()){
					cardNumInput = cardNumJTF.getText();
				} else {
					responseJTA.setText("No input");
					System.out.println("No input");
				}
				
				
				
				if (provMenu.validateMember(cardNumInput).equals("Validated")) {
					validCardNum = true;
					
				}
				
				// check members list to get member
				for (Member m : terminal.members) {
					System.out.println("Checking Member " + m.getName() + " (num " + m.getMemberNum() + ") against cardNumInput " + cardNumInput);
					if (cardNumInput.equals(m.getMemberNum())) {
						
						System.out.println("in update Card num matches for Member: " + m.getName());
						member = new Member(m);
						validCardNum = true;


						billToAdd.setMemberNum(m.getMemberNum()); // 1 of 7 set
						billToAdd.setProviderNum(provider.getProviderNum()); // 2 of 7 set
												
						break;
					} 
				}
				
				// IF VALID, DISPLAY MEMBER STATUS
				
				if (validCardNum) {		//get member status
					responseJTA.setText("Member (ID: "+cardNumInput+") status is: " + member.getStatus());
					
				} else {
					responseJTA.setText("Member (ID: "+cardNumInput+") status is: " + member.getStatus());
					//responseJTA.setText("Invalid Number");
					System.out.println("Member (ID: "+cardNumInput+") status is: " + member.getStatus());
					System.out.println("(update) validCardNum is false");
				}
				
				if (billingValidate && validCardNum) { // if used to validate for Bill Healthcare --> frame 14
					
					// ensure member status is not invalid
					if (provMenu.validateMember(cardNumInput).equals("Validated")) {
						billingValidate = false; // reset bool billingValidate
						validCardNum = false;	// reset bool validCardNum
						goToFrame(14);
						frameNum = 14;						
					} else {
						System.out.println("In update frame 13, provMenu.validateMember(" + cardNumInput + ") == " + provMenu.validateMember(cardNumInput));
					}
					
				} else {	// not used for Bill Healthcare
					System.out.println("in update else : billingValidate is " + String.valueOf(billingValidate) + ", validCardNum is " + String.valueOf(validCardNum));
					System.out.println("Awaiting backclick in update validateMem (non-billing)");
				}
			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in billing, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelValMemText.add(cardNumJTF, FlowLayout.LEFT);
		panelValMemText.add(submitB);
		panelValMemResponse.add(responseJTA);
		panelValMemButtons.add(backB);
		
		validateMemF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		validateMemF.getContentPane().add(panelValMemText, BorderLayout.CENTER);
		validateMemF.getContentPane().add(panelValMemResponse, BorderLayout.EAST);
		validateMemF.getContentPane().add(panelValMemButtons, BorderLayout.SOUTH);
		validateMemF.setVisible(false);
		frames.add(validateMemF);
	}
	
	// update frame 13 (clear existing data when back in provider menu -- frame 3)
	public static JFrame updateValidateMember() {
		
		validCardNum = false; // reset global bool
		
		JFrame validateMemF = new JFrame("Validate Member");
		
		JPanel panelValMemButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelValMemText = new JPanel(new FlowLayout());	//text input panel
		JPanel panelValMemResponse = new JPanel(new BorderLayout());	//response panel
		validateMemF.setSize(WIDTH,HEIGHT);
		
		// text fields for input of member card
		JTextField cardNumJTF = new JTextField("Scan Member Card Number:");
		cardNumJTF.setMinimumSize(new Dimension(150,50));
		
		
		// text area for user feedback
		JTextArea responseJTA = new JTextArea("");
		responseJTA.setMinimumSize(new Dimension(500, 500));
		
		// button to verify card number
		JButton submitB = new JButton("Submit");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button submit clicked in billing, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				String cardNumInput = "";
				
				if (!cardNumJTF.getText().isEmpty()){
					cardNumInput = cardNumJTF.getText();
				} else {
					responseJTA.setText("No input");
					System.out.println("No input");
				}
				
				
				
				if (provMenu.validateMember(cardNumInput).equals("Validated")) {
					validCardNum = true;
					
				}
				
				// check members list to get member
				for (Member m : terminal.members) {
					System.out.println("Checking Member " + m.getName() + " (num " + m.getMemberNum() + ") against cardNumInput " + cardNumInput);
					if (cardNumInput.equals(m.getMemberNum())) {
						
						System.out.println("in update Card num matches for Member: " + m.getName());
						member = new Member(m);
						validCardNum = true;


						billToAdd.setMemberNum(m.getMemberNum()); // 1 of 7 set
						billToAdd.setProviderNum(provider.getProviderNum()); // 2 of 7 set
						//provMenu.validateMember(cardNumInput); // validate member in the provider menu class
												
						break;
					} 
				}
				
				// IF VALID, DISPLAY MEMBER STATUS
				
				if (validCardNum) {		//get member status
					responseJTA.setText("Member (ID: "+cardNumInput+") status is: " + member.getStatus());
					//IDEAL --> responseJTA.setText("Member (ID: "+cardNumInput+") status is: " + currentMember.getStatus());
				} else {
					responseJTA.setText("Member (ID: "+cardNumInput+") status is: " + terminal.memberNameFromNum(cardNumInput));
					//responseJTA.setText("Invalid Number");
					System.out.println("Member (ID: "+cardNumInput+") status is: " + terminal.memberNameFromNum(cardNumInput));
					System.out.println("(update) validCardNum is false");
				}
				
				if (billingValidate && validCardNum) { // if used to validate for Bill Healthcare --> frame 14
					
					// ensure member status is not invalid
					if (provMenu.validateMember(cardNumInput).equals("Validated")) {
						billingValidate = false; // reset bool billingValidate
						validCardNum = false;	// reset bool validCardNum
						goToFrame(14);
						frameNum = 14;						
					} else {
						System.out.println("In update frame 13, provMenu.validateMember(" + cardNumInput + ") == " + provMenu.validateMember(cardNumInput));
					}
					
				} else {	// not used for Bill Healthcare
					System.out.println("in update else : billingValidate is " + String.valueOf(billingValidate) + ", validCardNum is " + String.valueOf(validCardNum));
					System.out.println("Awaiting backclick in update validateMem (non-billing)");
				}
			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in updated validate mem, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelValMemText.add(cardNumJTF, FlowLayout.LEFT);
		panelValMemText.add(submitB);
		panelValMemResponse.add(responseJTA);
		panelValMemButtons.add(backB);
		
		validateMemF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		validateMemF.getContentPane().add(panelValMemText, BorderLayout.CENTER);
		validateMemF.getContentPane().add(panelValMemResponse, BorderLayout.EAST);
		validateMemF.getContentPane().add(panelValMemButtons, BorderLayout.SOUTH);
		validateMemF.setVisible(false);
		//frames.add(validateMemF);
		return validateMemF;
	}
	
	// frame 14 (Links to frame 19)
	public static void initInputBilling() {
		
		JFrame billingF = new JFrame("Bill Healthcare");
		
		JPanel panelBillingButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelBillingText = new JPanel(new FlowLayout());	//text input panel
		billingF.setSize(WIDTH,HEIGHT);
		
		// text fields for input of email and pass
		JTextField dateJTF = new JTextField(15);
		dateJTF.setText("Enter Date");
		dateJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField codeJTF = new JTextField(15);
		codeJTF.setText("Enter Service Code");
		codeJTF.setMinimumSize(new Dimension(150,50));
		
		
		// button to verify email and password
		JButton submitB = new JButton("Submit");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button submit clicked in billing, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				
				String dateInput = dateJTF.getText();
				String codeInput = codeJTF.getText();
				
				// if not a valid date or code
				if ((!formatter.validFormatDate(dateInput)) || (!formatter.validFormatServiceCode(codeInput))) {
					
					prevFrameNum = frameNum;
					goToFrame(29); // Format Error frame
					frameNum = 29;
					
				} else {
					// If valid date and code format ... 
									
					// FETCH AND DISPLAY SERVICE INFO
					// uses provider dir
					Service selectedService = new Service();	// blank constructor
					
					for (Service s : terminal.getProviderDirectory()) {		// Does this need to have the provider input the code or the name of service ?
						System.out.println("FROM provDir: (name/code/fee) -- \n\t" + s.getName() + "\n\t" + s.getCode() + "\n\t" + s.getFee());
					
						// service selected or auto assigned?
						if (codeInput.equals(s.getCode())) { // code matches the provider input
							selectedService = s;
							
							billToAdd.setServiceCode(s.getCode()); // 3 of 7
							billToAdd.setFee(s.getFee());
							billToAdd.setDate(dateInput);
							
							System.out.println("INSIDE provDir - Selected Service initial: (name/code/fee) -- \n\t" + s.getName() + "\n\t" + s.getCode() + "\n\t" + s.getFee());
							System.out.println("INSIDE provDir - Selected Service assigned: (name/code/fee) -- \n\t" + selectedService.getName() + "\n\t" + selectedService.getCode() + "\n\t$" + String.format("%3.2f\n", selectedService.getFee()));
							break;
						}
					}
					
					
					// MUST CHECK A LIST/FILE OF VALID SERVICES AND THEIR INFORMATION
					if (selectedService.getName().equals("NO_SERVICE_NAME")) {
						
						System.out.println("code input: " + codeInput + "   not found");	
					} else {
						//DISPLAY SERVICE
						
						System.out.println("Service: \n\t" + selectedService.getName() + "\n\t" + selectedService.getCode() + "\n\t$" + String.format("%3.2f\n", selectedService.getFee()));
						
						// UPDATE BILL FEE
						billToAdd.setFee(selectedService.getFee());
						
						// UPDATE FRAME 18 (for fee)
						frames.add(18, updateDisplayFee(selectedService));
						frames.remove(19);	//remove old frame 18 (now pushed to 19)
						
						
						// UPDATE FRAME 19 (for service)
						frames.add(19, updateDisplayService(selectedService));	// pass Service selectedService 
						frames.remove(20);	//remove old frame 19 (now pushed to 20)
						
						System.out.println("Service: " + selectedService.getName());
					}
					// GO TO DISPLAY SERVICE INFO FRAME
					goToFrame(19);
					frameNum = 19;
				}
			}
		});
		
		JButton logoutB = new JButton("Back");
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logout clicked in billing, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				member = null; // reset global member 
				// VERIFY 
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelBillingText.add(dateJTF, FlowLayout.LEFT);
		panelBillingText.add(codeJTF, FlowLayout.LEFT);
		panelBillingText.add(submitB);
		panelBillingButtons.add(logoutB);
		
		billingF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		billingF.getContentPane().add(panelBillingText, BorderLayout.CENTER);
		billingF.getContentPane().add(panelBillingButtons, BorderLayout.SOUTH);
		billingF.setVisible(false);
		frames.add(billingF);
	}
	
	
	// frame 15 (now filler frame)
	// frame 16 (now filler frame)

	// frame 17 -> 20
	public static void initComments() {
		
		JFrame commentsF = new JFrame("Comments");
		
		JPanel panelCommentsButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelCommentsText = new JPanel(new FlowLayout());	//text input panel
		commentsF.setSize(WIDTH,HEIGHT);
		
		// text area for comments input
		JTextArea commentsJTA = new JTextArea("Enter Comments:", 30, 110);
		commentsJTA.setMinimumSize(new Dimension(250,150));

		// button to submit comments (<= 100 chars)
		JButton submitB = new JButton("Submit");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button submit clicked in comments, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY COMMENTS UNDER 100 chars long
				if (commentsJTA.getText().length() > 100) {
					System.out.println("COMMENTS TOO LONG!!");
					return;
				}
				
				// Create bill
				System.out.println("*\n*\nI SHOULD NEVER BE HERE IF THE COMMENTS ARE TOO LONG\n*\n*");
				
				// show comments or not?
				if (commentsJTA.getText().equals("Enter Comments:")) {					
					System.out.println("Comments EQUAL:" + commentsJTA.getText());
					billToAdd.setComments("No comments made.");	// 4 of 7
					
				} else if (commentsJTA.getText().equals("")) {
					System.out.println("Comments (NONE)");
					billToAdd.setComments("No comments made.");	// 4 of 7
					
				} else {
					System.out.println("Comments stored were exactly: -" + commentsJTA.getText() +"-");
					billToAdd.setComments(commentsJTA.getText());	// 4 of 7
				}
				
				frames.add(20, updatePreviewReport());
				frames.remove(21);
				
				goToFrame(20);
				frameNum = 20;					
				System.out.println("billToAdd.getComments() = " + billToAdd.getComments());
				
			}
		});
		
		JButton prevB = new JButton("Previous Page");
		prevB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button prevB clicked in comments, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(19);
				frameNum = 19;
				
			}
		});
		
		panelCommentsText.add(commentsJTA, FlowLayout.LEFT);
		panelCommentsText.add(submitB);
		panelCommentsButtons.add(prevB);
		
		commentsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		commentsF.getContentPane().add(panelCommentsText, BorderLayout.CENTER);
		commentsF.getContentPane().add(panelCommentsButtons, BorderLayout.SOUTH);
		commentsF.setVisible(false);
		frames.add(commentsF);
	}
	
	// frame 18 (updated by 14) links back to provider menu (3)
	public static void initDisplayFee() {
		
		JFrame feeF = new JFrame("Billing Fee");
		
		JPanel panelFeeButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelFeeResponse = new JPanel(new BorderLayout());	//response panel
		feeF.setSize(WIDTH,HEIGHT);
		
		// text area for user feedback/fee display
		JTextArea feeJTA = new JTextArea(80,80);
		feeJTA.setMinimumSize(new Dimension(500, 500));
		feeJTA.setMaximumSize(new Dimension(800,800));
		
		feeJTA.setText("fee: $" + String.format("%3.2f\n", serviceFee));
		System.out.println("fee: $" + String.format("%3.2f", serviceFee));
		
		JButton returnB = new JButton("Return To Provider Menu");
		returnB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Return To Provider Menu clicked in comments, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelFeeResponse.add(feeJTA);
		panelFeeButtons.add(returnB);
		
		feeF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		feeF.getContentPane().add(panelFeeResponse, BorderLayout.CENTER);
		feeF.getContentPane().add(panelFeeButtons, BorderLayout.SOUTH);
		feeF.setVisible(false);
		frames.add(feeF);
	}
	
	// UPDATES frame 18 ( updated by 14 )
	public static JFrame updateDisplayFee(Service s) {
		
		JFrame feeF = new JFrame("Billing Fee");
		
		JPanel panelFeeButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelFeeResponse = new JPanel(new BorderLayout());	//response panel
		feeF.setSize(WIDTH,HEIGHT);
		
		// text area for user feedback/fee display
		JTextArea feeJTA = new JTextArea(80,80);
		feeJTA.setMinimumSize(new Dimension(500, 500));
		feeJTA.setMaximumSize(new Dimension(800,800));
		
		feeJTA.setText("fee: $" + String.format("%3.2f\n", s.getFee()));
		System.out.println("fee: $" + String.format("%3.2f", s.getFee()));
		
		JButton returnB = new JButton("Return To Provider Menu");
		returnB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Return To Provider Menu clicked in updateFee, frameNum = " + frameNum+", nextFrame = " + nextFrame);

				// return to provider menu
				goToFrame(3);
				frameNum = 3;
				
			}
		});
		
		panelFeeResponse.add(feeJTA);
		panelFeeButtons.add(returnB);
		
		feeF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		feeF.getContentPane().add(panelFeeResponse, BorderLayout.CENTER);
		feeF.getContentPane().add(panelFeeButtons, BorderLayout.SOUTH);
		feeF.setVisible(false);
		
		return feeF;
	}
	
	// frame 19 -> 17
	public static void initDisplayService() {
		
		JFrame displayServiceF = new JFrame("Service Info");
		
		JPanel panelDisplayServiceButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelDisplayServiceResponse = new JPanel(new BorderLayout());	//response panel
		displayServiceF.setSize(WIDTH,HEIGHT);

		// text area for user feedback/fee display
		JTextArea responseJTA = new JTextArea("", 10, 30);	// CALLS GLOBAL VARIABLE serviceInfo

		responseJTA.setEditable(false);
		System.out.println("IN initDisplayService, serviceInfo IS: " + serviceInfo);
		responseJTA.setMaximumSize(new Dimension(30, 30));
		
		// button to submit comments (<= 50 chars)
		JButton continueB = new JButton("Yes");
		continueB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button continue clicked in displayService, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(17);
				frameNum = 17;					
			}
		});
		
		JButton prevB = new JButton("No");
		prevB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button prevB clicked in displayService, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(14);
				frameNum = 14;
				
			}
		});
		
		JTextField isThisCorrect = new JTextField("Is this the correct service?", 20);
		isThisCorrect.setEditable(false);

		panelDisplayServiceResponse.add(responseJTA, BorderLayout.CENTER);
		panelDisplayServiceButtons.add(isThisCorrect);
		panelDisplayServiceButtons.add(prevB);
		panelDisplayServiceButtons.add(continueB);
		
		displayServiceF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		displayServiceF.getContentPane().add(panelDisplayServiceResponse);
		displayServiceF.getContentPane().add(panelDisplayServiceButtons, BorderLayout.SOUTH);
		displayServiceF.setVisible(false);
		frames.add(displayServiceF);
	}
	
	// UPDATES FRAME 19 ( called by 14 )
	public static JFrame updateDisplayService(Service s) {
		
		JFrame updatedF = new JFrame("Service Info");
		
		JPanel panelUpdatedButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelUpdatedResponse = new JPanel(new FlowLayout());	//response panel
		updatedF.setSize(WIDTH/2,HEIGHT/2);	// smaller screen
		
		String textToAdd = "";
		textToAdd = (s.getName() + "\n" + s.getCode() + "\n" + String.format("%3.2f\n", s.getFee()));
		
		// text area for user feedback/fee display
		JTextArea updatedResponseJTA = new JTextArea(textToAdd, 10, 30);	// Ask user if this is valid
		updatedResponseJTA.setEditable(false);
		
		System.out.println("IN updateDisplayService, serviceInfo IS: " + s.getName());
		updatedResponseJTA.setMaximumSize(new Dimension(30, 30));

		// button to go to next page
		JButton continueB = new JButton("Yes");
		continueB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button continue clicked in updatedisplayService, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// continue to add comments after confirming service
				goToFrame(17);
				frameNum = 17;					
			}
		});
		
		JButton prevB = new JButton("No");
		prevB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button prevB clicked in displayService, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// return to billing (frame 14)
				goToFrame(14);
				frameNum = 14;
				
			}
		});
		
		// confirmation text
		JTextField isThisCorrect = new JTextField("Is this the correct service?", 20);
		isThisCorrect.setEditable(false);
		
		
		panelUpdatedResponse.add(updatedResponseJTA, BorderLayout.CENTER);
		panelUpdatedButtons.add(isThisCorrect);
		panelUpdatedButtons.add(prevB);
		panelUpdatedButtons.add(continueB);
		
		updatedF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		updatedF.getContentPane().add(panelUpdatedResponse);
		updatedF.getContentPane().add(panelUpdatedButtons, BorderLayout.SOUTH);
		updatedF.setVisible(false);
		
		return updatedF;
	}
	
	// frame 20 (links to 18)
	public static void initPreviewReport() {
		// THIS WILL NEED AN UPDATER METHOD
		JFrame previewF = new JFrame("Preview Report");
		
		JPanel panelPreviewButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelPreviewResponse = new JPanel(new BorderLayout());	//response panel
		previewF.setSize(WIDTH,HEIGHT);

		// text area for user feedback/fee display
		JTextArea previewJTA = new JTextArea(80,80);
		previewJTA.setMinimumSize(new Dimension(500, 500));
		previewJTA.setMaximumSize(new Dimension(800,800));

		DateAndTime dateTime = new DateAndTime();
		billToAdd.setTime(dateTime.getTime() + " " + dateTime.getTime());
		//billToAdd.setDate(dateTime.getDate());
		
		// SET OR GET RECORD DATA AND DISPLAY
		// CALL OTHER METHODS TO FILL THIS IN
		// DATA SHOWN IS TEMP **** REMOVE LATER ****
		previewJTA.setText("Current date and time: " + billToAdd.getTime()+
				   "\nDate service provided: " + billToAdd.getDate()+
				   "\nProvider number: " + billToAdd.getProviderNum()+
				   "\nMember number: " + billToAdd.getMemberNum()+
				   "\nService code: " + billToAdd.getServiceCode()+
				   "\nComments: " + billToAdd.getComments()+"\n");
		
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in initPreviewReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				goToFrame(17);
				frameNum = 17;
			}
		});
		
		JButton nextB = new JButton("Next");
		nextB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button next clicked in initPreviewReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// ADD SERVICE FOR MEMBER
				member.addService(billToAdd);
				// ADD TO ARRAYS
				provMenu.billHealthcare(billToAdd, member.getMemberNum());	// needed? 

				System.out.println("Bill added Successfully!!");

				billToAdd = new Bill();	// reset bill params
				
				// continue to display fee 
				goToFrame(18);
				frameNum = 18;
			}
		});
		
		panelPreviewResponse.add(previewJTA);
		panelPreviewButtons.add(backB);
		panelPreviewButtons.add(nextB);
		
		previewF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		previewF.getContentPane().add(panelPreviewResponse, BorderLayout.CENTER);
		previewF.getContentPane().add(panelPreviewButtons, BorderLayout.SOUTH);
		previewF.setVisible(false);
		frames.add(previewF);
	}

	// updates frame 20 (preview report)
	public static JFrame updatePreviewReport() {
		
		JFrame previewF = new JFrame("Preview Report");
		
		JPanel panelPreviewButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelPreviewResponse = new JPanel(new BorderLayout());	//response panel
		previewF.setSize(WIDTH,HEIGHT);

		// text area for user feedback/fee display
		JTextArea previewJTA = new JTextArea(80,80);
		previewJTA.setMinimumSize(new Dimension(500, 500));
		previewJTA.setMaximumSize(new Dimension(800,800));
		
		DateAndTime dateTime = new DateAndTime();
		billToAdd.setTime(dateTime.getDate() + " " + dateTime.getTime());
		//billToAdd.setDate(dateTime.getDate());
		
		// SET OR GET RECORD DATA AND DISPLAY
		
		previewJTA.setText("Current date and time: " + billToAdd.getTime()+
				   "\nDate service provided: " + billToAdd.getDate()+
				   "\nProvider number: " + billToAdd.getProviderNum()+
				   "\nMember number: " + billToAdd.getMemberNum()+
				   "\nService code: " + billToAdd.getServiceCode()+
				   "\nComments: " + billToAdd.getComments()+"\n");

		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in initPreviewReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);

				goToFrame(17);
				frameNum = 17;
			}
		});
		
		JButton nextB = new JButton("Next");
		nextB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button next clicked in initPreviewReport, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				// VERIFY 
				System.out.println("member.servicesReceived.size() (BEFORE): " + member.getServicesReceived().size());
				System.out.println("Member services/bills before adding(FRAMEBUILDER/updatePreviewReport):\n------------\n ");
				for (Bill b : member.getServicesReceived()) {
					System.out.println("Time: "+b.getTime());
					System.out.println("Date: "+b.getDate());
					System.out.println("ProvNum: "+b.getProviderNum());
					System.out.println("MemNum: "+b.getMemberNum());
					System.out.println("ServiceCode: "+b.getServiceCode());
					System.out.println("Comments: "+b.getComments()+"\n---------------\n");
				}
				member.addService(billToAdd);
				System.out.println("Bill added Successfully!!");
				System.out.println("member.servicesReceived.size() (AFTER): " + member.getServicesReceived().size());
				System.out.println("Member services/bills AFTER adding(FRAMEBUILDER/updatePreviewReport):\n------------\n ");
				for (Bill b : member.getServicesReceived()) {
					System.out.println("Time: "+b.getTime());
					System.out.println("Date: "+b.getDate());
					System.out.println("ProvNum: "+b.getProviderNum());
					System.out.println("MemNum: "+b.getMemberNum());
					System.out.println("ServiceCode: "+b.getServiceCode());
					System.out.println("Comments: "+b.getComments()+"\n---------------\n");
				}
				
				// ADD TO ARRAY
				provMenu.billHealthcare(billToAdd, member.getMemberNum());
				
				billToAdd = new Bill();	// reset bill params
				
				goToFrame(18);
				frameNum = 18;
			}
		});
		
		panelPreviewResponse.add(previewJTA);
		panelPreviewButtons.add(backB);
		panelPreviewButtons.add(nextB);
		
		previewF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		previewF.getContentPane().add(panelPreviewResponse, BorderLayout.CENTER);
		previewF.getContentPane().add(panelPreviewButtons, BorderLayout.SOUTH);
		previewF.setVisible(false);
		return previewF;
	}

	// frame 21
	public static void initInputAddMember() {
		
		JFrame addMemberF = new JFrame("Add Member");
		
		JPanel panelAddMemberButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelAddMemberText = new JPanel(new FlowLayout());	//text input panel
		addMemberF.setSize(WIDTH,HEIGHT);
		
		// text fields for Member Record
		JTextField nameJTF = new JTextField(15);
		nameJTF.setText("Enter Name");
		nameJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField addressJTF = new JTextField(15);
		addressJTF.setText("Enter Address");
		addressJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField cityJTF = new JTextField(15);
		cityJTF.setText("Enter City");
		cityJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField stateJTF = new JTextField(15);
		stateJTF.setText("Enter State");
		stateJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField zipJTF = new JTextField(15);
		zipJTF.setText("Enter Zip Code");
		zipJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField emailJTF = new JTextField(15);
		emailJTF.setText("Enter Email");
		emailJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField passwordJTF = new JTextField(15);
		passwordJTF.setText("Enter Password");
		passwordJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField memberNumJTF = new JTextField(15);
		memberNumJTF.setText("Enter Member Num");
		memberNumJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField statusJTF = new JTextField(15);
		statusJTF.setText("Enter Status");
		statusJTF.setMinimumSize(new Dimension(150,50));
		
		// button to verify email and password
		JButton submitB = new JButton("Add");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Add clicked in addMember, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				String nameToAdd = nameJTF.getText();
				String addressToAdd = addressJTF.getText();
				String cityToAdd = cityJTF.getText();
				String stateToAdd = stateJTF.getText();
				String zipcodeToAdd = zipJTF.getText();
				String emailToAdd = emailJTF.getText();
				String passwordToAdd = passwordJTF.getText();
				String memberNumToAdd = memberNumJTF.getText();
				String statusToAdd = statusJTF.getText();
				
				// Construct new member from info given
				Member memberToAdd = new Member(nameToAdd, addressToAdd,cityToAdd, stateToAdd, zipcodeToAdd,
												emailToAdd,passwordToAdd,memberNumToAdd,statusToAdd);

				// Check if the member being added already exists or not 
				if (terminal.memberExists(memberToAdd)) {
					System.out.println("ERROR: MEMBER ALREADY IN LIST");
				} else {
					// CHECK FORMAT OF PROVIDER
					if (formatter.validFormatMem(memberToAdd)) {
						terminal.members.add(memberToAdd);		// KEEP WHEN YOU DELETE TESTING LOOPS ABOVE AND BELOW						
						
						memberToAdd = null;
						// Go back to operator menu
						goToFrame(2);
						frameNum = 2;
					} else {
						// set previous frame num to this current frame num (so it returns to this frame after exiting error frame)
						System.out.println("Invalid format for member");
						prevFrameNum = frameNum;
						// display error page
						goToFrame(29);
						frameNum = 29;
					}
					// add member to member ArrayList
					System.out.println("Member added");
				}
			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in addMember, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				member = null; // reset global member 
				// VERIFY 
				goToFrame(2);
				frameNum = 2;
				
			}
		});
		
		panelAddMemberText.add(statusJTF, FlowLayout.LEFT);
		panelAddMemberText.add(zipJTF, FlowLayout.LEFT);
		panelAddMemberText.add(stateJTF, FlowLayout.LEFT);
		panelAddMemberText.add(cityJTF, FlowLayout.LEFT);
		panelAddMemberText.add(addressJTF, FlowLayout.LEFT);
		panelAddMemberText.add(memberNumJTF, FlowLayout.LEFT);
		panelAddMemberText.add(passwordJTF, FlowLayout.LEFT);
		panelAddMemberText.add(emailJTF, FlowLayout.LEFT);
		panelAddMemberText.add(nameJTF, FlowLayout.LEFT);
		panelAddMemberText.add(submitB);
		panelAddMemberButtons.add(backB);
		
		addMemberF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addMemberF.getContentPane().add(panelAddMemberText, BorderLayout.CENTER);
		addMemberF.getContentPane().add(panelAddMemberButtons, BorderLayout.SOUTH);
		addMemberF.setVisible(false);
		frames.add(addMemberF);
	}
	
	// frame 22
	public static void initDeleteMember() {
		// all the possible member reports are here
		JFrame operateMemReportsF = new JFrame("Delete Member");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateMemReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Member> memberIt = terminal.members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			
			JButton reportButton = new JButton(curMember.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for deletion, SELECTED FILE " + curMember.getName() +" and " + frameNum);
					// remove member if selected
					terminal.members.remove(curMember);
					
					
					goToFrame(2);	//go to operator menu
					frameNum = 2;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curMember.getName());		
	     }
					
		JButton backB = new JButton("Back");
				
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button backB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});

		panelOperateReportExit.add(backB);
		
		operateMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateMemReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateMemReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateMemReportsF.setVisible(false);
		frames.add(operateMemReportsF);
	}	
	
	// UPDATE frame 22
	public static JFrame updateDeleteMember() {
		// all the possible member reports are here
		JFrame operateMemReportsF = new JFrame("Delete Member");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateMemReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Member> memberIt = terminal.members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			
			JButton reportButton = new JButton(curMember.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for deletion, SELECTED FILE " + curMember.getName() +" and " + frameNum);
					// remove member if selected
					terminal.members.remove(curMember);

					goToFrame(2);	//go to operator menu
					frameNum = 2;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curMember.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		panelOperateReportExit.add(logoutB);
		
		operateMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateMemReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateMemReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateMemReportsF.setVisible(false);
		return operateMemReportsF;
	}	
	
	
	// frame 23
	public static void initUpdateMember() {
		// all the possible member reports are here
		JFrame operateMemReportsF = new JFrame("Update Member");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateMemReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Member> memberIt = terminal.members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			
			JButton reportButton = new JButton(curMember.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for updating, SELECTED FILE " + curMember.getName() +" and " + frameNum);
					
										
					updateMember(curMember);
					
					goToFrame(27);	//go to update interface
					frameNum = 27;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curMember.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		panelOperateReportExit.add(logoutB);
		
		operateMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateMemReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateMemReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateMemReportsF.setVisible(false);
		frames.add(operateMemReportsF);
	}
	
	// UPDATE frame 23
	public static JFrame updateUpdateMember() {
		// all the possible member reports are here
		JFrame operateMemReportsF = new JFrame("Update Member");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateMemReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Member> memberIt = terminal.members.listIterator();
	    
		// check for member existence
		while (memberIt.hasNext()) {
			Member curMember = memberIt.next();
			
			JButton reportButton = new JButton(curMember.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for updating, SELECTED FILE " + curMember.getName() +" and " + frameNum);
					
					// UPDATE member if selected
					updateMember(curMember);
					
					goToFrame(27);	//go to update interface
					frameNum = 27;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curMember.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});

		panelOperateReportExit.add(logoutB);
		
		operateMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateMemReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateMemReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateMemReportsF.setVisible(false);
		return operateMemReportsF;
	}

	// build frame 27 (update member info)
	public static void updateMember(Member mem) {
		
		JFrame updateMemberF = new JFrame("Update Member");
		
		JPanel panelUpdateMemberButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelUpdateMemberText = new JPanel(new FlowLayout());	//text input panel
		updateMemberF.setSize(WIDTH,HEIGHT);
		
		// text fields for Member Record
		JTextField nameJTF = new JTextField(15);
		nameJTF.setText(mem.getName());
		nameJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField addressJTF = new JTextField(15);
		addressJTF.setText(mem.getAddress());
		addressJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField cityJTF = new JTextField(15);
		cityJTF.setText(mem.getCity());
		cityJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField stateJTF = new JTextField(15);
		stateJTF.setText(mem.getState());
		stateJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField zipJTF = new JTextField(15);
		zipJTF.setText(mem.getZipcode());
		zipJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField emailJTF = new JTextField(15);
		emailJTF.setText(mem.getEmail());
		emailJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField passwordJTF = new JTextField(15);
		passwordJTF.setText(mem.getPassword());
		passwordJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField memberNumJTF = new JTextField(15);
		memberNumJTF.setText(mem.getMemberNum());
		memberNumJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField statusJTF = new JTextField(15);
		statusJTF.setText(mem.getStatus());
		statusJTF.setMinimumSize(new Dimension(150,50));
		
		// button to verify email and password
		JButton submitB = new JButton("Update");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Update clicked in updateMember, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				String nameToUpdate = nameJTF.getText();
				String addressToUpdate = addressJTF.getText();
				String cityToUpdate = cityJTF.getText();
				String stateToUpdate = stateJTF.getText();
				String zipcodeToUpdate = zipJTF.getText();
				String emailToUpdate = emailJTF.getText();
				String passwordToUpdate = passwordJTF.getText();
				String memberNumToUpdate = memberNumJTF.getText();
				String statusToUpdate = statusJTF.getText();
				
				// Construct new member from info given
				Member memberToUpdate = new Member(nameToUpdate, addressToUpdate,cityToUpdate, stateToUpdate, zipcodeToUpdate,
												emailToUpdate,passwordToUpdate,memberNumToUpdate,statusToUpdate);

				if ((terminal.memberExists(memberToUpdate)) && (formatter.validFormatMem(memberToUpdate))) {
					System.out.println("member exists and updates valid -> updating:");
					terminal.members.remove(mem);				// remove previous version of member
					terminal.members.add(memberToUpdate);		// add member to back into arraylist as a new member					
				
					// Clear boxes
					nameJTF.setText("Enter Name");
					addressJTF.setText("Enter Address");
					cityJTF.setText("Enter City");
					stateJTF.setText("Enter State");
					zipJTF.setText("Enter Zip");
					emailJTF.setText("Enter Email");
					passwordJTF.setText("Enter Password");
					memberNumJTF.setText("Enter Member Num");
					statusJTF.setText("Enter Status");
				
					// Go back to operator menu
					memberToUpdate = null;
					goToFrame(2);
					frameNum = 2;
					
					
				} else if (!formatter.validFormatMem(memberToUpdate)) {
						// set previous frame num to this current frame num (so it returns to this frame after exiting error frame)
						System.out.println("Invalid format for member");
						prevFrameNum = frameNum;
						// display error page
						goToFrame(29);
						frameNum = 29;
					}
				System.out.println("AFTER UPDATING MEMBER");

			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in updateMember, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				member = null; // reset global member 

				goToFrame(2);
				frameNum = 2;
				
			}
		});
		
		panelUpdateMemberText.add(statusJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(zipJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(stateJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(cityJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(addressJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(passwordJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(emailJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(memberNumJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(nameJTF, FlowLayout.LEFT);
		panelUpdateMemberText.add(submitB);
		panelUpdateMemberButtons.add(backB);
		
		updateMemberF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		updateMemberF.getContentPane().add(panelUpdateMemberText, BorderLayout.CENTER);
		updateMemberF.getContentPane().add(panelUpdateMemberButtons, BorderLayout.SOUTH);
		updateMemberF.setVisible(false);
		frames.remove(27);
		frames.add(27, updateMemberF);
	}

	// frame 24
	public static void initInputAddProvider() {
		
		JFrame addProviderF = new JFrame("Add Provider");
		
		JPanel panelAddProviderButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelAddProviderText = new JPanel(new FlowLayout());	//text input panel
		addProviderF.setSize(WIDTH,HEIGHT);
		
		// text fields for Provider Record
		JTextField nameJTF = new JTextField(15);
		nameJTF.setText("Enter Name");
		nameJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField addressJTF = new JTextField(15);
		addressJTF.setText("Enter Address");
		addressJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField cityJTF = new JTextField(15);
		cityJTF.setText("Enter City");
		cityJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField stateJTF = new JTextField(15);
		stateJTF.setText("Enter State");
		stateJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField zipJTF = new JTextField(15);
		zipJTF.setText("Enter Zip Code");
		zipJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField emailJTF = new JTextField(15);
		emailJTF.setText("Enter Email");
		emailJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField passwordJTF = new JTextField(15);
		passwordJTF.setText("Enter Password");
		passwordJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField providerNumJTF = new JTextField(15);
		providerNumJTF.setText("Enter Provider Num");
		providerNumJTF.setMinimumSize(new Dimension(150,50));
		
		// button to add a provider
		JButton submitB = new JButton("Add");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Add clicked in addProvider, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				String nameToAdd = nameJTF.getText();
				String addressToAdd = addressJTF.getText();
				String cityToAdd = cityJTF.getText();
				String stateToAdd = stateJTF.getText();
				String zipcodeToAdd = zipJTF.getText();
				String emailToAdd = emailJTF.getText();
				String passwordToAdd = passwordJTF.getText();
				String providerNumToAdd = providerNumJTF.getText();
				
				// Construct new provider from info given
				Provider providerToAdd = new Provider(nameToAdd,addressToAdd,cityToAdd,stateToAdd,zipcodeToAdd,
												emailToAdd,passwordToAdd,providerNumToAdd);
				
				
				// Check if the provider being added already exists or not (Display error somehow ?)
				if (terminal.providerExists(providerToAdd)) {
					System.out.println("ERROR: PROVIDER ALREADY IN LIST");
				} else {
					// CHECK FORMAT OF PROVIDER
					if (formatter.validFormatProv(providerToAdd)) {
						terminal.providers.add(providerToAdd);		// add provider to the list						
						providerToAdd = null;
						// Go back to operator menu
						goToFrame(2);
						frameNum = 2;
					} else {
						// set previous frame num to this current frame num (so it returns to this frame after exiting error frame)
						System.out.println("Invalid format for provider");
						prevFrameNum = frameNum;
						// display error page
						goToFrame(29);
						frameNum = 29;
					}
				}
				System.out.println("PROVIDER ADDED");
								
			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in addProvider, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				provider = null; // reset global provider 
				
				// clear input boxes
				nameJTF.setText("Enter Name");
				addressJTF.setText("Enter Address");
				cityJTF.setText("Enter City");
				stateJTF.setText("Enter State");
				zipJTF.setText("Enter Zip");
				emailJTF.setText("Enter Email");
				passwordJTF.setText("Enter Password");
				providerNumJTF.setText("Enter Provider Num");
				
				
				// return to operator menu
				goToFrame(2);
				frameNum = 2;
				
			}
		});
		
		panelAddProviderText.add(zipJTF, FlowLayout.LEFT);
		panelAddProviderText.add(stateJTF, FlowLayout.LEFT);
		panelAddProviderText.add(cityJTF, FlowLayout.LEFT);
		panelAddProviderText.add(addressJTF, FlowLayout.LEFT);
		panelAddProviderText.add(passwordJTF, FlowLayout.LEFT);
		panelAddProviderText.add(emailJTF, FlowLayout.LEFT);
		panelAddProviderText.add(providerNumJTF, FlowLayout.LEFT);
		panelAddProviderText.add(nameJTF, FlowLayout.LEFT);
		panelAddProviderText.add(submitB);
		panelAddProviderButtons.add(backB);
		
		addProviderF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addProviderF.getContentPane().add(panelAddProviderText, BorderLayout.CENTER);
		addProviderF.getContentPane().add(panelAddProviderButtons, BorderLayout.SOUTH);
		addProviderF.setVisible(false);
		frames.add(addProviderF);
	}
	
	// frame 25
	public static void initDeleteProvider() {
		// all the possible provider reports are here
		JFrame operateMemReportsF = new JFrame("Select Provider Report");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateMemReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Provider> providerIt = terminal.providers.listIterator();
	    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			
			JButton reportButton = new JButton(curProvider.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for deletion, SELECTED FILE " + curProvider.getName() +" and " + frameNum);
					// remove provider if selected
					terminal.providers.remove(curProvider);
					goToFrame(2);	//go to operator menu
					frameNum = 2;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curProvider.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});

		panelOperateReportExit.add(logoutB);
		
		operateMemReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateMemReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateMemReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateMemReportsF.setVisible(false);
		frames.add(operateMemReportsF);
	}	
	
	// UPDATES frame 25
	
	public static JFrame updateDeleteProvider() {
		// all the possible provider reports are here
		JFrame operateProvReportsF = new JFrame("Select Provider Report");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateProvReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Provider> providerIt = terminal.providers.listIterator();
	    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			
			JButton reportButton = new JButton(curProvider.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for deletion, SELECTED FILE " + curProvider.getName() +" and " + frameNum);
					// remove provider if selected
					terminal.providers.remove(curProvider);
					
					
					goToFrame(2);	//go to operator menu
					frameNum = 2;
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curProvider.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		panelOperateReportExit.add(logoutB);
		
		operateProvReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateProvReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateProvReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateProvReportsF.setVisible(false);
		return operateProvReportsF;
	}	

	// frame 26
	public static void initUpdateProvider() {
		// all the possible provider reports are here
		JFrame operateProvReportsF = new JFrame("Select Provider Report");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateProvReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Provider> providerIt = terminal.providers.listIterator();
	    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			
			JButton reportButton = new JButton(curProvider.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for update, SELECTED FILE " + curProvider.getName() +" and " + frameNum);
					// UPDATE provider if selected
					updateProvider(curProvider);
					
					goToFrame(28);
					frameNum = 28;
					
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curProvider.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		panelOperateReportExit.add(logoutB);
		
		operateProvReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateProvReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateProvReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateProvReportsF.setVisible(false);
		frames.add(operateProvReportsF);
	}
	
	// UPDATE frame 26
	public static JFrame updateUpdateProvider() {
		// all the possible provider reports are here
		JFrame operateProvReportsF = new JFrame("Select Provider Report");
		
		JPanel panelOperateButtons= new JPanel(new FlowLayout());	//button panel
		JPanel panelOperateReportExit= new JPanel(new FlowLayout());	//exit button panel
		
		operateProvReportsF.setSize(WIDTH,HEIGHT);
		
		ListIterator<Provider> providerIt = terminal.providers.listIterator();
	    
		// check for provider existence
		while (providerIt.hasNext()) {
			Provider curProvider = providerIt.next();
			
			JButton reportButton = new JButton(curProvider.getName());
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button reportButton in operate clicked for deletion, SELECTED FILE " + curProvider.getName() +" and " + frameNum);
					// UPDATE provider if selected
					updateProvider(curProvider);
					
					goToFrame(28);
					frameNum = 28;
					
				}
			});
			panelOperateButtons.add(reportButton);
			System.out.println(curProvider.getName());		
	     }
					
		JButton logoutB = new JButton("Back");
				
		logoutB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button logoutB clicked, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				goToFrame(2);	//go to operator menu
				frameNum = 2;
			}
		});
		
		
		panelOperateReportExit.add(logoutB);
		
		operateProvReportsF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		operateProvReportsF.getContentPane().add(panelOperateButtons, BorderLayout.CENTER);
		operateProvReportsF.getContentPane().add(panelOperateReportExit, BorderLayout.SOUTH);
		operateProvReportsF.setVisible(false);
		return operateProvReportsF;
	}
	
	
	// build frame 28 (update provider info)
	public static void updateProvider(Provider prov) {
		
		JFrame updateProviderF = new JFrame("Update Provider");
		
		JPanel panelUpdateProviderButtons = new JPanel(new FlowLayout());	//button panel
		JPanel panelUpdateProviderText = new JPanel(new FlowLayout());	//text input panel
		updateProviderF.setSize(WIDTH,HEIGHT);
		
		// text fields for Provider Record
		JTextField nameJTF = new JTextField(15);
		nameJTF.setText(prov.getName());
		nameJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField addressJTF = new JTextField(15);
		addressJTF.setText(prov.getAddress());
		addressJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField cityJTF = new JTextField(15);
		cityJTF.setText(prov.getCity());
		cityJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField stateJTF = new JTextField(15);
		stateJTF.setText(prov.getState());
		stateJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField zipJTF = new JTextField(15);
		zipJTF.setText(""+prov.getZipcode());
		zipJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField emailJTF = new JTextField(15);
		emailJTF.setText(prov.getEmail());
		emailJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField passwordJTF = new JTextField(15);
		passwordJTF.setText(prov.getPassword());
		passwordJTF.setMinimumSize(new Dimension(150,50));
		
		JTextField providerNumJTF = new JTextField(15);
		providerNumJTF.setText(""+prov.getProviderNum());
		providerNumJTF.setMinimumSize(new Dimension(150,50));
		
		// button to verify email and password
		JButton submitB = new JButton("Update");
		submitB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button Update clicked in updateProvider, frameNum = " + frameNum+", nextFrame = " + nextFrame);

				String nameToUpdate = nameJTF.getText();
				String addressToUpdate = addressJTF.getText();
				String cityToUpdate = cityJTF.getText();
				String stateToUpdate = stateJTF.getText();
				String zipcodeToUpdate = zipJTF.getText();
				String emailToUpdate = emailJTF.getText();
				String passwordToUpdate = passwordJTF.getText();
				String providerNumToUpdate = providerNumJTF.getText();
				
				// Construct new provider from info given
				Provider providerToUpdate = new Provider(nameToUpdate, addressToUpdate,cityToUpdate, stateToUpdate, zipcodeToUpdate,
												emailToUpdate,passwordToUpdate,providerNumToUpdate);
				
				System.out.println("BEFORE UPDATING PROVIDER");
				
				
				// Check if the provider being updated already exists or not (Display error somehow ?)
				if ((terminal.providerExists(prov)) && (formatter.validFormatProv(providerToUpdate))) {
					System.out.println("provider exists and updates are valid-> updating:");
					terminal.providers.remove(prov);
					terminal.providers.add(providerToUpdate);		// remove old provider, and add a new provider with updates
				
					// Clear boxes
					nameJTF.setText("Enter Name");
					addressJTF.setText("Enter Address");
					cityJTF.setText("Enter City");
					stateJTF.setText("Enter State");
					zipJTF.setText("Enter Zip");
					emailJTF.setText("Enter Email");
					passwordJTF.setText("Enter Password");
					providerNumJTF.setText("Enter Provider Num");
				
					// remove providerToUpdate after updateing to the array
					providerToUpdate = null;
					// Go back to operator menu
					goToFrame(2);
					frameNum = 2;
				
				} else if (!formatter.validFormatProv(providerToUpdate)) {
					
					prevFrameNum = frameNum;	// hold this page as the previous frame
					
					goToFrame(29); // go to error frame
					frameNum = 29;
				}
				
				System.out.println("AFTER UPDATING PROVIDER");

			}
		});
		
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button back clicked in updateProvider, frameNum = " + frameNum+", nextFrame = " + nextFrame);
				
				provider = null; // reset global provider 
				// return to operator menu
				goToFrame(2);
				frameNum = 2;
				
			}
		});
		
		panelUpdateProviderText.add(zipJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(stateJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(cityJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(addressJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(passwordJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(emailJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(providerNumJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(nameJTF, FlowLayout.LEFT);
		panelUpdateProviderText.add(submitB);
		panelUpdateProviderButtons.add(backB);
		
		updateProviderF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		updateProviderF.getContentPane().add(panelUpdateProviderText, BorderLayout.CENTER);
		updateProviderF.getContentPane().add(panelUpdateProviderButtons, BorderLayout.SOUTH);
		updateProviderF.setVisible(false);
		frames.remove(28);
		frames.add(28, updateProviderF);
	}
	

	// frame 29 - FORMATTING ERROR PAGE
	public static void initErrorPage() {
		// all the possible provider reports are here
		JFrame errorF = new JFrame("ERROR");
		
		JPanel panelErrorDisplay= new JPanel(new FlowLayout());	//button panel
		JPanel panelErrorExit= new JPanel(new FlowLayout());	//exit button panel
		
		JTextArea errorJTA = new JTextArea();	// error information
		
		errorF.setSize(WIDTH,HEIGHT);
		
		// formatting error text
		errorJTA.setText("Formatting Error Found: \n    User Name must be <= 25 characters\n    User Number must be EXACTLY 9 digits\n    "+
				"Address must be <= 25 characters\n    City must be <= 14 characters\n    State must be EXACTLY 2 digits (e.g. AL, OH, TX)\n    "+
				"ZIP code must be EXACTLY 5 digits (e.g. 35401)\n    Member Status must be EXACTLY: \"Validated\", \"Member suspended\", or \"Invalid Number\"    \n\n"+
				"Services must be formatted as follows:\n    Date of service must be written \"MM-DD-YYYY\"\n    - MM in range 01 to 12\n    - DD in range 01 to 31\n"+
				"    - YYYY in range 0001 to 9999\n    Date and Time received must be written \"MM-DD-YYYY HH:mm:SS\"\n    - HH in range 00 to 23\n"+
				"    - mm in range 00 to 59\n    - SS in range 00 to 59\n    Service code must be EXACTLY 6 digits (e.g. 598470)\n"+
				"    Service name must be <= 20 characters\n    "
				
				);
					
		JButton backB = new JButton("Back");
		backB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button backB clicked in format error page, prevFrame = " + getPreviousFrame() + "frameNum = " + frameNum);
				
				goToFrame(getPreviousFrame());	// go back to previous frame (stored globally)
				frameNum = getPreviousFrame();
			}
		});
		
		panelErrorDisplay.add(errorJTA);
		panelErrorExit.add(backB);
		
		errorF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		errorF.getContentPane().add(panelErrorDisplay, BorderLayout.CENTER);
		errorF.getContentPane().add(panelErrorExit, BorderLayout.SOUTH);
		errorF.setVisible(false);
		frames.add(errorF);
	}
	
	/**
	 * Avoids out of range errors before completion of framework
	 */
	public static void INSERT_FILLER_FRAME() {
		// all the possible member reports are here
		JFrame FILLERFRAME = new JFrame("FILLER FRAME");
		
		FILLERFRAME.setSize(WIDTH,HEIGHT);
				
		FILLERFRAME.setVisible(false);
		FILLERFRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frames.add(FILLERFRAME);
		
	}

	/**
	 * 
	 * @return previous frame number (int : prevFrameNum)
	 */
	public static int getPreviousFrame() {
		return prevFrameNum;
	}
	

}
