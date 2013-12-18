package org.aksw.jena_sparql_api.cache.staging;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheEntryH2;
import org.aksw.jena_sparql_api.cache.extra.SqlUtils;

//interface InputStreamProviderClosableFactory {
//	InputStreamProvider createInputStream
//}

public class CacheCoreIterator
	extends SinglePrefetchIterator<CacheEntryH2>
{
	private ResultSet rs;
	
	// The action to perform when closing the input stream of a generated
	// cache entry. E.g. close the result set, commit the transaction, ...
	private IClosable inputStreamCloseAction;
	
	public CacheCoreIterator(ResultSet rs, IClosable inputStreamCloseAction) {
		this.rs = rs;
		this.inputStreamCloseAction = inputStreamCloseAction;
	}
	
	@Override
	protected CacheEntryH2 prefetch()
			throws Exception
	{	
		if (rs.next()) {

			byte[] rawQueryHash = rs.getBytes("query_hash");
			String queryHash = StringUtils.bytesToHexString(rawQueryHash);

			String queryString = rs.getString("query_string");
			Blob data = rs.getBlob("data");
			
			Timestamp timeOfInsertion = rs.getTimestamp("time_of_insertion");
			Timestamp timeOfExpiration = rs.getTimestamp("time_of_expiration");


			CacheEntryH2 result = new CacheEntryH2(
					timeOfInsertion.getTime(),
					1000l, //timeOfExpiration.g,
					new InputStreamProviderBlobClosable(data, inputStreamCloseAction),
					queryString,
					queryHash
			);
			
			return result;
		}
		
		return finish();
	}
	
	@Override
	public void close() {
		SqlUtils.close(rs);
	}
}