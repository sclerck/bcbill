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

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.exceptions.ExchangeException;
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

	public ExchangeVerticle(Class<? extends Exchange> exchangeClazz) {
		this.exchangeClazz = exchangeClazz;
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void start() throws Exception {

		logger.info("Starting exchange {}", exchangeClazz);

		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClazz.getName());

		String friendlyName = exchangeClazz.getSimpleName();

		MarketDataService marketDataService = exchange.getMarketDataService();

		final AtomicDouble lastAsk = new AtomicDouble(0.0);
		final AtomicDouble lastBid = new AtomicDouble(0.0);

		executor.scheduleAtFixedRate(() -> {
			try {
				Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USD);

				if (ticker != null) {
					BigDecimal ask = ticker.getAsk();
					BigDecimal bid = ticker.getBid();

					if ((ask != null && ask.doubleValue() != lastAsk.get())
							|| (bid != null && bid.doubleValue() != lastBid.get())) {

						lastAsk.set(ask.doubleValue());
						lastBid.set(bid.doubleValue());

						JsonObject message = new JsonObject();
						message.put("exchange", friendlyName);
						message.put("timestamp", Long.toString(System.currentTimeMillis()));
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
			} catch (ExchangeException e) {
				logger.warn("Killing this verticle [{}] as getTicker() is throwing an exception", friendlyName, e);
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
				logger.error("Error connecting to exchange", e);
			}

		}, 1000, 1000, TimeUnit.MILLISECONDS); // TODO get this from properties
	}

	@Override
	public void stop() throws Exception {
		executor.shutdownNow();
	}
}
