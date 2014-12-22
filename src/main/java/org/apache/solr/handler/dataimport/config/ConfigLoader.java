package org.apache.solr.handler.dataimport.config;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ConfigLoader implements IConfigLoader {

	protected String configKey = null;
	protected Properties collectionProp = new Properties(); 
	protected Properties defaultProp = new Properties(); 
	protected Properties globalProp = new Properties(); 
	protected String configFile = null;
	
	public ConfigLoader(String configKey) {
		this.configKey = configKey.trim().toLowerCase();
	}

	@Override
	public String propReplace(String propValue) {
		
		Pattern p = Pattern.compile("\\$\\{config\\.(.*)\\}");
		Matcher m = p.matcher(propValue);
		while (m.find()) {
			String propName = m.group(1).toLowerCase(); // properties name are case insensitive
			
			String replacement = "";
			
			if (collectionProp.containsKey(propName)) {
				replacement = collectionProp.getProperty(propName);
			} else {
				if (defaultProp.containsKey(propName)) {
					replacement = defaultProp.getProperty(propName);
				}			
			}
			
			if (!"".equals(replacement)) {
				propValue = propValue.replaceAll("\\$\\{config\\." + propName.replace(".", "\\.") + "\\}", replacement);
			}
		}
		return propValue;
	}

	@Override
	public boolean reload() {
		return load(configFile);
	}
	
}
