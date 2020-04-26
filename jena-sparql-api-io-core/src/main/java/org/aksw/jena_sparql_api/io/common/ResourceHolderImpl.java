package org.aksw.jena_sparql_api.io.common;

/**
 * Associate a resource with a closing mechanism
 *
 * @author raven
 *
 * @param <T>
 */
public class ResourceHolderImpl<T>
    implements ResourceHolder<T>
{
    protected T value;
    protected boolean closeActionRun;
    protected AutoCloseable closeAction;

    public ResourceHolderImpl(T value, AutoCloseable closeAction) {
        super();
        this.value = value;
        this.closeActionRun = false;
        this.closeAction = closeAction;
    }

    @Override
    public void close() throws Exception {
        closeAction.close();
        closeActionRun = true;
    }

    @Override
    public boolean isValid() {
        return !closeActionRun;
    }

    @Override
    public T get() {
        if(isValid()) {
            return value;
        } else {
            throw new RuntimeException("Cannot get() from an invalidated ResourceHolder");
        }
    }
}
