package org.apache.solr.handler.dataimport.config;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DocBuilder;

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

	static public String getConfigKey(Context context, String configKeyRegex) {
		String coreName = context.getSolrCore().getName();
		String configKey = coreName;
		if (configKeyRegex!=null && !"".equals(configKeyRegex)) {
			Pattern pattern = Pattern.compile(configKeyRegex);
			Matcher matcher = pattern.matcher(configKey);
			if (matcher.find()) {
				configKey = matcher.group(1);
			}
		} else {
			if (configKey.indexOf("_shard")!=-1) configKey = configKey.substring(0,coreName.indexOf("_shard"));
		}
		return configKey;
	}

	static public ConfigLoader getInstance (String configLoaderClasseName, String configKey) {
		Class<?> configLoader;
		try {
			configLoader = Class.forName(configLoaderClasseName);
		} catch (ClassNotFoundException e) {
			throw new DataImportHandlerException(SEVERE, "Unable to load config loader : " + configLoaderClasseName);
		}

		ConfigLoader configLoaderInstance = null;
		try {
			Constructor<?> constructor;
			try {
				constructor = configLoader.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
			} catch (SecurityException e) {
				throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
			}
			try {
				configLoaderInstance = (ConfigLoader)constructor.newInstance(configKey);
			} catch (IllegalArgumentException e) {
				throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
			} catch (InvocationTargetException e) {
				throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
			}
		} catch (InstantiationException e) {
			throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
		} catch (IllegalAccessException e) {
			throw new DataImportHandlerException(SEVERE, "Unable to instantiate config loader : " + configLoaderClasseName);
		}
		return configLoaderInstance;
	}

	static Class loadClass(String name, SolrCore core) throws ClassNotFoundException {
		try {
			return core != null ?
					core.getResourceLoader().findClass(name, Object.class) :
						Class.forName(name);
		} catch (Exception e) {
			try {
				String n = DocBuilder.class.getPackage().getName() + "." + name;
				return core != null ?
						core.getResourceLoader().findClass(n, Object.class) :
							Class.forName(n);
			} catch (Exception e1) {
				throw new ClassNotFoundException("Unable to load " + name + " or " + DocBuilder.class.getPackage().getName() + "." + name, e);
			}
		}
	}

}
