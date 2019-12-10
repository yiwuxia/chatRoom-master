package chat;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ServerGui extends JFrame {

	private JPanel contentPane;
	
	public Server server;
	
	public JTextArea textArea;
	public JTextField tip=new JTextField();
	
	public JButton btnStart;
	public JButton btnStop;
	public JButton btnExit;
	
	public Thread runThread;

	
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
		
		 btnStart = new JButton("启动服务");
		btnStart.setBounds(29, 10, 93, 23);
		contentPane.add(btnStart);
		
		 btnStop = new JButton("停止服务");
		btnStop.setBounds(161, 10, 93, 23);
		contentPane.add(btnStop);
		
		 btnExit = new JButton("退出");
		btnExit.setBounds(314, 10, 93, 23);
		contentPane.add(btnExit);
		
		 textArea = new JTextArea();
		textArea.setBounds(10, 51, 367, 164);
		contentPane.add(textArea);
		
		
		tip.setEditable(false);
		tip.setBounds(10, 220, 100, 23);
		contentPane.add(tip);
		btnStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				tip.setText("已启动");
				 server = new Server();
				 runThread = new Thread(()-> {
					 server.start(textArea);//running
				 });
				 runThread.start();
			
			}
		});
		btnStop.addActionListener((e)->{
			//runThread.interrupt();
			server.StopServer();
			tip.setText("已停止");
		});
		btnExit.addActionListener((e)->{
			System.exit(0);
		});
	}
}
