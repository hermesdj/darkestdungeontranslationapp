package fr.hermesdj.java.darkestdungeontranslationapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TranslationAppConfigurationManager {
	private static volatile TranslationAppConfigurationManager INSTANCE;

	private PropertiesConfiguration prop;
	private String filename = "translationapp.conf";

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
		if (prop.getFile() != null) {
			System.out.println("Saving configuration file to "
					+ prop.getFile().getAbsolutePath());
		}
		try {
			prop.save(filename);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		try {
			File f = new File(filename);
			if (!f.exists()) {
				f.createNewFile();
				prop.load(getClass().getResourceAsStream("/" + filename));
			} else {
				prop.load(filename);
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getProperty(ConfigurationKey key) {
		String result = null;
		Object obj = prop.getProperty(key.name());
		if (obj instanceof ArrayList) {
			if (((ArrayList) obj).size() > 0) {
				result = (String) ((ArrayList) obj).get(0);
			}
		} else {
			result = (String) obj;
		}

		return result;
	}

	public PropertiesConfiguration getProperties() {
		return prop;
	}

	public void setProperty(ConfigurationKey key, String value) {
		prop.setProperty(key.name(), value.toString());
	}

	public static enum ConfigurationKey {
		TRANSLATION_FILES_LOCATION, DEFAULT_ORIGINAL_LANGUAGE, TRANSLATED_LANGUAGE, DEFAULT_BLANK_ONLY, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, APPLICATION_VERSION
	}

	public Boolean getBoolean(ConfigurationKey key, boolean b) {
		return prop.getBoolean(key.name(), b);
	}

}
