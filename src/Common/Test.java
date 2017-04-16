package Common;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.print.DocFlavor.STRING;
import javax.sound.sampled.Line;

import org.apache.commons.cli.Parser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@SuppressWarnings("deprecation")
public class Test{
	
	public static void main(String[] args) throws Exception{
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption( "a", "all", true, "do not hide entries starting with ." );
		options.addOption( "A", "almost-all", false, "do not list implied . and .." );
		options.addOption( "b", "escape", false, "print octal escapes for nongraphic "+"characters" );
		OptionBuilder.withLongOpt( "block-size" );
		OptionBuilder.withDescription( "use SIZE-byte blocks" );
		OptionBuilder.hasArg();
		OptionBuilder.withArgName("SIZE");
		options.addOption( OptionBuilder.create() );
		options.addOption( "B", "ignore-backups", false, "do not list implied entried "+"ending with ~");
		options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last" + "modification of file status information) with"+ "-l:show ctime and sort by name otherwise: sort" + "by ctime" );
		options.addOption( "C", false, "list entries by columns" );

		args = new String[]{ "--block-size=10", "-a=hah a" };

		try {
		    CommandLine line = parser.parse( options, args );
		    
		    if( line.hasOption( "block-size" ) ) {
		        System.out.println( line.getOptionValue( "block-size" ) );
		    }
		    if( !line.hasOption( "ignore-backups" ) ) {
		    	HelpFormatter hf = new HelpFormatter();
				hf.printHelp("Options", options);
		    }
		    System.out.println(line.getOptionValue("all"));
		    System.out.println(line.getOptionValue("b"));
		    System.out.println(line.hasOption("b"));
		    
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		
		System.out.println(InetAddress.getLocalHost().getHostName());
		
		String s = "publish";
		switch (s) {
		case "publish":
			System.out.println("publish");
			break;
		case "fetch":
			System.out.println("fetch");
			break;
		default:
			break;
		}
		
		String string = null;
		String[] strings = s.split(",");
		System.out.println();
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		
		int[] a = new int[]{1,2,3,4,5};
		
		JSONArray jArray = new JSONArray();
		JSONParser jsonParser = new JSONParser();
		
		jArray.add(a);
		JSONObject newCommand = new JSONObject();	
		JSONObject newjson = new JSONObject();
		newjson.put("a", "a");
		newCommand.put("command_name","Math");
		newCommand.put("method_name","add");
//		newCommand.put("tags", jArray);
//		newCommand.put("a", newjson);
		newCommand.put("string", "[\"a\",\"b\"]");
		Object object = jsonParser.parse(newCommand.toJSONString());	
		JSONObject jsonObject = (JSONObject) object;
		JSONArray jsonArray = new JSONArray();
		jsonArray.add("a");
		jsonArray.add("b");
		String[] sa = new String[]{"a","b"};
		newCommand.put("sa", jsonArray);
		System.out.println(newCommand.toJSONString());
//		jsonObject = (JSONObject) newCommand.get("sa");
		System.out.println(jsonArray.toJSONString());
//		System.out.println(sa[0]);
        System.out.println(jsonObject.get("string") == null);
//		System.out.println(array);
        System.out.println(jsonObject.get("command_name"));
		System.out.println(newCommand.toString());
		System.out.println(map.getClass());
		System.out.println(a.getClass());
//		System.out.println(string.getClass());
		System.out.println(strings.getClass());
		map.put("a", 2);
		System.out.println(map.get("a"));
		System.out.println("\0");
		String pattern = "[a-zA-Z0-9]+://.*";
	    boolean isMatch = Pattern.matches(".*\\S+.*", "   h     ");
	    System.out.println("ismatch"+isMatch);
	    try {
			throw new Exception("error message");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	    System.out.println(jsonObject.get("string"));
	    String string2 = null;

	    string = "[{\"server\":[\"a\",\"b\",\"c\"]}";
//	    JSONObject array = (JSONObject) jsonParser.parse(string);
//	    System.out.println(array.toJSONString());
//	    System.out.println(((String[])array.get("server"))[1]);
	    Object object2 = "{\"serverList\":\"[{\"hostname\":\"127.0.0.1\",\"port\":\"3001\"}]\",\"command\":\"EXCHANGE\"}";
	    System.out.println(jsonParser.parse((String) object2));
	    System.out.println((String)object2);
	    System.out.println(InetAddress.getLocalHost().getHostAddress());
	    string = "[     ]";
	    System.out.println("|"+string.substring(1, string.length()-1).split(",")+"|");
	    System.out.println(sa);
	    System.out.println(Pattern.matches("^\\s*$",string.substring(1, string.length()-1)));
	    System.out.println(string.substring(0,0));
	}
}
