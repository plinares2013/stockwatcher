package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StockIndexServiceAsync {
	public void calculateWorth (String symbol, AsyncCallback<String> callback);
	
	public void init (AsyncCallback<Void> callback);
}
