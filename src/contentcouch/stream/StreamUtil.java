package contentcouch.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import contentcouch.blob.BlobUtil;

public class StreamUtil
{
	public static void copyInputToOutput( InputStream is, OutputStream os )
		throws IOException
	{
		byte[] bytes = new byte[BlobUtil.READ_CHUNK_SIZE];
		int read;
		while( (read = is.read(bytes)) > 0 ) {
			os.write(bytes, 0, read);
		}
	}
	
	public static void close( Closeable c ) {
		try {
			c.close();
		} catch( IOException e ) {
			System.err.println("Error while closing something!");
		}
	}
}
