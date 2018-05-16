package ptcp.tcp.cliente;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class TelaPrincipal extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JTextPane textPane;
	private JTextField textField;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket socket;
	private String hostName;
	private int hostPort;
	private String mensagemServidor;

	public static void main(String[] args) {
		TelaPrincipal telaPrincipal = new TelaPrincipal("127.0.0.1", 12345);
		telaPrincipal.executarCliente();
	}
	
	public TelaPrincipal(String hostName, int hostPort) {
		this.hostName = hostName;
		this.hostPort = hostPort;
		setTitle("5PTCP Cliente");
		setSize(800, 650);
		setLayout(null);
		
		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBounds(50,40,700,500);
		getContentPane().add(scrollPane);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(50,560,550,30);
		textField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10) {
					enviarMensagem(textField.getText());
					textField.setText("");
				}
			}
		});
		getContentPane().add(textField);
		
		JButton button = new JButton("Enviar >>");
		button.setBounds(610, 560, 140, 30);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				enviarMensagem(textField.getText());
				textField.setText("");
			}
		});
		getContentPane().add(button);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void executarCliente() {
		try {
			conectarComServidor();
			getStreams();
			processarConexao();
		}catch(EOFException eofException) {
			exibirMensagem("\nConexão interrompida...", new Color(255, 0, 0));
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}finally {
			encerrarConexao();
		}
	}
	
	private void conectarComServidor() throws UnknownHostException, IOException {
		exibirMensagem("\nTentando conexão...", new Color(255, 0, 0));
		socket = new Socket(InetAddress.getByName(hostName), hostPort);
		exibirMensagem("\nConectado a " + socket.getInetAddress().getHostName() + "...", new Color(0, 100, 0));
	}
	
	private void getStreams() throws IOException {
		output = new ObjectOutputStream(socket.getOutputStream());
		output.flush();
		input = new ObjectInputStream(socket.getInputStream());
	}
	
	private void processarConexao() throws IOException {
		setTextFieldEditable(true);
		do {
			try {
				mensagemServidor = (String) input.readObject();
				exibirMensagem("\n"+socket.getInetAddress().getHostAddress()+": " + mensagemServidor, new Color(0, 100, 0));
			}catch(ClassNotFoundException classNotFoundException) {
				exibirMensagem("\nErro ao receber mensagem...", new Color(255, 0, 0));
			}
		}while(!mensagemServidor.equals(">>FIM"));
	}
	
	private void encerrarConexao() {
		exibirMensagem("\nEncerrando conexão...", new Color(255, 0, 0));
		setTextFieldEditable(false);
		try {
			output.close();
			input.close();
			socket.close();
			exibirMensagem("\nConexão encerrada...", new Color(255, 0, 0));
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public void enviarMensagem(String mensagem) {
		try {
			if(output != null) {
				output.writeObject(mensagem);
				output.flush();
				exibirMensagem("\nVocê: " + mensagem, new Color(255, 0, 0));
			}
		}catch(IOException ioException) {
			ioException.printStackTrace();
			exibirMensagem("\nErro no envio da mensagem...", new Color(255, 0, 0));
		}
	}
	
	private void exibirMensagem(String msg, Color c){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StyleContext sc = StyleContext.getDefaultStyleContext();
		        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
		        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		        int len = textPane.getDocument().getLength();
		        textPane.setCaretPosition(len);
		        textPane.setCharacterAttributes(aset, false);
		        textPane.replaceSelection(msg);
			}
		});
    }
	
	private void setTextFieldEditable(final boolean editable) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textField.setEditable(editable);
			}
		});
	}

}
