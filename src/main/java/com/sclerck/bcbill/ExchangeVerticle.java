/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Purpose:
 * 
 * @author sclerck
 * @date 22 Mar 2017
 *
 */
public class ExchangeVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(ExchangeVerticle.class);

	private Class<? extends Exchange> exchangeClazz;

	private ScheduledExecutorService executor;

	private String friendlyName;

	private String connectionStatus;

	public ExchangeVerticle(Class<? extends Exchange> exchangeClazz) {
		this.exchangeClazz = exchangeClazz;
		executor = Executors.newSingleThreadScheduledExecutor();
		connectionStatus = "unknown";
	}

	@Override
	public void start() throws Exception {

		logger.info("Starting exchange {}", exchangeClazz);

		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClazz.getName());

		friendlyName = exchangeClazz.getSimpleName().substring(0, exchangeClazz.getSimpleName().length() - 8);

		MarketDataService marketDataService = exchange.getMarketDataService();

		connectionStatus = "polling";

		JsonObject exchangeInfo = new JsonObject();
		exchangeInfo.put("exchange", friendlyName);
		exchangeInfo.put("status", connectionStatus);

		vertx.eventBus().publish("exchangeInfo", exchangeInfo);

		final AtomicDouble lastAsk = new AtomicDouble(0.0);
		final AtomicDouble lastBid = new AtomicDouble(0.0);
		final AtomicLong lastTimestamp = new AtomicLong(System.currentTimeMillis());

		executor.scheduleAtFixedRate(() -> {
			try {
				Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USD);

				if (ticker != null) {
					BigDecimal ask = ticker.getAsk();
					BigDecimal bid = ticker.getBid();

					if ((ask != null && Double.compare(ask.doubleValue(), lastAsk.get()) != 0)
							|| (bid != null && Double.compare(bid.doubleValue(), lastBid.get()) != 0)) {

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

			} catch (UnsupportedOperationException e) {
				logger.warn("Killing this verticle [{}] as getTicker() is not yet supported", friendlyName);

				try {
					this.stop();
				} catch (Exception e1) {
					logger.error("Exception stopping verticle", e1);
				}
			} catch (IOException e) {
				logger.warn("Killing this verticle [{}] as the exchange isn't responding", friendlyName, e);
				try {
					this.stop();
				} catch (Exception e1) {
					logger.error("Exception stopping verticle", e1);
				}
			} catch (Exception e) {
				logger.warn("Killing this verticle [{}] due to some other exception", friendlyName, e);
				try {
					this.stop();
				} catch (Exception e1) {
					logger.error("Exception stopping verticle", e1);
				}
			}

		}, 1000, 1000, TimeUnit.MILLISECONDS); // TODO get this from properties

		vertx.eventBus().consumer("currentMarketDataRequest", currentMarketData -> {

			JsonObject message = new JsonObject();
			message.put("exchange", friendlyName);
			message.put("timestamp", lastTimestamp.get());
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

		executor.shutdownNow();
	}
}
