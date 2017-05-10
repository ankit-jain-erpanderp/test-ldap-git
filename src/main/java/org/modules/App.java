package org.modules;

import java.util.Scanner;

public class App {

    public static void main(String[] args) throws Exception{
        LDAPClient lc = new LDAPClient();

        lc.connection();

        lc.lookUp("cn=admin,"+LDAPClient.suffix);
        System.out.println("Enter a string to terminate");
        Scanner scanner = new Scanner(System.in);
        String terminate = scanner.next();
        System.out.println(terminate);
    }




}