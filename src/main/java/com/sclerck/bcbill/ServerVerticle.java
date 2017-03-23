/**
 * $Header:
 * $Id:
 * $Name:
 */
package com.sclerck.bcbill;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitcoinde.BitcoindeExchange;
import org.knowm.xchange.bitcurex.BitcurexExchange;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.bitmarket.BitMarketExchange;
import org.knowm.xchange.bitso.BitsoExchange;
import org.knowm.xchange.bittrex.v1.BittrexExchange;
import org.knowm.xchange.bleutrade.BleutradeExchange;
import org.knowm.xchange.btc38.Btc38Exchange;
import org.knowm.xchange.btcchina.BTCChinaExchange;
import org.knowm.xchange.chbtc.ChbtcExchange;
import org.knowm.xchange.coinmate.CoinmateExchange;
import org.knowm.xchange.cryptofacilities.CryptoFacilitiesExchange;
import org.knowm.xchange.empoex.EmpoExExchange;
import org.knowm.xchange.gatecoin.GatecoinExchange;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.huobi.HuobiExchange;
import org.knowm.xchange.independentreserve.IndependentReserveExchange;
import org.knowm.xchange.itbit.v1.ItBitExchange;
import org.knowm.xchange.jubi.JubiExchange;
import org.knowm.xchange.lakebtc.LakeBTCExchange;
import org.knowm.xchange.livecoin.LivecoinExchange;
import org.knowm.xchange.loyalbit.LoyalbitExchange;
import org.knowm.xchange.mercadobitcoin.MercadoBitcoinExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.quadrigacx.QuadrigaCxExchange;
import org.knowm.xchange.ripple.RippleExchange;
import org.knowm.xchange.vaultoro.VaultoroExchange;
import org.knowm.xchange.yobit.YoBitExchange;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;

/**
 * Purpose:
 * 
 * @author sclerck
 * @date 22 Mar 2017
 *
 */
public class ServerVerticle extends AbstractVerticle {

	private static Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

	private static Collection<Class<? extends Exchange>> exchangesToIgnore = new LinkedList<>();

	static {
		exchangesToIgnore.add(RippleExchange.class);
		exchangesToIgnore.add(BittrexExchange.class);
		exchangesToIgnore.add(CoinmateExchange.class);
		exchangesToIgnore.add(IndependentReserveExchange.class);
		exchangesToIgnore.add(BitcoindeExchange.class);
		exchangesToIgnore.add(ChbtcExchange.class);
		exchangesToIgnore.add(BitcurexExchange.class);
		exchangesToIgnore.add(BitMarketExchange.class);
		exchangesToIgnore.add(BTCChinaExchange.class);
		exchangesToIgnore.add(ItBitExchange.class);
		exchangesToIgnore.add(PoloniexExchange.class);
		exchangesToIgnore.add(BleutradeExchange.class);

		exchangesToIgnore.add(GatecoinExchange.class);
		exchangesToIgnore.add(BitfinexExchange.class);
		exchangesToIgnore.add(GeminiExchange.class);
		exchangesToIgnore.add(EmpoExExchange.class);
		exchangesToIgnore.add(VaultoroExchange.class);
		exchangesToIgnore.add(HuobiExchange.class);
		exchangesToIgnore.add(MercadoBitcoinExchange.class);
		exchangesToIgnore.add(LoyalbitExchange.class);

		// These can work
		exchangesToIgnore.add(LakeBTCExchange.class); // Might actually work,
		// just needs
		// authentication
		exchangesToIgnore.add(BitsoExchange.class);// This will connect and get
													// data but kills the
													// connection if too many
													// requests
		exchangesToIgnore.add(QuadrigaCxExchange.class); // As Bitso
		exchangesToIgnore.add(CryptoFacilitiesExchange.class); // Null ticker
		exchangesToIgnore.add(Btc38Exchange.class); // Null ticker
		exchangesToIgnore.add(JubiExchange.class); // Null ticker
		exchangesToIgnore.add(LivecoinExchange.class); // Null ticker
		exchangesToIgnore.add(YoBitExchange.class); // Null ticker
	}

	@Override
	public void start() throws Exception {
		vertx.deployVerticle(new PersistenceVerticle());
		vertx.deployVerticle(new WebServerVerticle());

		List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());

		// This is Mac specific stuff to prevent lots of pointless warning
		// messages
		ReflectionsHelper.registerUrlTypes();

		ConfigurationBuilder config = new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("org.knowm.xchange")));

		Reflections reflections = new Reflections(config);

		for (Class<? extends Exchange> exchangeClazz : reflections.getSubTypesOf(Exchange.class)) {

			if (!exchangesToIgnore.contains(exchangeClazz) && !exchangeClazz.equals(BaseExchange.class)) {

				vertx.deployVerticle(new ExchangeVerticle(exchangeClazz));
			}
		}

	}
}