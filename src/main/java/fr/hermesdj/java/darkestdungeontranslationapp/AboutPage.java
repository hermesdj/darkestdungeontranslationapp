package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class AboutPage extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AboutPage frame = new AboutPage();
					frame.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AboutPage() {
		setTitle("A propos de l'application...");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel channelImage = new JLabel(new ImageIcon(getClass().getResource(
				"/images/illustration_small.png")));
		channelImage.setHorizontalAlignment(SwingConstants.RIGHT);

		contentPane.add(channelImage, BorderLayout.WEST);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		JLabel lblOutilDaide = new JLabel("Outil d'aide à la traduction");
		lblOutilDaide.setForeground(Color.GRAY);
		lblOutilDaide.setBounds(10, 36, 170, 14);
		panel.add(lblOutilDaide);

		JLabel lblDarkestDungeon = new JLabel("Darkest Dungeon");
		lblDarkestDungeon.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblDarkestDungeon.setBounds(10, 11, 222, 14);
		panel.add(lblDarkestDungeon);

		JLabel lblVersion = new JLabel("Version 0.0.2");
		lblVersion.setForeground(Color.GRAY);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblVersion.setHorizontalAlignment(SwingConstants.LEFT);
		lblVersion.setBounds(10, 200, 122, 14);
		panel.add(lblVersion);

		JLabel btnJaysGaming = new JLabel("Jay's Gaming");
		try {
			final URI uriJays = new URI(
					"https://www.youtube.com/channel/UCFAkedtc3jjDZQqqIULxqWg");
			btnJaysGaming.setBounds(10, 229, 84, 23);
			btnJaysGaming
					.setText("<HTML><FONT color=\"#000099\"><U>Jay's Gaming</U></FONT>"
							+ "</HTML>");
			btnJaysGaming.setBackground(Color.WHITE);
			btnJaysGaming
					.setToolTipText("Visiter la Chaîne Youtube de l'auteur.");
			btnJaysGaming.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnJaysGaming.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					browse(uriJays);
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		panel.add(btnJaysGaming);

		JLabel lblGithub = new JLabel("Github");
		lblGithub.setHorizontalAlignment(SwingConstants.CENTER);
		lblGithub.setBounds(104, 233, 56, 14);
		try {
			final URI uriGit = new URI(
					"https://github.com/hermesdj/darkestdungeontranslationapp");
			lblGithub
					.setText("<HTML><FONT color=\"#000099\"><U>Github</U></FONT>"
							+ "</HTML>");
			lblGithub.setBackground(Color.WHITE);
			lblGithub
					.setToolTipText("Visiter la page Github de l'Application.");
			lblGithub.setCursor(new Cursor(Cursor.HAND_CURSOR));
			lblGithub.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					browse(uriGit);
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		panel.add(lblGithub);

		JLabel projectDescr = new JLabel(
				"<html>Cette application a \u00e9t\u00e9e cr\u00e9\u00e9e par la cha\u00eene Jay's Gaming. <br>Son utilisation est libre de droit.</html>");
		projectDescr.setVerticalAlignment(SwingConstants.TOP);
		projectDescr.setHorizontalAlignment(SwingConstants.LEFT);
		projectDescr.setBounds(10, 57, 254, 159);
		panel.add(projectDescr);

		JLabel lblDD = new JLabel();
		lblDD.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDD.setBackground(Color.WHITE);
		lblDD.setBounds(160, 233, 114, 14);
		try {
			final URI uriDD = new URI("http://www.darkestdungeon.com/");
			lblDD.setText("<HTML><FONT color=\"#000099\"><U>Darkest Dungeon</U></FONT>"
					+ "</HTML>");
			lblDD.setBackground(Color.WHITE);
			lblDD.setToolTipText("Visiter la page officielle du Jeu.");
			lblDD.setCursor(new Cursor(Cursor.HAND_CURSOR));
			lblDD.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					browse(uriDD);
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		panel.add(lblDD);
	}

	protected void browse(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Not desktop...");
		}
	}
}
