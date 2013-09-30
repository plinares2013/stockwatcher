package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;


import org.mortbay.util.UrlEncoded;






import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.stream.JsonReader;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.sample.stockwatcher.client.YahooQuoteService;
import com.google.gwt.sample.stockwatcher.shared.StockInformation;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// TODO Need to make this class thread safe to have increased scalability. 
// Specifically the parseYahooResults().

public class YahooStockServiceImpl extends RemoteServiceServlet implements
		YahooQuoteService {
	
	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	
	private static final String YAHOO_URL = "http://query.yahooapis.com/v1/public/yql?q=select symbol, Change, EPSEstimateCurrentYear, " +
			"EPSEstimateNextYear, LastTradePriceOnly, ChangeinPercent, PriceSales, PriceBook, PERatio, PEGRatio, PriceEPSEstimateCurrentYear," +
			"PriceEPSEstimateNextYear from yahoo.finance.quotes where symbol in (";
	
	private static final String YAHOO_URL_KEYSTATS = "http://query.yahooapis.com/v1/public/yql?q=select symbol, ProfitMargin, ReturnonAssets, ReturnonEquity," +
			"TotalDebtEquity, CurrentRatio, PercentageHeldbyInsiders  from yahoo.finance.keystats where symbol in (";
	
	private static Logger logger = Logger.getLogger("Client Logger");
	
	String sPrice = "", sChange = "", sPercentChange = "", symbol="";
	String sPriceSales = "", sPriceBook = "", sEPSEstimateCurrentYear = "", sEPSEstimateNextYear = "";
	String sPriceEPSEstimateCurrentYear = "", sPriceEPSEstimateNextYear = "", sPERatio = "", sPEGRatio = "";
	String sProfitMargin = "", sReturnonAssets ="", sReturnonEquity = "", sTotalDebtEquity = "", sCurrentRatio ="", sPercentageHeldbyInsiders = "";
	Stock stockNew;
	double price = 0, change = 0, percentChange = 0, priceSales =0, priceBook = 0, PERatio = 0, PEGRatio = 0;
	double EPSEstimateCurrentYear = 0, EPSEstimateNextYear = 0, PriceEPSEstimateCurrentYear = 0, PriceEPSEstimateNextYear = 0;
	double profitMargin = 0, returnonAssets = 0, returnonEquity = 0, totalDebtEquity = 0, currentRatio = 0, percentageHeldbyInsiders = 0;
	StockInformation[] datas;
	Stock[] completeInfo, completeInfoKeystats;
	private int count = 0, countKeystats = 0;
	private int internalCounter=0, internalCounterKeystats=0;

	
	public StockInformation[] getStockInformation (String[] symbols)  {
		
		String url="", url_keystats = "";
		String query = "";
		
		datas = new StockInformation[symbols.length];
		completeInfo = new Stock[symbols.length];
		completeInfoKeystats = new Stock[symbols.length];
		for (int i=0; i<symbols.length; i++) {
			datas[i] = new StockInformation();
			completeInfo[i] = new Stock();
			completeInfoKeystats[i] = new Stock();
		}
	
/*
 * 		try {
 
			url = YAHOO_URL + MyURLEncode.URLencoding("select symbol, ChangeinPercent, LastTradePriceOnly, Change from yahoo.finance.quotes where symbol in (", "UTF-8") +
					MyURLEncode.URLencoding("\"" + symbol + "\")", "UTF-8") + "&format=json&env=store://datatables.org/alltableswithkeys";
		}  catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING,"Encoding error in getStockInformation(String symbol)");
		}
*/	
		// Prepare URL to call yahoo.finance.quotes
			url = YAHOO_URL;
			List<String> lSymbols = Arrays.asList(symbols);
			Iterator<String> iter = lSymbols.iterator();
			while (iter.hasNext()) {
				  url += "\"" + iter.next() + "\"";
				  if (iter.hasNext()) {
				    url += ",";
				  } else {
				    url += ")&format=json&env=store://datatables.org/alltableswithkeys";
				  }
				}

			 try{
				url = MyURLEncode.URLencoding(url,"UTF-8"); 
			 } catch (Exception e) {
				 logger.log(Level.WARNING,"url for yahoo quote incorrect");
			 }


			//Prepare URL to call yahoo.finance.keystats
			 
				url_keystats = YAHOO_URL_KEYSTATS;
				iter = lSymbols.iterator();
				while (iter.hasNext()) {
					  url_keystats += "\"" + iter.next() + "\"";
					  if (iter.hasNext()) {
					    url_keystats += ",";
					  } else {
					    url_keystats += ")&format=json&env=store://datatables.org/alltableswithkeys";
					  }
					}

				 try{
					url_keystats = MyURLEncode.URLencoding(url_keystats,"UTF-8"); 
				 } catch (Exception e) {
					 logger.log(Level.WARNING,"url for yahoo quote incorrect");
				 }

		
/*
 * 
		//Using App Engine URLFeth and Gson to parse JSON -- Painful
		try {
			URL yahooUrl = new URL (url);
			HTTPRequest request = new HTTPRequest(yahooUrl);
			URLFetchService service = URLFetchServiceFactory.getURLFetchService();
			HTTPResponse response = service.fetch(request);
			byte[] contents = response.getContent();
			query = new String(contents);
			
		} catch (Exception e) {
			logger.log(Level.WARNING,"TBD");
		}
			
 			try {	
			URL yahooUrl = new URL (url);
			InputStream is = yahooUrl.openStream();
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			JsonReader reader = new JsonReader(isr);
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("query")) {
					searchResults(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			reader.close();
		} catch (Exception e) {
			logger.log(Level.WARNING,"Exception with Yahoo URL Json retrieval \n");
		}
	
*/ 


/*
 *      Using Jersey 2.x with AppEngine, contact Yahoo JSON API yahoo.finance.quotes
 */     
 		Client client =  ClientBuilder.newClient();
 
		
		WebTarget target = client.target(url);
		
		Invocation invocation = target.request().buildGet();
		Response response = invocation.invoke();

// 		TODO Check the status of the response
//		Assert.assertTrue (response.getStatusInfo() == Response.Status.OK);
		query = response.readEntity (String.class);
		StringReader reader = new StringReader (query);
		JsonParser parser = Json.createParser(reader);


		//Parse all information
		completeInfo = parseYahooResults (parser);
		
/*
 *     Contact another JSON API yahoo.finance.keystats to retrieve additional stock data
 */	
		target = client.target(url_keystats);
		
		invocation = target.request().buildGet();
		response = invocation.invoke();
		
// 		TODO Check the status of the response
//		Assert.assertTrue (response.getStatusInfo() == Response.Status.OK);
		query = response.readEntity (String.class);
		reader = new StringReader (query);
		parser = Json.createParser(reader);

		//Parse all information
		completeInfoKeystats = parseYahooKeystatsResults(parser);
		
		//Make freshly retrieved data persistent in database
		try {
			PersistsResultsYahooQuote (completeInfo, completeInfoKeystats);
		} catch (NotLoggedInException e ){
			LOG.log(Level.WARNING,"Cannot store complete info in datastore");
		}
		
		// Then send the stocks back to the browser for display with a subset of the fields retrieved from Yahoo
		
		int i =0;
		for (StockInformation stockInfo : datas) {
			stockInfo.setSymbol(completeInfo[i].getSymbol());
			stockInfo.setPrice(completeInfo[i].getPrice());
			stockInfo.setChange(completeInfo[i].getChange());
			stockInfo.setPercentChange(completeInfo[i].getPercentChange());
			i++;
		}
		
		return datas;
	}

	private enum StockFields {
		SYMBOL(0) {
			@Override
			public String toString() {
				return ("symbol");
			}	
		},
		PRICE(1) {
			@Override
			public String toString() {
				return ("LastTradePriceOnly");
			}
		},
		CHANGE(2) {
			@Override 
			public String toString() {
				return ("Change");
			}
		},
		PERCENTCHANGE(3) {
			@Override
			public String toString() {
				return ("ChangeinPercent");
			}
		},
		PRICESALES(4) {
			@Override
			public String toString() {
			return ("PriceSales");
			}
		},
		PRICEBOOK(5) {
			@Override
			public String toString() {
			return ("PriceBook");
			}
		},
		EPSESTIMATECURRENTYEAR(6) {
			@Override
			public String toString() {
			return ("EPSEstimateCurrentYear");
			}
		},
		EPSESTIMATENEXTYEAR(7) {
			@Override
			public String toString() {
			return ("EPSEstimateNextYear");
			}
		},
		PRICEEPSESTIMATECURRENTYEAR(8) {
			@Override
			public String toString() {
			return ("PriceEPSEstimateCurrentYear");
			}
		},
		PRICEEPSESTIMATENEXTYEAR(9) {
			@Override
			public String toString() {
			return ("PriceEPSEstimateNextYear");
			}
		},
		PERATIO(10) {
			@Override
			public String toString() {
			return ("PERatio");
			}
		},
		PEGRATIO(9) {
			@Override
			public String toString() {
			return ("PEGRatio");
			}
		};
		
		private int value;
		private StockFields (int value) {
			this.value = value;
		}
		
		private int getValue() {
			return (this.value);
		}
	};
	
	private enum StockFieldsKeystats {
		SYMBOL(0) {
			@Override
			public String toString() {
				return ("symbol");
			}	
		},
		PROFITMARGIN(1) {
			@Override
			public String toString() {
				return ("ProfitMargin");
			}
		},
		RETURNONASSETS(2) {
			@Override 
			public String toString() {
				return ("ReturnonAssets");
			}
		},
		RETURNONEQUITY(3) {
			@Override
			public String toString() {
				return ("ReturnonEquity");
			}
		},
		TOTALDEBTEQUITY(4) {
			@Override
			public String toString() {
			return ("TotalDebtEquity");
			}
		},
		CURRENTRATIO(5) {
			@Override
			public String toString() {
			return ("CurrentRatio");
			}
		},
		PERCENTAGEHELDBYINSIDERS(6) {
			@Override
			public String toString() {
			return ("PercentageHeldbyInsiders");
			}
		};
		
		private int value;
		private StockFieldsKeystats (int value) {
			this.value = value;
		}
		
		private int getValue() {
			return (this.value);
		}
	};
	
	private Stock[] parseYahooResults (JsonParser parser) {

		count = 0;
		internalCounter =0;
		while (parser.hasNext() ) {
			JsonParser.Event event = parser.next();
			while (parser.hasNext() && 
					!(event.equals (JsonParser.Event.KEY_NAME) &&  
						parser.getString().equals(StockFields.PRICE.toString()))  &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFields.PERCENTCHANGE.toString()))   &&
					!(event.equals(JsonParser.Event.KEY_NAME)  &&  
						parser.getString().equals(StockFields.CHANGE.toString()))  &&
					!(event.equals(JsonParser.Event.KEY_NAME)&& 
						parser.getString().equals(StockFields.SYMBOL.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PRICESALES.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PRICEBOOK.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.EPSESTIMATECURRENTYEAR.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.EPSESTIMATENEXTYEAR.toString())) &&							
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PRICEEPSESTIMATECURRENTYEAR.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PRICEEPSESTIMATENEXTYEAR.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PERATIO.toString())) &&
					!(event.equals(JsonParser.Event.KEY_NAME)&&
						parser.getString().equals(StockFields.PEGRATIO.toString())) )
									
			    {
				event = parser.next();
				}
			
			    try {
				

				if (event.equals(JsonParser.Event.KEY_NAME )&& parser.getString().equals("symbol"))  {
					parser.next();
					symbol = parser.getString();
					if (symbol == null) {
						symbol = "UNKNOWN";
					}
					completeInfo[count].setSymbol(symbol);
					checkCountIncrement();
					continue;
				}
			
				if (event.equals(JsonParser.Event.KEY_NAME) && parser.getString().equals("LastTradePriceOnly")) {
					parser.next();
					sPrice = parser.getString();
					if (sPrice == null) {
						sPrice = "0";
					}
					price = Double.parseDouble(sPrice);
					completeInfo[count].setPrice(price);
					checkCountIncrement();
					continue;
				}
				
				if (event.equals(JsonParser.Event.KEY_NAME) && parser.getString().equals("ChangeinPercent")) {
					parser.next();
					sPercentChange = parser.getString();
					if (sPercentChange == null) {
						sPercentChange = "0";
					}
					sPercentChange = sPercentChange.replaceAll("%","");
					percentChange = Double.parseDouble(sPercentChange);
					completeInfo[count].setPercentChange(percentChange);
					checkCountIncrement();
					continue;
				}
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("Change")) {
					parser.next();
					sChange = parser.getString();
					if (sChange == null) {
						sChange = "0";
					}
					change = Double.parseDouble(sChange);
					completeInfo[count].setChange(change);
					checkCountIncrement();
					continue;
				}
				
			
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PriceSales")) {
					parser.next();
					sPriceSales = parser.getString();
					if (sPriceSales == null) {
						sPriceSales = "0";
					}
					priceSales = Double.parseDouble(sPriceSales);
					completeInfo[count].setPriceSales(priceSales);
					checkCountIncrement();
					continue;
				}
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PriceBook")) {
					parser.next();
					sPriceBook = parser.getString();
					if (sPriceBook == null) {
						sPriceBook = "0";
					}
					priceBook = Double.parseDouble(sPriceBook);
					completeInfo[count].setPriceBook(priceBook);
					checkCountIncrement();
					continue;
				}
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("EPSEstimateCurrentYear")) {
					parser.next();
					sEPSEstimateCurrentYear = parser.getString();
					if (sEPSEstimateCurrentYear == null) {
						sEPSEstimateCurrentYear = "0";
					}
					EPSEstimateCurrentYear = Double.parseDouble(sEPSEstimateCurrentYear);
					completeInfo[count].setOneYearEPS(EPSEstimateCurrentYear);
					checkCountIncrement();
					continue;
				}
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("EPSEstimateNextYear")) {
					parser.next();
					sEPSEstimateNextYear = parser.getString();
					if (sEPSEstimateNextYear == null) {
						sEPSEstimateNextYear = "0";
					}
					EPSEstimateNextYear = Double.parseDouble(sEPSEstimateNextYear);
					completeInfo[count].setEPSEstimateNextYear(EPSEstimateNextYear);
					checkCountIncrement();
					continue;
				}	
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PriceEPSEstimateCurrentYear")) {
					parser.next();
					sPriceEPSEstimateCurrentYear = parser.getString();
					if (sPriceEPSEstimateCurrentYear == null) {
						sPriceEPSEstimateCurrentYear = "0";
					}
					PriceEPSEstimateCurrentYear = Double.parseDouble(sPriceEPSEstimateCurrentYear);
					completeInfo[count].setPriceEstimateEPSCurrentYear(PriceEPSEstimateCurrentYear);
					checkCountIncrement();
					continue;
				}	
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PriceEPSEstimateNextYear")) {
					parser.next();
					sPriceEPSEstimateNextYear = parser.getString();
					if (sPriceEPSEstimateNextYear == null) {
						sPriceEPSEstimateNextYear = "0";
					}
					PriceEPSEstimateNextYear = Double.parseDouble(sPriceEPSEstimateNextYear);
					completeInfo[count].setPriceEstimateEPSNextYear(PriceEPSEstimateNextYear);
					checkCountIncrement();
					continue;
				}
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PERatio")) {
					parser.next();
					sPERatio = parser.getString();
					if (sPERatio == null) {
						sPERatio = "0";
					}
					PERatio = Double.parseDouble(sPERatio);
					completeInfo[count].setPERatio(PERatio);
					checkCountIncrement();
					continue;
				}
				
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().equals("PEGRatio")) {
					parser.next();
					sPEGRatio = parser.getString();
					if (sPEGRatio == null) {
						sPEGRatio = "0";
					}
					PEGRatio = Double.parseDouble(sPEGRatio);
					completeInfo[count].setPEGRatio(PEGRatio);
					checkCountIncrement();
					continue;
				}
				
					
				
			    } catch (IllegalStateException e) {
			    	//Simply increment the count
			    	checkCountIncrement();
			    	continue;
			    }
			
		}
		return (completeInfo);
	}
	
	
	private Stock[] parseYahooKeystatsResults(JsonParser parser) {
		//TODO : when symbols do not exist (e.g. OOOO), the JSON response from Yahoo is completely different
		//   and the parsing is incorrect. It creates a NullPointerException in PersistsResultsYahooQuote() method.
		countKeystats = 0;
		internalCounterKeystats =0;
		while (parser.hasNext() ) {
			JsonParser.Event event = parser.next();
			while (parser.hasNext() && 
					!(event.equals (JsonParser.Event.KEY_NAME) &&  
						parser.getString().equals(StockFieldsKeystats.SYMBOL.toString()))  &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.PROFITMARGIN.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.RETURNONASSETS.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.RETURNONEQUITY.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.TOTALDEBTEQUITY.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.CURRENTRATIO.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.PERCENTAGEHELDBYINSIDERS.toString())) &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().equals(StockFieldsKeystats.PROFITMARGIN.toString()))    )
									
			    {
				event = parser.next();
				}
			
			try {
			
			if (event.equals(JsonParser.Event.KEY_NAME )&& parser.getString().equals("symbol"))  {
				parser.next();
				symbol = parser.getString();
				if (symbol == null) {
					symbol = "UNKNOWN";
				}
				completeInfoKeystats[countKeystats].setSymbol(symbol);
				checkCountIncrementKeystats();
				continue;
			}
			
			if (event.equals(JsonParser.Event.KEY_NAME )&& parser.getString().equals("ProfitMargin")) {
				for (int  i = 1 ; i < 6 ; i++ ) {
					event = parser.next();
				}
				
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sProfitMargin = parser.getString()) == null) ||
							((sProfitMargin = parser.getString()).equals("N/A"))) {
						sProfitMargin = "0";
					}
					sProfitMargin = sProfitMargin.replaceAll("%","");
					profitMargin = Double.parseDouble(sProfitMargin);
					completeInfoKeystats[countKeystats].setProfitMargin(profitMargin);
					checkCountIncrementKeystats();
				}
			}
			
			if (event.equals(JsonParser.Event.KEY_NAME)&& parser.getString().equals("ReturnonAssets")) {
				for (int  i = 1 ; i < 6 ; i++ ) {
					event = parser.next();
				}
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sReturnonAssets = parser.getString()) == null) ||
							((sReturnonAssets = parser.getString()).equals("N/A"))) {
						sReturnonAssets = "0";
					}
					sReturnonAssets = sReturnonAssets.replaceAll("%","");
					returnonAssets = Double.parseDouble(sReturnonAssets);
					completeInfoKeystats[countKeystats].setROA(returnonAssets);
					checkCountIncrementKeystats();
				}
			}
				
			if (event.equals(JsonParser.Event.KEY_NAME)&& parser.getString().equals("ReturnonEquity")) {
				for (int  i = 1 ; i < 6 ; i++ ) {
					event = parser.next();
				}
				
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sReturnonEquity = parser.getString()) == null) ||
							((sReturnonEquity = parser.getString()).equals("N/A"))){
						sReturnonEquity = "0";
					}
					sReturnonEquity = sReturnonEquity.replaceAll("%","");
					returnonEquity = Double.parseDouble(sReturnonEquity);
					completeInfoKeystats[countKeystats].setROE(returnonEquity);
					checkCountIncrementKeystats();
				}
			}
			
			if (event.equals(JsonParser.Event.KEY_NAME)&& parser.getString().equals("TotalDebtEquity")) {
				for (int  i = 1 ; i < 6 ; i++ ) {
					event = parser.next();
				}
				
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sTotalDebtEquity = parser.getString()) == null) || 
					       ((sTotalDebtEquity = parser.getString()).equals("N/A")))  {
						sTotalDebtEquity = "0";
					}
					totalDebtEquity = Double.parseDouble(sTotalDebtEquity);
					completeInfoKeystats[countKeystats].setDebtEquity(totalDebtEquity);
					checkCountIncrementKeystats();
				}
			}
			
			if (event.equals(JsonParser.Event.KEY_NAME)&& parser.getString().equals("CurrentRatio")) {
				for (int  i = 1 ; i < 6 ; i++ ) {
					event = parser.next();
				}
				
				
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sCurrentRatio = parser.getString()) == null) ||
							((sCurrentRatio = parser.getString()).equals("N/A"))) {
						sCurrentRatio = "0";
					}
					currentRatio = Double.parseDouble(sCurrentRatio);
					completeInfoKeystats[countKeystats].setCurrent(currentRatio);
					checkCountIncrementKeystats();
				}
			}
			
			if (event.equals(JsonParser.Event.KEY_NAME)&& parser.getString().equals("PercentageHeldbyInsiders")) {	
				event = parser.next();
				if (event.equals (JsonParser.Event.VALUE_STRING)) {
					if (((sPercentageHeldbyInsiders = parser.getString()) == null) ||
							((sPercentageHeldbyInsiders = parser.getString()).equals("N/A"))) {
						sPercentageHeldbyInsiders = "0";
					}
					sPercentageHeldbyInsiders = sPercentageHeldbyInsiders.replaceAll("%","");
					percentageHeldbyInsiders = Double.parseDouble(sPercentageHeldbyInsiders);
					completeInfoKeystats[countKeystats].setManagementOwnership(percentageHeldbyInsiders);
					checkCountIncrementKeystats();
				}
			}
				
			}	catch (IllegalStateException e) {
				//Simply increment the count
		    	checkCountIncrementKeystats();
		    	continue;
			}
		}
		return (completeInfoKeystats);
	}

	private void PersistsResultsYahooQuote(Stock[] completeInfo, Stock[] completeInfoKeystats) throws NotLoggedInException {
		
		//Checking if the stock already exists in the datastore
		UtilityClass.checkLoggedIn();
		PersistenceManager pm = PMF.get().getPersistenceManager();	
		
		try {
			Query q = pm.newQuery(Stock.class, "user==u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<Stock> stocks = (List<Stock>) q.execute(UtilityClass.getUser());
			
			//for (Stock stock: stocks) {
			//	LOG.log(Level.INFO,"list retrieved from Query in PersistsYahooResults :" + stock.getSymbol());
			//}
			
			for (Stock stock : stocks) {
				for (Stock stockInfo : completeInfo) {
					// stock in data store, refresh with updated data
					if (stockInfo.getSymbol().equals(stock.getSymbol())) {
						stock.setPrice(stockInfo.getPrice());
						stock.setChange(stockInfo.getChange());
						stock.setPercentChange(stockInfo.getPercentChange());
						stock.setPriceSales(stockInfo.getPriceSales());
						stock.setPriceBook(stockInfo.getPriceBook());
						stock.setOneYearEPS(stockInfo.getOneYearEPS());
						stock.setEPSEstimateNextYear(stockInfo.getEPSEstimateNextYear());
						stock.setPriceEstimateEPSCurrentYear(stockInfo.getPriceEstimateEPSCurrentYear());
						stock.setPriceEstimateEPSNextYear(stockInfo.getPriceEstimateEPSNextYear());
						stock.setPERatio(stockInfo.getPERatio());
						stock.setPEGRatio(stockInfo.getPEGRatio());
						break;
					}
				}
				for (Stock keystatsInfo : completeInfoKeystats) {
					// stock in data store, refresh with updated data
					if (keystatsInfo.getSymbol().equals(stock.getSymbol())) {
						stock.setProfitMargin(keystatsInfo.getProfitMargin());
						stock.setROA(keystatsInfo.getROA());
						stock.setROE(keystatsInfo.getROE());
						stock.setDebtEquity(keystatsInfo.getDebtEquity());
						stock.setCurrent(keystatsInfo.getCurrent());
						stock.setManagementOwnership(keystatsInfo.getManagementOwnership());
						break;
					}
				}
						
							
						//LOG.log(Level.INFO, "before writing in data store, stock " + stock.getSymbol() + " has PEGRatio of : " + stock.getPEGRatio() + "\n");
						
						//Write in datastore
						pm.makePersistent(stock);
					}

			
		} finally {
			pm.close();
		}
	}

	/*	  
	 * Need to keep a counter for the parser. Count the number of times the parser has extracted 
	 * a field from the JSON. When all fields are read, iterate with next stock symbol.
	 */
	private void checkCountIncrement() {
		if (internalCounter == (StockFields.values().length -1)) {
			internalCounter=0;
			count++;
			return;
		}
		internalCounter++;
		}
	
	private void checkCountIncrementKeystats() {
		if (internalCounterKeystats == (StockFieldsKeystats.values().length -1)) {
			internalCounterKeystats =0;
			countKeystats++;
			return;
		}
		internalCounterKeystats++;
	}

	private void searchResults(JsonReader reader) {
		try{
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("results")) {
					getQuoteValues(reader);
				} else
					reader.skipValue();
			}
			reader.endObject();
		} catch (Exception e) {
			logger.log(Level.WARNING,"searchResults problem");
		}
		
	}

	private void getQuoteValues(JsonReader reader) {
		
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("quote")) {
					reader.beginObject();
					while (reader.hasNext()) {
						String name1 = reader.nextName();
						if (name1.equals("LastTradePriceOnly")) {
							sPrice = reader.nextString();
							continue;
						} else if (name1.equals("ChangeinPercent")) {
							sPercentChange = reader.nextString();
							continue;
						} else if (name1.equals("Change")) {
							sChange = reader.nextString();
							continue;
						} else {
							reader.skipValue();
						}
					}
				}
				
			}
			reader.endObject();
			} catch (Exception e) {
				logger.log(Level.WARNING,"getQuoteValues issue");
			}
		}
		
	}

