package Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Common.Resource;

import org.apache.commons.cli.*;

public class Server{
	
	private ServerSocket serverSocket;
	
	private String secret = "default";
	
	private int port = 3000;
	
	private boolean debug = false;
	
	private int exchangeInterval = 600;
	
	private String hostName = "127.0.0.1";
	
	private int connectionintervalLimit = 1;
	
	private List<String> serverList = new ArrayList<String>();

	private Map<String,List<Resource>> resourceList = new HashMap<String,List<Resource>>();
	
	public Server() throws Exception{
		
	}
	
	private void setup(String[] args) throws Exception{
		Options options = new Options();
		options.addOption( "a", "advertisedhostname", true, "advertised hostname" );
		options.addOption( "c", "connectionintervallimit", true, "connection interval limit in seconds" );
		options.addOption( "e", "exchangeinterval", true, "exchange interval in seconds" );
		options.addOption( "p", "port, an integer", true, "server port, an integer");
		options.addOption( "s", "secret", true, "secret");
		options.addOption( "d", "debug", true, "print debug information");
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse( options, args );
		String s = line.getOptionValue("a");
		if (s != null) {
			hostName = s;
		}
		else {
			this.hostName = InetAddress.getLocalHost().getHostName();
		}
		s = line.getOptionValue("p");
		if (s != null) {
			try {
				int port = Integer.parseInt(s);
				if (!(port >= 1024 && port <= 65535)) {
					throw new Exception();
				}
				this.port = port;
			} catch (Exception e) {
				throw new Exception("Port number should be an integer, between 1024 and 65535");
			} 
		}
		s = line.getOptionValue("e");
		if (s != null) {
			try {
				int exchangeinterval = Integer.parseInt(s);
				this.exchangeInterval = exchangeinterval;
			} catch (Exception e) {
				throw new Exception("exchangeinterval should be an integer");
			}
		}
		s = line.getOptionValue("c");
		if (s != null) {
			try {
				int connectionintervallimit = Integer.parseInt(s);
				this.connectionintervalLimit = connectionintervallimit;
			} catch (Exception e) {
				throw new Exception("connectionintervallimit should be an integer");
			}
		}
		s = line.getOptionValue("s");
		if (s != null) {
			secret = s;
		}
		if (line.hasOption("d")) {
			this.debug = true;
		}
	}
	 
	 
	
	
	class RequestHandler extends Thread{
		
		private DataInputStream inputStream;
		
		private DataOutputStream outputStream;
		
		public RequestHandler(DataInputStream inputStream,DataOutputStream outputStream){
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}
		
		private synchronized void handlePublishRequest(JSONObject request) throws Exception{
			JSONObject requestResource = (JSONObject) request.get("resource");
			if (requestResource == null) {
				throw new Exception("publish-missing resource");
			}
			String uri = (String) requestResource.get("uri");
			String name = (String) requestResource.get("name");
			String tags = (String) requestResource.get("tags");
            String description= (String) requestResource.get("description");
			String channel = (String) requestResource.get("channel");
			String owner = (String) requestResource.get("owner");
			String ezserver = (String) requestResource.get("ezserver");
			
			uri = uri==null?"":uri;
			name = name==null?"":name;
			tags = tags==null?"":tags;
			description = description==null?"":description;
			channel = channel==null?"":channel;
			owner = owner==null?"":owner;
			ezserver = ezserver==null?"":ezserver; 

			uri = uri.trim();
			name = name.trim();
			tags = tags.trim();
			description= description.trim();
			channel = channel.trim();
			owner = owner.trim();
			ezserver = ezserver.trim();
			
			if (uri.contains("\0")) {
				throw new Exception("publish-invalid resource");
			}
			if (name.contains("\0")) {
				throw new Exception("publish-invalid resource");
			}
			if (tags.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			if (description.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			if (channel.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			if (owner.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			if (ezserver.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			
			String uriPattern = "[a-zA-Z0-9]+://.*";
		    boolean isMatch = Pattern.matches(uriPattern, uri);
		    if ((!isMatch) || uri.startsWith("file://")) {
				throw new Exception("publish-cannot publish resource");
			}
			if (tags != "") {
				if (!tags.startsWith("[") || !tags.trim().endsWith("]")) {
					throw new Exception("publish-cannot publish resource");
				}
//				if (Pattern.matches("^\\s*$", tags.substring(1, tags.length()-1))) {
//					throw new Exception("publish-cannot publish resource");
//				}
			}
			if (owner == "*") {
				throw new Exception("publish-cannot publish resource");
			}
			if (resourceList.containsKey(uri)) {
				for(int i=0;i<resourceList.get(uri).size();i++){
					if ((name == resourceList.get(uri).get(i).getName()) && (channel == resourceList.get(uri).get(i).getChannel()) && (owner != resourceList.get(uri).get(i).getOwner())) {
						throw new Exception("publish-cannot publish resource");
					}
				}
			}
			else{
				resourceList.put(uri, new ArrayList<Resource>(1));
			}
			
			Resource newResource = new Resource();
			newResource.setChannel(channel);
			newResource.setDescription(description);
			newResource.setUri(uri);
			newResource.setName(name);
			newResource.setOwner(owner);
			newResource.setEzserver(ezserver);
			if (tags == "" || Pattern.matches("^\\s*$", tags.substring(1, tags.length()-1))){
				newResource.setTags(new String[]{});
			}
			else {
				newResource.setTags(tags.substring(1, tags.length()-1).split(","));
			}
			
			resourceList.get(uri).add(newResource);
			outputStream.writeUTF("{\"response\":\"success\"}");
		    return;
		}
		
		private synchronized void handleRemoveRequest(JSONObject request) throws Exception{
			JSONObject requestResource = (JSONObject) request.get("resource");
			if (requestResource == null) {
				throw new Exception("remove-missing resource");
			}
			
			String uri = (String) requestResource.get("uri");
			String channel = (String) requestResource.get("channel");
			String owner = (String) requestResource.get("owner");
				
			uri = uri==null?"":uri;
			channel = channel==null?"":channel;
			owner = owner==null?"":owner;
		
			uri = uri.trim();
			channel = channel.trim();
			owner = owner.trim();
			
			if (uri == "") {
				throw new Exception("remove-missing resource");
			}
			
			if (uri.contains("\0")) {
				throw new Exception("remove-invalid resource");
			}
			
			if (channel.contains("\0")) {
				throw new Exception("remove-invalid resource");
			}
			
			if (owner.contains("\0")) {
				throw new Exception("remove-invalid resource");
			}
			
			if (!resourceList.containsKey("uri")) {
				throw new Exception("remove-cannot remove resource");
			}
			else{
				boolean flag = false;
				for(int i=0;i<resourceList.get(uri).size();i++){
					if (resourceList.get(uri).get(i).getName() == owner && resourceList.get(uri).get(i).getChannel() == channel) {
						resourceList.get(uri).remove(i);
						flag = true;
					}
				}
				if (!flag) {
					throw new Exception("remove-cannot remove resource");
				}
			}
			outputStream.writeUTF("{\"response\":\"success\"}");
			return;
		}
		
		private synchronized void handleShareRequest(JSONObject request) throws Exception{
			JSONObject requestResource = (JSONObject) request.get("resource");
			if (requestResource == null) {
				throw new Exception("share-missing resource and/or secret");
			}
			String secretString = (String) request.get("secret");
			if (secretString == null) {
				throw new Exception("share-missing resource and/or secret");
			}
			secretString = secretString.trim();
			if (!secretString.equals(secret)) {
				throw new Exception("incorrect secret");
			}
			String uri = (String) requestResource.get("uri");
			String name = (String) requestResource.get("name");
			String tags = (String) requestResource.get("tags");
            String description= (String) requestResource.get("description");
			String channel = (String) requestResource.get("channel");
			String owner = (String) requestResource.get("owner");
			String ezserver = (String) requestResource.get("ezserver");
			
			uri = uri==null?"":uri;
			name = name==null?"":name;
			tags = tags==null?"":tags;
			description = description==null?"":description;
			channel = channel==null?"":channel;
			owner = owner==null?"":owner;
			ezserver = ezserver==null?"":ezserver; 

			uri = uri.trim();
			name = name.trim();
			tags = tags.trim();
			description= description.trim();
			channel = channel.trim();
			owner = owner.trim();
			ezserver = ezserver.trim();
			
			if (uri.contains("\0")) {
				throw new Exception("share-invalid resource");
			}
		
			if (name.contains("\0")) {
				throw new Exception("share-invalid resource");
			}	
			
			if (tags.contains("\0")) {
				throw new Exception("share-invliad resource");
			}
			
			if (description.contains("\0")) {
				throw new Exception("share-invliad resource");
			}
			
			if (channel.contains("\0")) {
				throw new Exception("share-invliad resource");
			}
		
			if (owner.contains("\0")) {
				throw new Exception("share-invliad resource");
			}
			
			if (ezserver.contains("\0")) {
				throw new Exception("share-invliad resource");
			}
			
			if (tags != "") {
		    	if (!tags.startsWith("[") || !tags.endsWith("]")) {
					throw new Exception("share-invalid resource");
				}
//		    	if (Pattern.matches("^\\s*$", tags.substring(1, tags.length()-1))) {
//					throw new Exception("share-cannot publish resource");
//				}
		    }
			 
			String uriPattern = "[a-zA-Z0-9]+://.*";
		    boolean isMatch = Pattern.matches(uriPattern, (String)requestResource.get("uri"));
		    if ((!isMatch) || (!uri.startsWith("file://"))) {
				throw new Exception("share-cannot share resource");
			}
		    
		    File file = new File(uri.substring(7));
		    if (!file.exists()) {
		    	throw new Exception("share-cannot share resource");
			}
		    
		    if (resourceList.containsKey(uri)) {
		    	for (int i = 0; i < resourceList.get(uri).size(); i++) {
		    		if ((channel == resourceList.get(uri).get(i).getChannel()) && (owner != resourceList.get(uri).get(i).getOwner())) {
						throw new Exception("share-cannot share resource");
					}
				}	
			}
		    else{
				resourceList.put(uri, new ArrayList<Resource>(1));
			}
			
			Resource newResource = new Resource();
			newResource.setChannel(channel);
			newResource.setDescription(description);
			newResource.setName(name);
			newResource.setOwner(owner);
			newResource.setEzserver(ezserver);
			newResource.setUri(uri);
			if (tags == "" || Pattern.matches("^\\s*$", tags.substring(1, tags.length()-1))){
				newResource.setTags(new String[]{});
			}
			else {
				newResource.setTags(tags.substring(1, tags.length()-1).split(","));
			}
			resourceList.get(uri).add(newResource);
			outputStream.writeUTF("{\"response\":\"success\"}");
		    return;
		}
		
		private synchronized void handleQueryRequest(JSONObject request) throws Exception{
			JSONObject requestResource = (JSONObject) request.get("resourceTemplate");
			if (requestResource== null) {
				throw new Exception("query-missing resourceTemplate");
			}
			
			String uri = (String) requestResource.get("uri");
			String name = (String) requestResource.get("name");
			String tags = (String) requestResource.get("tags");
            String description= (String) requestResource.get("description");
			String channel = (String) requestResource.get("channel");
			String owner = (String) requestResource.get("owner");
			String ezserver = (String) requestResource.get("ezserver");
			
			uri = uri==null?"":uri;
			name = name==null?"":name;
			tags = tags==null?"":tags;
			description = description==null?"":description;
			channel = channel==null?"":channel;
			owner = owner==null?"":owner;
			ezserver = ezserver==null?"":ezserver; 

			uri = uri.trim();
			name = name.trim();
			tags = tags.trim();
			description= description.trim();
			channel = channel.trim();
			owner = owner.trim();
			ezserver = ezserver.trim();
			
			if (uri.contains("\0")) {
				throw new Exception("query-invalid resource");
			}
		
			if (name.contains("\0")) {
				throw new Exception("query-invalid resource");
			}
			
			if (tags.contains("\0")) {
				throw new Exception("query-invliad resource");
			}
			
			if (description.contains("\0")) {
				throw new Exception("query-invliad resource");
			}
			
			if (channel.contains("\0")) {
				throw new Exception("query-invliad resource");
			}
			
			if (owner.contains("\0")) {
				throw new Exception("query-invliad resource");
			}
			
			if (ezserver.contains("\0")) {
				throw new Exception("publish-invliad resource");
			}
			
			if (tags != "") {
				if (!tags.startsWith("[") || !tags.endsWith("]")) {
					throw new Exception("query-invalid resourceTemplate");
				}
			}
			
			List<Resource> candidates = new ArrayList<Resource>();
			Iterator<String> iterator = resourceList.keySet().iterator();
			
			if (uri != "") {
				for (int i = 0; i < resourceList.get(uri).size(); i++) {
					candidates.add(resourceList.get(uri).get(i));
				}
			}
			else{
				while (iterator.hasNext()) {
					List<Resource> next = resourceList.get(iterator.next());
					for (int i = 0; i < next.size(); i++) {
						candidates.add(next.get(i));
					}
				}

			}
			if (owner != "") {
				for (int i = 0; i < candidates.size(); i++) {
					if (owner != candidates.get(i).getOwner()) {
						candidates.remove(i);
					}
				}
			}
			else {
				for (int i = 0; i < candidates.size(); i++) {
					candidates.get(i).setOwner("*");
				}
			}
			if (channel != "") {
				for (int i = 0; i < candidates.size(); i++) {
					if (owner != candidates.get(i).getChannel()) {
						candidates.remove(i);
					}
				}
			}
			
			if (tags != ""){
				if (Pattern.matches("^\\s*$", tags.substring(1, tags.length()-1))) {
					throw new Exception("share-invalid resourceTemplate");
				}
				String[] tagsArray = tags.split(",");
				for (int i = 0; i < candidates.size(); i++) {
					boolean flag = false;
					for (int j = 0; j < candidates.get(i).getTags().length; j++) {
						for(int k=0;k<tags.length();k++){
							if (tagsArray[k] == candidates.get(i).getTags()[j]) {
								flag = true;
							}
						}	
					}
					if (!flag) {
						candidates.remove(i);
					}
				}
			}
			if (name != "") {
				for (int i = 0; i < candidates.size(); i++) {
					if (!candidates.get(i).getName().contains(name)) {
						candidates.remove(i);
					}
				}
			}
			if (description != "") {
				for (int i = 0; i < candidates.size(); i++) {
					if (!candidates.get(i).getDescription().contains(description)) {
						candidates.remove(i);
					}
				}
			}
			int resultSize = candidates.size();
			outputStream.writeUTF("{\"response\":\"success\"}");
			for (int i = 0; i < candidates.size(); i++) {
				outputStream.writeUTF(candidates.get(i).toJSONString());
//				outputStream.writeUTF("\n");
			}
			outputStream.writeUTF("{\"resultSize\":"+resultSize+"}");
			return;
		}

		private synchronized void handleFetchRequest(JSONObject request) throws Exception {
			JSONObject requestResource = (JSONObject) request.get("resourceTemplate");
			if (requestResource== null) {
				throw new Exception("query-missing resourceTemplate");
			}
			
			String uri = (String) requestResource.get("uri");
			String channel = (String) requestResource.get("channel");
			
			
			uri = uri==null?"":uri;
			channel = channel==null?"":channel; 

			uri = uri.trim();
			channel = channel.trim();
		
			String uriPattern = "[a-zA-Z0-9]+://.*";
		    boolean isMatch = Pattern.matches(uriPattern, uri);
			if (!isMatch || !uri.startsWith("file://")) {
				throw new Exception("fetch-invalid resourceTemplate");
			}
			Resource resource = null;
			boolean flag = false;
			for (int i = 0; i < resourceList.get(uri).size(); i++) {
				if (channel.equals(resourceList.get(uri).get(i).getChannel())) {
					flag = true;
					resource = resourceList.get(uri).get(i);
				}
			}
			if (!flag) {
					throw new Exception("fetch-invalid resourceTemplate");
			}
			
			File file = new File(uri.substring(7));
			if (!file.exists()) {
				throw new Exception("fetch-invalid resourceTemplate");
			}
			
			long resourceSize = file.length();
			DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(uri.substring(7))));
			int bufferSize = 1024*1024;
			byte[] buf = new byte[bufferSize];
			outputStream.writeUTF("{\"response\":\"success\"}\n");
			outputStream.writeUTF(resource.toJSONString().replaceAll("}", ",\"resourceSize\":"+resourceSize+"}\n"));
			while (true) {  
                int read = 0;  
                if (dataInputStream != null) {  
                    read = dataInputStream.read(buf);   
                }  

                if (read == -1) {  
                    break;  
                }  
                outputStream.write(buf, 0, read);  
            }  
			outputStream.writeUTF("{\"resultSize\":1");
            outputStream.flush();
            dataInputStream.close();
		}
		
		private synchronized void handleExchangeRequest(JSONObject request) throws Exception{
			JSONArray serversList = (JSONArray) request.get("serverList");
			if (serversList == null) {
				throw new Exception("exchange-missing or invalid server list");
			}
			String serverString = serversList.toJSONString().trim();
		    if (!serverString.startsWith("[") || !serverString.endsWith("]")) {
					throw new Exception("exchange-missing or invalid server list");
			}
		    String pattern = ".*\\S+.*";
			boolean isMatch = Pattern.matches(pattern, serverString.substring(1, serverString.length()-1));
			if (!isMatch) {
				throw new Exception("exchange-missing or invalid server list");
			}
			serverString.replace("[", "");
			serverString.replace("]", "");
			String[] serverArray = serverString.split(",");
			
			JSONParser jsonParser = new JSONParser();
			JSONObject exchangeCommand = new JSONObject();
			exchangeCommand.put("command", "EXCHANGE");
			exchangeCommand.put("serverList", parseServerListToJSON());
			
			try{
				for (int i = 0; i < serverArray.length; i++) {
					JSONObject jsonObject = (JSONObject) jsonParser.parse(serverArray[i]);
					String hostName = ((String) jsonObject.get("hostname")).trim();
					Integer port = Integer.valueOf((String)jsonObject.get("port"));
					boolean flag = false;
					for (int j = 0; j < serverList.size(); j++) {
						if (serverList.get(i) == hostName+":"+port) {
							flag = true;
							break;
						}
						if (InetAddress.getLocalHost().getHostName() == hostName || InetAddress.getLocalHost().getHostAddress() == hostName) {
							flag = true;
							break;
						}
					}
					if (flag) {
						continue;
					}
					serversList.add(hostName+":"+port);
//					try(Socket socket = new Socket(hostName,port)){
//						DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
//						dataOutputStream.writeUTF(exchangeCommand.toJSONString());
//						dataOutputStream.flush();
//						int count = 0;
//						while(true){
//							if(dataInputStream.available()>0){
//								String message = dataInputStream.readUTF();
//								JSONObject res = (JSONObject) jsonParser.parse(message);
//								if (res.get("response") == "success") {
//									serverList.add(hostName+":"+port);
//									break;
//								}
//							}
//							count++;
//							Thread.sleep(500);
//							if (count == 3) {
//								throw new Exception("exchange-missing resourceTemplate");
//							}
//						}
//				}
//				catch (Exception e) {
//					throw new Exception("exchange-missing resourceTemplate");
//				}
				}	
			}
			catch(Exception exception){
				throw new Exception("exchange-missing resourceTemplate");
			}
			outputStream.writeUTF("{\"response\":\"success\"}");
		}
		
		private synchronized String parseServerListToJSON() {
			String result = "[";
			for(int i=0;i<serverList.size();i++){
				String server = serverList.get(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("hostname", server.substring(0, server.indexOf(":")));
				jsonObject.put("port", server.substring(server.indexOf(":")+1));
				result = result + jsonObject.toJSONString() + ",";
			}
			result = result.substring(0, result.length()-1);
			return result+"]";
		}
		
		public void run() {
			try{
				String message;
				while(true){
					if(inputStream.available()>0){
						message = inputStream.readUTF();
						System.out.println("Server:"+message);	
						break;
					}
				}
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject)jsonParser.parse(message);
				Object command = jsonObject.get("command");
				if (command == null) {
					throw new Exception("missing or incorrect type for command");
				}
				String requestCommand = (String) command;
				if (requestCommand.equals("PUBLISH")) {
					handlePublishRequest(jsonObject);
					return;
				}
				if (requestCommand.equals("REMOVE")) {
					handleRemoveRequest(jsonObject);
					return;
				}
				if (requestCommand.equals("SHARE")) {
					handleShareRequest(jsonObject);
					return;
				}
				if (requestCommand.equals("QUERY")) {
					handleQueryRequest(jsonObject);
					return;
				}
				if (requestCommand.equals("FETCH")) {
					handleFetchRequest(jsonObject);
					return;
				}
				if (requestCommand.equals("EXCHANGE")) {
					handleExchangeRequest(jsonObject);
					return;
				}
				else {
					throw new Exception("invalid command");
				}
			}
			catch(Exception e){
				try {
					outputStream.writeUTF("{\"response\":\"error\",\"errorMessage\":\""+e.getMessage()+"\"}");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	 
	 class CheckHeartbeat extends TimerTask{
		@Override
		public void run() {
			JSONObject exchangeCommand = new JSONObject();
			exchangeCommand.put("command", "EXCHANGE");
			exchangeCommand.put("serverList", parseServerListToJSON());
			for(int i = 0;i < serverList.size();i++){
				String hostName = serverList.get(i).split(":")[0];
				int port = Integer.valueOf(serverList.get(i).split(":")[1]);
				JSONParser jsonParser = new JSONParser();
				try(Socket socket = new Socket(hostName,port)){
					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF(exchangeCommand.toJSONString());
					dataOutputStream.flush();
					int count = 0;
					while(true){
						if(dataInputStream.available()>0){
							String message = dataInputStream.readUTF();
							JSONObject res = (JSONObject) jsonParser.parse(message);
							if (res.get("response") == "success") {
								break;
							}
						}
						count++;
						Thread.sleep(500);
						if (count == 3) {
							serverList.remove(i);
							break;
						}
					}
				}
				catch (Exception e) {
					
				}
			}
		}
		
		private synchronized String parseServerListToJSON() {
			String result = "[";
			for(int i=0;i<serverList.size();i++){
				String server = serverList.get(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("hostname", server.substring(0, server.indexOf(":")));
				jsonObject.put("port", server.substring(server.indexOf(":")+1));
				result = result + jsonObject.toJSONString() + ",";
			}
			result = result.substring(0, result.length()-1);
			return result+"]";
		} 
	 }
	 
	 public static void main(String[] args) throws Exception {
			Server server = new Server();
			server.setup(args);
			CheckHeartbeat checkHeartbeat = server.new CheckHeartbeat();
			Timer timer = new Timer();
			timer.schedule(checkHeartbeat, server.exchangeInterval*1000);
			System.out.println(System.getProperty("user.dir"));
			server.serverSocket = new ServerSocket(server.port);
			System.out.println("Waiting for client connection");
			while(true){
				Socket client = server.serverSocket.accept();
				System.out.println("Client connected");
				DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream()); 
				Server.RequestHandler requestHandler = server.new RequestHandler(dataInputStream,dataOutputStream);
				requestHandler.start();
			}
		}
}
