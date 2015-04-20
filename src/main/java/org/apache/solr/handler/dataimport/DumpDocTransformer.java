/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.dataimport;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * <p>
 * A {@link Transformer} implementation which do not transform anything 
 * but dump document into xml file
 * </p>
 *
 * @see Pattern
 */
public class DumpDocTransformer extends Transformer {
	private static final Logger LOG = LoggerFactory.getLogger(DumpDocTransformer.class);

	static boolean include(String name, List<String> fieldExcludeList, List<String> fieldIncludeList) {
		if (fieldExcludeList!=null && fieldExcludeList.contains(name))
			return false;
		if (fieldIncludeList==null || fieldIncludeList.contains(name))
			return true;
		return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> transformRow(Map<String, Object> row,
			Context ctx) {

		String path = ctx.getEntityAttribute("dumpPath");
		String idField = ctx.getEntityAttribute("dumpIdField");
		String fieldExclude = ctx.getEntityAttribute("dumpFieldExclude");
		String fieldInclude = ctx.getEntityAttribute("dumpFieldInclude");
		boolean fieldNameInclude = "true".equals(ctx.getEntityAttribute("dumpFieldNameInclude"));

		List<String> fieldExcludeList = null;
		if (fieldExclude!=null) {
			fieldExcludeList = Arrays.asList(fieldExclude.split("\\s*,\\s*"));
		}
		
		List<String> fieldIncludeList = null;
		if (fieldInclude!=null) {
			fieldIncludeList = new ArrayList<String>(Arrays.asList(fieldInclude.split("\\s*,\\s*")));
		}
		
		if (fieldNameInclude) {
			if (fieldInclude==null) fieldIncludeList = new ArrayList<String>(Arrays.asList("".split("\\s*,\\s*")));
			List<Map<String, String>> fields = ctx.getAllEntityFields();
		    for (Map<String, String> field : fields) {
		        String name = field.get(DataImporter.NAME).toLowerCase();
		        if (!fieldIncludeList.contains(name))
		        	fieldIncludeList.add(name);
		    }
		}
		
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return row;
		}
		Document document = builder.newDocument();

		Element add = document.createElement("add");
		Element doc = document.createElement("doc");

		for(String key : row.keySet()){
			Object o = row.get(key);
			
			key = key.toLowerCase();

			String value = null;
			if (o instanceof ArrayList) {
				ArrayList<String> stringList = (ArrayList<String>) o;
				for(String item: stringList){
					Element newField = document.createElement("field");
					newField.setAttribute("name", key);
					newField.setTextContent(item);
					if (include( key, fieldExcludeList, fieldIncludeList))
						doc.appendChild(newField);
				}
			} else {
				if (o instanceof Integer) {
					value = ((Integer)o).toString();
				}
				if (o instanceof String) {
					value = (String) o;
				}
				if (value!=null) {
					Element newField = document.createElement("field");
					newField.setAttribute("name", key);
					newField.setTextContent(value);
					if (include( key, fieldExcludeList, fieldIncludeList))
						doc.appendChild(newField);
				}
			}

			if (idField.toLowerCase().equals(key))
				if (value!=null)
					path += "/" + value + ".xml";
				else
					path += "/" + UUID.randomUUID() + ".xml";				
		}

		add.appendChild(doc);
		document.appendChild(add);	

		try {
			javax.xml.transform.Transformer transformer;
			transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(document);
			File file = new File(path);
			Result result = new StreamResult(file);
			transformer.transform(source,result);
		} catch (Exception e) {
			e.printStackTrace();
			return row;
		}
		/*           
		   transformer="RegexTransformer,DumpDocTransformer"
           dumpPath="/tmp"
           dumpIdField="id"
           dumpFieldExclude="kk,yy"
           dumpFieldInclude="ee"
           dumpFieldNameInclude="true"
           
		<add>
			<doc>
				<field name="id">1</field>
				<field name="description">description1</field>
				<field name="name">name1</field>
				<field name="attribute">1</field>
				<field name="attribute">2</field>
				<field name="attribute">3</field>
				<field name="attribute">4</field>
			</doc>
		</add>
		*/
		return row;
	}
}
