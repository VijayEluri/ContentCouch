package contentcouch.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import contentcouch.misc.ValueUtil;

public class DateUtil {

	public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	public static final DateFormat OLDDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat DISPLAYFORMAT = OLDDATEFORMAT; //DateFormat.getDateTimeInstance();

	static {
		DATEFORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		OLDDATEFORMAT.setLenient(true);
	}
	
	public static String formatDate(Date date) {
		return DATEFORMAT.format(date);
	}
	
	public static Date parseDate(String date) throws ParseException {
		try {
			return DATEFORMAT.parse(date);
		} catch(ParseException e) {}
		
		return OLDDATEFORMAT.parse(date);
	}
	
	public static Date getDate( Object o ) {
		if( o == null ) return null;
		if( o instanceof Date ) return (Date)o;
		if( o instanceof String ) {
			try {
				return parseDate((String)o);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return getDate( ValueUtil.getString(o) );
	}
}
