package fr.hermesdj.java.darkestdungeontranslationapp;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TranslationAppConfigurationManager {
	private static volatile TranslationAppConfigurationManager INSTANCE;

	private PropertiesConfiguration prop;

	private TranslationAppConfigurationManager() {
		prop = new PropertiesConfiguration();
	}

	public static TranslationAppConfigurationManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TranslationAppConfigurationManager();
		}
		return INSTANCE;
	}

	public boolean hasFile() {
		return prop.getFile() != null;
	}

	public void saveToFile(File f) {
		if (f != null) {
			prop.setFile(f);
		}

		try {
			prop.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void loadFromFile(File f) {
		prop.setFile(f);
		prop.reload();
		try {
			prop.refresh();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getProperty(ConfigurationKey key) {
		return (String) prop.getProperty(key.name());
	}

	public void setProperty(ConfigurationKey key, String value) {
		prop.setProperty(key.name(), value);
	}

	public static enum ConfigurationKey {
		TRANSLATION_FILES_LOCATION, DEFAULT_ORIGINAL_LANGUAGE, TRANSLATED_LANGUAGE
	}

}
