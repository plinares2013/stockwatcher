package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.JavaScriptObject;

public class QueryYQL extends JavaScriptObject{
	
	protected QueryYQL() {}
	
	public final native int getCount()
		/*-{ return this.query.count }-*/;
	
	public final native Quote getQuote (int index)
		/*-{ return this.query.results.quote[index] }-*/;
	
	public static class Quote extends JavaScriptObject {
		
		protected Quote() {}
		
		public final native String getSymbol()
		/*-{  return this.symbol }-*/;
		
		public final native String getLastTradePriceOnly()
		/*-{ return this.LastTradePriceOnly }-*/;
		
		public final native String getChange()
		/*-{ return this.Change }-*/;
		
		public final native String getChangeinPercent()
		/*-{ return this.ChangeinPercent }-*/;
	
}

}
