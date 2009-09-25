package contentcouch.active.expression;

import togos.mf.api.Request;
import togos.mf.api.Response;
import togos.mf.base.BaseRequest;
import contentcouch.misc.UriUtil;
import contentcouch.path.PathSimplifiableExpression;
import contentcouch.path.PathUtil;
import contentcouch.store.TheGetter;

public class UriExpression implements Expression, PathSimplifiableExpression {
	String uri;
	
	public UriExpression( String uri ) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String toString() {
		return UriUtil.sanitizeUri(uri);
	}
	
	public Response eval( Request req ) {
		BaseRequest subReq = new BaseRequest( req, uri );
		return TheGetter.call(subReq);
	}
	
	public String toUri() {
		return uri;
	}
	
	public boolean isConstant() {
		if( uri.startsWith("data:") ) return true;
		if( uri.startsWith("urn:sha1:") ) return true;
		return false;
	}
	
	public Expression appendPath(String path) {
		if( PathUtil.isHierarchicalUri(uri) ) {
			return new UriExpression(PathUtil.appendHierarchicalPath(uri, path, true));
		} else {
			return null;
		}
	}
	
	public Expression simplify() {
		return this;
	}
}
