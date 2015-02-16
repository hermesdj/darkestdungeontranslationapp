package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import fr.hermesdj.java.darkestdungeontranslationapp.TranslationAppConfigurationManager.ConfigurationKey;

public class TranslationAppMain {
	private JFrame frame = new JFrame();
	private JTextArea originalTextArea = new JTextArea();
	private JTextArea translatedTextArea = new JTextArea();
	private JFileChooser chooser = new JFileChooser();
	private JComboBox<String> fileSelector = new JComboBox<String>();
	private JButton nextButton = new JButton("Valider");
	private JButton skipButton = new JButton("Passer");
	private JLabel logLabel = new JLabel("Evenements...");
	private JMenuBar menuBar = new JMenuBar();
	private JLabel toTranslateLabel = new JLabel();
	private JLabel translatedLabel = new JLabel();
	private JTable table;
	private JScrollPane scrollPane;

	private File sourceDirectory;
	private String[] translationFiles;

	private String original_language;
	private String translation_language;
	
	private XPathFactory xFactory = XPathFactory.instance();
	private Document currentDocument;
	
	private TranslationAppConfigurationManager conf;
	private File configurationFile;
	private String currentEditedFile;

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
		updateTranslationProgress();
	}

	private void initializeProperties() {
		conf = TranslationAppConfigurationManager
				.getInstance();
		configurationFile = new File("translationapp.conf");
		conf.loadFromFile(configurationFile);

		sourceDirectory = new File(
				conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION));

		original_language = conf
				.getProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE);
		translation_language = conf
				.getProperty(ConfigurationKey.TRANSLATED_LANGUAGE);

		if (!sourceDirectory.exists()) {
			selectFileFolder();
		}
	}

	private void initialize() {
		initializeProperties();
		loadTranslationFiles();
		
		frame.setBounds(0, 0, 800, 700);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height
				/ 2 - frame.getSize().height / 2 - 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		
		JMenu fileMenu = new JMenu("Fichier");
		JMenuItem openFile = new JMenuItem("Dossier...", new ImageIcon(getClass().getResource("/images/folder.png")));
		openFile.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				selectFileFolder();
			}
		});
		fileMenu.add(openFile);
		menuBar.add(fileMenu);
		frame.setJMenuBar(menuBar);
		
		frame.add(new JSeparator(JSeparator.HORIZONTAL));
		
		JToolBar toolbar = new JToolBar("Outils");
		toolbar.add(new JButton(new ImageIcon(getClass().getResource("/images/folder.png"))));
		toolbar.add(new JButton(new ImageIcon(getClass().getResource("/images/disk.png"))));
		frame.add(toolbar, BorderLayout.NORTH);
		toolbar.setFloatable(false);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		frame.add(panel, BorderLayout.CENTER);
		
		table = new JTable(constructTableData(), new String[]{"Text Original : " + original_language, "Texte Traduit : " + translation_language, "Id"});
		// Configure some of JTable's paramters
		table.setShowHorizontalLines( false );
		table.setRowSelectionAllowed( true );
		table.setColumnSelectionAllowed( true );

		// Change the selection colour
		table.setSelectionBackground( new Color(224,224,224) );
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMaxWidth(150);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				originalTextArea.setText(table.getValueAt(table.getSelectedRow(), 0).toString());
				translatedTextArea.setText(table.getValueAt(table.getSelectedRow(), 1).toString());
			}
		});

		// Add the table to a scrolling pane
		scrollPane = new JScrollPane(table);
		panel.add(scrollPane);
		
		JPanel bottom = new JPanel();
		frame.add(bottom, BorderLayout.SOUTH);
		bottom.setPreferredSize(new Dimension(800, 240));
		
		JPanel translationPanel = new JPanel();
		translationPanel.setPreferredSize(new Dimension(750, 200));
		bottom.add(translationPanel, BorderLayout.WEST);
		translationPanel.setLayout(new GridLayout(2, 1));
		
		JToolBar translationToolBar = new JToolBar("Translation Tools", JToolBar.VERTICAL);
		translationToolBar.add(new JButton(new ImageIcon(getClass().getResource("/images/arrow_left.png"))));
		translationToolBar.add(new JButton(new ImageIcon(getClass().getResource("/images/arrow_right.png"))));
		translationToolBar.add(new JSeparator());
		translationToolBar.add(new JButton(new ImageIcon(getClass().getResource("/images/accept.png"))));
		translationToolBar.setAlignmentX(SwingConstants.TOP);
		translationToolBar.setAlignmentY(SwingConstants.LEFT);
		translationToolBar.setFloatable(false);
		
		bottom.add(translationToolBar, BorderLayout.EAST);
		
		originalTextArea.setBackground(new Color(224,224,224));
		translatedTextArea.setBackground(new Color(229,255,204));
		
		originalTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		translatedTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		translationPanel.add(originalTextArea);
		translationPanel.add(translatedTextArea);
		
		logLabel.setPreferredSize(new Dimension(750, 20));
		bottom.add(logLabel, BorderLayout.SOUTH);
	}

	private void loadTranslationFiles() {
		translationFiles = sourceDirectory.list();
		if (translationFiles.length > 0) {
			changeFile(translationFiles[0]);
		}
	}

	private String[][] constructTableData() {
		Element root = currentDocument.getRootElement();
		
		XPathExpression<Element> browseOriginal = xFactory.compile(
				"//language[@id='" + original_language + "']/entry[string-length(@id) > 0]/.[text()]",
				Filters.element());
		
		List<Element> originalElements = browseOriginal.evaluate(root);
		System.out.println(originalElements.size());
		
		String[][] data = new String[originalElements.size()][3];
		for(int i = 0; i < originalElements.size(); i++){
			Element el = originalElements.get(i);
			String id = el.getAttributeValue("id");
			data[i][0] = el.getText();
			data[i][1] = xFactory.compile(
					"//language[@id='" + translation_language + "']/entry[@id='"+id+"']",
					Filters.element()).evaluateFirst(root).getText();
			data[i][2] = id;
		}
		
		return data;
	}
	
	protected void updateTranslationProgress(){
		Double total = xFactory.compile(
				"count(//language[@id='" + original_language + "']/entry[string-length(@id) > 0]/.[text()])",
				Filters.fdouble()).evaluateFirst(currentDocument.getRootElement());
		Double translated = xFactory.compile(
				"count(//language[@id='" + translation_language + "']/entry[string-length(@id) > 0]/.[text()])",
				Filters.fdouble()).evaluateFirst(currentDocument.getRootElement());
		System.out.println(translated);
		Double percent = Math.floor((translated / total) * 100);
		
		logLabel.setText(percent.intValue() + "% traduit, " + total.intValue() + " entrées à traduire.");
	}

	protected void selectFileFolder() {
		chooser.setCurrentDirectory(sourceDirectory);
		chooser.setDialogTitle("Sélectionner le dossier localization à traduire");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			sourceDirectory = chooser.getSelectedFile();
			conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
					sourceDirectory.getAbsolutePath());
			conf.saveToFile(configurationFile);
			System.out.println("Picked directory is : " + sourceDirectory);
		} else {
			System.out.println("No selection");
		}
	}

	protected void changeFile(String selectedItem) {
		System.out.println("File edited is : " + selectedItem);
		frame.setTitle("Traducteur Darkest Dungeon - " + selectedItem);
		currentEditedFile = selectedItem;
		loadFileDocument(new File(sourceDirectory
				.getAbsolutePath()
				+ File.separatorChar
				+ currentEditedFile));
	}

	private void loadFileDocument(File file) {
		System.out.println("Loading translation file " + file);
		SAXBuilder builder = new SAXBuilder();

		try {
			currentDocument = (Document) builder.build(file);

		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
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
	

	protected void loadCurrentTranslation(File file) {
		loadTranslationFile(file);
		displayTranslationData();
	}

	private void loadTranslationFiles() {
		translationFiles = sourceDirectory.list();
		if (translationFiles.length > 0) {
			changeFile(translationFiles[0]);
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
					"//language[@id='" + original_language + "']",
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
	*/
}
