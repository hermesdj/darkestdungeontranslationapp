package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import fr.hermesdj.java.darkestdungeontranslationapp.ConfigurationManager.ConfigurationKey;
import fr.hermesdj.java.darkestdungeontranslationapp.Localization.LocalizationKey;

public class PropertiesPage extends JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1708375186779889179L;
    private static final Logger LOG = Logger.getLogger(PropertiesPage.class
	    .toString());
    private JPanel contentPane;
    private JTextField defaultFolderTxt;
    private JLabel lblLangageSource;
    private JTextField clientIdTxt;
    private JPasswordField clientSecretTxt;
    private JComboBox<String> targetLanguage;
    private JComboBox<String> sourceLanguage;
    private JFileChooser chooser;
    private TranslationAppMain main;
    private Localization lang;

    /**
     * Create the frame.
     * 
     * @param translationAppMain
     */
    public PropertiesPage(TranslationAppMain translationAppMain) {
	main = translationAppMain;
	final ConfigurationManager conf = ConfigurationManager.getInstance();
	lang = Localization.getInstance();

	setTitle(lang.getString(LocalizationKey.SETTING_FRAME_TITLE));

	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	setResizable(false);
	setBounds(100, 100, 597, 396);
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
		/ 2 - this.getSize().height / 2 - 150);

	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	contentPane.setLayout(null);

	JLabel lblDefaultFolderLocation = new JLabel(
		lang.getString(LocalizationKey.SETTINGS_DEFAULT_FOLDER_LABEL));
	lblDefaultFolderLocation.setBounds(10, 11, 125, 14);
	contentPane.add(lblDefaultFolderLocation);

	defaultFolderTxt = new JTextField();
	defaultFolderTxt.setBounds(138, 8, 401, 20);
	contentPane.add(defaultFolderTxt);
	defaultFolderTxt.setColumns(10);
	defaultFolderTxt.setText(conf
		.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION));

	JButton folderSelectBtn = new JButton(new ImageIcon(getClass()
		.getResource("/images/folder.png")));
	folderSelectBtn.setBounds(549, 7, 25, 23);
	folderSelectBtn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		selectFile();
	    }
	});
	contentPane.add(folderSelectBtn);

	lblLangageSource = new JLabel(
		lang.getString(LocalizationKey.SETTINGS_SOURCE_LANGUAGE_LABEL));
	lblLangageSource.setHorizontalAlignment(SwingConstants.RIGHT);
	lblLangageSource.setBounds(20, 52, 206, 14);
	contentPane.add(lblLangageSource);

	sourceLanguage = new JComboBox<String>();
	sourceLanguage.setModel(new DefaultComboBoxModel(new String[] {
		"english", "french", "italian", "german", "spanish" }));
	sourceLanguage.setBounds(236, 49, 303, 20);
	contentPane.add(sourceLanguage);
	sourceLanguage.setSelectedItem(conf
		.getProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE));

	JLabel lblLangageCible = new JLabel(
		lang.getString(LocalizationKey.SETTINGS_TRANSLATED_LANGUAGE_LABEL));
	lblLangageCible.setHorizontalAlignment(SwingConstants.RIGHT);
	lblLangageCible.setBounds(20, 90, 206, 14);
	contentPane.add(lblLangageCible);

	targetLanguage = new JComboBox<String>();
	targetLanguage.setModel(new DefaultComboBoxModel(new String[] {
		"english", "french", "italian", "german", "spanish" }));
	targetLanguage.setSelectedIndex(1);
	targetLanguage.setBounds(236, 87, 303, 20);
	contentPane.add(targetLanguage);
	targetLanguage.setSelectedItem(conf
		.getProperty(ConfigurationKey.TRANSLATED_LANGUAGE));

	JLabel lblMicrosoftAzureClient = new JLabel("Client ID");
	lblMicrosoftAzureClient.setHorizontalAlignment(SwingConstants.RIGHT);
	lblMicrosoftAzureClient.setBounds(102, 198, 125, 14);
	contentPane.add(lblMicrosoftAzureClient);

	clientIdTxt = new JTextField();
	clientIdTxt.setBounds(237, 195, 302, 20);
	contentPane.add(clientIdTxt);
	clientIdTxt.setColumns(10);
	clientIdTxt.setText(conf.getProperty(ConfigurationKey.AZURE_CLIENT_ID));

	JLabel lblMicrosoftAzureSecret = new JLabel("Client Secret ");
	lblMicrosoftAzureSecret.setHorizontalAlignment(SwingConstants.RIGHT);
	lblMicrosoftAzureSecret.setBounds(85, 229, 142, 14);
	contentPane.add(lblMicrosoftAzureSecret);

	clientSecretTxt = new JPasswordField();
	clientSecretTxt.setBounds(237, 226, 302, 20);
	contentPane.add(clientSecretTxt);
	clientSecretTxt.setColumns(10);
	clientSecretTxt.setText(conf
		.getProperty(ConfigurationKey.AZURE_CLIENT_SECRET));

	JButton validationBtn = new JButton("OK");
	validationBtn.setBounds(492, 334, 89, 23);
	validationBtn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		save();
	    }
	});
	contentPane.add(validationBtn);

	JSeparator separator = new JSeparator();
	separator.setBounds(20, 141, 564, 2);
	contentPane.add(separator);

	JLabel lblIdentifiantsMicrosoftAzure = new JLabel(
		lang.getString(LocalizationKey.AZURE_CLIENT_CREDENTIALS_TITLE));
	lblIdentifiantsMicrosoftAzure
		.setFont(new Font("Tahoma", Font.PLAIN, 14));
	lblIdentifiantsMicrosoftAzure.setBounds(20, 154, 303, 33);
	contentPane.add(lblIdentifiantsMicrosoftAzure);

	JSeparator separator_1 = new JSeparator();
	separator_1.setBounds(10, 271, 564, 14);
	contentPane.add(separator_1);

	JLabel lblLanguage = new JLabel(
		lang.getString(LocalizationKey.SETTINGS_APPLICATION_LANGUAGE));
	lblLanguage.setHorizontalAlignment(SwingConstants.RIGHT);
	lblLanguage.setBounds(20, 299, 206, 14);
	contentPane.add(lblLanguage);

	JComboBox comboBox = new JComboBox();
	comboBox.setBounds(236, 296, 303, 20);
	comboBox.setModel(new DefaultComboBoxModel(lang.getAvailableLocales()));
	comboBox.setSelectedItem(lang.getCurrentLocale().getDisplayLanguage());
	comboBox.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		JComboBox<String> cb = (JComboBox<String>) e.getSource();
		Locale locale = lang.availableLocales.get(cb.getSelectedIndex());

		lang.setCurrentLocale(locale);
		ResourceBundle.clearCache();

		conf.setProperty(ConfigurationKey.DEFAULT_APPLICATION_LANGUAGE,
			locale.getLanguage() + "_" + locale.getCountry());
		conf.save();
	    }

	});

	contentPane.add(comboBox);

	chooser = new JFileChooser();
    }

    protected void selectFile() {
	ConfigurationManager conf = ConfigurationManager.getInstance();
	chooser.setCurrentDirectory(new File(conf
		.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION)));
	chooser.setDialogTitle(lang
		.getString(LocalizationKey.FILE_CHOOSER_TITLE));
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setAcceptAllFileFilterUsed(false);

	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	    conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
		    (String) chooser.getSelectedFile().getAbsolutePath());
	    defaultFolderTxt.setText(chooser.getSelectedFile()
		    .getAbsolutePath());
	    conf.save();
	    System.out.println("Picked directory is : "
		    + chooser.getSelectedFile().getAbsolutePath());
	} else {
	    System.out.println("No selection");
	}
    }

    private void save() {
	ConfigurationManager conf = ConfigurationManager.getInstance();
	conf.setProperty(ConfigurationKey.AZURE_CLIENT_ID,
		clientIdTxt.getText());
	conf.setProperty(ConfigurationKey.AZURE_CLIENT_SECRET,
		clientSecretTxt.getText());
	conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
		(String) defaultFolderTxt.getText());
	conf.setProperty(ConfigurationKey.TRANSLATED_LANGUAGE,
		(String) targetLanguage.getSelectedItem());
	conf.setProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE,
		(String) sourceLanguage.getSelectedItem());

	conf.save();
	this.setVisible(false);
	main.initializeProperties();
    }
}
