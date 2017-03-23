/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Purpose:
 * 
 * @author sclerck
 * @date 22 Mar 2017
 *
 */
public class PersistenceVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(PersistenceVerticle.class);

	@Override
	public void start() throws Exception {

		try {
			String host = "localhost";
			int port = 27017; // TODO get these from properties

			MongoClient mongoClient = new MongoClient(host, port);

			MongoDatabase db = mongoClient.getDatabase("bcbill");

			MongoCollection<Document> collection = db.getCollection("tickData");

			logger.info("Connected to mongo on {}:{}", host, port);

			vertx.eventBus().consumer("marketDataUpdate", marketDataUpdate -> {
				JsonObject message = (JsonObject) marketDataUpdate.body();

				Document document = new Document();
				document.put("timestamp", message.getString("timestamp"));
				document.put("exchange", message.getString("exchange"));
				document.put("bid", message.getDouble("bid"));
				document.put("ask", message.getDouble("ask"));

				collection.insertOne(document);
			});
		} catch (Exception e) {
			logger.error("Unable to interact with mongo", e);
		}
	}
}
