package Common;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Resource {
	
	private String name;
	
	private String description;
	
	private String[] tags;
	
	private String uri;
	
	private String channel;
	
	private String owner;
	
	private String ezserver;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEzserver() {
		return ezserver;
	}

	public void setEzserver(String ezserver) {
		this.ezserver = ezserver;
	}
	
	public String toJSONString(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", name==null?"":name);
		jsonObject.put("tags", parseArraytoJSONString(tags));
		jsonObject.put("description", description==null?"":description);
		jsonObject.put("uri", uri==null?"":uri);
		jsonObject.put("channel", channel==null?"":channel);
		jsonObject.put("owner", owner==null?"":owner);
		jsonObject.put("ezserver", ezserver==null?"":ezserver);
		return jsonObject.toString();
	}

	private String parseArraytoJSONString(String[] array){
		if (array == null || array.length == 0) {
			return "[]";
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] strings = {"a","b","c"};
		Map<String, Integer> map = new HashMap<>();
		
		System.out.println(strings.toString());
	}

}
