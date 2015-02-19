package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.hermesdj.java.darkestdungeontranslationapp.ConfigurationManager.ConfigurationKey;
import fr.hermesdj.java.darkestdungeontranslationapp.Localization.LocalizationKey;

public class AboutPage extends JFrame {

    private JPanel contentPane;
    private Localization lang;
    private static final Logger LOG = LogManager.getLogger(AboutPage.class);

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
	lang = Localization.getInstance();
	setTitle(lang.getString(LocalizationKey.ABOUT_PAGE_TITLE));

	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	setResizable(false);
	setBounds(100, 100, 450, 300);
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2
		- this.getSize().height / 2 - 150);

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

	JLabel lblOutilDaide = new JLabel(
		lang.getString(LocalizationKey.ABOUT_APPLICATION_SHORT_DESCRIPTION));
	lblOutilDaide.setForeground(Color.GRAY);
	lblOutilDaide.setBounds(10, 45, 170, 14);
	panel.add(lblOutilDaide);

	JLabel lblDarkestDungeon = new JLabel("Darkest Dungeon");
	lblDarkestDungeon.setFont(new Font("Tahoma", Font.BOLD, 14));
	lblDarkestDungeon.setBounds(10, 11, 222, 23);
	panel.add(lblDarkestDungeon);

	JLabel lblVersion = new JLabel("Version "
		+ ConfigurationManager.getInstance().getProperty(
			ConfigurationKey.APPLICATION_VERSION));
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
	    btnJaysGaming.setToolTipText(lang
		    .getString(LocalizationKey.ABOUT_AUTHOR_TOOLTIP));
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
	    lblGithub.setToolTipText(lang
		    .getString(LocalizationKey.ABOUT_SOURCE_TOOLTIP));
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
		lang.getString(LocalizationKey.ABOUT_APPLICATION_DESCRIPTION));
	projectDescr.setVerticalAlignment(SwingConstants.TOP);
	projectDescr.setHorizontalAlignment(SwingConstants.LEFT);
	projectDescr.setBounds(10, 75, 254, 139);
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
	    lblDD.setToolTipText(lang
		    .getString(LocalizationKey.ABOUT_OFFICIAL_PAGE_TOOLTIP));
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
