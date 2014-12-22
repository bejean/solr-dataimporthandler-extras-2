Solr DIH JDBC Datasource
========================

The goal of this development on top of Solr DIH is to allow DIH configuration be common to various collections by externalize some settings to a shared configuration file.

Several collections can use the same schema and the same DIH settings. For instance, DIH settings for query, deltaQuery and deltaImportQuery attributes of SqlEntityProcessor are the same, but Database schema name, JDBC URL, user and password are different.

Sample DIH configuration file
-----------------------------

<dataConfig> 
	<dataSource 
		type="JdbcDataSource2" 
		configfile="/<path_to_solr_directory/props.properties"
		configloader="PropertiesFileConfigLoader"
		configkeyregex="^(.*)_shard"
		driver="com.mysql.jdbc.Driver"
		url="${config.url}"
		user="${config.user}"
		password="${config.password}"
		batchSize="1" />

	 <document>  
		<entity name="entry" pk="id"
			processor="SqlEntityProcessor2"
			configfile="/<path_to_solr_directory/props.properties"
			configloader="PropertiesFileConfigLoader"
			configkeyregex="^(.*)_shard"
			query="SELECT * FROM ${config.table}"
			deltaQuery="select id from ${config.table}">
			deltaImportQuery="select * from ${config.table} where id='${dih.delta.id}'"
		>  
		    <field column="id" name="id" />  
		    <field column="name" name="name_t" />  
		    <field column="description" name="description_t" />  
		</entity>  
	</document>  
</dataConfig> 


Sample external properties file
-------------------------------

default.driver=com.mysql.jdbc.Driver

# this is a comment
dih.url=jdbc:mysql://localhost:3306/dih_data
dih.user=solr
dih.password=solr123
dih.table=entry


In this sample, "dih" property key allows to associate setting with the DIH configuration file.
This key matches with the core name and the regex define in the "configkeyregex" attribute of the dataSource and the entity elements


Example
-------

core name = dih_shard1_replica1
configkeyregex="^(.*)_shard"
-> key = dih

core name = people_application1_shard1_replica1
configkeyregex="^people_(.*)_shard"
-> key = application1

default value for configkeyregex is "^(.*)_shard"


Available properties
--------------------

Any properties can be used.

In DIH configuration file, all replacer ${config.foo}, will be replace by property key.foo


Datasource attributes
---------------------

type="JdbcDataSource2	
    Make the DIH use the JdbcDataSource2 class
configfile="/<path_to_solr_directory/props.properties"	
    The path to the properties file
configloader="PropertiesFileConfigLoader"	
    The name of the class to be used in order to load parameters
configkeyregex="^(.*)_shard"	
    The regex used in order to extract from the core name (“ge_activity_shard1_replica1”)  a key to be use in order to read in the properties file. With configkeyregex="^(.*)_shard", the key will be « ge_activity ». Properties read in the properties file will be : ge_activity.url=..., ge_activity.user=...


Entity attributes
-----------------

processor="SqlEntityProcessor2 »	
	Make the DIH use the SqlEntityProcessor2  class
configfile="/<path_to_solr_directory/props.properties"	
	The path to the properties file
configloader="PropertiesFileConfigLoader"	
	The name of the class to be used in order to load parameters
configkeyregex="^(.*)_shard"	
	The regex used in order to extract from the core name (“ge_activity_shard1_replica1”)  a key to be use in order to read in the properties file. With configkeyregex="^(.*)_shard", the key will be « ge_activity ». Properties read in the properties file will be : ge_activity.schema=...,  ge_activity.table=...


Configuration loader
--------------------

The attribute "configloader" specifies which configuration loader has to be used.
"PropertiesFileConfigLoader" is the available in order to load configuration from standard properties files.

It is possible to implement a specific configuration loader. A Class have to be developed and implement IConfigLoader interface (in package org.apache.solr.handler.dataimport.config)


Build
-----

You need maven build tool.

1. unzip project archive file
2. cd to the project root directory
3. build
   mvn package -Dmaven.test.skip=true


Tested with Solr 4.10.2
    