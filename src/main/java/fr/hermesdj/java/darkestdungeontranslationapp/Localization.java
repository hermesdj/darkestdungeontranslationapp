package fr.hermesdj.java.darkestdungeontranslationapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.hermesdj.java.darkestdungeontranslationapp.ConfigurationManager.ConfigurationKey;

public class Localization {
    private static Localization INSTANCE;
    private static final Logger LOG = LogManager.getLogger(Localization.class);

    public List<Locale> availableLocales = new ArrayList<Locale>();
    private Locale currentLocale = new Locale("fr", "FR");
    private ResourceBundle currentResourceBundle;

    private Localization() {
	availableLocales.add(new Locale("fr", "FR"));
	availableLocales.add(new Locale("en", "EN"));

	ConfigurationManager conf = ConfigurationManager.getInstance();
	String propLocale = conf
		.getProperty(ConfigurationKey.DEFAULT_APPLICATION_LANGUAGE);
	String[] localeTags = propLocale.split("_");
	LOG.info("Stored locale is " + propLocale);

	setCurrentLocale(new Locale(localeTags[0], localeTags[1]));
	LOG.info("Using locale " + currentLocale.getLanguage());

	currentResourceBundle = ResourceBundle.getBundle("languages.messages",
		currentLocale);
    }

    public static Localization getInstance() {
	if (INSTANCE == null) {
	    INSTANCE = new Localization();
	}

	return INSTANCE;
    }

    public String getString(LocalizationKey key) {
	return currentResourceBundle.getString(key.name());
    }

    public void setCurrentLocale(Locale locale) {
	currentLocale = locale;
	Locale.setDefault(locale);
	LOG.info("Changed current locale to " + locale.getDisplayLanguage());
    }

    public Locale getCurrentLocale() {
	return currentLocale;
    }

    public String[] getAvailableLocales() {
	String[] locales = new String[availableLocales.size()];
	int i = 0;
	for (Locale l : availableLocales) {
	    locales[i] = l.getDisplayLanguage();
	    i++;
	}
	return locales;
    }

    public enum LocalizationKey {
	STATUS_EVENTS,
	STATUS_DONE_LOADING,
	FILE_CHOOSER_TITLE,
	MAIN_FRAME_TITLE,
	BING_TRANSLATOR_ERROR,

	/* MENU ELEMENTS */
	MENU_FILE,
	MENU_FILE_FOLDER,
	MENU_FILE_FOLDER_TOOLTIP,
	MENU_SAVE,
	MENU_SAVE_TOOLTIP,
	MENU_REFRESH,
	MENU_REFRESH_TOOLTIP,
	MENU_SETTINGS,
	MENU_EXIT,
	MENU_EDIT,
	MENU_EDIT_VALIDATE_TRANSLATION,
	MENU_EDIT_DELETE_TRANSLATION,
	MENU_EDIT_TRANSLATION_HINT,
	MENU_EDIT_ACCEPT_TRANSLATION,
	MENU_EDIT_TRANSLATE_ALL,
	MENU_EDIT_TRANSLATE_ALL_TOOLTIP,
	MENU_EDIT_TRANSLATE_ALL_TOOLTIP_UNAVAILABLE,
	MENU_EDIT_CLEAN_ALL,
	MENU_EDIT_CLEAN_ALL_TOOLTIP,
	MENU_EDIT_NEXT,
	MENU_EDIT_PREVIOUS,
	MENU_ABOUT,
	MENU_ABOUT_SOFTWARE,

	/* Toolbar */
	TOOLBAR_TITLE,
	OPEN_FOLDER_TOOLTIP,
	SAVE_TOOLTIP,
	COPY_ORIGINAL_TOOLTIP,
	BLANK_ONLY_TOOLTIP,
	FILE_SELECTOR_TOOLTIP,
	TABLE_FILTER_TOOLTIP,
	TRANSLATION_TOOLBAR_TITLE,
	PREVIOUS_BUTTON_TOOLTIP,
	NEXT_BUTTON_TOOLTIP,
	HINT_BUTTON_TOOLTIP,
	ACCEPT_HINT_BUTTON_TOOLTIP,
	TRANSLATION_AREA_TOOLTIP,
	ACCEPT_TRANSLATION_BUTTON_LABEL,
	ACCEPT_TRANSLATION_BUTTON_TOOLTIP,
	DELETE_TRANSLATION_BUTTON_LABEL,
	DELETE_TRANSLATION_BUTTON_TOOLTIP,
	TABLE_ORIGINAL_COLUMN_LABEL,
	TABLE_TRANSLATED_COLUMN_LABEL,
	STATUS_TRANSLATION_PROGRESS,
	TOOLBAR_COLOR_TABLE_TOOLTIP,

	/* ABOUT */
	ABOUT_PAGE_TITLE,
	ABOUT_APPLICATION_SHORT_DESCRIPTION,
	ABOUT_AUTHOR_TOOLTIP,
	ABOUT_SOURCE_TOOLTIP,
	ABOUT_APPLICATION_DESCRIPTION,
	ABOUT_OFFICIAL_PAGE_TOOLTIP,

	/* SETTINGS */
	SETTING_FRAME_TITLE,
	SETTINGS_DEFAULT_FOLDER_LABEL,
	SETTINGS_SOURCE_LANGUAGE_LABEL,
	SETTINGS_TRANSLATED_LANGUAGE_LABEL,
	SETTINGS_TAB_TRANSLATION_LABEL,
	SETTINGS_TAB_APP_LABEL,
	SETTINGS_TRANSLATED_COLOR_LABEL,
	SETTINGS_UNTRANSLATED_COLOR_LABEL,
	AZURE_CLIENT_CREDENTIALS_TITLE,
	SETTINGS_APPLICATION_LANGUAGE,
	POPUP_CHANGE_LANGUAGE_DESC,
	POPUP_CHANGE_LANGUAGE_TITLE,
	POPUP_CHANGE_LANGUAGE_YES,
	POPUP_CHANGE_LANGUAGE_NO,
	POPUP_CHANGE_LANGUAGE_CANCEL,
	POPUP_TRANSLATE_ALL_DESC,
	POPUP_TRANSLATE_ALL_TITLE,
	POPUP_COLOR_PICKER_TITLE,
	POPUP_CLEAN_ALL_DESC,
	POPUP_CLEAN_ALL_TITLE,
	POPUP_CLEAN_ALL_RESULT,
	POPUP_TRANSLATE_ALL_RESULT
    }
}
