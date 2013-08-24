package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.sample.stockwatcher.shared.StockInformation;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("yahooQuote")
public interface YahooQuoteService extends RemoteService {
	public StockInformation[] getStockInformation (String[] symbols);

}
