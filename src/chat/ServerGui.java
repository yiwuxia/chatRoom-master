package chat;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ServerGui extends JFrame {

	private JPanel contentPane;
	
	public Server server;
	
	public JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGui frame = new ServerGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ServerGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnStart = new JButton("启动服务");
		btnStart.setBounds(29, 10, 93, 23);
		contentPane.add(btnStart);
		
		JButton btnStop = new JButton("停止服务");
		btnStop.setBounds(161, 10, 93, 23);
		contentPane.add(btnStop);
		
		JButton btnExit = new JButton("退出");
		btnExit.setBounds(314, 10, 93, 23);
		contentPane.add(btnExit);
		
		 textArea = new JTextArea();
		textArea.setBounds(10, 51, 367, 164);
		contentPane.add(textArea);
		
		btnStart.addActionListener((e)->{
			 server = new Server();
			 server.start(textArea);//running
		});
		btnStop.addActionListener((e)->{
			server.running=false;
		});
		btnExit.addActionListener((e)->{
			System.exit(0);
		});
	}
}
