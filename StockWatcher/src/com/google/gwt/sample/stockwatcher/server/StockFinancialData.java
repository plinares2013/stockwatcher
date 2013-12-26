package com.google.gwt.sample.stockwatcher.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.stream.JsonReader;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.sample.stockwatcher.shared.StockInformation;

// Update Stocks information : all fields for each stock

public class StockFinancialData {
	
	//private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	
	private static final String YAHOO_URL = "http://query.yahooapis.com/v1/public/yql?q=select symbol, Change, EPSEstimateCurrentYear, " +
			"EPSEstimateNextYear, LastTradePriceOnly, ChangeinPercent, PriceSales, PriceBook, PERatio, PEGRatio, PriceEPSEstimateCurrentYear," +
			"PriceEPSEstimateNextYear from yahoo.finance.quotes where symbol in (";
	
	private static final String YAHOO_URL_KEYSTATS = "http://query.yahooapis.com/v1/public/yql?q=select symbol, ProfitMargin, ReturnonAssets, ReturnonEquity," +
			"TotalDebtEquity, CurrentRatio, PercentageHeldbyInsiders  from yahoo.finance.keystats where symbol in (";
	
	private static final String BW_URL = "http://investing.businessweek.com/research/stocks/financials/ratios.asp?ticker=";
	
	private static Logger logger = Logger.getLogger(StockServiceImpl.class.getName());
	
	String sPrice = "", sChange = "", sPercentChange = "", symbol="";
	String sPriceSales = "", sPriceBook = "", sEPSEstimateCurrentYear = "", sEPSEstimateNextYear = "";
	String sPriceEPSEstimateCurrentYear = "", sPriceEPSEstimateNextYear = "", sPERatio = "", sPEGRatio = "";
	String sProfitMargin = "", sReturnonAssets ="", sReturnonEquity = "", sTotalDebtEquity = "", sCurrentRatio ="", sPercentageHeldbyInsiders = "";
	Stock stockNew;
	double price = 0, change = 0, percentChange = 0, priceSales =0, priceBook = 0, PERatio = 0, PEGRatio = 0;
	double EPSEstimateCurrentYear = 0, EPSEstimateNextYear = 0, PriceEPSEstimateCurrentYear = 0, PriceEPSEstimateNextYear = 0;
	double profitMargin = 0, returnonAssets = 0, returnonEquity = 0, totalDebtEquity = 0, currentRatio = 0, percentageHeldbyInsiders = 0;
	StockInformation[] datas;
	Stock[] completeInfo, completeInfoKeystats, bwInfo;
	private int count = 0, countKeystats = 0;
	private int internalCounter=0, internalCounterKeystats=0;
	
	private Date date = null;
	private DateFormat dateFormat = new SimpleDateFormat("yyy:MM:dd HH:mm:ss");
	private String symb = null;

	
	public StockInformation[] getStockInformation (String[] symbols)  {
		
		String url="", url_keystats = "";
		String query = "";
		double quickRatio = 0;
		int j = 0;
		
		StringReader reader = null;
		JsonParser parser = null;
		
		datas = new StockInformation[symbols.length];
		completeInfo = new Stock[symbols.length];
		completeInfoKeystats = new Stock[symbols.length];
		bwInfo = new Stock[symbols.length];
		for (int i=0; i<symbols.length; i++) {
			datas[i] = new StockInformation();
			completeInfo[i] = new Stock();
			completeInfoKeystats[i] = new Stock();
			bwInfo[i] = new Stock();
		}
		
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
 *      Using Jersey 2.x with AppEngine, contact Yahoo JSON API yahoo.finance.quotes
 */     
 		Client client =  ClientBuilder.newClient();
 
		
		WebTarget target = client.target(url);
		
		Invocation invocation = target.request().buildGet();
		
		//Troubleshooting performance on AppEngine
		Response response = null;
		//iter = lSymbols.iterator();
		//symb = iter.next();
		
		//date = new Date();
		//logger.log(Level.INFO,"StockFinancial - starting call yahoo.finance.quotes for symbol " + symb + " at " + dateFormat.format(date));
		try {
			 response = invocation.invoke();
		} catch (Exception e) {
			logger.log(Level.WARNING,"Caught exception with invocation.invoke for Yahoo.finance.quotes call");
			//logger.log (Level.WARNING,e.getMessage());
			//logger.log(Level.SEVERE,e.toString(),e);
		}
		//date = new Date();
		//logger.log(Level.INFO,"StockFinancial - ending call yahoo.finance.quotes for symbol " + symb + " at " + dateFormat.format(date));

		// 	Check the status of the response and parse
		try {
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				query = response.readEntity (String.class);
				reader = new StringReader (query);
				parser = Json.createParser(reader);

				//Parse all information

				//Troubleshooting performance on AppEngine
				//date = new Date();
				//logger.log(Level.INFO,"StockFinancial - starting parsing result yahoo.finance.quotes for symbol " + symb + " at " + dateFormat.format(date));

				try {
					completeInfo = parseYahooResults (parser);
				} catch (Exception e) {
					// Using the fact that there is only 1 symbol in the array symbols[]
					logger.log(Level.WARNING,"Parsing error in parseYahooResults for symbol " + symbols[0]);
				}
				//Troubleshooting performance in AppEngine
				//date = new Date();
				//logger.log(Level.INFO,"StockFinancial - ending parsing result yahoo.finance.quotes for symbol " + symb + " at " + dateFormat.format(date));

			} else {
				return (null);
			}
		} catch (Exception e ) {
			logger.log(Level.WARNING,"Caught nullpointer Exception in YahooQuotes response");
		}
		

/*
 *     Contact another JSON API yahoo.finance.keystats to retrieve additional stock data
 */	
		try {
		target = client.target(url_keystats);
		
		invocation = target.request().buildGet();
		response = invocation.invoke();
		} catch (Exception e){
			logger.log(Level.WARNING,"Caught exception with invocation.invoke for Yahoo.finance.keystats call for symbol " + symbols[0]);
		}
		
		//Check the status of the response and parse
		try {
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				query = response.readEntity (String.class);
				reader = new StringReader (query);
				parser = Json.createParser(reader);	
				
				//Parse all information
				try {
					completeInfoKeystats = parseYahooKeystatsResults(parser);
				} catch (Exception e) {
					//Using the fact that there is only 1 symbol in the array symbols[]
					logger.log(Level.WARNING,"Parsing error with ParseYahooKeystatsResults for symbol " + symbols[0]);
				}
			} else {
				return (null);
			}
		} catch (Exception e) {
			//Using the fact that there is only 1 symbol in the array symbols[]
			logger.log(Level.WARNING,"After invocation.invoke  with Entity reader for symbol " + symbols[0]);
		}
	
		// Contact the Business Week site to get extra info per stock (Jsoup is used)
		j=0;
		iter = lSymbols.iterator();
		while (iter.hasNext()) {
			String symb = iter.next();
			// Troubleshooting performance in AppEngine
			//date = new Date();
			//logger.log(Level.INFO,"StockFinancial - calling BWscrape for symbol " + symb + " at " + dateFormat.format(date));
			
			quickRatio = scrapeBwData("http://investing.businessweek.com/research/stocks/financials/ratios.asp?ticker=" + symbol);
			bwInfo[j].setSymbol(symb);
			bwInfo[j].setQuickRatio(quickRatio);
			j++;
			
			//Troubleshooting performance in AppEngine
			//date = new Date();
			//logger.log(Level.INFO,"StockFinancial - ending BWscrape for symbol " + symb + " at " + dateFormat.format(date));
		}
	    
		//Make freshly retrieved data persistent in database
		try {
			if ((PersistsResultsYahooQuote (completeInfo, completeInfoKeystats, bwInfo)) != 0) {
				return (null);
			}
		} catch (Exception e ){
			logger.log(Level.WARNING,"Cannot store complete info in datastore");
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

	private double scrapeBwData(String url) {
		int index =0;
		Elements floatR;
		Element div;
		String sQuickRatio = "";
		double quickRatio = 0;
	    String html = getUrl(url);
	    if (html.equals("Error connecting to the URL")) {
	    	return (0);
	    }
	    try {
		    Document doc = Jsoup.parse(html);
		    //If symbol does not exist return 0
		    Elements errorList= doc.getElementsMatchingOwnText("No matches found");
		    if (!errorList.isEmpty()) {
		    	return (0);
		    }
		    //Fetch the data when symbol exists
		    Elements ratioTable = doc.getElementsByClass("ratioTable");
		    outerloop:
		    for (Element table: ratioTable) {
		    	Elements rowList = table.getElementsByTag("tr");
		    	for (Element row: rowList) {
		    		Elements floatList = row.getElementsByClass("floatL");
		    		for (Element bold: floatList) {
		    			Elements boldList = bold.getElementsByClass("bold");
		    			index = 0;
		    			for (Element item: boldList) {
		    				index++;
		    				if (item.html().equals("Quick Ratio")) {
		    					floatR = row.getElementsByClass("floatR");
		    					div = floatR.get(index).child(0);
		    					sQuickRatio = div.html();
		    					break outerloop;
		    				}
		    			}
		    		}
		    	}
		    }
		    //System.out.println("File has been read and value for quickRation is " + sQuickRatio);
		    if (sQuickRatio.equals("--") || sQuickRatio.equals("") || sQuickRatio.equals("NM")) {
		    	quickRatio = 0;
		    } else {
			    sQuickRatio = normalize(sQuickRatio);
			    quickRatio = Double.parseDouble(sQuickRatio);
		    }
	    } catch (Exception e) {
	    	logger.log(Level.WARNING,"Caught exception in srapeBwData");
	    }


	    return quickRatio;
	}

	private String getUrl(String url) {
	    URL urlObj = null;
	    try{
	      urlObj = new URL(url);
	    }
	    catch(MalformedURLException e){
	      System.out.println("The url was malformed!");
	      return "";
	    }
	    URLConnection urlCon = null;
	    BufferedReader in = null;
	    String outputText = "";
	    try{
	      urlCon = urlObj.openConnection();
	      in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
	      String line = "";
	      while((line = in.readLine()) != null){
	        outputText += line;
	      }
	      in.close();
	    }catch(IOException e){
	      System.out.println("There was an error connecting to the URL : " + url);
	      return "Error connecting to the URL";
	    }
	    return outputText;
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
		PEGRATIO(11) {
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
					sPrice = normalize(sPrice);
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
					sPercentChange = normalize(sPercentChange);
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
					sChange = normalize(sChange);
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
					sPriceSales = normalize(sPriceSales);
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
					sPriceBook = normalize(sPriceBook);
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
					sEPSEstimateCurrentYear = normalize(sEPSEstimateCurrentYear);
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
					sEPSEstimateNextYear = normalize(sEPSEstimateNextYear);
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
					sPriceEPSEstimateCurrentYear = normalize (sPriceEPSEstimateCurrentYear);
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
					sEPSEstimateNextYear = normalize(sEPSEstimateNextYear);
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
					sPERatio = normalize(sPERatio);
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
					sPEGRatio = normalize(sPEGRatio);
					PEGRatio = Double.parseDouble(sPEGRatio);
					completeInfo[count].setPEGRatio(PEGRatio);
					checkCountIncrement();
					continue;
				}
				
					
				
			    } catch (IllegalStateException e) {
			    	//logger.log(Level.WARNING,"Caught Exception in parseYahooResults");
			    	//logger.log(Level.SEVERE,e.toString(),e);
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
					sProfitMargin = normalize(sProfitMargin);
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
					sReturnonAssets = normalize(sReturnonAssets);
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
					sReturnonEquity = normalize(sReturnonEquity);
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
					sTotalDebtEquity = normalize(sTotalDebtEquity);
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
					sCurrentRatio = normalize(sCurrentRatio);
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
					sPercentageHeldbyInsiders = normalize(sPercentageHeldbyInsiders);
					percentageHeldbyInsiders = Double.parseDouble(sPercentageHeldbyInsiders);
					completeInfoKeystats[countKeystats].setManagementOwnership(percentageHeldbyInsiders);
					checkCountIncrementKeystats();
				}
			}
				
			}	catch (IllegalStateException e) {
		    	//logger.log(Level.WARNING,"Caught Exception in parseYahooKeyStatResults");
		    	//logger.log(Level.SEVERE,e.toString(),e);
				//Simply increment the count
		    	checkCountIncrementKeystats();
		    	continue;
			}
		}
		return (completeInfoKeystats);
	}

	private String normalize(String valueFloat) {
		valueFloat = valueFloat.replaceAll("x",""); // Get rid of x
		valueFloat = valueFloat.replaceAll(",","");  // Get rid of "," in the number
		valueFloat = valueFloat.replaceAll(" ","");  // Get rid of " " in the number
		valueFloat = valueFloat.replaceAll("%","");  //Get rid of %
		return (valueFloat);
	}

	private int PersistsResultsYahooQuote(Stock[] completeInfo, Stock[] completeInfoKeystats, Stock[] bwInfo) throws NotLoggedInException {
		
		//No need to check if the stock exists already in the datastore. The stock is already there!
		//TODO: see the protection of this access to the DB. COnfigured in the cron.xml file

		//TODO: there is no user in the datastore when Stock are created in the backend. Safer to add a user
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String symbol = null;
		
		//Get the symbol of the first stock. Taking advantage that completeInfo is an array of 1 element
		//If symbol from completeInfo and completeInfoKeystats is null, log error
		//TODO - Handle case when pointer is null
		// TODO - Handle the case when all parameters of PersistsResultsYahooQuote are arrays of more than 1 element
	  try{
			
		if (completeInfo[0] != null) {
			if ((symbol = completeInfo[0].getSymbol()).equals(null)) {
				if (completeInfoKeystats[0] != null) {
					if ((symbol = completeInfoKeystats[0].getSymbol()).equals(null)) {
						logger.log(Level.WARNING,"Stock ticker is null upon entry PersistsYahooResults");
					}
				}
			}
		}

		try {
			//Query q = pm.newQuery(Stock.class, "user==u");
			Query q = pm.newQuery(Stock.class, "symbol==sym");
			//q.declareParameters("com.google.appengine.api.users.User u");
			q.declareParameters("String sym");
			//q.setOrdering("createDate");
			//List<Stock> stocks = (List<Stock>) q.execute(UtilityClass.getUser());
			List<Stock> stocks = (List<Stock>) q.execute(symbol);
			
			//for (Stock stock: stocks) {
			//	LOG.log(Level.INFO,"list retrieved from Query in PersistsYahooResults :" + stock.getSymbol());
			//}

			if (stocks.isEmpty()) {
				logger.log(Level.WARNING,"Stock " + symbol + "does not exist in table");
				return (-1);
			}
				
			
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

				for (Stock bwData: bwInfo) {
					if (bwData.getSymbol().equals(stock.getSymbol())) {
						stock.setQuickRatio(bwData.getQuickRatio());
						stock.setLastUpdated(new Date());
						break;
					}
				}
			
				
						logger.log(Level.INFO, "before writing in data store, stock " + stock.getSymbol() + " has PEGRatio of : " + stock.getPEGRatio() + "\n");
						
						//Write in datastore
				pm.makePersistent(stock);
				}
			} finally {
			pm.close();
			}
			
		} catch (Exception e) {
			logger.log(Level.WARNING,"Got exception in PersistsResultsYahooQuote");
			//logger.log(Level.SEVERE,e.toString(),e);
			return (-1);
		}	
		
	return(0);	
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
