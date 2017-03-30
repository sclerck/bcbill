/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import org.knowm.xchange.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Purpose: The web server
 * 
 * If you are on mac and the web server complains of blocked threads, see this:
 * https://thoeni.io/post/macos-sierra-java/
 * 
 * @date 12 Mar 2017
 * @author sclerck
 *
 */
public class WebServerVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(WebServerVerticle.class);

	@Override
	public void start() throws Exception {

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		router.route("/*")
				.handler(StaticHandler.create().setWebRoot("src/main/webapp").setIndexPage("dist/index.html"));

		server.requestHandler(router::accept);

		server.websocketHandler(ws -> {

			logger.info("Connection established");

			for (Class<? extends Exchange> exchangeClazz : ServerVerticle.exchangesToIgnore) {
				String friendlyName = exchangeClazz.getSimpleName().substring(0,
						exchangeClazz.getSimpleName().length() - 8);

				JsonObject message = new JsonObject();
				message.put("exchange", friendlyName);
				message.put("status", "ignored");

				JsonObject output = new JsonObject();
				output.put("type", "exchangeInfo");
				output.put("data", message);

				ws.writeTextMessage(output.toString());
			}

			// Ask for the current connection status from all exchanges
			MessageConsumer<Object> currentConnectionStatusConsumer = vertx.eventBus()
					.consumer("currentConnectionStatus", currentConnectionStatus -> {
						JsonObject output = new JsonObject();
						output.put("type", "exchangeInfo");
						output.put("data", currentConnectionStatus.body());

						ws.writeTextMessage(output.toString());
					});

			vertx.eventBus().publish("currentConnectionStatusRequest", "");

			// Subscribe to exchange info
			MessageConsumer<Object> exchangeConsumer = vertx.eventBus().consumer("exchangeInfo", exchangeInfo -> {
				JsonObject output = new JsonObject();
				output.put("type", "exchangeInfo");
				output.put("data", exchangeInfo.body());

				ws.writeTextMessage(output.toString());
			});

			// Ask for the current market data snapshot from all exchanges
			MessageConsumer<Object> currentMarketDataConsumer = vertx.eventBus().consumer("currentMarketData",
					currentMarketData -> {
						JsonObject output = new JsonObject();
						output.put("type", "tick");
						output.put("data", currentMarketData.body());

						ws.writeTextMessage(output.toString());
					});

			vertx.eventBus().publish("currentMarketDataRequest", "");

			// Now subscribe to ticks
			MessageConsumer<Object> marketDataConsumer = vertx.eventBus().consumer("marketDataUpdate",
					marketDataUpdate -> {
						JsonObject output = new JsonObject();
						output.put("type", "tick");
						output.put("data", marketDataUpdate.body());

						ws.writeTextMessage(output.toString());
					});

			ws.closeHandler(ch -> {

				logger.info("Connection dropped");

				marketDataConsumer.unregister();

				exchangeConsumer.unregister();

				currentMarketDataConsumer.unregister();

				currentConnectionStatusConsumer.unregister();
			});
		});

		server.listen(8080);
	}

}
