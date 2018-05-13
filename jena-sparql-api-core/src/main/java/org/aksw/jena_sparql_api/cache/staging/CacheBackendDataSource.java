package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.aksw.jena_sparql_api.cache.extra.CacheEntryImpl;
import org.aksw.jena_sparql_api.cache.extra.SqlUtils;

//interface StreamEndecoder {
//	public void read(Out);
//	public void write(InputStream in, OutputStream out);
//}

public class CacheBackendDataSource
    implements CacheBackend
{
    //private static final Logger logger = LoggerFactory.getLogger(CacheBackendDataSource.class);

    private DataSource dataSource;
    private CacheBackendDao dao;

    // A set for keeping track of which queries that are currently being cached
    private Map<String, ReentrantReadWriteLock> activeQueries = new HashMap<String, ReentrantReadWriteLock>();

    public CacheBackendDataSource(DataSource dataSource, CacheBackendDao dao) {
        this.dataSource = dataSource;
        this.dao = dao;
    }

    private void releaseLock(String key) {
        ReentrantReadWriteLock obj = activeQueries.get(key);

//		if(write) {
//			obj.writeLock().unlock();
//		} else {
//			obj.readLock().unlock();
//		}

        if(obj.getReadHoldCount() == 0 && obj.getWriteHoldCount() == 0) {
            activeQueries.remove(obj);
        }
    }

    /**
     * Acquires a lock and locks it either read or write
     *
     * @param key
     * @param write
     * @return
     */
    private ReentrantReadWriteLock acquireLock(String key) {

        ReentrantReadWriteLock rwLock = activeQueries.get(key);

        if(rwLock == null) {
            rwLock = new ReentrantReadWriteLock();
            activeQueries.put(key, rwLock);
        }

//		if(write) {
//			rwLock.writeLock().lock();
//		} else {
//			rwLock.readLock().lock();
//		}
        return rwLock;
    }

    @Override
    public CacheEntry lookup(String service, String queryString) {

        String key = service + queryString;

        ReentrantReadWriteLock obj;
        synchronized(activeQueries) {
            obj = acquireLock(key);
            obj.readLock().lock();
        }

        try {
            CacheEntry result;
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                //System.out.println("ConnectionWatch Opened (lookup) " + conn);


                conn.setAutoCommit(false);


                result = dao.lookup(conn, service, queryString, true);

                //result = new CacheEntryImpl(timestamp, lifespan, inputStream, queryString, queryHash)
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        } finally {
            obj.readLock().unlock();
            releaseLock(key);
        }
    }

    @Override
    public void write(String service, String queryString, InputStream in) {

        String key = service + queryString;

        ReentrantReadWriteLock rwLock = null;

        boolean isOngoingWrite = false;
        synchronized(activeQueries) {
            rwLock = acquireLock(key);
            isOngoingWrite = rwLock.writeLock().isHeldByCurrentThread();
        }

        rwLock.writeLock().lock();

        Connection conn = null;
        try {
            if(!isOngoingWrite) {
                conn = dataSource.getConnection();
                //System.out.println("ConnectionWatch Opened (write) " + conn);
                conn.setAutoCommit(false);

                try {
                    dao.write(conn, service, queryString, in);
                } catch(Exception f) {
                    conn.rollback();
                    throw f;
                } finally {
                    conn.commit();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            SqlUtils.close(conn);
            //System.out.println("ConnectionWatch Closed (write) " + conn);
            rwLock.writeLock().unlock();
        }
    }

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}



//	public void commit() {
//		try {
//			conn.commit();
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public void rollback() {
//		try {
//			conn.rollback();
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public void close() {
//		SqlUtils.close(conn);
//	}

}
