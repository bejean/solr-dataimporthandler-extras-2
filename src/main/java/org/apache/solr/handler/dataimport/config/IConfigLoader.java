package org.apache.solr.handler.dataimport.config;

public interface IConfigLoader {
	boolean load(String configFile);
	//boolean reload();
	String propReplace(String propValue);
}
