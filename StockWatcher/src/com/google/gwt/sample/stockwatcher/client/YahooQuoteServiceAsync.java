package com.google.gwt.sample.stockwatcher.client;


import com.google.gwt.sample.stockwatcher.shared.StockInformation;
import com.google.gwt.user.client.rpc.AsyncCallback;


public interface YahooQuoteServiceAsync {
	public void getStockInformation (String[] symbols, AsyncCallback<StockInformation[]> callback);

}
