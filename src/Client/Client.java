package Client;

import java.awt.TexturePaint;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.cert.TrustAnchor;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Common.Request;

public class Client {

	private String ip = "127.0.0.1";
	
	private int port = 3000;
	
	private List<String> serverList = new ArrayList<String>();
	
	private Request request =  new Request();;
	
	private boolean debug = false;
	
	private void setup(String[] args) throws Exception{
		Options options = new Options();
		options.addOption("channel","channel",true,"channel" );
		options.addOption("debug","debug",false,"print debug information" );
		options.addOption("description","description",true,"resource description" );
		options.addOption("exchange","exchange",false,"exchange server list with server");
		options.addOption("fetch","fetch",false,"fetch resources from server");
		options.addOption("host","host",true,"server host, a domain name or a IP address");
		options.addOption("name","name",true,"resource name");
		options.addOption("owner","owner",true,"owner");
		options.addOption("port","port",true,"server port, an integer");
		options.addOption("publish","publish",false,"publish resource on server");
		options.addOption("query","query",false,"query for resources from server");
		options.addOption("remove","remoce",false,"remove resources from server");
		options.addOption("secret","secret",true,"secret");
		options.addOption("servers","servers",true,"server list,host1:port1,host2:port2,...");
		options.addOption("share","share",false,"share resource on server");
		options.addOption("tags","tags",true,"resource tags,tag1,tag2,tag3,...");
		options.addOption("uri","uri",true,"resource URI");
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse( options, args );
		
		int n = 0;
		String command = null;
		String[] commandList = {"fetch","publish","query","remove","share","exchange"};
		for(int i=0;i<6;i++){
			if (line.hasOption(commandList[i])) {
				command = commandList[i];
				n++;
				if (n == 2) {
					throw new Exception("Please use only one command each time,"+commandList.toString());
				}
			}
		}
		
		if(command == null){
			throw new Exception("Please input a command,"+commandList.toString());
		}
		
		if(line.hasOption("debug")) {
			this.debug = true;
		}
		
		this.request.setRequestType(command);
		this.request.setServers(line.getOptionValue("servers"));
		this.request.getResource().setUri(line.getOptionValue("uri"));
		this.request.getResource().setTags(parseTags(line.getOptionValue("tag")));
		this.request.setSecret(line.getOptionValue("secret"));
		this.request.getResource().setName(line.getOptionValue("name"));
		this.request.getResource().setChannel(line.getOptionValue("channel"));
		this.request.getResource().setDescription(line.getOptionValue("description"));
		this.request.getResource().setOwner(line.getOptionValue("owner"));
	}
	
	private JSONArray parseServerstoJSONArray(String servers) {
		JSONArray jsonArray = new JSONArray();
		String[] serversArray = servers.split(",");
		for (int i = 0; i < serversArray.length; i++) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("hostname", serversArray[i].split(":")[0].trim());
			jsonObject.put("port", serversArray[i].split(":")[1].trim());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}
	
	@SuppressWarnings("unused")
	private String parseUri(String command, String uri) throws Exception{
		switch (command) {
		case "publish":
			if (uri.startsWith("file")) {
				throw new Exception("URI cannot be a file scheme");
			}
			else if (uri == null){
				throw new Exception("missing the uri parameter");
			}
			return uri;
		case "remove":
			if (uri.startsWith("file")) {
				throw new Exception("URI cannot be a file scheme");
			}
			else if (uri == null){
				throw new Exception("missing the uri parameter");
			}
			return uri;
		case "share":
			if (!uri.startsWith("file")) {
				throw new Exception("URI must be a file scheme");
			}
			else if (uri == null){
				throw new Exception("missing the uri parameter");
			}
			return uri;
		case "fetch":
			if (!uri.startsWith("file")) {
				throw new Exception("URI must be a file scheme");
			}
			else if (uri == null){
				throw new Exception("missing the uri parameter");
			}
			return uri;
		case "exchange":
			return uri;
		case "query":
			return uri;
		}
		throw new Exception("Unknown error, plaese try again");
	}
	
	private String[] parseServers(String command,String servers){
			if (servers == null) {
				return (String[]) this.serverList.toArray();
			}
			String[] s = servers.split(",");
			return s;
	}
	
	private String[] parseTags(String tag){
		if (tag != null) {
			return tag.split(",");
		}
		return null;
	}
	
	private String parseNullPara(String string){
		if (string == null) {
			return "";
		}
		return string;
	}
	
	private String parseArraytoJSONString(String[] array){
		if (array == null) {
			return null;
		}
		String result = "";
		result = result+"[";
		for (int i = 0; i < array.length; i++) {
			result = result+"\""+array[i]+"\",";
		}
		result = result.substring(0,result.length()-1);
		result = result + "]";
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private String request(Request request) throws Exception{
		JSONObject newRequest = new JSONObject();
		JSONObject newResource = new JSONObject();
		if (request.getRequestType() == "publish") {
			newResource.put("name", request.getResource().getName());
			newResource.put("description", request.getResource().getDescription());
			newResource.put("uri", request.getResource().getUri());
			newResource.put("channel", request.getResource().getChannel());
			newResource.put("owner", request.getResource().getOwner());
			newResource.put("ezserver", null);
			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("resource", newResource);
			return newRequest.toJSONString();
		}
		if (request.getRequestType() == "remove") {
			newResource.put("name", request.getResource().getName());
			newResource.put("description", request.getResource().getDescription());
			newResource.put("uri", request.getResource().getUri());
			newResource.put("channel", request.getResource().getChannel());
			newResource.put("owner", request.getResource().getOwner());
			newResource.put("ezserver", null);
			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("resource", newResource);
			return newRequest.toJSONString();
		}
		if (request.getRequestType() == "share") {
			newResource.put("name", request.getResource().getName());
			newResource.put("description", request.getResource().getDescription());
			newResource.put("uri", request.getResource().getUri());
			newResource.put("channel", request.getResource().getChannel());
			newResource.put("owner", request.getResource().getOwner());
			newResource.put("ezserver", null);
			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("secret", request.getSecret());
			newRequest.put("resource", newResource);
			return newRequest.toJSONString();
		}
		if (request.getRequestType() == "query") {
			newResource.put("name", request.getResource().getName());
			newResource.put("description", request.getResource().getDescription());
			newResource.put("uri", request.getResource().getUri());
			newResource.put("channel", request.getResource().getChannel());
			newResource.put("owner", request.getResource().getOwner());
			newResource.put("ezserver", null);
			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("reply", request.getReply());
			newRequest.put("resourceTemplate", newResource);
			return newRequest.toJSONString();
		}
		if (request.getRequestType() == "fetch") {
			newResource.put("name", request.getResource().getName());
			newResource.put("description", request.getResource().getDescription());
			newResource.put("uri", request.getResource().getUri());
			newResource.put("channel", request.getResource().getChannel());
			newResource.put("owner", request.getResource().getOwner());
			newResource.put("ezserver", null);
			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("resourceTemplate", newResource);
			return newRequest.toJSONString();
		}
		if (request.getRequestType() == "exchange") {
			if (request.getServers() == null) {
				throw new Exception("cannot find servers value");
			}
//			newResource.put("name", request.getResource().getName());
//			newResource.put("description", request.getResource().getDescription());
//			newResource.put("uri", request.getResource().getUri());
//			newResource.put("channel", request.getResource().getChannel());
//			newResource.put("owner", request.getResource().getOwner());
//			newResource.put("ezserver", request.getResource().getEzserver());
//			newResource.put("tags", parseArraytoJSONString(request.getResource().getTags()));
			newRequest.put("command", request.getRequestType().toUpperCase());
			newRequest.put("serverList", parseServerstoJSONArray(request.getServers()));
//			newRequest.put("resourceTemplate", newResource);
			return newRequest.toJSONString();
		}
		throw new Exception("Unknown error, please try again");
	}
	
	
	public static void main(String[] args) throws Exception{
		Client client = new Client();
		client.setup(args);
		
//		JSONObject jsonObjectResource = new JSONObject();
//		jsonObjectResource.put("name", "name");
//		jsonObjectResource.put("uri", "ftp://www.123.com");
//		JSONObject jsonObjectRequest = new JSONObject();
//		jsonObjectRequest.put("command", "PUBLISH");
//		jsonObjectRequest.put("command", "REMOVE");
//		jsonObjectRequest.put("command", "SHARE");
//		jsonObjectRequest.put("command", "QUERY");
//		jsonObjectRequest.put("command", "FETCH");
//		jsonObjectRequest.put("command","EXCNANGE");
//		jsonObjectRequest.put("resource", jsonObjectResource);
		
		String request = client.request(client.request);
		System.out.println("Trying to connect server");
		try(Socket socket = new Socket(client.ip,client.port)){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeUTF(request);
			output.flush();
			
			while(true){
				if(input.available()>0){
					String message = input.readUTF();
					if (client.request.getRequestType().toLowerCase() == "fetch") {
						JSONParser jsonParser = new JSONParser();
						System.out.println("Server:"+message);
						if (((JSONObject)jsonParser.parse(message)).get("response").equals("success")) {
							String resource = input.readUTF();
							if (((JSONObject)jsonParser.parse(resource)).get("resourceSize") == null) {
								throw new Exception("fetch file error");
							}
							else{
								System.out.println("Server:"+resource);
								RandomAccessFile file = new RandomAccessFile(client.request.getResource().getUri().substring(7), "rw");
								long fileSize = (long)((JSONObject)jsonParser.parse(resource)).get("resourceSize");
								int chunkSize;
								if (fileSize > 1024*1024) {
									chunkSize = 1024*1024;
								}
								else {
									chunkSize = (int) fileSize;
								}
								byte[] buf;
								int num;
								buf = new byte[chunkSize];
								while ((num = input.read(buf)) > 0) {
									file.write(Arrays.copyOf(buf, num));
									fileSize = fileSize - chunkSize;
									if (fileSize <= 0) {
										break;
									}
									if (fileSize > 1024*1024) {
										chunkSize = 1024*1024;
									}
									else {
										chunkSize = (int) fileSize;
									}
									buf = new byte[chunkSize];
								}
								file.close();
							}
						}
						message = input.readUTF();
					}
					System.out.println("Server:"+message);	
				}
				break;
			}
		}
		catch(IOException e){
			
		}
	}

}
