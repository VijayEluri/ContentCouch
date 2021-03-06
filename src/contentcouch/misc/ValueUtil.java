package contentcouch.misc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import contentcouch.blob.Blob;
import contentcouch.blob.BlobUtil;

public class ValueUtil {
	//// Get bytes ////

	public static CharsetDecoder UTF_8_DECODER = Charset.forName("UTF-8").newDecoder();
	
	public static byte[] getBytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] getBytes( Object o ) {
		if( o == null ) return null;
		if( o instanceof byte[] ) return (byte[])o;
		if( o instanceof String ) return getBytes((String)o);
		if( o instanceof Blob ) return BlobUtil.getBytes((Blob)o);
		throw new RuntimeException("Don't know how to turn " + o.getClass().getName() + " into bytes");
	}
	
	//// Get strings ////
	
	public static String getString(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getString(byte[] bytes, int offset, int length) {
		try {
			return new String(bytes, offset, length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getString(Object obj) {
		if( obj == null ) return null;
		if( obj instanceof String ) return (String)obj;
		if( obj instanceof byte[] ) return getString((byte[])obj);
		if( obj instanceof Blob ) return BlobUtil.getString((Blob)obj);
		return obj.toString();
	}
	
	//// Get numbers ////
	
	public static Number getNumber( Object o, Number defaultValue ) {
		if( o == null ) {
			return defaultValue;
		} else if( o instanceof Number ) {
			return (Number)o;
		} else {
			return Double.valueOf(getString(o));
		}
	}
	
	public static Number getNumber( Object o ) {
		return getNumber( o, null );
	}
	
	public static int getNumber( Object o, int defaultValue ) {
		return getNumber( o, new Integer(defaultValue) ).intValue();
	}

	//// Get booleans ////
	
	static Map boolStringValues = new HashMap();
	static {
		boolStringValues.put("true", Boolean.TRUE);
		boolStringValues.put("yes", Boolean.TRUE);
		boolStringValues.put("yeah", Boolean.TRUE);
		boolStringValues.put("on", Boolean.TRUE);
		boolStringValues.put("ja", Boolean.TRUE);
		boolStringValues.put("oui", Boolean.TRUE);
		boolStringValues.put("si", Boolean.TRUE);
		boolStringValues.put("ok", Boolean.TRUE);
		
		boolStringValues.put("false", Boolean.FALSE);
		boolStringValues.put("no", Boolean.FALSE);
		boolStringValues.put("nah", Boolean.FALSE);
		boolStringValues.put("off", Boolean.FALSE);
		boolStringValues.put("nein", Boolean.FALSE);
		boolStringValues.put("non", Boolean.FALSE);
	}
	
	public static boolean getBoolean( Object o, boolean defaultValue ) {
		if( o == null ) return defaultValue;
		if( o instanceof Boolean ) return ((Boolean)o).booleanValue();
		if( o instanceof Number ) {
			return ((Number)o).doubleValue() != 0;
		}
		if( o instanceof String ) {
			String s = ((String)o).toLowerCase();
			Boolean b = (Boolean)boolStringValues.get(s);
			if( b != null ) return b.booleanValue();
			o = Double.valueOf(s);
		}
		return getBoolean( getString(o), defaultValue );
	}
	
	public static String describe( Object value ) {
		if( value == null ) return "null";
		else return value.getClass().getName();
	}
}
