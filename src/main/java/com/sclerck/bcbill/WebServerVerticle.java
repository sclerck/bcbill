/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Purpose: The web server
 * 
 * If you are on mac and the web server complains of blocked threads, see this:
 * https://thoeni.io/post/macos-sierra-java/
 * 
 * @date 12 Mar 2017
 *
 */
public class WebServerVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(WebServerVerticle.class);

	@Override
	public void start() throws Exception {

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		router.route("/*").handler(StaticHandler.create().setWebRoot("app").setIndexPage("dist/index.html"));

		server.requestHandler(router::accept);

		server.websocketHandler(ws -> {

			logger.info("Connection established");

			MessageConsumer<Object> mc = vertx.eventBus().consumer("marketDataUpdate", marketDataUpdate -> {
				ws.writeTextMessage(marketDataUpdate.body().toString()); // We
																			// know
																			// this
																			// is
																			// a
																			// JsonObject
																			// so
																			// we
																			// can
																			// just
																			// toString
			});

			ws.closeHandler(ch -> {

				logger.info("Connection dropped");

				mc.unregister();
			});
		});

		server.listen(8080);
	}

}
