package org.apache.solr.handler.dataimport.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.solr.handler.dataimport.Context;

public class PropertiesFileConfigLoader extends ConfigLoader implements IConfigLoader {

	public PropertiesFileConfigLoader(String collection) {
		super(collection);
	}

	@Override
	public boolean load(Context context, String configFile) {
		this.configFile = configFile;
		File f = new File(configFile);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.trim().length() > 0 && line.indexOf("=")!=-1 && !line.startsWith("#")) {
					String[] items = line.split("=");
					items[0] = items[0].trim().toLowerCase(); // properties name are case insensitive
					if (items[0].indexOf(".")==-1) globalProp.setProperty(items[0], items[1].trim());
					if (items[0].startsWith("default.")) defaultProp.setProperty(items[0].substring("default.".length()), items[1].trim());
					if (items[0].startsWith(configKey + ".")) collectionProp.setProperty(items[0].substring(configKey.length()+1), items[1].trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
