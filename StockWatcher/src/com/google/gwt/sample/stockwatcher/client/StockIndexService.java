package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("index")
public interface StockIndexService extends RemoteService {
	public String calculateWorth (String symbol) throws NotLoggedInException;
	
	public void init () throws NotLoggedInException;
}
