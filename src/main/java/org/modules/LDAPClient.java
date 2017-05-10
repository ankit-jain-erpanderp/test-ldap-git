package org.modules;

import com.sun.jndi.ldap.LdapCtx;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class LDAPClient {
    public static String suffix = "dc=mulesoft,dc=local";
    public static Hashtable<String, String> environment;
    private static DirContext ctx;
    private static final String url = "ldap://172.17.0.3";
    private static final String connectionType = "simple";
    private static final String adminDN = "cn=admin,"+suffix;
    private static final String password = "mule@mule";

    public LDAPClient() {
        environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, url);
        environment.put(Context.OBJECT_FACTORIES, "java.naming.factory.object");
        environment.put(Context.SECURITY_AUTHENTICATION, connectionType);
        environment.put(Context.SECURITY_PRINCIPAL, adminDN);
        environment.put(Context.SECURITY_CREDENTIALS, password);
        environment.put("com.sun.jndi.ldap.connect.pool", "true");

        //This property is a system property
        environment.put("com.sun.jndi.ldap.connect.pool.initsize", "5");
    }

    public void connection() {
        try {
            ctx = new InitialDirContext(environment);
            System.out.println("Connection is created");
        } catch (NamingException exc) {
            System.out.println(exc.getResolvedName().toString());
            System.err.println(exc.getMessage().toString());
        }
    }

    /*simple lookup operation using JNDI */
    public void lookUp(String lookupDn) {
        try {
            Object obj = ctx.lookup(lookupDn);
            LdapCtx ldapctx = (LdapCtx) ctx.lookup(lookupDn);
            System.out.println(obj.getClass());
            System.out.println(obj.toString());
            System.out.println("entry exist in server");
        } catch (NamingException exc) {
            System.out.println(exc.getResolvedName().toString());
            System.err.println(exc.getMessage().toString());
        }
    }

    /* creating simple user entry to the LDAP server
     * EntryDn is the Dn and is of String Type
     * Attributes we have to add when creating an Entry*/
    public void createUserEntry(String entryDN, Attributes attributes) {
        try {
            ctx.createSubcontext(entryDN, attributes);
        } catch (NamingException exc) {
            System.out.println(exc.getResolvedName().toString());
            System.err.println(exc.getMessage().toString());
        }
    }

}
