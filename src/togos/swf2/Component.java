package togos.swf2;

import java.util.Map;

import togos.mf.api.RequestHandler;
import togos.mf.value.Arguments;

public interface Component extends RequestHandler {
	/** Return assorted metadata about this component that may be useful to other components. */
	public Map getMetadata();	

	/** Return the URI for a page generated by this component based on the given
	 * component-specific arguments */
	public String getUriFor(Arguments args);
}