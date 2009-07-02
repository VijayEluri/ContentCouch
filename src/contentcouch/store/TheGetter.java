package contentcouch.store;

import java.util.Collections;
import java.util.Map;

import togos.rra.BaseRequest;
import togos.rra.Request;
import togos.rra.RequestHandler;
import togos.rra.Response;
import contentcouch.active.Context;
import contentcouch.app.Log;
import contentcouch.misc.UriUtil;
import contentcouch.misc.ValueUtil;
import contentcouch.rdf.CcouchNamespace;
import contentcouch.value.Blob;
import contentcouch.value.Directory;

public class TheGetter {
	public static class AbnormalResponseException extends RuntimeException {
		public AbnormalResponseException( String verb, String uri, int status, Object content ) {
			super( verb + " " + uri + " resulted in " + status + (content == null ? "" : ": " + ValueUtil.getString(content)));
		}
	}
	
	public static final String CTXVAR = CcouchNamespace.INTERNAL_NS + "getter"; 
	
	public static RequestHandler globalInstance;
	
	public static RequestHandler getGenericGetter() {
		RequestHandler theGetter = (RequestHandler)Context.get(CTXVAR);
		if( theGetter == null ) {
			theGetter = globalInstance;
		}
		if( theGetter == null ) {
			throw new RuntimeException("No "+CTXVAR+" registered");
		}
		return theGetter;
	}
	
	protected static String describeContent(Object content) {
		String desc = content.getClass().getName();
		if( content instanceof Blob ) {
			desc += " (" + ((Blob)content).getLength() + " bytes)";
		}
		return desc;
	}
	
	public static Response handleRequest( Request req ) {
		Log.log(Log.EVENT_REQUEST_SUBMITTED, req.getVerb(), req.getUri(), (req.getContent() == null ? "" : describeContent(req.getContent())) );
		return getGenericGetter().handleRequest(req);
	}

	public static Object getResponseValue( Response res, String verb, String uri ) {
		switch( res.getStatus() ) {
		case( Response.STATUS_NORMAL ): return res.getContent();
		case( Response.STATUS_DOESNOTEXIST ): return null;
		default:
			throw new AbnormalResponseException( verb, uri, res.getStatus(), res.getContent() );
		}
	}
	
	public static Object getResponseValue( Response res, String uri ) {
		return getResponseValue(res, "GET", uri );
	}

	public static Object getResponseValue( Response res, Request req ) {
		return getResponseValue(res, req.getVerb(), req.getUri() );
	}

	public static Object get(String uri) {
		Request req = new BaseRequest("GET",uri);
		return getResponseValue(handleRequest(req), req);
	}
	
	public static Object put(String uri, Object obj) {
		BaseRequest putReq = new BaseRequest(Request.VERB_PUT, uri, obj, Collections.EMPTY_MAP);
		return getResponseValue(handleRequest(putReq), putReq);
	}

	public static Directory getDirectory( String uri ) {
		return (Directory)get("active:contentcouch.directoryize+operand@" + UriUtil.uriEncode(uri));
	}
	
	public static String identify( Object obj, Map metadata ) {
		BaseRequest idReq = new BaseRequest(Request.VERB_POST, "x-ccouch-repo:identify", obj, metadata );
		return ValueUtil.getString(TheGetter.getResponseValue(TheGetter.handleRequest(idReq), idReq.uri));
	}
}