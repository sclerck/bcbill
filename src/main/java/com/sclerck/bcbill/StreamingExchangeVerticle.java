/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Purpose:
 * 
 * @author sclerck
 * @date 22 Mar 2017
 *
 */
public class StreamingExchangeVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(StreamingExchangeVerticle.class);

	private Class<? extends StreamingExchange> exchangeClazz;

	private StreamingExchange exchange;

	private String friendlyName;

	private String connectionStatus;

	public StreamingExchangeVerticle(Class<? extends StreamingExchange> exchangeClazz) {
		this.exchangeClazz = exchangeClazz;
		connectionStatus = "unknown";
	}

	@Override
	public void start() throws Exception {

		logger.info("Starting streaming exchange {}", exchangeClazz);

		exchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeClazz.getName());

		friendlyName = exchangeClazz.getSimpleName().substring(0, exchangeClazz.getSimpleName().length() - 17);

		// Connect to the Exchange WebSocket API. Blocking wait for the
		// connection.
		exchange.connect().blockingAwait();

		connectionStatus = "streaming";

		JsonObject exchangeStatus = new JsonObject();
		exchangeStatus.put("exchange", friendlyName);
		exchangeStatus.put("status", connectionStatus);

		vertx.eventBus().publish("exchangeInfo", exchangeStatus);

		final AtomicDouble lastAsk = new AtomicDouble(0.0);
		final AtomicDouble lastBid = new AtomicDouble(0.0);
		final AtomicLong lastTimestamp = new AtomicLong(System.currentTimeMillis());

		// Subscribe to live trades update.
		exchange.getStreamingMarketDataService().getTicker(CurrencyPair.BTC_USD).subscribe(ticker -> {
			if (ticker != null) {
				BigDecimal ask = ticker.getAsk();
				BigDecimal bid = ticker.getBid();

				if ((ask != null && ask.doubleValue() != lastAsk.get())
						|| (bid != null && bid.doubleValue() != lastBid.get())) {

					lastAsk.set(ask.doubleValue());
					lastBid.set(bid.doubleValue());
					lastTimestamp.set(System.currentTimeMillis());

					JsonObject message = new JsonObject();
					message.put("exchange", friendlyName);
					message.put("timestamp", System.currentTimeMillis());
					message.put("bid", ticker.getBid().doubleValue());
					message.put("ask", ticker.getAsk().doubleValue());

					vertx.eventBus().publish("marketDataUpdate", message);
				}
			} else {
				logger.warn("Null ticker for exchange {}", friendlyName);
			}
		}, throwable -> {
			logger.warn("Killing this verticle [{}] as getTicker() is throwing an exception", friendlyName, throwable);
			try {
				this.stop();
			} catch (Exception e1) {
				logger.error("Exception stopping verticle", e1);
			}
		});

		vertx.eventBus().consumer("currentMarketDataRequest", currentMarketData -> {

			JsonObject message = new JsonObject();
			message.put("exchange", friendlyName);
			message.put("timestamp", lastTimestamp);
			message.put("bid", lastBid.get());
			message.put("ask", lastAsk.get());

			vertx.eventBus().publish("currentMarketData", message);
		});

		vertx.eventBus().consumer("currentConnectionStatusRequest", currentConnectionStatus -> {

			JsonObject message = new JsonObject();
			message.put("exchange", friendlyName);
			message.put("status", connectionStatus);

			vertx.eventBus().publish("currentConnectionStatus", message);
		});
	}

	@Override
	public void stop() throws Exception {

		connectionStatus = "disconnected";

		JsonObject message = new JsonObject();
		message.put("exchange", friendlyName);
		message.put("status", connectionStatus);

		vertx.eventBus().publish("exchangeInfo", message);

		exchange.disconnect().subscribe(() -> logger.info("Disconnected from the exchange"));
	}
}
