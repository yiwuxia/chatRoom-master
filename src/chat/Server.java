package chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

import chat.util.FileUtils;

/**
 * 聊天室服务端
 * 
 * @author Administrator
 *
 */
public class Server {
	/*
	 * 运行在服务端的Socket 用来接收客户端的连接。
	 */
	public ServerSocket server;
	
	
	public boolean running=true;
	/*
	 * 线程池
	 */
	public ExecutorService threadPool;

	public JTextArea textArea;
	
	/*
	 * 存放所有客户端输出流的共享集合
	 */
	public List<PrintWriter> allOut;
	public Set<String> users=new HashSet<String>();
	public Map<String, PrintWriter> userOutMap = new HashMap<String, PrintWriter>();

	/**
	 * 构造方法，用来初始化服务端
	 */
	public Server() {
		users.clear();
		userOutMap.clear();
		try {
			/*
			 * 初始化共享集合
			 */
			allOut = new ArrayList<PrintWriter>();
			/*
			 * 读取配置文件 java.util.Properties
			 * 
			 */

			/*
			 * void load(InputStream in) 该方法用于读取给定的流中的数据，然后进行解析
			 * 
			 * 我们可以使用FileInputStream这个流来读取我们 定义的配置文件config.properties,所以我们可以
			 * 创建这个流，然后将该流作为参数传给load方法。 这样Properties就可以通过FileInputStream读取 我们的配置文件了。
			 */

			// 获取服务端端口号
			/*
			 * String getProperty(String key) 给定配置文件中等号左面的内容，可以获取对应的 值
			 */
			String port = FileUtils.getProperty("serverport");
			System.out.println("服务端口:" + port);
			/*
			 * 初始化ServerSocket时要传入一个参数 该参数就是服务端对外开启的服务端口 客户端就是通过该端口与服务端进行连接的
			 */
			server = new ServerSocket(Integer.parseInt(port));
			/*
			 * 获取线程的数量
			 */
			String threadCount = FileUtils.getProperty("threadcount");
			System.out.println("线程池线程数量:" + threadCount);

			/*
			 * 初始化线程池
			 */
			threadPool = Executors.newFixedThreadPool(Integer.parseInt(threadCount));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向共享集合中添加一个输出流
	 * 
	 * @param out
	 */
	public synchronized void addOut(PrintWriter out) {
		allOut.add(out);
	}

	/**
	 * 从共享集合中删除给定的输出流
	 * 
	 * @param out
	 */
	public synchronized void removeOut(PrintWriter out) {
		allOut.remove(out);
	}

	/**
	 * 遍历共享集合中的所有输出流,将给定的消息发送给所有 的客户端
	 * 
	 * @param message
	 */
	public synchronized void sendMessageToAllClient(String message) {
		for (PrintWriter out : allOut) {
			out.println(message);
		}
	}

	/**
	 * 服务端开始工作的方法
	 * @param textArea 
	 */
	public void start(JTextArea textArea) {
		this.textArea=textArea;
		try {
			/*
			 * Socket accept() 该方法是ServerSocket开始监听8088端口，这个方法 是一个阻塞方法，直到一个客户端连接为止，若客户端
			 * 连接了，会返回一个Socket,这个Socket就是用来与 该客户端进行通讯的。
			 */
			while (true  ) {
				textArea.append("等待一个客户端连接...");
				textArea.append("\n");
				System.out.println("等待一个客户端连接...");
				Socket socket = server.accept();
				if (!running) {
					break;
				}
				textArea.append("一个客户端连接了！");
				textArea.append("\n");
				/*
				 * 启动一个线程，并将刚刚连接的客户端的Socket 传给它，让它去处理与这个客户端的交互。
				 */
				ClientHandler clientHandler = new ClientHandler(socket);
//				Thread t = new Thread(clientHandler);
//				t.start();
				threadPool.execute(clientHandler);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/*
	 * public static void main(String[] args) { Server server = new Server();
	 * server.start(); }
	 */
	

	/**
	 * 该内部类是为服务端服务的。用来与一个客户端进行 交互的。
	 * 
	 * @author Administrator
	 *
	 */
	class ClientHandler implements Runnable {
		/*
		 * 该线程用来交互的客户端的Socket
		 */
		public Socket socket;
		/*
		 * 该客户端的昵称
		 */
		public String nickName;

		public ClientHandler(Socket socket) {
			this.socket = socket;
			/*
			 * 获取远程计算机的地址信息
			 */
			InetAddress address = socket.getInetAddress();
			// 获取远程计算机的地址
			String add = address.getHostAddress();
			textArea.append(add + "上线了!");
			textArea.append("\n");

		}

		public void run() {
			PrintWriter pw = null;
			try {
				/*
				 * 通过Socket获取输出流，用于将消息发送给 客户端
				 */
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				pw = new PrintWriter(osw, true);

				// 将该客户端的输出流放入共享集合中
				addOut(pw);
				/*
				 * InputStream getInputStream() Socket的该方法用来获取远程计算机发送过来的数据
				 */
				InputStream in = socket.getInputStream();

				InputStreamReader isr = new InputStreamReader(in, "UTF-8");

				BufferedReader br = new BufferedReader(isr);
				/*
				 * 首先读取一行字符串，因为客户端发送过来的第一 行字符串是该客户端的昵称，读取到后将其设置到 属性nickName上
				 */
				nickName = br.readLine();
				users.add(nickName);
				userOutMap.put(nickName, pw);
				String usersCommonSeprate=tranSetToStr(users);
				/*
				 * 广播，该用户上线了   刚上线的放第一个 把所有用户都传给他
				 */
				sendMessageToAllClient(FileUtils.mesgBuilder(1, null, nickName+","+usersCommonSeprate));

				System.out.println("[" + nickName + "]上线了");
				textArea.append("[" + nickName + "]上线了");
				textArea.append("\n");

				// 读取客户端发送过来的一行字符串

				/*
				 * 使用BufferedReader的readLine方法读取客户端发送 过来的一行字符串时，由于客户端所使用的操作系统
				 * 不同，这里在客户端与服务端断开连接后，该方法的 反应是不同的。
				 * 
				 * 若是windows系统的客户端与我们断开连接，那么 readLine方法会抛出异常。
				 * 
				 * 若是linux系统的客户端与我们断开连接，那么 readLine方法会返回null。
				 */
				String message = null;
				while ((message = br.readLine()) != null) {
					if (!running) {
						break;
					}
					// 将读取到的内容转发给所有客户端
					// 解析message
					String[] arr = FileUtils.parseMesg(message);
					sendMsgByReceiveInfo(arr, nickName);
					// sendMessageToAllClient(nickName + "说:" + message);
				}

			} catch (Exception e) {

			} finally {
				/*
				 * 无论是linux还是windows的客户端，断开连接 后都要做的事情，就放到finally中来解决
				 */
				// 将该客户端的输出流从共享集合中删除
				removeOut(pw);
				// 广播，通知所有客户端该用户下线了
				sendMessageToAllClient(FileUtils.mesgBuilder(0, null, nickName));
				userOutMap.remove(nickName);
				System.out.println("[" + nickName + "]下线了.");
				textArea.append("[" + nickName + "]下线了.");
				textArea.append("\n");
				/*
				 * 将该客户端的socket关闭。 关闭socket同时也就将使用它获取的输入流与 输出流关掉了。
				 */
				try {
					socket.close();
				} catch (IOException e) {
				}
			}

		}

	}

	public void sendMsgByReceiveInfo(String[] arr, String nickName) {
		String to = arr[1];
		String content = arr[2];
		// 普通消息
		if (to.equals("all")) {
			for (PrintWriter out : allOut) {
				textArea.append(nickName + "说:" + content);
				textArea.append("\n");
				out.println(FileUtils.mesgBuilder(2, null, nickName + "说:" + content)  );
			}
		} else {
			textArea.append(nickName +"对"+to+ "说:" + content);
			textArea.append("\n");
			userOutMap.get(to).println(FileUtils.mesgBuilder(2, null, nickName + "说:" + content)  );
		}

	}

	public String tranSetToStr(Set<String> users2) {
		StringBuffer buffer=new StringBuffer();
		for(String u:users2) {
			buffer.append(u).append(",");
		}
		String str=buffer.toString();
		return str.substring(0, str.length()-1);
	}

}
