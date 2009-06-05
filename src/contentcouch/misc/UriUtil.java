package contentcouch.misc;

import java.io.UnsupportedEncodingException;

import contentcouch.active.ActiveUriResolver;

public class UriUtil {


	public static String uriEncode( String text, boolean keepUriSpecialChars ) {
		byte[] inbytes;
		try {
			inbytes = text.getBytes("UTF-8");
		} catch( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
		char[] outchars = new char[inbytes.length*3];
		int inidx=0, outidx=0;
		while( inidx < inbytes.length ) {
			byte c = inbytes[inidx++];
			if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
			    (c >= '0' && c <= '9') || c == '.' || c == ',' ||
			    c == '/' || c == '-' || c == '_' ||
			    (keepUriSpecialChars && (
			    	c == '%' || c == '+' || c == ':' || c == ';' ||
			    	c == '?' || c == '&' || c == '=' || c == '#'
			    )))
			{
				outchars[outidx++] = (char)c;
			} else {
				outchars[outidx++] = '%';
				outchars[outidx++] = ActiveUriResolver.HEXCHARS[(c>>4)&0xF];
				outchars[outidx++] = ActiveUriResolver.HEXCHARS[ c    &0xF];
			}
		}
		return new String(outchars,0,outidx);
	}
	
	public static String uriEncode( String text ) {
		return uriEncode( text, false );
	}

	public static String sanitizeUri( String text ) {
		return uriEncode( text, true );
	}

	protected static final int hexValue( char digit ) {
		return digit <= '9' ? digit - '0' : digit <= 'F' ? digit - 'A' + 10 : digit - 'a' + 10;
	}
	
	protected static final int hexValue( char hiDigit, char loDigit ) {
		return (hexValue(hiDigit) << 4) | hexValue(loDigit);
	}
	
	public static byte[] uriDecodeBytes( String text ) {
		char[] inchars = text.toCharArray();
		int escapecount = 0;
		for( int i=inchars.length-1; i>=0; --i ) {
			if( inchars[i] == '%' ) ++escapecount;
		}
		byte[] outbytes = new byte[inchars.length - (escapecount<<1)];
		int inidx=0, outidx=0;
		while( inidx < inchars.length ) {
			char c = inchars[inidx++];
			if( c == '%' ) {
				char hiDigit = inchars[inidx++];
				char loDigit = inchars[inidx++];
				outbytes[outidx++] = (byte)hexValue(hiDigit, loDigit);
			} else {
				outbytes[outidx++] = (byte)c;
			}
		}
		return outbytes;
	}
	
	public static String uriDecode( String text ) {
		try {
			return new String(uriDecodeBytes(text), "UTF-8");
		} catch( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
	}
	
	public static String makeDataUri( String data ) {
		return "data:," + uriEncode(data);
	}
}
