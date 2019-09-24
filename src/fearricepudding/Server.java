package fearricepudding;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.Gson;

import fearricepudding.Storage;

public class Server implements Runnable {
	
	
	private static final int PORT = 8080;
	private static final boolean debug = false;
	private static boolean ALLOW_OVERWRITE = false;
	static String version = null;

	private Socket connect;
	
	/**
	 * Generate socket
	 * s
	 * @param c
	 */
	public Server(Socket c) {
		connect = c;
	}
	
	/**
	 * Starting point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if (args[0] != null) {
				ALLOW_OVERWRITE = true;
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			ALLOW_OVERWRITE = false;
		}
		
		if(debug) {
			System.out.println("=== Starting server ===");
			System.out.println("Version: "+getVersion());
			System.out.println("Overwrite: "+ALLOW_OVERWRITE);
			System.out.println("Running Port: "+PORT);
			System.out.println("Store dir: "+Storage.dir());
		} else {
			System.out.println("Server ready...");
		}
		
		
		try {
			@SuppressWarnings("resource")
			ServerSocket serverConnect = new ServerSocket(PORT);
			
			while(true) {
				Server myserver = new Server(serverConnect.accept());
				
			
				
				Thread thread = new Thread(myserver);
				thread.start();
			}
		} catch (IOException e) {
			System.out.println("Server error - "+ e.getMessage());
		}
		
	}
	
	/**
	 * Get version string from version file
	 * 
	 * @return version string
	 */
	public static String getVersion() {
		try {
			String location = System.getProperty("user.dir");
			Path path = Paths.get(location+Storage.getSlash()+"version");
			
			List<String> list = Files.readAllLines(path);
			
			list.forEach(line -> version = line );
		}catch(IOException e) {
			System.out.println("Version error: "+e.getMessage());
		}
		
		return version;
	}


	@Override
	public void run() {
		
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String key = null;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
	
			
			String line;
			int postDataI = -1;
			while ((line = in.readLine()) != null && (line.length() != 0)) {
				if (line.indexOf("Content-Length:") > -1) {
				postDataI =  Integer.valueOf(
					line.substring(line.indexOf("Content-Length:") + 16,line.length())).intValue();
				}
			}
			String postData = "";
			if (postDataI > 0) {
			    char[] charArray = new char[postDataI];
			    in.read(charArray, 0, postDataI);
			    postData = new String(charArray);
			}
		
				  
			
			key = parse.nextToken().toLowerCase().substring(1);
			
			
			Storage data = new Storage(key);
			Gson gson = new Gson();
			
			String contentMimeType = "application/json";
			out.println("HTTP/1.1 200 OK");
			out.println("Server: tinykeystore");
			out.println("Date: " + new Date());
			out.println("Access-Control-Allow-Origin: *");
			out.println("Access-Control-Allow-Headers: x-http-method-override");
			out.println("Access-Control-Allow-Methods: GET, DELETE, PUT");
			out.println("Content-type: " + contentMimeType);
			out.println(); // blank line between headers and content
			out.flush();
      
	
			if(debug) {
				System.out.println("*** new request ***");
				System.out.println("Method: "+method);
				System.out.println("Request key: "+key);
				System.out.println("post: "+postData);
			}
			
			if(method.equals("GET")) {
				
				String response = null;
	
				// *** GET METHOD *** //
				
				data.find(key);
				response = gson.toJson(data);
				
				dataOut.write(response.getBytes(), 0, response.getBytes().length);
				dataOut.flush();
			
			}else if(method.equals("PUT")) {
				
				// *** PUT METHOD *** //
				
				String response = null;
			
				boolean succ = data.store(postData, key, ALLOW_OVERWRITE);
				if(succ) {
					data.find(key);
					response = gson.toJson(data);
				} else {
					 response = "{data:\"Key exists, overwrite disabled\", status:\"error\"}";
				}
	
			
				dataOut.write(response.getBytes(), 0, response.getBytes().length);
				dataOut.flush();
				
			}else if(method.contentEquals("DELETE")) {
				
				// *** DELETE METHOD *** //
				
				String response = null;
				
				boolean succ = data.delete(key);
				if(succ) {
					response = "{status:\"ok\"}";
				}else {
					response = "{status:\"error\"}";
				}
				
				dataOut.write(response.getBytes(), 0, response.getBytes().length);
				dataOut.flush();
				
				
			}
			
		} catch (IOException e) {
			System.out.println("Genral run error");
			e.printStackTrace();
		} finally {
			
			try {
			
				in.close();
				out.close();
				dataOut.close();
				connect.close();
			
			} catch (IOException e) {
				System.out.println("Error closing steam: "+ e.getMessage());
			}
			
		}
	}
	
}

