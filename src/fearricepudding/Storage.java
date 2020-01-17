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

public class Storage {
	
	public String key = "";
	public String data = "";
	public String status = "";
	private static int levels = 2;

	/**
	 * Constructor set hash of key
	 * 
	 * @param k key
	 */
	public Storage(String k) {
		// Set the public key string
		key = toMd5(k);
	}

	/**
	 * Convert a string to an MD5 hash
	 * 
	 * @param k String to hash
	 * 
	 * @return MD5 hash
	 */
	public static String toMd5(String k){
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
	 * Delete an item with key
	 * 
	 * @param deleteKey - key to delete
	 * 
	 * @return Boolean status
	 */
	public Boolean delete(String deleteKey) {
		// Loop through the directory levels
		for(int i = 0; i < levels; i++) {
			String file = toMd5(deleteKey);
			String currentDir = toMd5(Integer.toString(i));
			String location = dir()+getSlash()+currentDir+getSlash()+file;
			boolean exists = new File(location).exists();
			if(exists) {
				new File(location).delete();
				status = "ok";
				return true;
			}
		}
		status = "error";
		return false;
	}
	
	/**
	 * Get the OS style directory slash
	 * 
	 * @return OS directory slash type
	 */
	static String getSlash() {
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
	 * 
	 * @return data directory path
	 */
	static String dir() {
		return System.getProperty("user.dir")+getSlash()+"data";
	}
	
	/**
	 * Get the key data from cache or storage
	 * 
	 * @param findKey - key to find data from
	 * 
	 * @return - data from key
	 */
	public Boolean find(String findKey) {
		try {
			//Make sure the key is an md5 hash
			if (key.length() == 32) {
				// loop through the storage levels
				for(int i = 0; i < levels; i++) {
					String file = toMd5(findKey);
					String currentDir = toMd5(Integer.toString(i));
					String location = dir()+getSlash()+currentDir+getSlash()+file;
					boolean exists = new File(location).exists();
					if(exists) {
						Path path = Paths.get(location);
						List<String> list = Files.readAllLines(path);
						this.data = "";
						list.forEach( line -> this.data = this.data+line );
						status = "ok";
						return true;
					}
				}
			} else {
				System.out.println("Not md5.");
			}
			status = "error";
		} catch (IOException e) {
			System.out.println("IO error");
		}
		status = "error";
		return false;
	}
	
	/**
	 * Store key and data in the data folder
	 * 
	 * @param data - data to store
	 * @param newKey - key to store with
	 * 
	 * @return - boolean status
	 */
	public boolean store(String data, String newKey, Boolean overwrite) {
		try {
			if ((data.length()) > 0) {
				String selectedDataDir = null;
				int currentDataDirSize = Integer.MAX_VALUE;
				boolean found = (this.find(newKey) == true);
				if(found && overwrite == false) {
					System.out.println("Overwrite disabled.");
				} else {
					for(int i = 0; i < levels; i++) {
						String pathName = toMd5( Integer.toString(i) );
						String currentPath = dir()+getSlash()+pathName;
					
						File location =  new File(currentPath);
						if(location.exists()) {
							int size = location.list().length;
							if(size <= currentDataDirSize) {
								selectedDataDir = currentPath; 
								currentDataDirSize = size;
							}
						}else {
							location.mkdir();
							
							selectedDataDir = currentPath; 
							currentDataDirSize = 0;
						}
					}
					String file = toMd5(newKey);
					Path path = Paths.get(selectedDataDir+getSlash()+file);
					Files.write(path, data.getBytes(), StandardOpenOption.CREATE);
					status = "ok";
					return true;
				}
			} else {
				status = "error";
				return true;
			}
		} catch(IOException e) {
			System.out.println("error");
		}
		status = "error";
		return false;
	}
}
