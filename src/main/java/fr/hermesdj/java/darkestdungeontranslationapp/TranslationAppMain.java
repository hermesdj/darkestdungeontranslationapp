package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import fr.hermesdj.java.darkestdungeontranslationapp.TranslationAppConfigurationManager.ConfigurationKey;

public class TranslationAppMain {
	private JFrame frame;
	private JTextArea toTranslateArea = new JTextArea();
	private JTextArea translatedArea = new JTextArea();
	private JFileChooser chooser = new JFileChooser();
	private JComboBox<String> fileSelector = new JComboBox<String>();
	private JButton nextButton = new JButton("Suivant");
	private JButton skipButton = new JButton("Passer");
	private JLabel logLabel = new JLabel();

	private File sourceDirectory;
	private String[] translationFiles;
	private int currentTranslationIndex = 0;

	private String currentToTranslateLabel;
	private String currentTranslatedLabel;
	private Element currentToTranslateElement;
	private XPathFactory xFactory;
	private List<Element> toTranslateEntries;
	private Element translationFileElement;
	private Document currentDocument;
	protected String currentEditedFile;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TranslationAppMain window = new TranslationAppMain();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TranslationAppMain() {
		initialize();
	}

	private void initializeProperties() {
		TranslationAppConfigurationManager conf = TranslationAppConfigurationManager
				.getInstance();
		File saveFile = new File("translationapp.conf");
		conf.loadFromFile(saveFile);

		sourceDirectory = new File(
				conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION));

		currentToTranslateLabel = conf
				.getProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE);
		currentTranslatedLabel = conf
				.getProperty(ConfigurationKey.TRANSLATED_LANGUAGE);

		if (!sourceDirectory.exists()) {
			chooser.setCurrentDirectory(sourceDirectory);
			chooser.setDialogTitle("Sélectionner le dossier localization à traduire");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				sourceDirectory = chooser.getSelectedFile();
				conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
						sourceDirectory.getAbsolutePath());
				conf.saveToFile(saveFile);
				System.out.println("Picked directory is : " + sourceDirectory);
			} else {
				System.out.println("No selection");
				System.exit(0);
			}
		}
	}

	private void initialize() {
		initializeProperties();
		loadTranslationFiles();

		frame = new JFrame();
		frame.setBounds(0, 0, 400, 300);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height
				/ 2 - frame.getSize().height / 2 - 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		// Text translated area
		toTranslateArea.setBounds(5, 50, 185, 100);
		toTranslateArea.setLineWrap(true);
		panel.add(toTranslateArea);

		// Text to translate area
		translatedArea.setBounds(frame.getSize().width - 195, 50, 185, 100);
		translatedArea.setLineWrap(true);
		panel.add(translatedArea);

		// Add log label
		logLabel.setBounds(5, frame.getSize().height - 60, 200, 30);
		panel.add(logLabel);

		// File Picker
		for (String translationFile : translationFiles) {
			fileSelector.addItem(translationFile);
		}

		fileSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentEditedFile = (String) ((JComboBox<String>) e.getSource())
						.getSelectedItem();

				loadCurrentTranslation(new File(sourceDirectory
						.getAbsolutePath()
						+ File.separatorChar
						+ currentEditedFile));
			}

		});
		fileSelector.setBounds(5, 10, 200, 20);
		panel.add(fileSelector);

		// Next Button
		nextButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Element el = getTargetTranslatedElement();

				if (!el.getText().equals(translatedArea.getText())) {
					System.out.println("Translating "
							+ el.getAttributeValue("id") + " from \""
							+ el.getText() + "\" to \""
							+ translatedArea.getText() + "\"");

					el.setContent(new CDATA(translatedArea.getText()));
					logLabel.setText("Sauvegarde du fichier...");
					saveCurrentTranslationFile();
				} else {
					logLabel.setText("Pas de changement");
					System.out.println("Pas de changement");
				}

				currentTranslationIndex++;
				displayTranslationData();
			}

		});
		nextButton.setBounds(frame.getSize().width - 90,
				frame.getSize().height - 65, 80, 30);
		panel.add(nextButton);

		// Skip Button
		skipButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentTranslationIndex++;
				displayTranslationData();
			}

		});
		skipButton.setBounds(frame.getSize().width - 175,
				frame.getSize().height - 65, 80, 30);
		panel.add(skipButton);
	}

	protected void saveCurrentTranslationFile() {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		try {
			System.out.println("Sauvegarde du fichier "
					+ sourceDirectory.getAbsolutePath() + File.separatorChar
					+ currentEditedFile);
			xmlOutput.output(currentDocument,
					new FileWriter(sourceDirectory.getAbsolutePath()
							+ File.separatorChar + currentEditedFile));
			logLabel.setText("Sauvegarde ok !");
		} catch (IOException e) {
			logLabel.setText("Erreur sauvegarde. Voir log.");
			e.printStackTrace();
		}
	}

	protected Element getCurrentTranslatedElement() {
		return toTranslateEntries.get(currentTranslationIndex);
	}

	protected Element getTargetTranslatedElement() {
		Element currentTranslatedElement = getCurrentTranslatedElement();
		String id = currentTranslatedElement.getAttributeValue("id");

		XPathExpression<Element> translatedEntryXpath = xFactory.compile(
				"//language[@id='" + currentTranslatedLabel + "']/entry[@id='"
						+ id + "']", Filters.element());

		return translatedEntryXpath.evaluateFirst(translationFileElement);
	}

	protected void loadCurrentTranslation(File file) {
		loadTranslationFile(file);
		displayTranslationData();
	}

	private void loadTranslationFiles() {
		translationFiles = sourceDirectory.list();
		if (translationFiles.length > 0) {
			currentEditedFile = translationFiles[0];
			System.out.println("File edited is : " + currentEditedFile);
			loadTranslationFile(new File(sourceDirectory.getAbsolutePath()
					+ File.separatorChar + currentEditedFile));
			displayTranslationData();
		}
	}

	private void loadTranslationFile(File file) {
		System.out.println("Loading translation file " + file);
		SAXBuilder builder = new SAXBuilder();

		try {
			currentDocument = (Document) builder.build(file);
			translationFileElement = currentDocument.getRootElement();

			xFactory = XPathFactory.instance();

			XPathExpression<Element> toTranslate = xFactory.compile(
					"//language[@id='" + currentToTranslateLabel + "']",
					Filters.element());

			currentToTranslateElement = toTranslate
					.evaluateFirst(translationFileElement);
			System.out.println("Loaded "
					+ currentToTranslateElement.getAttributeValue("id")
					+ " section.");

			XPathExpression<Element> entryExp = xFactory.compile(
					"//entry[string-length(@id) > 0]", Filters.element());
			List<Element> temp = entryExp.evaluate(currentToTranslateElement);
			toTranslateEntries = new ArrayList<Element>();

			for (Element elem : temp) {
				if (elem.getText().length() > 0) {
					toTranslateEntries.add(elem);
				}
			}

			currentTranslationIndex = 0;

		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void displayTranslationData() {
		System.out.println("Display Translation data for line "
				+ currentTranslationIndex + " / " + toTranslateEntries.size());
		// If there is still entries to translate...
		if (currentTranslationIndex < toTranslateEntries.size()) {
			Element toTranslateElem = getCurrentTranslatedElement();
			System.out.println("Translating element id "
					+ toTranslateElem.getAttributeValue("id"));

			// Display original text
			String id = toTranslateElem.getAttributeValue("id");
			toTranslateArea.setText(toTranslateElem.getText());

			// Display translated text if any
			Element translatedElem = getTargetTranslatedElement();
			translatedArea.setText(translatedElem.getText());
		} else {
			System.out.println("End of translation !");
		}

	}
}
