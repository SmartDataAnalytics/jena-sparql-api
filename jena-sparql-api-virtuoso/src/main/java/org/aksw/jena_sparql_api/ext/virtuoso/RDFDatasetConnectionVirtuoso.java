package org.aksw.jena_sparql_api.ext.virtuoso;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Transactional;

public class RDFDatasetConnectionVirtuoso
    extends RDFDatasetAccessConnectionBase
    implements RDFDatasetConnection 
{
    protected Connection sqlConnection;
    protected SparqlQueryConnection queryConn;
    
    
    public RDFDatasetConnectionVirtuoso(SparqlQueryConnection queryConn, Connection sqlConnection) {
        super();
        this.queryConn = queryConn;
        this.sqlConnection = sqlConnection;
    }

    public static File resolveToFile(String filename) {
        File file = new File(filename);
//        URI uri = file.toURI();
//        URI uri = resolveFile(filename);
//        String filePath = uri.toURL().getFile();
//        if(Strings.isNullOrEmpty(filePath)) {
//            throw new FileNotFoundException("File: " + filename);
//        }
//        
//        File file = new File(filename);

        return file;
    }
    
    @Override
    public void load(String graphName, String filename) {
        File file = resolveToFile(filename);
        try {
            VirtuosoBulkLoad.load(sqlConnection, file, graphName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(String graphName, Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String graphName, String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String graphName, Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String graphName) {
    	try {
			VirtuosoBulkLoad.clearGraph(sqlConnection, graphName, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadDataset(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadDataset(Dataset dataset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDataset(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDataset(Dataset dataset) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SparqlQueryConnection getQueryConnection() {
        return queryConn;
    }
    
    @Override
    public void close() {
        try {
            sqlConnection.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally { 
            super.close();
        }
    }

	@Override
	public void begin(TxnType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean promote(Promote mode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ReadWrite transactionMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TxnType transactionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transactional getDelegate() {
		return null;
	}

}
