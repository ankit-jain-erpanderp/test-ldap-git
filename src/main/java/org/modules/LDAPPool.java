package org.modules;

import org.joda.time.DateTime;

import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class LDAPPool {

    private Object contextCreatorObject;
    Method contextCreatorMethod;
    Object[] contextCreatorArgs;

    int initialPoolSize=1;
    int maxPoolSize=5;
    int timeout=10000;

    HashMap<Context, ContextInformation> contexts;

    public void setContextCreator(Object contextCreatorObject, Method contextCreatorMethod, Object[] contextCreatorArgs){
        this.contextCreatorObject = contextCreatorObject;
        this.contextCreatorMethod = contextCreatorMethod;
        this.contextCreatorArgs = contextCreatorArgs;
    }

    public synchronized Context getConnection() throws InterruptedException, InvocationTargetException, IllegalAccessException, NamingException {
        System.out.println("Entered getConnection in LDAPPool");
        //remove stale connections
        removeTimedoutConnections();

        Context context = attemptGetConnection();
        if(context != null)
            return context;
        //if max pool size is achieved then wait until a connection is released
        while(context == null) {
            contexts.wait();
            context = attemptGetConnection();
        }
        return context;
    }

    //if a connection is available or maxPoolSize is not reached returns back the Context and updates ContextInformation
    //otherwise returns null
    private Context attemptGetConnection() throws InvocationTargetException, IllegalAccessException {
        //scan through the contexts and if a connection is available then return that
        for (Context context:contexts.keySet()) {
            ContextInformation contextInformation = contexts.get(context);
            if(!contextInformation.isAcquired()){
                contextInformation.setAcquired(true);
                return context;
            }
        }
        System.out.println("No connection could be found, so will attempt to create a new connection");
        //if maxPoolSize is not reached
        //then create a new connection
        if(maxPoolSize > contexts.size())
        {

            Context context = createNewConnection();
            return context;
        }
        return null;
    }

    //marks acquired as false
    public void releaseConnection(Context context)
    {
        ContextInformation contextInformation = contexts.get(context);
        contextInformation.setAcquired(false);
        contextInformation.setLastReleaseTime(DateTime.now());
    }

    //Check for connections which are timedout and call releaseConnection on them
    void removeTimedoutConnections() throws NamingException {
        for (Context context:contexts.keySet()) {
            ContextInformation contextInformation = contexts.get(context);
            //If is released then check lastReleaseTime against currentTime - timeout
            if(!contextInformation.isAcquired())
            {
                if(contextInformation.lastReleaseTime.isBefore(DateTime.now().minusMillis(timeout))) {
                    //contextCloserMethod.invoke(contextCloserObject, contextCloserArgs);
                    System.out.println("About to remove timedout connection" + context);
                    context.close();
                    contexts.remove(context);
                }
            }
        }
    }

    //populate the entries in contexts by calling createConnection
    public void initConnections() throws InvocationTargetException, IllegalAccessException {
        contexts = new HashMap<Context, ContextInformation>();
        while(contexts.size() <= initialPoolSize)
        {
            System.out.println(initialPoolSize+"\t"+contexts.size());
            Context c = createNewConnection();
            ContextInformation contextInformation = new ContextInformation();
            contextInformation.setAcquired(false);
            contextInformation.lastReleaseTime = DateTime.now();
            contexts.put(c, contextInformation);
        }
        System.out.println("Finished creating connection");
    }

    Context createNewConnection() throws InvocationTargetException, IllegalAccessException {
        Context context = (Context)contextCreatorMethod.invoke(contextCreatorObject, contextCreatorArgs);
        ContextInformation contextInformation = new ContextInformation();
        contextInformation.setAcquired(true);
        contexts.put(context, contextInformation);
        return context;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

class ContextInformation{
    DateTime lastReleaseTime;
    boolean acquired;

    public DateTime getLastReleaseTime() {
        return lastReleaseTime;
    }

    public void setLastReleaseTime(DateTime lastReleaseTime) {
        this.lastReleaseTime = lastReleaseTime;
    }

    public boolean isAcquired() {
        return acquired;
    }

    public void setAcquired(boolean acquired) {
        this.acquired = acquired;
    }

}