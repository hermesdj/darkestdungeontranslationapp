package fr.hermesdj.java.darkestdungeontranslationapp;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TranslationAppConfigurationManager {
	private static volatile TranslationAppConfigurationManager INSTANCE;

	private PropertiesConfiguration prop;
	private String filename = "/translationapp.conf";

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

	public void save() {
		System.out.println("Saving configuration file to "
				+ getClass().getResource("/").getPath());
		try {
			prop.save(getClass().getResource(filename));
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		System.out.println("Loading configuration file from "
				+ getClass().getResource("/").getPath());
		try {
			prop.load(getClass().getResourceAsStream(filename));
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getProperty(ConfigurationKey key) {
		return (String) prop.getProperty(key.name());
	}

	public PropertiesConfiguration getProperties() {
		return prop;
	}

	public void setProperty(ConfigurationKey key, String value) {
		prop.setProperty(key.name(), value);
	}

	public static enum ConfigurationKey {
		TRANSLATION_FILES_LOCATION, DEFAULT_ORIGINAL_LANGUAGE, TRANSLATED_LANGUAGE, DEFAULT_BLANK_ONLY, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, APPLICATION_VERSION
	}

	public Boolean getBoolean(ConfigurationKey key, boolean b) {
		return prop.getBoolean(key.name(), b);
	}

}
