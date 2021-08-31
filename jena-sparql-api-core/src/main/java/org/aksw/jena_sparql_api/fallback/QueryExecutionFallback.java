package org.aksw.jena_sparql_api.fallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.aksw.jena_sparql_api.arq.core.query.QueryExecutionDecoratorBase;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutionFallback
    extends QueryExecutionDecoratorBase<QueryExecution>
{
    private final Logger logger = LoggerFactory.getLogger(QueryExecutionFallback.class);

    private List<QueryExecution> decoratees;

    private ReturnPreference returnPreference = ReturnPreference.BEST_SUCCESSFUL;

    enum ReturnPreference {
        FIRST_SUCCESSFUL,
        BEST_SUCCESSFUL
    }

    public QueryExecutionFallback(List<QueryExecution> decoratees) {
        super(null);
        this.decoratees = decoratees;
    }

    @Override
    public boolean execAsk() {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execAsk();
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public ResultSet execSelect() {
        ExecutorService threadPool = Executors.newFixedThreadPool(decoratees.size());
        CompletionService<ResultSet> completionService = new ExecutorCompletionService<ResultSet>(
                threadPool);
        List<Future<ResultSet>> futures = new ArrayList<Future<ResultSet>>(decoratees.size());
        for (final QueryExecution qe : decoratees) {
            futures.add(completionService.submit(new Callable<ResultSet>() {
                @Override
                public ResultSet call() throws Exception {
                    try {
                        return qe.execSelect();
                    } catch (Exception e) {
                        logger.warn(String.format("Query execution failed. Tried %s", qe), e);
                    }
                    return null;
                }
            }));
        }
        List<ResultSet> results = new ArrayList<ResultSet>(Collections.nCopies(decoratees.size(), (ResultSet)null));
        boolean firstSuccessful = true;
        int bestSuccessfulIndex = -1;
        int bestFailedIndex = decoratees.size();
        int bestIndex = 0;
        for (int i = 0; i < decoratees.size(); i++) {
            try {
                Future<ResultSet> future = completionService.take();
                ResultSet result = future.get();
                int currentIndex = futures.indexOf(future);
                System.out.println("Finished " + (currentIndex+1) + ". query execution. Status: " + (result == null ? "failed" : "successful") );
                //if execution was successful
                if(result != null){
                    //if strategy is to return the result of the first successful query execution
                    if(returnPreference == ReturnPreference.FIRST_SUCCESSFUL && firstSuccessful){
                        System.out.println("Returning first successful solution returned by " + (currentIndex+1) + ". query execution.");
                        shutdownAll(threadPool, currentIndex);
                        return result;
                    }
                    //else check if solution was the most preferred, i.e. the first in the list that not already failed
                    if(currentIndex == bestIndex){
                        System.out.println("Returning best successful solution.");
                        shutdownAll(threadPool, currentIndex);
                        return result;
                    }
                    firstSuccessful = false;
                    bestSuccessfulIndex = Math.min(currentIndex, bestSuccessfulIndex);
                } else {
                    bestFailedIndex = Math.min(currentIndex, bestFailedIndex);
                    //if query execution of the best index failed
                    if(currentIndex == bestIndex){
                        bestIndex++;
                    }
                }
                //if we already got a successful solution which is the next most preferred
                if(bestSuccessfulIndex - bestFailedIndex == 1){
                    System.out.println("Returning best successful solution.");
                    shutdownAll(threadPool, currentIndex);
                    return results.get(bestSuccessfulIndex);
                }
                results.add(currentIndex, result);
            } catch (InterruptedException e) {
                logger.warn("Thread interrupted", e);
            } catch (ExecutionException e) {
                logger.warn("Execution exception", e);
            }
        }
        threadPool.shutdown();
        //return the best non null solution
        for (ResultSet result : results) {
            if(result != null){
                return result;
            }
        }

        throw new QueryExecutionFallbackFailedException();
    }

    private void shutdownAll(ExecutorService threadPool, int position){
        for (int j = 0; j < decoratees.size(); j++) {
            if(j != position){
                System.out.println("Cancelling " + (j+1) + ". query execution.");
                decoratees.get(j).abort();
            }
        }
        threadPool.shutdownNow();
    }

    public ResultSet execSelectMultihreaded() {
        ExecutorService threadPool = Executors.newFixedThreadPool(decoratees.size());
        CompletionService<ResultSet> completionService = new ExecutorCompletionService<ResultSet>(
                threadPool);
        List<FutureTask<ResultSet>> futures = new ArrayList<FutureTask<ResultSet>>(decoratees.size());
        for (final QueryExecution qe : decoratees) {
            QueryExecutionTask<ResultSet> task = new QueryExecutionTask<ResultSet>(new Callable<ResultSet>() {
                @Override
                public ResultSet call() throws Exception {
                    try {
                        return qe.execSelect();
                    } catch (Exception e) {
                        logger.warn(String.format("Query execution failed. Tried %s", qe), e);
                    }
                    return null;
                }
            });
            threadPool.submit(task);
        }
        threadPool.shutdown();

        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Model execConstruct() {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execConstruct();
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Model execConstruct(final Model model) {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execConstruct(model);
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execConstructTriples();
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Model execDescribe() {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execDescribe();
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Model execDescribe(final Model model) {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execDescribe(model);
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        for (QueryExecution qe : decoratees) {
            try {
                return qe.execDescribeTriples();
            } catch (Exception e) {
                logger.warn(String.format("Query execution failed. Tried %s", qe), e);
            }
        }
        throw new QueryExecutionFallbackFailedException();
    }

    class QueryExecutionTask<T> extends FutureTask<T>{

        private QueryExecution qe;

        /**
         * @param callable
         */
        public QueryExecutionTask(Callable<T> callable) {
            super(callable);
        }

        /**
         * @param callable
         */
        public QueryExecutionTask(QueryExecution qe, Callable<T> callable) {
            super(callable);
            this.qe = qe;
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.FutureTask#cancel(boolean)
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            qe.abort();System.out.println("Aborting...");
            return super.cancel(mayInterruptIfRunning);
        }

    }
}