package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.http.client.Request; 
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback; 
import com.google.gwt.http.client.RequestException; 
import com.google.gwt.http.client.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.sample.stockwatcher.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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

private static final int REFRESH_INTERVAL = 5000; // ms	
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

private static final String JSON_URL = GWT.getModuleBaseURL() + "stockPrices?q=";

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
	
	// Create table for stock data.  
	stocksFlexTable.setText(0, 0, "Symbol");  
	stocksFlexTable.setText(0, 1, "Price");  
	stocksFlexTable.setText(0, 2, "Change");  
	stocksFlexTable.setText(0, 3, "Remove");
	
    // Add styles to elements in the stock list table.
    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
    stocksFlexTable.addStyleName("watchList");
    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
    
    //Retrieve stocks from persistence Datastore
    loadStocks();
    
	 // Assemble Add Stock panel.
    addPanel.add(newSymbolTextBox);
    addPanel.add(addStockButton);
    addPanel.addStyleName("addPanel");
    addPanel.addStyleName("addPanel");
    
    errorMsgLabel.setStyleName("errorMessage"); 
    errorMsgLabel.setVisible(false);

    // Assemble Main panel.
    mainPanel.add(errorMsgLabel);
 	mainPanel.add(signOutLink);
    mainPanel.add(stocksFlexTable);
    //stocksFlexTable.setSize("283px", "58px");
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);
    lastUpdatedLabel.setHeight("33px");
    
    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);
    //mainPanel.setSize("430px", "280px");
    mainPanel.setVisible(true);
    
    // Move cursor focus to the input box.
    newSymbolTextBox.setFocus(true);
    
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

//Persist in Datastore then display symbol
private void addStock(final String symbol) {
	StockServiceAsync stockService = GWT.create(StockService.class);
	
	stockService.addStock (symbol, new AsyncCallback<Void>() {
		public void onFailure (Throwable error) {
			handleError(error);
		}
		
		@Override
		public void onSuccess(Void ignore) {
			displayStock(symbol);
		}
	});
}

//Logic to display stocks

public void displayStock (final String symbol) {
 
	// Add the stock to the table.
    int row = stocksFlexTable.getRowCount();
    stocks.add(symbol);
    stocksFlexTable.setText(row, 0, symbol);

    // Add a button to remove this stock from the table.
    Button removeStockButton = new Button("x");
    removeStockButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
    	removeStockSymbolInDatastore(symbol);
        int removedIndex = stocks.indexOf(symbol);
        stocks.remove(removedIndex);        
        stocksFlexTable.removeRow(removedIndex + 1);
      }
    });
    stocksFlexTable.setWidget(row, 3, removeStockButton);
    stocksFlexTable.setWidget(row, 2, new Label());
    
    stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
    stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
    removeStockButton.addStyleDependentName("remove");

    //Get the stock price.
    refreshWatchList();	
}

/**
 * Remove the stock from the datastore
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
private void refreshWatchList_orig() {
    //Auto-generated method stub
    final double MAX_PRICE = 100.0; // $100.00
    final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

    StockPrice[] prices = new StockPrice[stocks.size()];
    for (int i = 0; i < stocks.size(); i++) {
      double price = Random.nextDouble() * MAX_PRICE;
      double change = price * MAX_PRICE_CHANGE
          * (Random.nextDouble() * 2.0 - 1.0);

      prices[i] = new StockPrice(stocks.get(i), price, change);
    }

   // updateTable(prices);
  }

private void refreshWatchList () {
	String url = JSON_URL;

	// Append watch list stock symbols to query URL.
	Iterator<String> iter = stocks.iterator();
	while (iter.hasNext()) {
	  url += iter.next();
	  if (iter.hasNext()) {
	    url += "+";
	  }
	}

	url = URL.encode(url);

	  // Send request to server and catch any errors.
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

    try {
      Request request = builder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          displayError("Couldn't retrieve JSON");
        }

        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            updateTable  ((JsArray<StockData>) JsonUtils.safeEval(response.getText()));
          } else {
            displayError("Couldn't retrieve JSON (" + response.getStatusText()
                + ")");
          }
        }
      });
    } catch (RequestException e) {
      displayError("Couldn't retrieve JSON");
    }
}

/**  * If can't get JSON, display error message.  
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

/* 
 *  Unused code
 *
 * private void updateTable_orig (StockPrice[] prices) {
 *    // Auto-generated method stub
 *   for (int i = 0; i < prices.length; i++) {
 *       //updateTable(prices[i]);
 *     }
 *}
 */   
    private void updateTable(JsArray<StockData> prices) {
        // Auto-generated method stub
        for (int i = 0; i < prices.length(); i++) {
            updateTable(prices.get(i));
          }
    
 // Display timestamp showing last refresh.  
    lastUpdatedLabel.setText("Last update : "  + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
  
 // Clear any errors.  
    errorMsgLabel.setVisible(false);
    }


/**
 * Update a single row in the stock table.
 *
 * @param price Stock data for a single row.
 */
// private void updateTable_orig(StockPrice price) Original call

private void updateTable (StockData price) {
	   // Make sure the stock is still in the stock table.
    if (!stocks.contains(price.getSymbol())) {
      return;
    }

    int row = stocks.indexOf(price.getSymbol()) + 1;

    // Format the data in the Price and Change fields.
    String priceText = NumberFormat.getFormat("#,##0.00").format(
        price.getPrice());
    NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
    String changeText = changeFormat.format(price.getChange());
    String changePercentText = changeFormat.format(price.getChangePercent());

    // Populate the Price and Change fields with new data.
    stocksFlexTable.setText(row, 1, priceText);
    Label changeWidget = (Label)stocksFlexTable.getWidget(row, 2);
    changeWidget.setText(changeText + " (" + changePercentText + "%)");
    
    // Change the color of text in the Change field based on its value.
    String changeStyleName = "noChange";
    if (price.getChangePercent() < -0.1f) {
      changeStyleName = "negativeChange";
    }
    else if (price.getChangePercent() > 0.1f) {
      changeStyleName = "positiveChange";
    }

    changeWidget.setStyleName(changeStyleName);
	
}

private void loadStocks() {
	
	AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {  
		public void onFailure(Throwable error) {
			handleError(error);
		}  
		@Override
		public void onSuccess(String[] results) {  
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
