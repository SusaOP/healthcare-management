package healthcare;

public class VerifyFormat {

	/**
	 * Default Constructor
	 */
	public VerifyFormat() {
		
	}

	/**
	 *
	 * @param prov		Provider to be checked for valid formatting
	 * @return	False (any part is not valid), True (all parts are valid)
	 */
	public boolean validFormatProv(Provider prov) {
		
		if (!validFormatName(prov.getName())) return false;
		if (!validFormatAddress(prov.getAddress())) return false;
		if (!validFormatCity(prov.getCity())) return false;
		if (!validFormatState(prov.getState())) return false;
		if (!validFormatZIP(prov.getZipcode())) return false;
		if (!validFormatNumber(prov.getProviderNum())) return false;
		
		System.out.println("Valid Format for Provider: " + prov.getName());
		return true;
	}

	/**
	 *
	 * @param mem		Member to be checked for valid formatting
	 * @return	False (any part is not valid), True (all parts are valid)
	 */
	public boolean validFormatMem(Member mem) {
		
		if (!validFormatName(mem.getName())) return false;
		if (!validFormatAddress(mem.getAddress())) return false;
		if (!validFormatCity(mem.getCity())) return false;
		if (!validFormatState(mem.getState())) return false;
		if (!validFormatZIP(mem.getZipcode())) return false;
		if (!validFormatNumber(mem.getMemberNum())) return false;
		
		System.out.println("Valid Format for Member: " + mem.getName());
		return true;
	}

	/**
	 *
	 * @param name		Name to be checked
	 * @return	True (valid length), False (name too long)
	 */
	public boolean validFormatName(String name) {
		if (name.length() <= 25) return true;
		return false;
	}
	/*
	 * Check that number == 9 digits. If < 9, pad with 0's. If non-digit/too long, error.
	 */
	public boolean validFormatNumber(String number) {
		
		if (number.length() != 9) return false;
		for (int i = 0; i < number.length(); i++) {
			if (!Character.isDigit(number.charAt(i))) return false;
		}
		
		return true;
	}

	/*
	 * Check length of city is <= 14 characters
	 */
	public boolean validFormatCity(String city) {
		if (city.length() <= 14) return true;
		return false;
	}
	/*
	 * Check length of address is <= 25 characters
	 */
	public boolean validFormatAddress(String address) {
		if (address.length() <= 25) return true;
		return false;
	}
	/*
	 * Check length of State is 2 letters
	 */
	public boolean validFormatState(String state) {
		if (state.length() != 2) return false;
		return true;
	}
	/*
	 * Check zip code is 5 digits long
	 */
	public boolean validFormatZIP(String zip) {
		if (zip.length() != 5) return false;
		for (int i = 0; i < zip.length(); i++) {
			if (!Character.isDigit(zip.charAt(i))) return false;
		}
		return true;
	}
	/*
	 * Check date has valid date input and uses "-" as delimiter
	 */
	public boolean validFormatDate(String date) {
		if (date.length() != 10) return false;
		//Check that month is <= 12, day is <= 31
		if (Integer.parseInt(""+date.charAt(0)+date.charAt(1)) > 12) return false;
		if (Integer.parseInt(""+date.charAt(3)+date.charAt(4)) > 31) return false;
		//Check for - formatting
		if (date.charAt(2) != '-' || date.charAt(5) != '-') return false;
		return true;
	}
	/*
	 * Checks that service code is 6 digits. Pads zeroes to front if < 6.
	 */
	public boolean validFormatServiceCode(String serviceCode) {

		if (serviceCode.length() != 6) return false;
		for (int i = 0; i < serviceCode.length(); i++) {
			if (!Character.isDigit(serviceCode.charAt(i))) {
				System.out.println("ERROR: in validFormatServiceCode, " + serviceCode.charAt(i) + " is not digit");
				return false;
			}
		}
		
		
		return true;
	}

}
