package fearricepudding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Storage {
	
	private static int levels = 5; // Folder structure for storing values

	/**
	 * Constructor set hash of key
	 * @param k key
	 */
	public Storage() {
		//	System.out.println("[*] Storage");
	}

	/**
	 * Get hashtable number
	 * @param h String hash of key
	 * @return Int level where stored
	 */
	public static String calcHashValue(String h){
		int mid = (h.length()-1)/2;
		char midChar = h.charAt(mid);
		int midSquare = (int)midChar*(int)midChar;
		int hash = midSquare % levels; 
		return String.valueOf(hash);
	}

	/**
	 * Convert a string to an MD5 hash 
	 * @param k String to hash 
	 * @return MD5 hash
	 */
	private String toMd5(String k){
		try {
		 	MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hashInBytes = md.digest(k.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hashInBytes) {
			    sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "md5 Failed";
		}
	}
	
	/**
	 * Find key
	 * @param key String
	 * @return String path to key
	 */
	private String getPathToKey(String key){
		String pathHash = toMd5(calcHashValue(key));
		String keyHash = toMd5(key);
		return dir()+getSlash()+pathHash+getSlash()+keyHash;
	}

	/**
	 * Delete an item with key
	 * @param deleteKey - key to delete
	 * @return Boolean status
	 *
	 * TODO: HashTable to find key
	 */
	public Boolean delete(String deleteKey) {
		String location = getPathToKey(deleteKey);
		if(fileExists(location)) {
			new File(location).delete();
		}
		return true;
	}
	
	/**
	 * Get the OS style directory slash
	 * @return OS directory slash type
	 */
	public static String getSlash() {
		String os = System.getProperty("os.name");
		String slash; 
		if(os == "Windows") {
			slash = "\\";
		} else {
			slash = "/";
		}
		return slash; 
	}
	
	/**
	 * Generate data directory path
	 * @return data directory path
	 */
	public static String dir() {
		return System.getProperty("user.dir")+getSlash()+"data";
	}

	/**
	 * Read file content
	 * @param p String path to file
	 * @return String file content
	 */
	private String readFileContent(String p){
		Path path = Paths.get(p);
		Charset encoding = StandardCharsets.UTF_8;
		try{
			byte[] encoded = Files.readAllBytes(path);
			return new String(encoded, encoding);
		}catch(IOException e){
			// eh
			System.out.println("Error reading file: "+e);
			return "";
		}
	}

	/**
	 * Check if file exists
	 * @param p String file location
	 * @return boolean
	 */
	private boolean fileExists(String p){
		return new File(p).exists();
	}
	
	/**
	 * Get the key data from storage
	 * @param findKey - key to find data from
	 * @return - data from key
	 */
	public String get(String k) {
		String location = getPathToKey(k);
		if(fileExists(location)) {
			return readFileContent(location);
		};
		return ""; // If none exist
	}

	/**
	 * Write content to file
	 * @param path String
	 * @param data String
	 * @return boolean
	 */
	private boolean writeContentToFile(String key, String value){
		try{
			String hashPath = toMd5(calcHashValue(key));
			File location = new File(dir()+getSlash()+hashPath);
			if(!location.exists()){
				location.mkdir();
			}
			Path path = Paths.get(getPathToKey(key));
			Files.write(path, value.getBytes(), StandardOpenOption.CREATE);
			return true;
		}catch(IOException e){
			System.out.println("Error writing file: "+ e);
			return false;
		}
	}	

	/**
	 * Store key and data in the data folder
	 * @param data - data to store
	 * @param newKey - key to store with
	 * @return - boolean status
	 */
	public boolean store(String data, String newKey, Boolean overwrite) {
		String location = getPathToKey(newKey);
		if(fileExists(location) && !overwrite){
			return false;
		}
		return writeContentToFile(newKey, data);
	}
}
