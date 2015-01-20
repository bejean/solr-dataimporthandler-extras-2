package org.apache.solr.handler.dataimport.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import org.apache.solr.handler.dataimport.Context;

public class DbSimpleConfigLoader extends ConfigLoader implements IConfigLoader {

	public DbSimpleConfigLoader(String collection) {
		super(collection);
	}

	@Override
	public boolean load(final Context context, String configFile) {

		// load settings for settings db access
		this.configFile = configFile;
		File f = new File(configFile);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		String driver = null;
		String url = null;
		String table = null;
		String user = null;
		String password = null;

		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.trim().length() > 0 && line.indexOf("=")!=-1 && !line.startsWith("#")) {
					String[] items = line.split("=");
					items[0] = items[0].trim().toLowerCase(); // properties name are case insensitive
					if ("driver".equals(items[0])) driver = items[1].trim();
					if ("url".equals(items[0])) url = items[1].trim();
					if ("table".equals(items[0])) table = items[1].trim();
					if ("user".equals(items[0])) user = items[1].trim();
					if ("password".equals(items[0])) password = items[1].trim();
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

		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", password);

		// load settings in db
		Connection c = null;
		try {
			c = DriverManager.getConnection(url, connectionProps);
		} catch (SQLException e) {
			// DriverManager does not allow you to use a driver which is not loaded through
			// the class loader of the class which is trying to make the connection.
			// This is a workaround for cases where the user puts the driver jar in the
			// solr.home/lib or solr.home/core/lib directories.
			Driver d;
			try {
				d = (Driver) loadClass(driver, context.getSolrCore()).newInstance();
				c = d.connect(url, connectionProps);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
				return false;
			}
		}

		try {
			Statement stmt = null;
			try {
				c.setReadOnly(true);
				c.setAutoCommit(true);
				c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				c.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);

				String query = "SELECT scope, name, value FROM " + table;
				stmt = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				if (stmt.execute(query)) {
					ResultSet resultSet = stmt.getResultSet();


					while (resultSet.next()) {
						String scope = resultSet.getString("scope");
						String name = resultSet.getString("name");
						String value = resultSet.getString("value");
						if ("default".equals(scope)) defaultProp.setProperty(name, value);
						if (configKey.equals(scope)) collectionProp.setProperty(name, value);
					}
				} else {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (stmt != null) stmt.close();
				if (c != null) c.close();
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
