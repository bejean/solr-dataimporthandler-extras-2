package org.apache.solr.handler.dataimport.config;

import org.apache.solr.handler.dataimport.Context;

public interface IConfigLoader {
	public boolean load(Context context, String configFile);
	String propReplace(String propValue);
}
