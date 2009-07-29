package contentcouch.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import contentcouch.blob.BlobUtil;
import contentcouch.blob.ByteArrayBlob;
import contentcouch.value.Blob;

import togos.rra.BaseRequestHandler;
import togos.rra.BaseResponse;
import togos.rra.Request;
import togos.rra.Response;

public class InternalStreamRequestHandler extends BaseRequestHandler {
	static final InternalStreamRequestHandler instance = new InternalStreamRequestHandler();
	public static InternalStreamRequestHandler getInstance() {
		return instance;
	}
	
	static final String URI_PREFIX = "x-internal-stream:";
	
	public HashMap streams = new HashMap();
	public HashMap cachedInput = new HashMap();
	
	public void addInputStream( String name, InputStream stream ) {
		streams.put(name, stream);
	}
	
	public void addOutputStream( String name, OutputStream stream ) {
		streams.put(name, stream);
	}

	public void putBlob( Blob blob, String streamName ) {
		Object oso = streams.get(streamName);
		if( oso == null ) throw new RuntimeException("No such stream: " + streamName);
		if( !(oso instanceof OutputStream) ) throw new RuntimeException("Not an output stream: " + streamName);
		OutputStream os = (OutputStream)oso;
		BlobUtil.writeBlobToOutputStream(blob, os);
		try {
			os.close();
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	public Blob getBlob( String streamName ) {
		Blob blob = (Blob)cachedInput.get(streamName);
		if( blob != null ) return blob;
		Object iso = streams.get(streamName);
		if( iso == null ) throw new RuntimeException("No such stream: " + streamName);
		if( !(iso instanceof InputStream) ) throw new RuntimeException("Not an input stream: " + streamName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BlobUtil.copyInputToOutput((InputStream)iso, baos);
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
		blob = new ByteArrayBlob(baos.toByteArray());
		cachedInput.put(streamName, blob);
		return blob;
	}

	public Response handleRequest(Request req) {
		if( !req.getUri().startsWith(URI_PREFIX) ) return BaseResponse.RESPONSE_UNHANDLED;
		String streamName = req.getUri().substring(URI_PREFIX.length());
		if( Request.VERB_POST.equals(req.getVerb()) || Request.VERB_PUT.equals(req.getVerb()) ) {
			Object content = req.getContent();
			if( content == null ) {
				throw new RuntimeException("Can't PUT or POST to " + req.getUri() + " without content");
			}
			Blob blob = BlobUtil.getBlob(content);
			putBlob(blob, streamName);
			return new BaseResponse(Response.STATUS_NORMAL, "Written");
		} else if( Request.VERB_GET.equals(req.getVerb()) ) {
			return new BaseResponse(Response.STATUS_NORMAL, getBlob(streamName));
		} else {
			throw new RuntimeException("Don't know how to handle " + req.getVerb() );
		}
	}

}