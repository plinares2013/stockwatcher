package com.google.gwt.sample.stockwatcher.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
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


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
	

	
	public StockInformation[] getStockInformation (String[] symbols)  {		// Then send the stocks back to the browser for display with a subset of the fields retrieved from Yahoo
		
		StockInformation[] datas = new StockInformation[symbols.length];
		
		return datas;
	}

}

