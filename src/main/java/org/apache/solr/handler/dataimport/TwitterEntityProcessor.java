/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An EntityProcessor instance which can index tweets 
 * 
 * @since solr 4.10
 */

/*
<document>
	<entity processor="TwitterEntityProcessor"    
       	consumerKey="xxx";
		consumerSecret="xxx";
		accessToken="xxx-xxx";
		accessTokenSecret="xxx" ;
		criteria = "#prestige";
	/>
</document>
*/

public class TwitterEntityProcessor extends EntityProcessorBase {

	private Twitter twitter = null;
	private Query query = null;
	private QueryResult results = null;
	private ArrayList<Status> tweets = null;
	private int index = 0;
	private static final Logger LOG = LoggerFactory.getLogger(DataImporter.class);
	String since;

	private static final SimpleDateFormat sinceDateParser = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
	private static final SimpleDateFormat afterFmt = 
			new SimpleDateFormat("yyyy/MM/dd", Locale.ROOT);

	public void init(Context context) {
		super.init(context);

		// get parameters
		String consumerKey=getStringFromContext("consumerKey", null);
		String consumerSecret=getStringFromContext("consumerSecret", null);
		String accessToken=getStringFromContext("accessToken", null);
		String accessTokenSecret=getStringFromContext("accessTokenSecret", null);

		// connect to twitter
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(accessToken);
		cb.setOAuthAccessTokenSecret(accessTokenSecret);

		twitter = new TwitterFactory(cb.build()).getInstance(); 

		// initiate the search
		String criteria = getStringFromContext("criteria", null);
		since = context.replaceTokens("${dih.last_index_time}");
		query = new Query(criteria);
		//query.setSince(since.substring(0, since.indexOf(" ")).trim());		
		query.setSince("2015-02-12");		
		
		index = 0;

		logConfig();
	}

	@Override
	public Map<String,Object> nextRow() {

		Map<String,Object> row = new HashMap<>();

		if (twitter==null || query==null) return null;

		try {
			if (results==null) {
				results = twitter.search(query);
				if (results==null || results.getCount()==0) return null;
			}
			if (tweets==null) tweets = (ArrayList<Status>) results.getTweets();

			Status tweet = null;
			if (index<tweets.size()) {
				tweet=(Status) tweets.get(index++);				
			} else {
				query = results.nextQuery();
				if (query!=null) {
					results = twitter.search(query);
					if (results==null || results.getCount()==0) return null;
					tweets = (ArrayList<Status>) results.getTweets();
					index=0;
					tweet=(Status) tweets.get(index++);		
				}
			}
			if (tweet==null) return null;

			// id
			row.put(MESSAGE_ID, tweet.getId());

			// lang
			row.put(MESSAGE_LANG, tweet.getLang());

			// user
			User user = tweet.getUser();

			// name
			row.put(MESSAGE_USER, user.getName());

			// pseudo
			row.put(MESSAGE_PSEUDO, tweet.getUser().getScreenName());

			// text
			row.put(MESSAGE_TEXT, tweet.getText());

			// date
			Date date = tweet.getCreatedAt();			
			row.put(MESSAGE_DATE, date.toString());

		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}

		return row;
	}

	private void logConfig() {
		if (!LOG.isInfoEnabled()) return;

		String lineSep = System.getProperty("line.separator"); 

		StringBuffer config = new StringBuffer();
		//    config.append("user : ").append(user).append(lineSep);
		//    config
		//        .append("pwd : ")
		//        .append(
		//            password != null && password.length() > 0 ? "<non-null>" : "<null>")
		//        .append(lineSep);
		//    config.append("protocol : ").append(protocol)
		//        .append(lineSep);
		//    config.append("host : ").append(host)
		//        .append(lineSep);
		LOG.info(config.toString());
	}

	// Fields To Index
	private static final String MESSAGE_ID = "id";
	private static final String MESSAGE_LANG = "tweet_lang";
	private static final String MESSAGE_USER = "tweet_user";
	private static final String MESSAGE_PSEUDO = "tweet_pseudo";
	private static final String MESSAGE_TEXT = "tweet_text";
	private static final String MESSAGE_DATE = "tweet_date";

//	private int getIntFromContext(String prop, int ifNull) {
//		int v = ifNull;
//		try {
//			String val = context.getEntityAttribute(prop);
//			if (val != null) {
//				val = context.replaceTokens(val);
//				v = Integer.valueOf(val);
//			}
//		} catch (NumberFormatException e) {
//			// do nothing
//		}
//		return v;
//	}
//
//	private boolean getBoolFromContext(String prop, boolean ifNull) {
//		boolean v = ifNull;
//		String val = context.getEntityAttribute(prop);
//		if (val != null) {
//			val = context.replaceTokens(val);
//			v = Boolean.valueOf(val);
//		}
//		return v;
//	}

	private String getStringFromContext(String prop, String ifNull) {
		String v = ifNull;
		String val = context.getEntityAttribute(prop);
		if (val != null) {
			val = context.replaceTokens(val);
			v = val;
		}
		return v;
	}
}
