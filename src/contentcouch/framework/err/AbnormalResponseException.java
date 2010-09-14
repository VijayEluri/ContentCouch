/**
 * 
 */
package contentcouch.framework.err;

import togos.mf.api.Request;
import togos.mf.api.Response;
import togos.mf.api.ResponseCodes;
import contentcouch.misc.ValueUtil;

public class AbnormalResponseException extends RuntimeException {
	Request request;
	Response response;
	
	public AbnormalResponseException( Response res, Request req ) {
		super( req.getVerb() + " " + req.getResourceName() + " resulted in " +
			res.getStatus() + (res.getContent() == null ? "" : ": " + ValueUtil.getString(res.getContent())));
		this.request = req;
		this.response = res;
	}
	
	public Request getRequest() {
		return request;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public static AbnormalResponseException createFor( Response res, Request req ) {
		switch( res.getStatus() ) {
		case( ResponseCodes.RESPONSE_UNHANDLED ):
			throw new UnhandledException(res,req);
		case( ResponseCodes.RESPONSE_NOTFOUND ):
			throw new NotFoundException(res,req);
		case( ResponseCodes.RESPONSE_DOESNOTEXIST ):
			throw new DoesNotExistException(res,req);
		case( ResponseCodes.RESPONSE_CALLER_ERROR ):
			return new CallerErrorException(res,req);
		default:
			// expand if need more specific ones
			return new AbnormalResponseException(res,req);
		}
	}
	
	public static void throwIfNonNormal( Response res, Request req ) {
		if( res.getStatus() == ResponseCodes.RESPONSE_NORMAL ) return;
		throw createFor(res,req);
	}
}