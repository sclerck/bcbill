/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Purpose:
 * 
 * @author sclerck
 * @date 24 Mar 2017
 *
 */
public class ExampleAlgoVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(ExchangeVerticle.class);

	@Override
	public void start() throws Exception {

		final AtomicDouble lowestBuy = new AtomicDouble(Double.MAX_VALUE);
		final AtomicReference<String> lowestBuyExchange = new AtomicReference<String>();
		final AtomicDouble highestSell = new AtomicDouble(Double.MIN_VALUE);
		final AtomicReference<String> highestSellExchange = new AtomicReference<String>();

		vertx.eventBus().consumer("marketDataUpdate", marketDataUpdate -> {
			JsonObject tick = (JsonObject) marketDataUpdate.body();

			double bid = tick.getDouble("bid");
			double ask = tick.getDouble("ask");
			String exchange = tick.getString("exchange");

			boolean isChanged = false;

			if (ask < lowestBuy.get()) {
				lowestBuy.set(ask);
				lowestBuyExchange.set(exchange);
				isChanged = true;
			}

			if (bid > highestSell.get()) {
				highestSell.set(bid);
				highestSellExchange.set(exchange);
				isChanged = true;
			}

			if (isChanged)
				logger.info("Buy from {} at {} and sell to {} at {}", lowestBuyExchange.get(), lowestBuy.get(),
						highestSellExchange.get(), highestSell.get());

		});
	}

}
