package contentcouch.app.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import togos.rra.BaseRequest;
import togos.rra.Request;
import togos.rra.Response;
import contentcouch.blob.BlobUtil;
import contentcouch.explorify.RdfSourcePageGenerator;
import contentcouch.explorify.SlfSourcePageGenerator;
import contentcouch.misc.MetadataUtil;
import contentcouch.misc.UriUtil;
import contentcouch.misc.ValueUtil;
import contentcouch.path.PathUtil;
import contentcouch.rdf.DcNamespace;
import contentcouch.repository.MetaRepoConfig;
import contentcouch.store.TheGetter;
import contentcouch.value.Blob;

public class ContentCouchExplorerServlet extends HttpServlet {
	public interface HttpServletRequestHandler {
		public void handle( HttpServletRequest request, HttpServletResponse response ) throws IOException;
	}
	
	protected MetaRepoConfig metaRepoConfig;

	protected void copyFile( File src, File dest ) throws IOException {
		FileInputStream is = new FileInputStream(src);
		FileOutputStream os = new FileOutputStream(dest);
		try {
			byte[] buf = new byte[512];
			int len;
			while( (len = is.read(buf)) > 0 ) {
				os.write(buf, 0, len);
			}
		} finally {
			is.close();
			os.close();
		}
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		metaRepoConfig = new MetaRepoConfig();
		TheGetter.globalInstance = metaRepoConfig.getRequestKernel();
		File configFile = getConfigFile();
		String configFileUri = PathUtil.maybeNormalizeFileUri(configFile.getPath());
		System.err.println("001: " + configFileUri);
		metaRepoConfig.handleArguments(new String[]{"-file",configFileUri}, 0, ".");
		System.err.println("002");
	}
	
	protected File getConfigFile() {
		String webPath = getServletContext().getRealPath("");
		File configFile = new File(webPath + "/repo-config");
		File configTemplateFile = new File(webPath + "/repo-config.template");
		if( !configFile.exists() ) {
			try {
				copyFile(configTemplateFile, configFile);
			} catch( IOException e ) {
				throw new RuntimeException("Failed to copy " + configTemplateFile.getPath() + " to " + configFile.getPath(), e);
			}
		}
		return configFile;
	}

	protected Object exploreObject( Object obj, String path ) {
		if( obj instanceof Blob ) {
			Blob b = (Blob)obj;
			String ct = MetadataUtil.guessContentType(b);
			if( MetadataUtil.CT_SLF.equals(ct) ) {
				return new SlfSourcePageGenerator(b, path);
			} else if( MetadataUtil.CT_RDF.equals(ct) ) {
				return new RdfSourcePageGenerator((Blob)obj, path);
			}
		}
		return obj;
	}
		
	protected Map getSingularParams(Map multiMap) {
		HashMap singleMap = new HashMap();
		for( Iterator i=multiMap.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Map.Entry)i.next();
			Object val = e.getValue();
			if( val instanceof Object[] ) val = ((Object[])val)[0];
			singleMap.put(e.getKey(), val);
		}
		return singleMap;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String pi = request.getPathInfo();
		if( pi == null ) pi = request.getRequestURI();
		if( pi == null ) pi = "/";
		
		try {
			String uri = null;
			if( pi.equals("/explore") ) {
				uri = request.getParameter("uri");
			} else if( pi.startsWith("/explore/") ) {
				uri = "active:contentcouch.explorify+operand@" + UriUtil.uriEncode(PathUtil.appendPath("x-ccouch-repo:all-repos-dir", pi.substring(9)));
			} else if( pi.equals("/") ) {
				uri = "file:web/_index.html";
			} else {
				uri = "file:web" + pi + ".html";
			}
			
			BaseRequest subReq = new BaseRequest(Request.VERB_GET, uri);
			Response subRes = TheGetter.handleRequest(subReq);
			
			response.setHeader("Content-Type", ValueUtil.getString(subRes.getContentMetadata().get(DcNamespace.DC_FORMAT)));
			BlobUtil.writeBlobToOutputStream( BlobUtil.getBlob( subRes.getContent() ), response.getOutputStream() );
		} catch( RuntimeException e ) {
			response.setHeader("Content-Type", "text/plain");
			e.printStackTrace(response.getWriter());
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
}
