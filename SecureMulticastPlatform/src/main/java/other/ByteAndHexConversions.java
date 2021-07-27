package other;

public class ByteAndHexConversions {
	 private byte hexToByte(String hexString) {
	        int firstDigit = toDigit(hexString.charAt(0));
	        int secondDigit = toDigit(hexString.charAt(1));
	        return (byte) ((firstDigit << 4) + secondDigit);
	    }

	    private int toDigit(char hexChar) {
	        int digit = Character.digit(hexChar, 16);
	        if(digit == -1) {
	            throw new IllegalArgumentException(
	              "Invalid Hexadecimal Character: "+ hexChar);
	        }
	        return digit;
	    }
	    
	    public byte[] decodeHexString(String hexString) {
	        if (hexString.length() % 2 == 1) {
	            throw new IllegalArgumentException(
	              "Invalid hexadecimal String supplied.");
	        }
	        
	        byte[] bytes = new byte[hexString.length() / 2];
	        for (int i = 0; i < hexString.length(); i += 2) {
	            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
	        }
	        return bytes;
	    }
	    
	    public String encodeHexString(byte[] byteArray) {
	        StringBuffer hexStringBuffer = new StringBuffer();
	        for (int i = 0; i < byteArray.length; i++) {
	            hexStringBuffer.append(byteToHex(byteArray[i]));
	        }
	        return hexStringBuffer.toString();
	    }
	    
	    private String byteToHex(byte num) {
	        char[] hexDigits = new char[2];
	        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
	        hexDigits[1] = Character.forDigit((num & 0xF), 16);
	        return new String(hexDigits);
	    }
}
