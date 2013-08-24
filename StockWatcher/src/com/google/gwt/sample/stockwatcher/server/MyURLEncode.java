package com.google.gwt.sample.stockwatcher.server;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public class MyURLEncode {
	static BitSet dontNeedEncoding = null;
    static final int caseDiff = ('a' - 'A');
    static {
        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
        dontNeedEncoding.set('&');
        dontNeedEncoding.set('=');
    }
    public static String char2Unicode(char c) {
        if(dontNeedEncoding.get(c)) {
            return String.valueOf(c);
        }
        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("%");
        char ch = Character.forDigit((c >> 4) & 0xF, 16);
            if (Character.isLetter(ch)) {
            ch -= caseDiff;
        }
        resultBuffer.append(ch);
            ch = Character.forDigit(c & 0xF, 16);
            if (Character.isLetter(ch)) {
            ch -= caseDiff;
        }
         resultBuffer.append(ch);
        return resultBuffer.toString();
    }
    private static String URLEncoding(String url,String enc) throws UnsupportedEncodingException {
        StringBuffer stringBuffer = new StringBuffer();
        if(!dontNeedEncoding.get('/')) {
            dontNeedEncoding.set('/');
        }
        if(!dontNeedEncoding.get(':')) {
            dontNeedEncoding.set(':');
        }
        byte [] buff = url.getBytes(enc);
        for (int i = 0; i < buff.length; i++) {
            stringBuffer.append(char2Unicode((char)buff[i]));
        }
        return stringBuffer.toString();
    }
    private static String URIEncoding(String uri , String enc) throws UnsupportedEncodingException {
        StringBuffer stringBuffer = new StringBuffer();
        if(dontNeedEncoding.get('/')) {
            dontNeedEncoding.clear('/');
        }
        if(dontNeedEncoding.get(':')) {
            dontNeedEncoding.clear(':');
        }
        byte [] buff = uri.getBytes(enc);
        for (int i = 0; i < buff.length; i++) {
            stringBuffer.append(char2Unicode((char)buff[i]));
        }
        return stringBuffer.toString();
    }

    public static String URLencoding(String url , String enc) throws UnsupportedEncodingException {
        int index = url.indexOf('?');
        StringBuffer result = new StringBuffer();
        if(index == -1) {
            result.append(URLEncoding(url, enc));
        }else {
            result.append(URLEncoding(url.substring(0 , index),enc));
            result.append("?");
            result.append(URIEncoding(url.substring(index+1),enc));
        }
        return result.toString();
    }

}
