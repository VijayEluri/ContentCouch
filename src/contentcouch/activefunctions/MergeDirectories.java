package contentcouch.activefunctions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import togos.rra.BaseResponse;
import togos.rra.Response;

import contentcouch.active.BaseActiveFunction;
import contentcouch.blob.BlobUtil;
import contentcouch.digest.DigestUtil;
import contentcouch.misc.SimpleDirectory;
import contentcouch.rdf.CcouchNamespace;
import contentcouch.value.Directory;

public class MergeDirectories extends BaseActiveFunction {
	protected static final int MERGE_STRICT = 0; // Source and dest must be strictly mergeable
	protected static final int MERGE_KEEP_EXISTING = 1; // When there is a merge conflict, take the existing directory entry
	protected static final int MERGE_TAKE_INCOMING = 2; // When there is a merge conflict, take the incoming directory entry
	protected static final int MERGE_MASK = 3;
	
	protected static String identify( Object o ) {
		return DigestUtil.getSha1Urn(BlobUtil.getBlob(o));
	}
	
	protected static void mergeIntoEntry( SimpleDirectory.Entry destEntry, Directory.Entry srcEntry, int flags ) {
		if( destEntry.target == null ) {
			SimpleDirectory.cloneInto( destEntry, srcEntry, SimpleDirectory.DEEPCLONE_SIMPLEDIRECTORY );
			return;
		}
		
		if( CcouchNamespace.OBJECT_TYPE_DIRECTORY.equals(destEntry.getTargetType()) &&
		    CcouchNamespace.OBJECT_TYPE_DIRECTORY.equals(srcEntry.getTargetType())
		) {
			// Both Directories
			if( !(destEntry.target instanceof SimpleDirectory) ) {
				// If it's a SimpleDirectory, it's already been cloned.
				destEntry.target = SimpleDirectory.cloneTarget( destEntry.target, SimpleDirectory.DEEPCLONE_SIMPLEDIRECTORY );
			}
			mergeInto( (SimpleDirectory)destEntry.target, (Directory)srcEntry.getValue(), flags );
		} else if(
			CcouchNamespace.OBJECT_TYPE_BLOB.equals(destEntry.getTargetType()) &&
			CcouchNamespace.OBJECT_TYPE_BLOB.equals(srcEntry.getTargetType())
		) {
			// Both Blobs
			switch( flags & MERGE_MASK ) {
			case( MERGE_STRICT ):
				String srcId = identify(srcEntry.getValue());
				String destId = identify(destEntry.getValue());
				if( srcId == null || destId == null ) {
					throw new RuntimeException("Could not identify object for strict merge");
				}
				if( !srcId.equals(destId) ) {
					throw new RuntimeException("Cannot merge strictly. " + destId + " != " + srcId);
				}
				SimpleDirectory.cloneMetadataInto( destEntry, srcEntry );
				break;
			case( MERGE_KEEP_EXISTING ):
				break;
			case( MERGE_TAKE_INCOMING ):
				destEntry.target = SimpleDirectory.cloneTarget(srcEntry.getValue(), SimpleDirectory.DEEPCLONE_SIMPLEDIRECTORY);
				SimpleDirectory.cloneMetadataInto( destEntry, srcEntry );
				break;
			default:
				throw new RuntimeException("Invalid merge type: " + (flags & MERGE_MASK) );
			}
		} else {
			// Type mismatch
			switch( flags & MERGE_MASK ) {
			case( MERGE_STRICT ):
				String srcId = identify(srcEntry.getValue());
				String destId = identify(destEntry.getValue());
				if( srcId == null || destId == null ) {
					throw new RuntimeException("Could not identify object for strict merge (type check already failed)");
				}
				if( !srcId.equals(destId) ) {
					throw new RuntimeException("Cannot merge strictly.  Type check failed; " + destId + " != " + srcId);
				}
				SimpleDirectory.cloneMetadataInto( destEntry, srcEntry );
				break;
			case( MERGE_KEEP_EXISTING ):
				break;
			case( MERGE_TAKE_INCOMING ):
				destEntry.target = SimpleDirectory.cloneTarget(srcEntry.getValue(), SimpleDirectory.DEEPCLONE_SIMPLEDIRECTORY);
				SimpleDirectory.cloneMetadataInto( destEntry, srcEntry );
				break;
			default:
				throw new RuntimeException("Invalid merge type: " + (flags & MERGE_MASK) );
			}
		}
	}
	
	protected static void mergeInto( SimpleDirectory dest, Directory src, int flags ) {
		for( Iterator ei=src.getDirectoryEntrySet().iterator(); ei.hasNext(); ) {
			Directory.Entry srcEntry = (Directory.Entry)ei.next();
			SimpleDirectory.Entry existingEntry = (SimpleDirectory.Entry)dest.getDirectoryEntry(srcEntry.getKey());
			if( existingEntry == null ) {
				existingEntry = new SimpleDirectory.Entry();
				existingEntry.name = srcEntry.getKey();
				dest.addEntry(existingEntry);
			}
			mergeIntoEntry( existingEntry, srcEntry, flags );
		}
	}
	
	public Response call(Map argumentExpressions) {
		List dirs = getPositionalArgumentValues(argumentExpressions);
		SimpleDirectory result = new SimpleDirectory();
		
		for( Iterator i=dirs.iterator(); i.hasNext(); ) {
			Directory indir = (Directory)i.next();
			mergeInto( result, indir, 0 );
		}
		return new BaseResponse(Response.STATUS_NORMAL, result);
	}
}
