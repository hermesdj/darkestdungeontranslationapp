package fr.hermesdj.java.darkestdungeontranslationapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

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
import javax.swing.JOptionPane;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import fr.hermesdj.java.darkestdungeontranslationapp.ConfigurationManager.ConfigurationKey;
import fr.hermesdj.java.darkestdungeontranslationapp.Localization.LocalizationKey;

public class TranslationAppMain extends JFrame {
    private static final long serialVersionUID = 8925633482064125521L;

    private static final Logger LOG = LogManager
	    .getLogger(TranslationAppMain.class);
    private final JTextArea originalTextArea = new JTextArea();
    private final JTextArea translatedTextArea = new JTextArea();
    private final JFileChooser chooser = new JFileChooser();
    private JLabel logLabel;
    private final JMenuBar menuBar = new JMenuBar();
    private JTable table;
    private JScrollPane scrollPane;
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JCheckBox blankTranslateCheckbox = new JCheckBox();

    private File sourceDirectory;
    private String[] translationFiles;

    private Vector<Vector<String>> tableData;

    private String original_language;
    private String translation_language;

    private final XPathFactory xFactory = XPathFactory.instance();
    private Document currentDocument;

    private ConfigurationManager conf;
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
    private Localization lang;

    private boolean isTranslatorAvailable = false;

    private final JMenuItem translateAllItem = new JMenuItem();

    private JMenuItem translationHintMenu;

    private JMenuItem acceptTranslationHintMenu;

    private JButton getHintBtn;

    private JButton copyHintBtn;

    protected boolean isTableColored = false;

    private JMenuItem cleanAllItem;

    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		try {
		    LOG.info("Launching main window...");
		    TranslationAppMain window = new TranslationAppMain();
		    window.setVisible(true);
		} catch (Exception e) {
		    LOG.error(e.getMessage(), e);
		    ;
		}
	    }
	});
    }

    public TranslationAppMain() {
	initialize();
	postInitialize();
    }

    private void postInitialize() {
	SwingWorker translatorWorker = new SwingWorker<Boolean, Void>() {

	    @Override
	    protected Boolean doInBackground() throws Exception {
		LOG.info("Testing if Bing Translator is available");
		Boolean isTranslatorAvailable;
		try {
		    Translate.execute("test string", Language.ENGLISH);
		    isTranslatorAvailable = true;
		} catch (Exception e) {
		    LOG.error("Unable to contact Translator Servers", e);
		    isTranslatorAvailable = false;
		    ;
		}

		return isTranslatorAvailable;
	    }

	    @Override
	    public void done() {
		try {
		    isTranslatorAvailable = get();
		    translateAllItem.setEnabled(isTranslatorAvailable);
		    translationHintMenu.setEnabled(isTranslatorAvailable);
		    acceptTranslationHintMenu.setEnabled(isTranslatorAvailable);
		    getHintBtn.setEnabled(isTranslatorAvailable);
		    copyHintBtn.setEnabled(isTranslatorAvailable);

		    if (isTranslatorAvailable) {
			LOG.info("Bing Translator is available.");
			translateAllItem
				.setToolTipText(lang
					.getString(LocalizationKey.MENU_EDIT_TRANSLATE_ALL_TOOLTIP));
		    } else {
			LOG.error("Bing Translator is not available.");
			translateAllItem
				.setToolTipText(lang
					.getString(LocalizationKey.MENU_EDIT_TRANSLATE_ALL_TOOLTIP_UNAVAILABLE));
		    }
		} catch (InterruptedException e) {
		    LOG.error(e.getMessage(), e);
		    ;
		} catch (ExecutionException e) {
		    LOG.error(e.getMessage(), e);
		    ;
		}
	    }

	};

	translatorWorker.execute();
    }

    public void initializeProperties() {
	conf = ConfigurationManager.getInstance();
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

	String proxyHost = conf.getProperty(ConfigurationKey.HTTP_PROXY_HOST);
	String proxyPort = conf.getProperty(ConfigurationKey.HTTP_PROXY_PORT);

	if (!proxyHost.isEmpty()) {
	    LOG.info("HTTP proxy set to " + proxyHost + ":" + proxyPort);
	    System.setProperty("http.proxyHost", proxyHost);
	    System.setProperty("http.proxyPort", proxyPort);
	    System.setProperty("https.proxyHost", proxyHost);
	    System.setProperty("https.proxyPort", proxyPort);
	}

	lang = Localization.getInstance();

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

	    @Override
	    public void actionPerformed(ActionEvent e) {
		saveTranslation();
	    }
	};

	ActionListener open = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		selectFileFolder();
	    }
	};

	ActionListener clearTranslation = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		deleteTranslation();
	    }
	};

	ActionListener reloadFile = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		changeFile(currentEditedFile,
			blankTranslateCheckbox.isSelected());
	    }
	};

	Action selectPreviousEntry = new AbstractAction() {

	    @Override
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

	    @Override
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

	    @Override
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

	    @Override
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
		    hintArea.setText(lang
			    .getString(LocalizationKey.BING_TRANSLATOR_ERROR));
		    hintArea.setBackground(new Color(255, 160, 158));
		}
	    }
	};

	Action translateAll = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		translateAll();
	    }

	};

	Action cleanAll = new AbstractAction() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		cleanAll();
	    }
	};

	// Configure Menu

	// FICHIER
	JMenu fileMenu = new JMenu(lang.getString(LocalizationKey.MENU_FILE));
	JMenuItem openFile = new JMenuItem(
		lang.getString(LocalizationKey.MENU_FILE_FOLDER),
		new ImageIcon(getClass().getResource("/images/folder.png")));
	openFile.setPreferredSize(new Dimension(200, 20));
	openFile.setToolTipText(lang
		.getString(LocalizationKey.MENU_FILE_FOLDER_TOOLTIP));
	openFile.addActionListener(open);
	openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
		.getDefaultToolkit().getMenuShortcutKeyMask()));

	JMenuItem saveItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_SAVE), new ImageIcon(
			getClass().getResource("/images/disk.png")));
	saveItem.setToolTipText(lang
		.getString(LocalizationKey.MENU_SAVE_TOOLTIP));
	saveItem.addActionListener(save);
	saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
		.getDefaultToolkit().getMenuShortcutKeyMask()));

	JMenuItem reloadItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_REFRESH), new ImageIcon(
			getClass().getResource("/images/arrow_refresh.png")));
	reloadItem.addActionListener(reloadFile);
	reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit
		.getDefaultToolkit().getMenuShortcutKeyMask()));

	JMenuItem settingsItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_SETTINGS), new ImageIcon(
			getClass().getResource("/images/wrench_orange.png")));
	settingsItem.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		propertiesPage.setVisible(true);
	    }
	});

	JMenuItem exit = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EXIT));
	exit.addActionListener(new ActionListener() {

	    @Override
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
	JMenu modifier = new JMenu(lang.getString(LocalizationKey.MENU_EDIT));
	menuBar.add(modifier);

	JMenuItem acceptTranslationItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_VALIDATE_TRANSLATION),
		new ImageIcon(getClass().getResource("/images/accept.png")));
	acceptTranslationItem.setPreferredSize(new Dimension(300, 20));
	acceptTranslationItem.addActionListener(acceptTranslationAction);
	acceptTranslationItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_ENTER, 0));
	modifier.add(acceptTranslationItem);

	JMenuItem clearTranslationItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_DELETE_TRANSLATION),
		new ImageIcon(getClass().getResource("/images/delete.png")));
	clearTranslationItem.setPreferredSize(new Dimension(250, 20));
	clearTranslationItem.addActionListener(clearTranslation);
	clearTranslationItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_DELETE, KeyEvent.ALT_MASK));
	modifier.add(clearTranslationItem);

	translationHintMenu = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_TRANSLATION_HINT),
		new ImageIcon(getClass().getResource("/images/lightbulb.png")));
	translationHintMenu.setPreferredSize(new Dimension(250, 20));
	translationHintMenu.addActionListener(showHintAction);
	translationHintMenu.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_ENTER, KeyEvent.ALT_MASK));
	translationHintMenu.setEnabled(false);
	modifier.add(translationHintMenu);

	acceptTranslationHintMenu = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_ACCEPT_TRANSLATION),
		new ImageIcon(getClass().getResource(
			"/images/lightbulb_add.png")));
	acceptTranslationHintMenu.setPreferredSize(new Dimension(250, 20));
	acceptTranslationHintMenu
		.addActionListener(acceptTranslationHintAction);
	acceptTranslationHintMenu.setEnabled(false);
	acceptTranslationHintMenu.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
	modifier.add(acceptTranslationHintMenu);
	modifier.add(new JSeparator());

	JMenuItem suivantMenu = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_NEXT), new ImageIcon(
			getClass().getResource("/images/arrow_down.png")));
	suivantMenu.addActionListener(selectNextEntry);
	suivantMenu.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_PAGE_DOWN, 0));
	modifier.add(suivantMenu);

	JMenuItem precedentMenu = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_PREVIOUS),
		new ImageIcon(getClass().getResource("/images/arrow_up.png")));
	precedentMenu.addActionListener(selectPreviousEntry);
	precedentMenu.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_PAGE_UP, 0));
	modifier.add(precedentMenu);
	modifier.add(new JSeparator());

	translateAllItem.setText(lang
		.getString(LocalizationKey.MENU_EDIT_TRANSLATE_ALL));
	translateAllItem.setIcon(new ImageIcon(getClass().getResource(
		"/images/lightning.png")));
	translateAllItem.addActionListener(translateAll);
	translateAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,
		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	translateAllItem.setEnabled(isTranslatorAvailable);
	modifier.add(translateAllItem);

	cleanAllItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_EDIT_CLEAN_ALL),
		new ImageIcon(getClass().getResource(
			"/images/table_relationship.png")));
	cleanAllItem.setToolTipText(lang
		.getString(LocalizationKey.MENU_EDIT_CLEAN_ALL_TOOLTIP));
	cleanAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,
		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	cleanAllItem.addActionListener(cleanAll);
	modifier.add(cleanAllItem);

	JMenu about = new JMenu(lang.getString(LocalizationKey.MENU_ABOUT));
	menuBar.add(about);
	JMenuItem aboutItem = new JMenuItem(
		lang.getString(LocalizationKey.MENU_ABOUT_SOFTWARE));
	aboutItem.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		aboutPage.setVisible(true);
	    }
	});
	about.add(aboutItem);

	this.setJMenuBar(menuBar);

	getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

	// Configure Toolbar
	JToolBar toolbar = new JToolBar(
		lang.getString(LocalizationKey.TOOLBAR_TITLE));
	JButton openFolder = new JButton(new ImageIcon(getClass().getResource(
		"/images/folder.png")));
	openFolder.setToolTipText(lang
		.getString(LocalizationKey.OPEN_FOLDER_TOOLTIP));
	openFolder.addActionListener(open);
	toolbar.add(openFolder);

	JButton saveFile = new JButton(new ImageIcon(getClass().getResource(
		"/images/disk.png")));
	saveFile.setToolTipText(lang.getString(LocalizationKey.SAVE_TOOLTIP));
	saveFile.addActionListener(save);
	toolbar.add(saveFile);
	getContentPane().add(toolbar, BorderLayout.NORTH);
	toolbar.setFloatable(false);

	JButton copyToClipboard = new JButton(new ImageIcon(getClass()
		.getResource("/images/page_copy.png")));
	copyToClipboard.setToolTipText(lang
		.getString(LocalizationKey.COPY_ORIGINAL_TOOLTIP));
	copyToClipboard.addActionListener(new ActionListener() {

	    @Override
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
	reloadFileTool.setToolTipText(lang
		.getString(LocalizationKey.MENU_REFRESH_TOOLTIP));
	reloadFileTool.addActionListener(reloadFile);

	toolbar.add(reloadFileTool);

	blankTranslateCheckbox.setToolTipText(lang
		.getString(LocalizationKey.BLANK_ONLY_TOOLTIP));
	blankTranslateCheckbox.setSelectedIcon(new ImageIcon(getClass()
		.getResource("/images/tag_green.png")));
	blankTranslateCheckbox.setIcon(new ImageIcon(getClass().getResource(
		"/images/tag_red.png")));
	toolbar.add(blankTranslateCheckbox);

	JCheckBox colorTable = new JCheckBox();
	colorTable.setToolTipText(lang
		.getString(LocalizationKey.TOOLBAR_COLOR_TABLE_TOOLTIP));
	colorTable.setIcon(new ImageIcon(getClass().getResource(
		"/images/flag_red.png")));
	colorTable.setSelectedIcon(new ImageIcon(getClass().getResource(
		"/images/flag_green.png")));
	colorTable.setSelected(isTableColored);

	colorTable.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		isTableColored = ((JCheckBox) e.getSource()).isSelected();
		table.repaint();
	    }
	});

	toolbar.add(colorTable);

	JComboBox<String> fileSelector = new JComboBox<String>();
	fileSelector.setModel(new DefaultComboBoxModel<String>(
		getFileList(sourceDirectory)));
	fileSelector.setSelectedIndex(0);
	fileSelector.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		JComboBox<String> cb = (JComboBox<String>) e.getSource();
		changeFile((String) cb.getSelectedItem(),
			blankTranslateCheckbox.isSelected());
	    }

	});

	JSeparator separator = new JSeparator();
	separator.setOrientation(SwingConstants.VERTICAL);
	toolbar.add(separator);
	fileSelector.setToolTipText(lang
		.getString(LocalizationKey.FILE_SELECTOR_TOOLTIP));
	toolbar.add(fileSelector);
	toolbar.add(new JSeparator(JSeparator.VERTICAL));

	// Search area
	JButton searchButton = new JButton(new ImageIcon(getClass()
		.getResource("/images/magnifier.png")));

	final JTextField searchField = new JTextField();
	searchField.setToolTipText(lang
		.getString(LocalizationKey.TABLE_FILTER_TOOLTIP));
	toolbar.add(searchField);
	toolbar.add(searchButton);

	// Main Panel
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	panel.setBorder(new EmptyBorder(1, 1, 1, 1));
	getContentPane().add(panel, BorderLayout.CENTER);

	// Tableau
	table = new JTable(new DefaultTableModel()) {
	    @Override
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

	    @Override
	    public Component prepareRenderer(TableCellRenderer renderer,
		    int row, int column) {
		Component c = super.prepareRenderer(renderer, row, column);

		if (!isRowSelected(row) && isTableColored) {
		    c.setBackground(getBackground());
		    int modelRow = convertRowIndexToModel(row);
		    String value = (String) getModel().getValueAt(modelRow, 1);
		    if (value.trim().equals("")) {
			c.setBackground(conf
				.getColor(ConfigurationKey.DEFAULT_EMPTY_ROW_COLOR));
		    } else {
			c.setBackground(conf
				.getColor(ConfigurationKey.DEFAULT_TRANSLATED_ROW_COLOR));
		    }
		}

		if (!isTableColored && !isRowSelected(row)) {
		    c.setBackground(null);
		}

		return c;
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
		if (table.getModel().getRowCount() > 0) {
		    try {
			table.getSelectionModel().setSelectionInterval(0, 0);
			scrollToRow(0);
		    } catch (ArrayIndexOutOfBoundsException e1) {
			LOG.warn("Cannot select first row, the table is not displaying any... ");
		    }
		}
	    }
	};

	blankTranslateCheckbox.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		JCheckBox box = (JCheckBox) e.getSource();

		if (box.isSelected()) {
		    sorter.setRowFilter(new RowFilter<Object, Object>() {

			@Override
			public boolean include(Entry entry) {
			    return ((String) entry.getValue(1)).trim().equals(
				    "");
			}
		    });
		} else {
		    sorter.setRowFilter(null);
		}
		if (table.getModel().getRowCount() > 0) {
		    try {
			table.getSelectionModel().setSelectionInterval(0, 0);
			scrollToRow(0);
		    } catch (ArrayIndexOutOfBoundsException e1) {
			LOG.warn("Cannot select first row, the table is not displaying any... ");
		    }
		}
	    }
	});

	if (blankTranslateCheckbox.isSelected()) {
	    sorter.setRowFilter(new RowFilter<Object, Object>() {

		@Override
		public boolean include(Entry entry) {
		    return ((String) entry.getValue(1)).trim().equals("");
		}
	    });
	} else {
	    sorter.setRowFilter(null);
	}

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
	JToolBar translationToolBar = new JToolBar(
		lang.getString(LocalizationKey.TRANSLATION_TOOLBAR_TITLE),
		JToolBar.VERTICAL);

	JButton previousItem = new JButton(new ImageIcon(getClass()
		.getResource("/images/arrow_up.png")));
	previousItem.setToolTipText(lang
		.getString(LocalizationKey.PREVIOUS_BUTTON_TOOLTIP));
	previousItem.addActionListener(selectPreviousEntry);
	translationToolBar.add(previousItem);

	JButton nextItem = new JButton(new ImageIcon(getClass().getResource(
		"/images/arrow_down.png")));
	nextItem.setToolTipText(lang
		.getString(LocalizationKey.NEXT_BUTTON_TOOLTIP));
	nextItem.addActionListener(selectNextEntry);
	translationToolBar.add(nextItem);
	translationToolBar.add(new JSeparator());

	getHintBtn = new JButton(new ImageIcon(getClass().getResource(
		"/images/lightbulb.png")));
	getHintBtn.setToolTipText(lang
		.getString(LocalizationKey.HINT_BUTTON_TOOLTIP));
	getHintBtn.setEnabled(false);
	getHintBtn.addActionListener(showHintAction);
	translationToolBar.add(getHintBtn);
	translationToolBar.setFloatable(false);

	copyHintBtn = new JButton(new ImageIcon(getClass().getResource(
		"/images/lightbulb_add.png")));
	copyHintBtn.setEnabled(false);

	copyHintBtn.setToolTipText(lang
		.getString(LocalizationKey.ACCEPT_HINT_BUTTON_TOOLTIP));
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
	idField.setEditable(false);
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
	originalTextArea.setEditable(false);
	originalTextArea.setWrapStyleWord(true);
	originalTextArea.setLineWrap(true);

	originalTextArea.setBackground(new Color(224, 224, 224));
	originalTextArea.setDisabledTextColor(Color.BLACK);
	originalTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	translatedTextArea.setWrapStyleWord(true);

	translatedTextArea.setBackground(new Color(229, 255, 204));
	translatedTextArea.setLineWrap(true);
	translatedTextArea
		.setBorder(BorderFactory.createLineBorder(Color.GRAY));

	translatedTextArea.setToolTipText(lang
		.getString(LocalizationKey.TRANSLATION_AREA_TOOLTIP));
	InputMap input = translatedTextArea.getInputMap();
	KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
	input.put(shiftEnter, "insert-break");
	input.put(enter, "text-submit");

	ActionMap actions = translatedTextArea.getActionMap();
	actions.put("text-submit", new AbstractAction() {
	    @Override
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
	hintArea.setEditable(false);
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

	JButton acceptTranslation = new JButton(
		lang.getString(LocalizationKey.ACCEPT_TRANSLATION_BUTTON_LABEL),
		new ImageIcon(getClass().getResource("/images/accept.png")));
	acceptTranslation.setToolTipText(lang
		.getString(LocalizationKey.ACCEPT_TRANSLATION_BUTTON_TOOLTIP));
	acceptTranslation.addActionListener(acceptTranslationAction);
	c_3 = new GridBagConstraints();
	c_3.gridwidth = 2;
	c_3.weightx = 0.5;
	c_3.fill = GridBagConstraints.BOTH;
	c_3.insets = new Insets(0, 5, 5, 0);
	c_3.gridx = 4;
	c_3.gridy = 7;
	translationArea.add(acceptTranslation, c_3);

	JButton deleteTranslation = new JButton(
		lang.getString(LocalizationKey.DELETE_TRANSLATION_BUTTON_LABEL),
		new ImageIcon(getClass().getResource("/images/delete.png")));
	deleteTranslation.addActionListener(clearTranslation);
	deleteTranslation.setToolTipText(lang
		.getString(LocalizationKey.DELETE_TRANSLATION_BUTTON_TOOLTIP));
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

	logLabel = new JLabel(lang.getString(LocalizationKey.STATUS_EVENTS));
	statusBar.add(logLabel);

	progressBar.setStringPainted(true);
	progressBar.setAlignmentX(JProgressBar.RIGHT_ALIGNMENT);
	statusBar.add(progressBar);
    }

    protected void cleanAll() {
	Object[] options = {
		lang.getString(LocalizationKey.POPUP_CHANGE_LANGUAGE_YES),
		lang.getString(LocalizationKey.POPUP_CHANGE_LANGUAGE_NO) };
	int n = JOptionPane.showOptionDialog(this,
		lang.getString(LocalizationKey.POPUP_CLEAN_ALL_DESC),
		lang.getString(LocalizationKey.POPUP_CLEAN_ALL_TITLE),
		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		options, options[0]);

	if (n == 0) {
	    final CleanAllTask task = new CleanAllTask(
		    (DefaultTableModel) table.getModel(), currentDocument,
		    translation_language);
	    task.addPropertyChangeListener(new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		    int progress = task.getProgress();

		    if (evt.getPropertyName().equals("state")) {
			if (((SwingWorker.StateValue) evt.getNewValue())
				.equals(SwingWorker.StateValue.DONE)) {
			    try {
				setCursor(null);
				JOptionPane.showMessageDialog(
					TranslationAppMain.this,
					String.format(
						lang.getString(LocalizationKey.POPUP_CLEAN_ALL_RESULT),
						task.get()));
			    } catch (HeadlessException | InterruptedException
				    | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			    }
			    updateTranslationProgress();
			}
		    }

		    if (!task.isDone()) {
			progressBar.setValue(progress);
		    }
		}
	    });

	    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    progressBar.setValue(0);
	    task.execute();
	}
    }

    protected void translateAll() {
	Object[] options = {
		lang.getString(LocalizationKey.POPUP_CHANGE_LANGUAGE_YES),
		lang.getString(LocalizationKey.POPUP_CHANGE_LANGUAGE_NO) };
	int n = JOptionPane.showOptionDialog(this,
		lang.getString(LocalizationKey.POPUP_TRANSLATE_ALL_DESC),
		lang.getString(LocalizationKey.POPUP_TRANSLATE_ALL_TITLE),
		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		options, options[0]);

	if (n == 0) {
	    final TranslateAllTask task = new TranslateAllTask(
		    (DefaultTableModel) table.getModel(), currentDocument,
		    original_language, translation_language);
	    task.addPropertyChangeListener(new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		    int progress = task.getProgress();

		    if (evt.getPropertyName().equals("state")) {
			if (((SwingWorker.StateValue) evt.getNewValue())
				.equals(SwingWorker.StateValue.DONE)) {
			    try {
				setCursor(null);
				JOptionPane.showMessageDialog(
					TranslationAppMain.this,
					String.format(
						lang.getString(LocalizationKey.POPUP_TRANSLATE_ALL_RESULT),
						task.get()));
			    } catch (HeadlessException | InterruptedException
				    | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			    }
			    updateTranslationProgress();
			}
		    }

		    if (!task.isDone()) {
			progressBar.setValue(progress);
		    }
		}
	    });

	    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    progressBar.setValue(0);
	    task.execute();
	}
    }

    protected void deleteTranslation() {
	translatedTextArea.setText("");

	String id = idField.getText();
	Integer subid = Integer.valueOf((String) table.getModel().getValueAt(
		table.convertRowIndexToModel(table.getSelectedRow()), 3));
	clearTranslationItem(id, subid);
	updateTranslationProgress();
    }

    private void clearTranslationItem(String id, int subid) {
	table.getModel().setValueAt("", table.getSelectedRow(), 1);

	LOG.info("Clearing the following translation id: " + id);

	// Save in dom document
	XPathExpression<Element> expression = xFactory.compile(
		"//language[@id='" + translation_language + "']/entry[@id='"
			+ id + "']", Filters.element());

	List<Element> el = expression.evaluate(currentDocument);
	el.get(subid).setText("");

	updateTranslationProgress();
    }

    protected void acceptTranslation() {
	int currentSelected = table.convertRowIndexToModel(table
		.getSelectedRow());

	LOG.debug("Current row selected in model is " + currentSelected);
	LOG.debug("Current row selected in table is " + table.getSelectedRow());

	String id = (String) table.getModel().getValueAt(currentSelected, 2);
	int subid = Integer.valueOf((String) table.getModel().getValueAt(
		currentSelected, 3));

	LOG.debug("id : " + id + ":" + subid);

	saveTranslationItem(translatedTextArea.getText(), id, subid);

	if (currentSelected + 1 >= table.getRowCount()) {
	    scrollToRow(0);
	} else {
	    scrollToRow(currentSelected + 1);
	}
    }

    protected void saveTranslationItem(String text, String id, int subid) {
	table.getModel().setValueAt(translatedTextArea.getText(),
		table.convertRowIndexToModel(table.getSelectedRow()), 1);

	if (text.isEmpty() || id.isEmpty()) {
	    return;
	}

	LOG.info("Saving the following translation into id " + id
		+ " and subid " + subid);
	LOG.info(text);

	// Save in dom document
	XPathExpression<Element> expression = xFactory.compile(
		"//language[@id='" + translation_language + "']/entry[@id='"
			+ id + "']", Filters.element());

	List<Element> el = expression.evaluate(currentDocument);

	el.get(subid).setContent(
		new CDATA(new String(text.getBytes(), StandardCharsets.UTF_8)));

	updateTranslationProgress();
    }

    private void scrollToRow(int newRow) {
	table.setRowSelectionInterval(newRow, newRow);
	table.scrollRectToVisible(new Rectangle(table.getCellRect(newRow, 0,
		true)));
    }

    public void initTable(Vector<Vector<String>> tableData) {

	DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	Vector<String> columnsNames = new Vector<String>();
	columnsNames.add(lang
		.getString(LocalizationKey.TABLE_ORIGINAL_COLUMN_LABEL)
		+ original_language);
	columnsNames.add(lang
		.getString(LocalizationKey.TABLE_TRANSLATED_COLUMN_LABEL)
		+ translation_language);
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

		    @Override
		    public void valueChanged(ListSelectionEvent e) {
			if (table.getSelectedRow() != -1) {
			    originalTextArea.setText(table.getValueAt(
				    table.getSelectedRow(), 0).toString());

			    translatedTextArea.setText(table.getValueAt(
				    table.getSelectedRow(), 1).toString());
			    translatedTextArea.requestFocusInWindow();

			    idField.setText(table.getValueAt(
				    table.getSelectedRow(), 2).toString());

			    LOG.debug("Current row selected in model is "
				    + table.convertRowIndexToModel(table
					    .getSelectedRow()));
			    LOG.debug("Current row selected in table is "
				    + table.getSelectedRow());
			    LOG.debug("Data is "
				    + idField.getText()
				    + ":"
				    + table.getModel().getValueAt(
					    table.getSelectedRow(), 3));
			}
		    }
		});

	if (table.getModel().getRowCount() > 0) {
	    table.setRowSelectionInterval(0, 0);
	}
    }

    private String[] getFileList(File sourceDirectory) {
	return sourceDirectory.list();
    }

    public void loadFileContent(File file, boolean blankOnly) {
	this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	loadFileTask = new LoadFileTask(this, file, progressBar, tableData,
		currentDocument, original_language, translation_language);
	loadFileTask.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		int progress = loadFileTask.getProgress();
		if (progress < 100) {
		    progressBar.setValue(progress);
		}
	    }
	});
	showLog("Loading file content...");
	loadFileTask.execute();
    }

    protected void saveTranslation() {
	XMLOutputter xmlOutput = new XMLOutputter();
	xmlOutput.setFormat(Format.getPrettyFormat());
	try {
	    LOG.info("Saving file " + sourceDirectory.getAbsolutePath()
		    + File.separatorChar + currentEditedFile);
	    xmlOutput.output(currentDocument,
		    new FileWriter(sourceDirectory.getAbsolutePath()
			    + File.separatorChar + currentEditedFile));
	} catch (IOException e) {
	    showLog("File saving error, please check logfile");
	    LOG.error(e.getMessage(), e);
	}
    }

    public void showLog(String string) {
	if (logLabel != null) {
	    logLabel.setText(string);
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

	    showLog(lang.getString(LocalizationKey.STATUS_TRANSLATION_PROGRESS)
		    + " " + percent.intValue() + "%" + " ("
		    + translated.intValue() + " / " + total.intValue() + ")");
	    progressBar.setValue(percent.intValue());
	}
    }

    protected void selectFileFolder() {
	chooser.setCurrentDirectory(sourceDirectory);
	chooser.setDialogTitle(lang
		.getString(LocalizationKey.FILE_CHOOSER_TITLE));
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setAcceptAllFileFilterUsed(false);

	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	    sourceDirectory = chooser.getSelectedFile();
	    conf.setProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION,
		    sourceDirectory.getPath());
	    conf.save();
	    LOG.info("Picked directory is : " + sourceDirectory);
	} else {
	    if (conf.getProperty(ConfigurationKey.TRANSLATION_FILES_LOCATION)
		    .isEmpty()) {
		System.exit(0);
	    }
	}
    }

    protected void changeFile(String selectedItem, boolean blankOnly) {
	LOG.info("File edited is : " + selectedItem);
	this.setTitle(lang.getString(LocalizationKey.MAIN_FRAME_TITLE)
		+ selectedItem);
	currentEditedFile = selectedItem;
	loadFileContent(new File(sourceDirectory.getAbsolutePath()
		+ File.separatorChar + currentEditedFile), blankOnly);
    }

    public void doneLoadingFile(Vector<Vector<String>> tableData,
	    Document currentDocument) {
	this.currentDocument = currentDocument;
	this.tableData = tableData;

	initTable(tableData);
	showLog(lang.getString(LocalizationKey.STATUS_DONE_LOADING));
	updateTranslationProgress();
	setCursor(null);
	LOG.info("Done loading file content !");

    }
}
