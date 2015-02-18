package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import fr.hermesdj.java.darkestdungeontranslationapp.TranslationAppConfigurationManager.ConfigurationKey;

public class TranslationAppMain extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8925633482064125521L;
	private JTextArea originalTextArea = new JTextArea();
	private JTextArea translatedTextArea = new JTextArea();
	private JFileChooser chooser = new JFileChooser();
	private JLabel logLabel = new JLabel("Evenements...");
	private JMenuBar menuBar = new JMenuBar();
	private JTable table;
	private JScrollPane scrollPane;
	private JProgressBar progressBar = new JProgressBar(0, 100);
	private JCheckBox blankTranslateCheckbox = new JCheckBox();

	private File sourceDirectory;
	private String[] translationFiles;

	private Vector<Vector<String>> tableData;

	private String original_language;
	private String translation_language;

	private XPathFactory xFactory = XPathFactory.instance();
	private Document currentDocument;

	private TranslationAppConfigurationManager conf;
	private String currentEditedFile;
	private LoadFileTask loadFileTask;
	private JTextArea hintArea;
	private JTextField idField;
	private AboutPage aboutPage;
	private PropertiesPage propertiesPage;
	private GridBagConstraints c_1;
	private GridBagConstraints c_2;
	private GridBagConstraints c_3;
	private GridBagConstraints c_4;
	private GridBagConstraints c_5;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.out.println("Lancement de l'application...");
					TranslationAppMain window = new TranslationAppMain();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TranslationAppMain() {
		initialize();
	}

	public void initializeProperties() {
		conf = TranslationAppConfigurationManager.getInstance();
		conf.load();

		sourceDirectory = new File(
				conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION));

		original_language = conf
				.getProperty(ConfigurationKey.DEFAULT_ORIGINAL_LANGUAGE);
		translation_language = conf
				.getProperty(ConfigurationKey.TRANSLATED_LANGUAGE);

		Boolean blankOnly = conf.getBoolean(
				ConfigurationKey.DEFAULT_BLANK_ONLY, false);
		blankTranslateCheckbox.setSelected(blankOnly);

		Translate.setClientId(conf
				.getProperty(ConfigurationKey.AZURE_CLIENT_ID));
		Translate.setClientSecret(conf
				.getProperty(ConfigurationKey.AZURE_CLIENT_SECRET));

		if (!sourceDirectory.exists()) {
			selectFileFolder();
		}
	}

	private void initialize() {
		initializeProperties();
		loadTranslationFiles();

		// Configure this
		this.setBounds(0, 0, 800, 700);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
				/ 2 - this.getSize().height / 2 - 150);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);

		aboutPage = new AboutPage();
		propertiesPage = new PropertiesPage(this);

		// Configure Shared Action Listeners
		ActionListener save = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				saveTranslation();
			}
		};

		ActionListener open = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectFileFolder();
			}
		};

		ActionListener clearTranslation = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				deleteTranslation();
			}
		};

		ActionListener reloadFile = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeFile(currentEditedFile,
						blankTranslateCheckbox.isSelected());
			}
		};

		Action selectPreviousEntry = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				int currentSelected = table.getSelectedRow();
				if (currentSelected - 1 < 0) {
					scrollToRow(table.getRowCount() - 1);
				} else {
					scrollToRow(currentSelected - 1);
				}
			}
		};

		Action selectNextEntry = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				int currentSelected = table.getSelectedRow();
				if (currentSelected + 1 >= table.getRowCount()) {
					scrollToRow(0);
				} else {
					scrollToRow(currentSelected + 1);
				}
			}
		};

		ActionListener acceptTranslationAction = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				acceptTranslation();
			}
		};

		Action acceptTranslationHintAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				translatedTextArea.setText(hintArea.getText());
			}
		};

		Action showHintAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				try {
					hintArea.setBackground(new Color(224, 224, 224));
					hintArea.setText(Translate.execute(originalTextArea
							.getText(), Language.valueOf(original_language
							.toUpperCase()), Language
							.valueOf(translation_language.toUpperCase())));
					hintArea.setBackground(new Color(229, 255, 204));
				} catch (Exception e1) {
					e1.printStackTrace();
					hintArea.setText("Impossible de communiquer avec l'api d'aide \u00e0 la traduction.");
					hintArea.setBackground(new Color(255, 160, 158));
				}
			}
		};

		// Configure Menu

		// FICHIER
		JMenu fileMenu = new JMenu("Fichier");
		JMenuItem openFile = new JMenuItem("Dossier...", new ImageIcon(
				getClass().getResource("/images/folder.png")));
		openFile.setPreferredSize(new Dimension(200, 20));
		openFile.setToolTipText("S\u00e9lectionner un nouveau dossier de travail.");
		openFile.addActionListener(open);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		JMenuItem saveItem = new JMenuItem("Sauvegarder", new ImageIcon(
				getClass().getResource("/images/disk.png")));
		saveItem.setToolTipText("Sauvegarder la traduction dans le fichier s\u00e9lectionn\u00e9");
		saveItem.addActionListener(save);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		JMenuItem reloadItem = new JMenuItem("Recharger", new ImageIcon(
				getClass().getResource("/images/arrow_refresh.png")));
		reloadItem.addActionListener(reloadFile);
		reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		JMenuItem settingsItem = new JMenuItem("Configurer...", new ImageIcon(
				getClass().getResource("/images/wrench_orange.png")));
		settingsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				propertiesPage.setVisible(true);
			}
		});

		JMenuItem exit = new JMenuItem("Quitter");
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		fileMenu.add(openFile);
		fileMenu.add(saveItem);
		fileMenu.add(reloadItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(settingsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exit);
		menuBar.add(fileMenu);

		// MODIFIER
		JMenu modifier = new JMenu("Modifier");
		menuBar.add(modifier);

		JMenuItem acceptTranslationItem = new JMenuItem(
				"Valider la Traduction", new ImageIcon(getClass().getResource(
						"/images/accept.png")));
		acceptTranslationItem.setPreferredSize(new Dimension(300, 20));
		acceptTranslationItem.addActionListener(acceptTranslationAction);
		acceptTranslationItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, 0));
		modifier.add(acceptTranslationItem);

		JMenuItem clearTranslationItem = new JMenuItem(
				"Supprimer la Traduction", new ImageIcon(getClass()
						.getResource("/images/delete.png")));
		clearTranslationItem.setPreferredSize(new Dimension(250, 20));
		clearTranslationItem.addActionListener(clearTranslation);
		clearTranslationItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, KeyEvent.ALT_MASK));
		modifier.add(clearTranslationItem);

		JMenuItem translationHintMenu = new JMenuItem(
				"Suggestion de Traduction", new ImageIcon(getClass()
						.getResource("/images/lightbulb.png")));
		translationHintMenu.setPreferredSize(new Dimension(250, 20));
		translationHintMenu.addActionListener(showHintAction);
		translationHintMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, KeyEvent.ALT_MASK));
		modifier.add(translationHintMenu);

		JMenuItem acceptTranslationHintMenu = new JMenuItem(
				"Accepter Suggestion", new ImageIcon(getClass().getResource(
						"/images/lightbulb_add.png")));
		acceptTranslationHintMenu.setPreferredSize(new Dimension(250, 20));
		acceptTranslationHintMenu
				.addActionListener(acceptTranslationHintAction);
		acceptTranslationHintMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit()
						.getMenuShortcutKeyMask()));
		modifier.add(acceptTranslationHintMenu);
		modifier.add(new JSeparator());

		JMenuItem suivantMenu = new JMenuItem("Suivant", new ImageIcon(
				getClass().getResource("/images/arrow_down.png")));
		suivantMenu.addActionListener(selectNextEntry);
		suivantMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_PAGE_DOWN, 0));
		modifier.add(suivantMenu);

		JMenuItem precedentMenu = new JMenuItem("Precedent", new ImageIcon(
				getClass().getResource("/images/arrow_up.png")));
		precedentMenu.addActionListener(selectPreviousEntry);
		precedentMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_PAGE_UP, 0));
		modifier.add(precedentMenu);

		JMenu about = new JMenu("?");
		menuBar.add(about);
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				aboutPage.setVisible(true);
			}
		});
		about.add(aboutItem);

		this.setJMenuBar(menuBar);

		getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

		// Configure Toolbar
		JToolBar toolbar = new JToolBar("Outils");
		JButton openFolder = new JButton(new ImageIcon(getClass().getResource(
				"/images/folder.png")));
		openFolder
				.setToolTipText("S\u00e9lectionner un nouveau dossier de travail.");
		openFolder.addActionListener(open);
		toolbar.add(openFolder);

		JButton saveFile = new JButton(new ImageIcon(getClass().getResource(
				"/images/disk.png")));
		saveFile.setToolTipText("Sauvegarder la traduction dans le fichier s\u00e9lectionn\u00e9");
		saveFile.addActionListener(save);
		toolbar.add(saveFile);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		toolbar.setFloatable(false);

		JButton copyToClipboard = new JButton(new ImageIcon(getClass()
				.getResource("/images/page_copy.png")));
		copyToClipboard
				.setToolTipText("Copier l'original dans le presse papier.");
		copyToClipboard.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Clipboard clip = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				StringSelection stringSelection = null;
				if (originalTextArea.getSelectedText() != null) {
					stringSelection = new StringSelection(originalTextArea
							.getSelectedText());
				} else {
					stringSelection = new StringSelection(originalTextArea
							.getText());
				}

				clip.setContents(stringSelection, null);
			}
		});

		toolbar.add(copyToClipboard);

		JButton reloadFileTool = new JButton(new ImageIcon(getClass()
				.getResource("/images/arrow_refresh.png")));
		reloadFileTool
				.setToolTipText("Recharger le fichier depuis le disque. Attention, perte de donn\u00e9es si non enregistr\u00e9es");
		reloadFileTool.addActionListener(reloadFile);

		toolbar.add(reloadFileTool);

		blankTranslateCheckbox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				conf.setProperty(ConfigurationKey.DEFAULT_BLANK_ONLY,
						String.valueOf(box.isSelected()));
				conf.save();
				changeFile(currentEditedFile, box.isSelected());
			}
		});
		blankTranslateCheckbox
				.setToolTipText("Recharger la page en n'affichant que les lignes non traduites (vides).");

		toolbar.add(blankTranslateCheckbox);

		JComboBox<String> fileSelector = new JComboBox<String>();
		fileSelector.setModel(new DefaultComboBoxModel<String>(
				getFileList(sourceDirectory)));
		fileSelector.setSelectedIndex(0);
		fileSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				changeFile((String) cb.getSelectedItem(),
						blankTranslateCheckbox.isSelected());
			}

		});

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		toolbar.add(separator);
		fileSelector.setToolTipText("Choisir le fichier \u00e0 traduire.");
		toolbar.add(fileSelector);
		toolbar.add(new JSeparator(JSeparator.VERTICAL));

		// Search area
		JButton searchButton = new JButton(new ImageIcon(getClass()
				.getResource("/images/magnifier.png")));

		final JTextField searchField = new JTextField();
		searchField.setToolTipText("Filtrer le tableau");
		toolbar.add(searchField);
		toolbar.add(searchButton);

		// Main Panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		getContentPane().add(panel, BorderLayout.CENTER);

		// Tableau
		table = new JTable(new DefaultTableModel()) {
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);

				try {
					tip = getValueAt(rowIndex, 2).toString();
				} catch (RuntimeException e1) {
					e1.printStackTrace();
				}

				return tip;
			}
		};

		// Filtrage
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				table.getModel());
		table.setRowSorter(sorter);
		Action searchAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = searchField.getText();
				if (text.length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(RowFilter.regexFilter(text));
				}
			}
		};
		searchButton.addActionListener(searchAction);
		InputMap searchInput = searchField.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		searchInput.put(enter, "text-submit");
		ActionMap searchActions = searchField.getActionMap();
		searchActions.put("text-submit", searchAction);

		// Add the table to a scrolling pane
		scrollPane = new JScrollPane(table);
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED,
				null, null, null, null));
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);

		GridBagLayout gbl_translationArea = new GridBagLayout();
		JPanel translationArea = new JPanel(gbl_translationArea);
		translationArea.setBorder(new SoftBevelBorder(BevelBorder.LOWERED,
				null, null, null, null));
		panel.add(translationArea);

		// Translation Toolbar
		JToolBar translationToolBar = new JToolBar("Translation Tools",
				JToolBar.VERTICAL);

		JButton previousItem = new JButton(new ImageIcon(getClass()
				.getResource("/images/arrow_up.png")));
		previousItem
				.setToolTipText("Aller \u00e0 la l'entr\u00e9e pr\u00e9c\u00e9dente.");
		previousItem.addActionListener(selectPreviousEntry);
		translationToolBar.add(previousItem);

		JButton nextItem = new JButton(new ImageIcon(getClass().getResource(
				"/images/arrow_down.png")));
		nextItem.setToolTipText("Aller \u00e0 la prochaine entr\u00e9e.");
		nextItem.addActionListener(selectNextEntry);
		translationToolBar.add(nextItem);
		translationToolBar.add(new JSeparator());

		JButton getHintBtn = new JButton(new ImageIcon(getClass().getResource(
				"/images/lightbulb.png")));
		getHintBtn
				.setToolTipText("Afficher une traduction provenant de Bing Translate");
		getHintBtn.addActionListener(showHintAction);
		translationToolBar.add(getHintBtn);
		translationToolBar.setFloatable(false);

		JButton copyHintBtn = new JButton(new ImageIcon(getClass().getResource(
				"/images/lightbulb_add.png")));
		copyHintBtn
				.setToolTipText("Copier la traduction propos\u00e9e dans la champ en cours.");
		copyHintBtn.addActionListener(acceptTranslationHintAction);
		translationToolBar.add(copyHintBtn);

		c_2 = new GridBagConstraints();
		c_2.anchor = GridBagConstraints.NORTHWEST;
		c_2.gridheight = 4;
		c_2.insets = new Insets(5, 0, 5, 0);
		c_2.gridx = 7;
		c_2.gridy = 0;
		translationArea.add(translationToolBar, c_2);

		idField = new JTextField();
		idField.setBackground(new Color(224, 224, 224));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.SOUTH;
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.BOTH;
		translationArea.add(idField, c);

		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new GridLayout(2, 1, 0, 5));

		c_1 = new GridBagConstraints();
		c_1.weightx = 15.0;
		c_1.weighty = 1.0;
		c_1.insets = new Insets(0, 5, 5, 5);
		c_1.fill = GridBagConstraints.BOTH;
		c_1.ipady = 50;
		c_1.gridheight = 7;
		c_1.gridwidth = 4;
		c_1.gridx = 0;
		c_1.gridy = 1;

		translationArea.add(translationPanel, c_1);
		originalTextArea.setWrapStyleWord(true);
		originalTextArea.setLineWrap(true);

		originalTextArea.setBackground(new Color(224, 224, 224));
		originalTextArea.setDisabledTextColor(Color.BLACK);
		originalTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		originalTextArea.setEnabled(false);
		translatedTextArea.setWrapStyleWord(true);

		translatedTextArea.setBackground(new Color(229, 255, 204));
		translatedTextArea.setLineWrap(true);
		translatedTextArea
				.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		translatedTextArea
				.setToolTipText("SHIFT+ENTER pour revenir \u00e0 la ligne.\n ENTER pour valider la traduction et passer automatiquement \u00e0 la suivante.\n PAGE-UP et PAGE-DOWN pour parcourir les \u00e9l\u00e9ments.");
		InputMap input = translatedTextArea.getInputMap();
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, "insert-break");
		input.put(enter, "text-submit");

		ActionMap actions = translatedTextArea.getActionMap();
		actions.put("text-submit", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				acceptTranslation();
			}
		});

		KeyStroke pageDown = KeyStroke.getKeyStroke("Page Down");
		KeyStroke pageUp = KeyStroke.getKeyStroke("Page Up");
		input.put(pageUp, "page-up");
		input.put(pageDown, "page-down");

		actions.put("page-up", selectPreviousEntry);
		actions.put("page-down", selectNextEntry);

		translationPanel.add(originalTextArea);
		translationPanel.add(translatedTextArea);

		hintArea = new JTextArea();
		hintArea.setBackground(new Color(224, 224, 224));
		hintArea.setLineWrap(true);
		hintArea.setDisabledTextColor(Color.BLACK);

		c_5 = new GridBagConstraints();
		c_5.weighty = 0.5;
		c_5.fill = GridBagConstraints.BOTH;
		c_5.gridwidth = 3;
		c_5.gridheight = 6;
		c_5.insets = new Insets(5, 5, 20, 5);
		c_5.gridx = 4;
		c_5.gridy = 0;
		translationArea.add(hintArea, c_5);

		JButton acceptTranslation = new JButton("Valider", new ImageIcon(
				getClass().getResource("/images/accept.png")));
		acceptTranslation.setToolTipText("Valider cette traduction.");
		acceptTranslation.addActionListener(acceptTranslationAction);
		c_3 = new GridBagConstraints();
		c_3.gridwidth = 2;
		c_3.weightx = 0.5;
		c_3.fill = GridBagConstraints.BOTH;
		c_3.insets = new Insets(0, 5, 5, 0);
		c_3.gridx = 4;
		c_3.gridy = 7;
		translationArea.add(acceptTranslation, c_3);

		JButton deleteTranslation = new JButton("Supprimer", new ImageIcon(
				getClass().getResource("/images/delete.png")));
		deleteTranslation.addActionListener(clearTranslation);
		deleteTranslation.setToolTipText("Supprimer cette traduction.");
		c_4 = new GridBagConstraints();
		c_4.weightx = 0.5;
		c_4.fill = GridBagConstraints.BOTH;
		c_4.insets = new Insets(0, 5, 5, 5);
		c_4.gridx = 6;
		c_4.gridy = 7;
		translationArea.add(deleteTranslation, c_4);

		// STATUS BAR
		JPanel statusBar = new JPanel(new GridLayout(1, 2));
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		statusBar.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		statusBar.add(logLabel);

		progressBar.setStringPainted(true);
		progressBar.setAlignmentX(JProgressBar.RIGHT_ALIGNMENT);
		statusBar.add(progressBar);
	}

	protected void deleteTranslation() {
		translatedTextArea.setText("");
		int currentSelected = table.getSelectedRow();
		String id = (String) table.getModel().getValueAt(currentSelected, 2);
		int subid = Integer.valueOf((String) table.getModel().getValueAt(
				currentSelected, 3));
		clearTranslationItem(id, subid);
		updateTranslationProgress();
	}

	private void clearTranslationItem(String id, int subid) {
		table.getModel().setValueAt("", table.getSelectedRow(), 1);

		System.out.println("Clearing the following translation id: " + id);

		// Save in dom document
		XPathExpression<Element> expression = xFactory.compile(
				"//language[@id='" + translation_language + "']/entry[@id='"
						+ id + "']", Filters.element());

		List<Element> el = expression.evaluate(currentDocument);
		el.get(subid).setText("");

		updateTranslationProgress();
	}

	protected void acceptTranslation() {
		int currentSelected = table.getSelectedRow();
		String id = (String) table.getModel().getValueAt(currentSelected, 2);
		int subid = Integer.valueOf((String) table.getModel().getValueAt(
				currentSelected, 3));

		saveTranslationItem(translatedTextArea.getText(), id, subid);

		if (currentSelected + 1 >= table.getRowCount()) {
			scrollToRow(0);
		} else {
			scrollToRow(currentSelected + 1);
		}
	}

	protected void saveTranslationItem(String text, String id, int subid) {
		table.getModel().setValueAt(translatedTextArea.getText(),
				table.getSelectedRow(), 1);

		if (text.isEmpty() || id.isEmpty()) {
			return;
		}

		System.out.println("Saving the following translation into id " + id
				+ " and subid " + subid);
		System.out.println(text);

		// Save in dom document
		XPathExpression<Element> expression = xFactory.compile(
				"//language[@id='" + translation_language + "']/entry[@id='"
						+ id + "']", Filters.element());

		List<Element> el = expression.evaluate(currentDocument);
		el.get(subid).setText(text);

		updateTranslationProgress();
	}

	private void scrollToRow(int newRow) {
		table.setRowSelectionInterval(newRow, newRow);
		table.scrollRectToVisible(new Rectangle(table.getCellRect(newRow, 0,
				true)));
	}

	private void initTable() {

		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		Vector<String> columnsNames = new Vector<String>();
		columnsNames.add("Text Original : " + original_language);
		columnsNames.add("Texte Traduit : " + translation_language);
		columnsNames.add("Id");
		columnsNames.add("SubId");

		dtm.setDataVector(tableData, columnsNames);
		table.removeColumn(table.getColumnModel().getColumn(3));

		// Configure some of JTable's paramters
		table.setShowHorizontalLines(false);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);

		// Change the selection colour
		table.setSelectionBackground(new Color(224, 224, 224));
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMinWidth(50);

		table.scrollRectToVisible(new Rectangle(table.getCellRect(0, 0, true)));

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						if (table.getSelectedRow() != -1) {
							originalTextArea.setText(table.getValueAt(
									table.getSelectedRow(), 0).toString());

							translatedTextArea.setText(table.getValueAt(
									table.getSelectedRow(), 1).toString());
							translatedTextArea.requestFocusInWindow();

							idField.setText(table.getValueAt(
									table.getSelectedRow(), 2).toString());
						}
					}
				});
	}

	private String[] getFileList(File sourceDirectory) {
		return sourceDirectory.list();
	}

	public void loadFileContent(File file, boolean blankOnly) {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		loadFileTask = new LoadFileTask(file, blankOnly);
		loadFileTask.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				int progress = loadFileTask.getProgress();
				if (progress < 100) {
					progressBar.setValue(progress);
				}
			}
		});
		loadFileTask.execute();
	}

	class LoadFileTask extends SwingWorker<Void, Void> {
		private File file;
		private boolean blankOnly;

		public LoadFileTask(File file, boolean blankOnly) {
			this.file = file;
			this.blankOnly = blankOnly;
			progressBar.setValue(0);
			setProgress(0);
		}

		@Override
		protected Void doInBackground() throws Exception {
			System.out.println("Loading translation file " + file);

			SAXBuilder builder = new SAXBuilder();
			logLabel.setText("Loading File...");

			try {
				currentDocument = (Document) builder.build(file);
				Element root = currentDocument.getRootElement();

				System.out.println("Parsing source language "
						+ original_language);

				XPathExpression<Element> browseOriginal = xFactory.compile(
						"//language[@id='" + original_language
								+ "']/entry[string-length(@id) > 0]/.[text()]",
						Filters.element());

				List<Element> originalElements = browseOriginal.evaluate(root);
				System.out.println("This file contains "
						+ originalElements.size() + " elements to translate.");

				logLabel.setText("Parsing " + originalElements.size()
						+ " elements...");

				tableData = new Vector<Vector<String>>();
				int i = 0;
				int subid = 0;
				String lastid = "";
				for (Element el : originalElements) {
					String id = el.getAttributeValue("id");

					List<Element> elems = xFactory.compile(
							"//language[@id='" + translation_language
									+ "']/entry[@id='" + id + "']",
							Filters.element()).evaluate(root);

					if (id.equals(lastid)) {
						subid++;
					} else {
						subid = 0;
					}

					if (!elems.get(subid).getText().isEmpty() && blankOnly) {
						continue;
					}

					Vector<String> vector = new Vector<String>();
					vector.add(el.getText());
					vector.add(elems.get(subid).getText());
					vector.add(id);
					vector.add(String.valueOf(subid));

					tableData.add(vector);

					Double progress = Math
							.ceil(((double) i / (double) originalElements
									.size()) * 100);

					setProgress(progress.intValue());
					i++;
					lastid = id;
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void done() {
			initTable();
			logLabel.setText("Done loading file !");
			updateTranslationProgress();
			TranslationAppMain.this.setCursor(null);
			table.setRowSelectionInterval(0, 0);
			super.done();
		}
	}

	protected void saveTranslation() {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		try {
			System.out.println("Sauvegarde du fichier "
					+ sourceDirectory.getAbsolutePath() + File.separatorChar
					+ currentEditedFile);
			xmlOutput.output(currentDocument,
					new FileWriter(sourceDirectory.getAbsolutePath()
							+ File.separatorChar + currentEditedFile));
		} catch (IOException e) {
			logLabel.setText("Erreur sauvegarde. Voir log.");
			e.printStackTrace();
		}
	}

	private void loadTranslationFiles() {
		translationFiles = sourceDirectory.list();
		if (translationFiles.length > 0) {
			changeFile(translationFiles[0], blankTranslateCheckbox.isSelected());
		}
	}

	protected void updateTranslationProgress() {
		if (currentDocument != null) {
			Double total = xFactory.compile(
					"count(//language[@id='" + original_language
							+ "']/entry[string-length(@id) > 0]/.[text()])",
					Filters.fdouble()).evaluateFirst(
					currentDocument.getRootElement());
			Double translated = xFactory.compile(
					"count(//language[@id='" + translation_language
							+ "']/entry[string-length(@id) > 0]/.[text()])",
					Filters.fdouble()).evaluateFirst(
					currentDocument.getRootElement());
			Double percent = Math.floor((translated / total) * 100);

			logLabel.setText("Translation Progress (" + translated.intValue()
					+ " / " + total.intValue() + ") :");
			progressBar.setValue(percent.intValue());
		}
	}

	protected void selectFileFolder() {
		chooser.setCurrentDirectory(sourceDirectory);
		chooser.setDialogTitle("S\u00e9lectionner le dossier localization \u00e0 traduire");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			sourceDirectory = chooser.getSelectedFile();
			conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
					(String) sourceDirectory.getPath());
			conf.save();
			System.out.println("Picked directory is : " + sourceDirectory);
		} else {
			System.out.println("No selection");
		}
	}

	protected void changeFile(String selectedItem, boolean blankOnly) {
		System.out.println("File edited is : " + selectedItem);
		this.setTitle("Traducteur Darkest Dungeon - " + selectedItem);
		currentEditedFile = selectedItem;
		loadFileContent(new File(sourceDirectory.getAbsolutePath()
				+ File.separatorChar + currentEditedFile), blankOnly);
	}
}
