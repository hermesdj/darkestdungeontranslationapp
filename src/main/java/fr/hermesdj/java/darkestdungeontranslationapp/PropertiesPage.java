package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSeparator;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.SwingConstants;

import fr.hermesdj.java.darkestdungeontranslationapp.TranslationAppConfigurationManager.ConfigurationKey;

public class PropertiesPage extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1708375186779889179L;
	private JPanel contentPane;
	private JTextField defaultFolderTxt;
	private JLabel lblLangageSource;
	private JTextField clientIdTxt;
	private JTextField clientSecretTxt;
	private JComboBox<String> targetLanguage;
	private JComboBox<String> sourceLanguage;
	private JFileChooser chooser;
	private TranslationAppMain main;

	/**
	 * Create the frame.
	 * @param translationAppMain 
	 */
	public PropertiesPage(TranslationAppMain translationAppMain) {
		main = translationAppMain;
		TranslationAppConfigurationManager conf = TranslationAppConfigurationManager.getInstance();
		
		setTitle("Configuration");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 599, 356);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblDefaultFolderLocation = new JLabel("Dossier par d\u00E9faut");
		lblDefaultFolderLocation.setBounds(10, 11, 118, 14);
		contentPane.add(lblDefaultFolderLocation);
		
		defaultFolderTxt = new JTextField();
		defaultFolderTxt.setBounds(138, 8, 401, 20);
		contentPane.add(defaultFolderTxt);
		defaultFolderTxt.setColumns(10);
		defaultFolderTxt.setText(conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION));
		
		JButton folderSelectBtn = new JButton(new ImageIcon(
				getClass().getResource("/images/folder.png")));
		folderSelectBtn.setBounds(549, 7, 25, 23);
		folderSelectBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		contentPane.add(folderSelectBtn);
		
		lblLangageSource = new JLabel("Langage Source");
		lblLangageSource.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLangageSource.setBounds(138, 52, 88, 14);
		contentPane.add(lblLangageSource);
		
		sourceLanguage = new JComboBox<String>();
		sourceLanguage.setModel(new DefaultComboBoxModel(new String[] {"english", "french", "italian", "german", "spanish"}));
		sourceLanguage.setBounds(236, 49, 303, 20);
		contentPane.add(sourceLanguage);
		sourceLanguage.setSelectedItem(conf.getProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE));
		
		JLabel lblLangageCible = new JLabel("Langage Cible");
		lblLangageCible.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLangageCible.setBounds(138, 90, 88, 14);
		contentPane.add(lblLangageCible);
		
		targetLanguage = new JComboBox<String>();
		targetLanguage.setModel(new DefaultComboBoxModel(new String[] {"english", "french", "italian", "german", "spanish"}));
		targetLanguage.setSelectedIndex(1);
		targetLanguage.setBounds(236, 87, 303, 20);
		contentPane.add(targetLanguage);
		targetLanguage.setSelectedItem(conf.getProperty(ConfigurationKey.TRANSLATED_LANGUAGE));
		
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
		
		clientSecretTxt = new JTextField();
		clientSecretTxt.setBounds(237, 226, 302, 20);
		contentPane.add(clientSecretTxt);
		clientSecretTxt.setColumns(10);
		clientSecretTxt.setText(conf.getProperty(ConfigurationKey.AZURE_CLIENT_SECRET));
		
		JButton validationBtn = new JButton("OK");
		validationBtn.setBounds(485, 287, 89, 23);
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
		
		JLabel lblIdentifiantsMicrosoftAzure = new JLabel("Identifiants Microsoft Azure Translator");
		lblIdentifiantsMicrosoftAzure.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblIdentifiantsMicrosoftAzure.setBounds(20, 154, 273, 14);
		contentPane.add(lblIdentifiantsMicrosoftAzure);
		
		chooser = new JFileChooser();
	}
	
	protected void selectFile() {
		TranslationAppConfigurationManager conf = TranslationAppConfigurationManager.getInstance();
		chooser.setCurrentDirectory(new File(conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION)));
		chooser.setDialogTitle("S\u00e9lectionner le dossier localization \u00e0 traduire");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
					chooser.getSelectedFile().getAbsolutePath());
			defaultFolderTxt.setText(chooser.getSelectedFile().getAbsolutePath());
			conf.saveToFile(TranslationAppMain.configurationFile);
			System.out.println("Picked directory is : " + chooser.getSelectedFile().getAbsolutePath());
		} else {
			System.out.println("No selection");
		}
	}

	private void save(){
		TranslationAppConfigurationManager conf = TranslationAppConfigurationManager.getInstance();
		conf.setProperty(ConfigurationKey.AZURE_CLIENT_ID, clientIdTxt.getText());
		conf.setProperty(ConfigurationKey.AZURE_CLIENT_SECRET, clientSecretTxt.getText());
		conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION, defaultFolderTxt.getText());
		conf.setProperty(ConfigurationKey.TRANSLATED_LANGUAGE, (String) targetLanguage.getSelectedItem());
		conf.setProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE, (String) sourceLanguage.getSelectedItem());
		
		conf.saveToFile(TranslationAppMain.configurationFile);
		this.setVisible(false);
		main.initializeProperties();
	}
}
