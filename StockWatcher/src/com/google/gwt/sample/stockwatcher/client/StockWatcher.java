package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.http.client.Request; 
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback; 
import com.google.gwt.http.client.RequestException; 
import com.google.gwt.http.client.Response;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.sample.stockwatcher.client.QueryYQL.Quote;
import com.google.gwt.sample.stockwatcher.shared.FieldVerifier;
import com.google.gwt.sample.stockwatcher.shared.StockInformation;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {

private static final int REFRESH_INTERVAL = 10000; // ms	
private VerticalPanel mainPanel = new VerticalPanel();  
private FlexTable stocksFlexTable = new FlexTable();  
private HorizontalPanel addPanel = new HorizontalPanel();  
private TextBox newSymbolTextBox = new TextBox();  
private Button addStockButton = new Button("Add");  
private Label lastUpdatedLabel = new Label();
private ArrayList<String> stocks = new ArrayList<String>();
private Label errorMsgLabel = new Label();

private LoginInfo loginInfo = null;
private VerticalPanel loginPanel = new VerticalPanel();
private Label loginLabel = new Label("Please sign in to your Google Account to access the StockWatcher application.");
private Anchor signInLink = new Anchor("Sign In");
private Anchor signOutLink = new Anchor("Sign Out");

private final StockServiceAsync stockService = GWT.create(StockService.class);
private final YahooQuoteServiceAsync yQuoteService = GWT.create(YahooQuoteService.class);

//private static final String JSON_URL = GWT.getModuleBaseURL() + "stockPrices?q=";
private static final String JSON_URL = "http://query.yahooapis.com/v1/public/yql?q=select symbol, " +
		"ChangeinPercent, LastTradePriceOnly, Change from yahoo.finance.quotes where symbol in (";
private StockInformation data = new StockInformation();

private StockInformation[] datas;

private static Logger logger = Logger.getLogger("Client Logger");

/**  * Entry point method.  */  
public void onModuleLoad() { 
	   // Check login status using login service.
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
    	  handleError(error);
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result;
        if(loginInfo.isLoggedIn()) {
          loadStockWatcher();
        } else {
          loadLogin();
        }
      }
    });
	
	}

private void loadLogin() {
	//Build login panel
	signInLink.setHref(loginInfo.getLoginUrl());
	loginPanel.add(signInLink);
	loginPanel.add(loginLabel);
	RootPanel.get("stockList").add(loginPanel);
}

private void loadStockWatcher() {
	
	//Offer possibility to logout
	signOutLink.setHref(loginInfo.getLogoutUrl());
	
	//Initialize service on the server - Not needed as this is done on the server side.
   /* StockIndexServiceAsync stockIndexService = GWT.create(StockIndexService.class);
	*
	*stockIndexService.init (new AsyncCallback<Void>() {
	*	public void onFailure (Throwable error) {
	*		handleError(error);
	*	}
	*	
	*	@Override
	*	public void onSuccess(Void ignore) {
	*		logger.log(Level.WARNING,"StockIndexService is initialized");
	*	}
	*});
	*/
	
	// Create table for stock data.  
	stocksFlexTable.setText(0, 0, "Symbol");  
	stocksFlexTable.setText(0, 1, "Price");  
	stocksFlexTable.setText(0, 2, "Change");  
	stocksFlexTable.setText(0, 3, "Remove");
	stocksFlexTable.setText(0, 4, "Calculate");
	stocksFlexTable.setText(0, 5, "Calculate Results");
	
    // Add styles to elements in the stock list table.
    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
    stocksFlexTable.addStyleName("watchList");
    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 4, "watchListCalculateColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 5, "watchListCalculateResultsColumn");
    
	 // Assemble Add Stock panel.
    addPanel.add(newSymbolTextBox);
    addPanel.add(addStockButton);
    addPanel.addStyleName("addPanel");
    //addPanel.addStyleName("addPanel");
    
    errorMsgLabel.setStyleName("errorMessage"); 
    errorMsgLabel.setVisible(false);

    // Assemble Main panel.
    mainPanel.add(errorMsgLabel);
 	mainPanel.add(signOutLink);
    mainPanel.add(stocksFlexTable);
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);
    lastUpdatedLabel.setHeight("33px");
    
    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);
    //mainPanel.setSize("430px", "280px");
    mainPanel.setVisible(true);
    
    // Move cursor focus to the input box.
    newSymbolTextBox.setFocus(true);
    
    //Retrieve stocks from persistence Datastore - Not any more with 1000 stocks in datastore
    //loadStocks();
    
    // Setup timer to refresh list automatically.
    Timer refreshTimer = new Timer() {
      @Override
      public void run() {
        refreshWatchList();
      }
    };
    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
    
    // Listen for mouse events on the Add button.
    addStockButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addStock();
      }
    });
 // Listen for keyboard events in the input box.
    newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          addStock();
        }
      }
    });
}

/**
 * Add stock to FlexTable. Executed when the user clicks the addStockButton or
 * presses enter in the newSymbolTextBox.
 */
protected void addStock() {
	   final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
	    newSymbolTextBox.setFocus(true);

	    // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
	    if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
	      Window.alert("'" + symbol + "' is not a valid symbol.");
	      newSymbolTextBox.selectAll();
	      return;
	    }

	    newSymbolTextBox.setText("");

	    //Don't add the stock if it's already in the table.
	    if (stocks.contains(symbol))
	        return;
	    
	    //Display stock
	    addStock (symbol);
	
}

//Display symbol
private void addStock(final String symbol) {
	
	//StockServiceAsync stockService = GWT.create(StockService.class);
	
	//stockService.addStock (symbol, new AsyncCallback<Void>() {
	//	public void onFailure (Throwable error) {
	//		handleError(error);
	//	}
		
	//	@Override
	//	public void onSuccess(Void ignore) {
	//		displayStock(symbol);
	//	}
	//});
	
	displayStock(symbol);
}

//Logic to display stocks

public void displayStock (final String symbol) {
 
	// Add the stock to the table.
    int row = stocksFlexTable.getRowCount();
    stocks.add(symbol);
    stocksFlexTable.setText(row, 0, symbol);
    
    stocksFlexTable.setWidget(row, 2, new Label());

    // Add a button to remove this stock from the table.
    Button removeStockButton = new Button("x");
    removeStockButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
    	//removeStockSymbolInDatastore(symbol);
        int removedIndex = stocks.indexOf(symbol);
        stocks.remove(removedIndex);        
        stocksFlexTable.removeRow(removedIndex + 1);
      }
    });
    stocksFlexTable.setWidget(row, 3, removeStockButton);
    
    //Add a button to calculate the worthiness value associated  to the stock
    Button calculateStockWorthButton = new Button("Calc");
    calculateStockWorthButton.addClickHandler(new ClickHandler() {
    	public void onClick (ClickEvent event) {
    		calculateStockIndex(symbol);
    	}	
    });
    stocksFlexTable.setWidget(row, 4, calculateStockWorthButton);
    
    stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
    removeStockButton.addStyleDependentName("remove");
    stocksFlexTable.getCellFormatter().addStyleName(row, 4, "watchListRemoveColumn");
    calculateStockWorthButton.addStyleDependentName("worthindex");
    stocksFlexTable.getCellFormatter().addStyleName(row, 5, "watchListCalculateResultsColumn");
    //Get the stock price.
    refreshWatchList();	
}

protected void calculateStockIndex(final String symbol) {
	
	StockIndexServiceAsync stockIndexService = GWT.create(StockIndexService.class);
	
	stockIndexService.calculateWorth (symbol, new AsyncCallback<String>() {
		public void onFailure (Throwable error) {
			handleError(error);
		}
		
		@Override
		public void onSuccess(String worth) {
			displayStockIndex(symbol, worth);
		}
	});
}

protected void displayStockIndex(String symbol, String worth) {
	//Identify the row in the table associated to the symbol
	int row = 0;
	for (String str : stocks) {
		if (str.equals(symbol)) {
			break;
		}
		row++;	
	}
	
	//  Write the stock worth index in the main StockFlex table
    stocksFlexTable.setText(row+1 , 5, worth);
    
}

/**
 * Remove the stock from the datastore - Not used any more from client side.
 */
private void removeStockSymbolInDatastore(String symbol) {
	AsyncCallback<Void> callback = new AsyncCallback<Void> () {
		@Override
		public void onFailure(Throwable caught) {
			handleError (caught);	
		}
		@Override
		public void onSuccess(Void result) {
			logger.log(Level.WARNING,"Symbol deleted from Datastore.");
		}
	};
	
	stockService.removeStock(symbol, callback);
	logger.log(Level.WARNING,"Request to delete a symbol in DataStore");
}

/**
 *  Generate stock price
 */

private void refreshWatchList () {
	//String url = JSON_URL;

	//Nothing to refresh if the list is empty
	if (stocks.isEmpty()) {
		return;
	}
	
	// Append watch list stock symbols to query URL.
	//Iterator<String> iter = stocks.iterator();
	//while (iter.hasNext()) {
	//  url += "\"" + iter.next() + "\"";
	//  if (iter.hasNext()) {
	//    url += ",";
	//  } else {
	//    url += ")&format=json&env=store://datatables.org/alltableswithkeys";
	//  }
	//}

	//url = URL.encode(url);
	
	//First method to refresh stock data is to contact
	// the Yahoo REST API directly from the client (this program)
	
/*
 * 	  JsonpRequestBuilder builder = new JsonpRequestBuilder();
	    builder.requestObject(url, new AsyncCallback<QueryYQL>() {
	      public void onFailure(Throwable caught) {
	        displayError("Couldn't retrieve JSON");
	      }
	      
	      public void onSuccess(QueryYQL query) {
	        if (query == null) {
	        	displayError ("No JSON data retrieved");
	        	return;
	        }
	        
	        String symbol, sPrice, sChange, sPercentChange;
	        double price, change, percentChange;
	        StockInformation[] datas;
	       
	        
	        int count;
	        
	        count = query.getCount();
	    
	        if (count==0) {
	        	logger.log(Level.WARNING,"No JSON returned for the requested stock");
	        	return;
	        }
	        
	        datas = new StockInformation[count];
	        
	        for (int i=0; i<query.getCount(); i++) {
	        	Quote quote = query.getQuote(i);
	        	symbol = quote.getSymbol();
	        	if ((sPrice = quote.getLastTradePriceOnly()) == null){
	        		sPrice = "0";
	        	}else {
	        		sPrice = sPrice.replaceAll("\"", "");
	        	}
	        	price = Double.parseDouble(sPrice);
	        	if ((sChange = quote.getChange()) == null) {
	        		sChange="0";
	        	} else {
	        		sChange = sChange.replaceAll("\"","");
	        	}
	        	change = Double.parseDouble(sChange);
	        	if ((sPercentChange = quote.getChangeinPercent()) == null) {
	        		sPercentChange = "0";
	        	} else {
	        		sPercentChange = sPercentChange.replaceAll("\"", "");
	        		sPercentChange = sPercentChange.replaceAll("%","");
	        	}
	        	percentChange = Double.parseDouble(sPercentChange);
	        	
	        	datas[i] = new StockInformation();
	        	datas[i].setSymbol(symbol);
	        	datas[i].setPrice(price);
	        	datas[i].setChange(change);
	        	datas[i].setPercentChange(percentChange);
	        }
        	updateTable(datas);
	      }
	    });        
*/
	    //Second method to refresh a stock is to call the server using RPC 
	    // and have the server retrieve the stock information from the data store.
	    // The data sent back from the server is a StockInformation[] .
	    
	    
	 

			  AsyncCallback<StockInformation[]> yCallback = new AsyncCallback<StockInformation[]> () {
				  public void onFailure (Throwable error) {
				        displayError("Couldn't retrieve stock info from stockService");
				  }
				  
				  public void onSuccess (StockInformation stockInfo[]) {
					  StockInformation[] datas = stockInfo;
					  updateTable(datas);
				  }
			  };
			  stockService.getStockInformation(stocks.toArray(new String[stocks.size()]), yCallback);
		  	
}

/**  * If can't get information from backend server, display error message.  
 **    @param error  
 **/  
private void displayError(String error) {  
	errorMsgLabel.setText("Error: " + error);  
	errorMsgLabel.setVisible(true);  } 

/**
 * Update the Price and Change fields all the rows in the stock table.
 *
 * @param prices Stock data for all rows.
 */

  

/**
 * Update a single row in the stock table.
 *
 * @param price Stock data for a single row.
 */

// Update the information of the stocks in the table
private void updateTable(StockInformation[] infos) {
	//logger.log(Level.WARNING, "infos length = " + infos.length);
	for ( int i=0; i < infos.length ; i++) {
		updateTable(infos[i]);
	}
	
	// Display timestamp showing last refresh.  
    lastUpdatedLabel.setText("Last update : "  + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
  
    // Clear any errors.  
    errorMsgLabel.setVisible(false);
 }

// Update one line for each stock in the table of stocks
private void updateTable(StockInformation info) {
	   // Make sure the stock is still in the stock table.
 if (!stocks.contains(info.getSymbol())) {
   return;
 }

 int row = stocks.indexOf(info.getSymbol()) + 1;

 // Format the data in the Price and Change fields.
 String priceText = NumberFormat.getFormat("#,##0.00").format(
     info.getPrice());
 NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
 String changeText = changeFormat.format(info.getChange());
 String changePercentText = changeFormat.format(info.getChangePercent());

 // Populate the Price and Change fields with new data.
 stocksFlexTable.setText(row, 1, priceText);
 Label changeWidget = (Label)stocksFlexTable.getWidget(row, 2);
 changeWidget.setText(changeText + " (" + changePercentText + "%)");
 
 // Change the color of text in the Change field based on its value.
 String changeStyleName = "noChange";
 if (info.getChangePercent() < -0.1f) {
   changeStyleName = "negativeChange";
 }
 else if (info.getChangePercent() > 0.1f) {
   changeStyleName = "positiveChange";
 }

 changeWidget.setStyleName(changeStyleName);
	
}

// This function is not called. Now all stock management is done in the back end with 1000s of stocks.
private void loadStocks() {
	
	AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {  
		public void onFailure(Throwable error) {
			handleError(error);
		}  
		@Override
		public void onSuccess(String[] results) { 
			if (results.length == 0 ){
				return;
			}
				
			for (String symbol : results) {
				displayStock(symbol);
			}  
		}
	};	

	stockService.getStocks(callback);
}
private void  handleError (Throwable error) {
	Window.alert (error.getMessage());
	if (error instanceof NotLoggedInException) {
		Window.Location.replace(loginInfo.getLogoutUrl());
	}
}
}
