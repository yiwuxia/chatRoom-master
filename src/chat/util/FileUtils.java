package chat.util;

import java.io.FileInputStream;
import java.util.Properties;

public class FileUtils {
	
	
	public static Properties properties = new Properties();
	public  static String sep;

	static {
		FileInputStream fis;
		try {
			fis = new FileInputStream("config.properties");
			properties.load(fis);
			sep=properties.getProperty("seperationStr");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String string) {
		return properties.getProperty(string);
	}
	
	/**
	 * 
	 * @param type 0 下线，1上线。2正常消息
	 * @param to 发给谁 群发 all:私聊 userName
	 * @param info 消息体
	 * @return
	 */
	public static String mesgBuilder(int type,String to,String info) {
		StringBuffer buffer=new StringBuffer();
		buffer.append(type).append(sep);
		buffer.append(to==null?"all":to).append(sep);
		buffer.append(info);
		return buffer.toString();
	}
	
	public static String[] parseMesg(String msg) {
		String [] arr=msg.split(sep);
		return arr;
	}

}
