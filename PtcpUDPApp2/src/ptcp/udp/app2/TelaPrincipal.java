package ptcp.udp.app2;



import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
	private DatagramSocket socket;
	private String host;
	private int port;

	public static void main(String[] args) {
		TelaPrincipal telaPrincipal = new TelaPrincipal("127.0.0.1", 5000);
		telaPrincipal.aguardarPacotes();
	}

	public TelaPrincipal(String host, int port) {
		this.host = host;
		this.port = port;
		setTitle("5PTCP App2");
		setSize(800, 650);
		setLayout(null);

		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBounds(50, 40, 700, 500);
		getContentPane().add(scrollPane);

		textField = new JTextField();
		textField.setBounds(50, 560, 550, 30);
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					enviarPacote(textField.getText());
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
				enviarPacote(textField.getText());
				textField.setText("");
			}
		});
		getContentPane().add(button);

		try {
			socket = new DatagramSocket(5001);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void aguardarPacotes() {
		while (true) {
			try {
				byte[] data = new byte[100];
				DatagramPacket pacoteRecebido = new DatagramPacket(data, data.length);
				socket.receive(pacoteRecebido);
				exibirMensagem(
						"\n" + pacoteRecebido.getAddress() + ": "
								+ new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()),
						new Color(0, 100, 0));
			} catch (IOException ioException) {
				exibirMensagem("\nErro no recebimento de mensagem...", new Color(255, 0, 0));
			}
		}
	}

	public void enviarPacote(String mensagem) {
		try {
			byte[] data = mensagem.getBytes();
			
			DatagramPacket pacote = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
			socket.send(pacote);
			exibirMensagem("\nVocê: "+mensagem, new Color(255, 0, 0));
		}catch(IOException ioException) {
			ioException.printStackTrace();
			exibirMensagem("\nErro no envio de mensagem...", new Color(255, 0, 0));
		}
	}

	private void exibirMensagem(String msg, Color c) {
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

}
