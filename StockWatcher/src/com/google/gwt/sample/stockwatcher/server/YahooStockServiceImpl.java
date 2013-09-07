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


public class YahooStockServiceImpl extends RemoteServiceServlet implements
		YahooQuoteService {
	
	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	private static final String YAHOO_URL = "http://query.yahooapis.com/v1/public/yql?q=select symbol, ChangeinPercent, LastTradePriceOnly, Change from yahoo.finance.quotes where symbol in (";
	
	private static Logger logger = Logger.getLogger("Client Logger");
	String sPrice = "", sChange = "", sPercentChange = "", symbol="";
	Stock stockNew;
	double price = 0, change = 0, percentChange = 0;
	StockInformation[] datas;
	private int count = 0;
	private int internalCounter=0;

	
	public StockInformation[] getStockInformation (String[] symbols)  {
		
		String url="";
		String query = "";
		

		int check = symbols.length;
		datas = new StockInformation[symbols.length];
		for (int i=0; i<symbols.length; i++) {
			datas[i] = new StockInformation();
		}
	
/*
 * 		try {
 
			url = YAHOO_URL + MyURLEncode.URLencoding("select symbol, ChangeinPercent, LastTradePriceOnly, Change from yahoo.finance.quotes where symbol in (", "UTF-8") +
					MyURLEncode.URLencoding("\"" + symbol + "\")", "UTF-8") + "&format=json&env=store://datatables.org/alltableswithkeys";
		}  catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING,"Encoding error in getStockInformation(String symbol)");
		}
*/	

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
 *      Using Jersey 2.x with AppEngine
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
		datas = parseYahooResults (parser);
		
//     
		
		//Make freshly retrieved data persistent in database
		try {
			PersistsResultsYahooQuote (datas);
		} catch (NotLoggedInException e ){
			LOG.log(Level.WARNING,"Cannot store stock info in datastore");
		}
		
		// Then send the stocks back to the browser for display
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
		};
		
		private int value;
		private StockFields (int value) {
			this.value = value;
		}
		
		private int getValue() {
			return (this.value);
		}
	};
	
	private StockInformation[] parseYahooResults (JsonParser parser) {

		//TODO - Replace all references to JSON fields with an Enum and a loop.
		
		count = 0;
		while (parser.hasNext() ) {
			JsonParser.Event event = parser.next();
			while (parser.hasNext() && !(event.equals (JsonParser.Event.KEY_NAME) &&  
					parser.getString().matches(StockFields.PRICE.toString()))  &&
					!(event.equals (JsonParser.Event.KEY_NAME) && 
						parser.getString().matches(StockFields.PERCENTCHANGE.toString()))   &&
					!(event.equals(JsonParser.Event.KEY_NAME)  &&  
						parser.getString().matches(StockFields.CHANGE.toString()))  &&
					!(event.equals(JsonParser.Event.KEY_NAME)&& 
							parser.getString().matches(StockFields.SYMBOL.toString())) ) {
				event = parser.next();
				}
			
				if (event.equals(JsonParser.Event.KEY_NAME )&& parser.getString().matches("symbol"))  {
					parser.next();
					symbol = parser.getString();
					if (symbol == null) {
						symbol = "UNKNOWN";
					}
					datas[count].setSymbol(symbol);
					checkCountIncrement();
					continue;
				}
			
				if (event.equals(JsonParser.Event.KEY_NAME) && parser.getString().matches("LastTradePriceOnly")) {
					parser.next();
					sPrice = parser.getString();
					if (sPrice == null) {
						sPrice = "0";
					}
					price = Double.parseDouble(sPrice);
					datas[count].setPrice(price);
					checkCountIncrement();
					continue;
				}
				
				if (event.equals(JsonParser.Event.KEY_NAME) && parser.getString().matches("ChangeinPercent")) {
					parser.next();
					sPercentChange = parser.getString();
					if (sPercentChange == null) {
						sPercentChange = "0";
					}
					sPercentChange = sPercentChange.replaceAll("%","");
					percentChange = Double.parseDouble(sPercentChange);
					datas[count].setPercentChange(percentChange);
					checkCountIncrement();
					continue;
				}
				
				if (event.equals (JsonParser.Event.KEY_NAME) && parser.getString().matches("Change")) {
					parser.next();
					sChange = parser.getString();
					if (sChange == null) {
						sChange = "0";
					}
					change = Double.parseDouble(sChange);
					datas[count].setChange(change);
					checkCountIncrement();
					continue;
				}	
		}
		return (datas);
	}

	private void PersistsResultsYahooQuote(StockInformation[] datas) throws NotLoggedInException {
		
		//Checking if the stock already exists in the datastore
		UtilityClass.checkLoggedIn();
		PersistenceManager pm = PMF.get().getPersistenceManager();	
		
		try {
			Query q = pm.newQuery(Stock.class, "user==u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<Stock> stocks = (List<Stock>) q.execute(UtilityClass.getUser());
			
			for (StockInformation stockInfo : datas) {
				for (Stock stock : stocks) {
					// If stock is already in datastore, refresh with updated data
					if (stock.getSymbol().equals(stockInfo.getSymbol())) {
						stock.setPrice(stockInfo.getPrice());
						stock.setChange(stockInfo.getChange());
						stock.setPercentChange(stockInfo.getChangePercent());
						pm.makePersistent(stock);
						break;
					}
				}
				//If we execute this code, then we have a new Stock
				stockNew = new Stock (UtilityClass.getUser(), stockInfo.getSymbol(),stockInfo.getPrice(), 
						stockInfo.getChange(),stockInfo.getChangePercent());

				pm.makePersistent(stockNew);
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
		if (internalCounter == 3) {
			internalCounter=0;
			count++;
			return;
		}
		internalCounter++;
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

