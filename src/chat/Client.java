package chat;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.omg.CORBA.INITIALIZE;

import chat.util.FileUtils;

/**
 * 聊天室客户端
 * 
 * @author Administrator
 */
public class Client {

	private Socket socket;
	private JFrame frame;
	private JTextArea showMessage;
	private JTextArea inputMessage;
	private JScrollPane sp;
	private JButton btnNewButton;
	private String nickName;
	private OutputStream out;
	private OutputStreamWriter osw;
	private PrintWriter pw;
	private String sendTo = "all";

	private Set<String> setUser = new HashSet<String>();

	// 用户选择
	private JComboBox cmb;

	/**
	 * 构造函数，初始化
	 */
	public Client() {
		try {
			/*
			 * initialize（）
			 */
			socket = new Socket("localhost", 8888);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 私聊就是传参数给服务端，让服务端选择对应的客户端发送。
	 * 
	 * @param argss
	 */

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			public void run() {

				try {
					Client window = new Client();
					window.start();
					window.frame.setTitle(window.nickName + "的聊天室");
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * start方法
	 */
	public void start() {

		nickName = JOptionPane.showInputDialog("请输入本次聊天的昵称，仅本次有效!");
		nickName.trim();
		while (nickName.length() == 0 || nickName.trim().equals("")) {
			JOptionPane.showMessageDialog(frame, "请至少输入一个字符!");
			nickName = JOptionPane.showInputDialog("请输入本次聊天的昵称，仅本次有效!");

		}
		initialize();
		// 将nickName发送给服务器用于广播上线
		try {
			/*
			 * OutputStream getOutputStream() Socket的方法用于获取一个输出流，将数据发送给远端计算机
			 */

			out = socket.getOutputStream();

			/*
			 * 使用字符流包装后，我们就可以按照给定的字符集向远程计算机发送字符了
			 */
			osw = new OutputStreamWriter(out, "UTF-8");

			/*
			 * 使用缓冲字符输出流包装后，就可以以行为单位写出字符串了
			 */

			pw = new PrintWriter(osw, true);

			/*
			 * 首先使用pw发送一个字符串，这个字符串是昵称
			 */

			pw.println(nickName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * 启动用来读取服务器端消息的线程
		 *
		 */
		GetServerMessageHandler handler = new GetServerMessageHandler();
		Thread t = new Thread(handler);
		t.start();
	}

	/*
	 * 该线程负责读取服务端发送过来的消息
	 * 
	 * @author minchao
	 */
	class GetServerMessageHandler implements Runnable {
		public void run() {
			try {
				/*
				 * 通过socket获取输入流，循环读取服务器端发送过来的每一行字符串，并输出到控制台即可
				 */
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String message = null;
				while ((message = br.readLine()) != null) {
					// showMessage.append(message);
					// showMessage.append("\n");
					String arr[] = FileUtils.parseMesg(message);
					// 有人上线啦 更新用户选项
					dealWithInAndOut(arr);
					System.out.println(Arrays.toString(arr));
					if (arr[0].equals("0") || arr[0].equals("1")) {
						showMessage.append(arr[2].split(",")[0] + ":下线");
						showMessage.append("\n");
					}
					if (arr[0].equals("2")) {
						showMessage.append(arr[2]);
						showMessage.append("\n");
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void dealWithInAndOut(String[] arr) {
		if (arr[0].equals("0") || arr[0].equals("1")) {
			// cmb.getIte更新combox
			int count = cmb.getItemCount();
			Set<String> set = transStrToSet(arr[2]);
			// Set<String> set =new HashSet<String>();
			for (int i = 0; i < count; i++) {
				set.add(cmb.getItemAt(i).toString());
			}
			if (arr[0].equals("1")) {
				set.add(arr[2].split(",")[0]);
			}
			if (arr[0].equals("0")) {
				set.remove(arr[2].split(",")[0]);
			}
			set.remove(nickName);
			cmb.removeAllItems();
			for (String s : set) {
				cmb.addItem(s);
			}
		}

	}

	private Set<String> transStrToSet(String string) {
		Set<String> set = new HashSet<String>();
		String[] arr = string.split(",");
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
		}
		return set;
	}

	/*
	 * 窗口init
	 * 
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		showMessage = new JTextArea(10, 50);
		sp = new JScrollPane(showMessage);
		sp.setBounds(25, 25, 400, 150);
		showMessage.setBounds(25, 25, 400, 140);
		frame.getContentPane().add(sp);

		cmb = new JComboBox(); // 创建JComboBox
		cmb.addItem("群发"); // 向下拉列表中添加一项

		cmb.setBounds(40, 180, 100, 20);
		frame.getContentPane().add(cmb);
		inputMessage = new JTextArea();
		inputMessage.setBounds(40, 210, 231, 40);
		frame.getContentPane().add(inputMessage);
		btnNewButton = new JButton("发送");
		btnNewButton.addActionListener(new buttonAction());
		btnNewButton.setBounds(282, 195, 102, 49);
		frame.getContentPane().add(btnNewButton);

	}

	/*
	 * 发送按钮单击
	 * 
	 */
	private class buttonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stup
			if (e.getSource() == btnNewButton) {

				String messageString = inputMessage.getText();

				// 发送消息

				if (messageString.isEmpty() || messageString.trim().equals("")) {
					JOptionPane.showMessageDialog(frame, "聊天记录不能为空!");
				} else {
					messageString.trim();
					// 正常发送消息
					String to = cmb.getSelectedItem().toString().equals("群发") ? "all"
							: cmb.getSelectedItem().toString();
					pw.println(FileUtils.mesgBuilder(2, to, messageString));
					inputMessage.setText("");

				}

			}
		}

	}
	/*
	 * public static String mesgBuilder(int type,String to,String info) {
	 * StringBuffer buffer=new StringBuffer(); buffer.append(type).append(sep);
	 * buffer.append(to==null?"all":to).append(sep); buffer.append(info); return
	 * buffer.toString(); }
	 */

}
