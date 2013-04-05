
package com.boxuk.groxy;

import com.google.code.samples.oauth2.OAuth2Authenticator;
import com.google.code.samples.oauth2.OAuth2SaslClientFactory;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * Allows testing of Gmail search using the X-GM-RAW IMAP extension
 * 
 * @author rod
 */
public class Main {

    /**
     * @var String
     */
    private static final String GMAIL_IMAP_HOST = "imap.gmail.com";
    
    /**
     * @var integer
     */
    private static final int GMAIL_IMAP_PORT = 993;
    
    /**
     * @param args
     */
    public static void main(final String args[]) {
        
        if (args.length != 3) {
            System.err.println("Usage: EMAIL TOKEN QUERY");
            System.exit(1);
        }

        OAuth2Authenticator.initialize();
        
        final String email = args[0];
        final String token = args[1];
        final String query = args[2];
        
        System.out.println("Connecting with...");
        System.out.println("Email: " +email);
        System.out.println("Token: " +token);
        System.out.println("");
        
        final Properties properties = new Properties();
        properties.put("mail.imaps.sasl.enable", "true");
        properties.put("mail.imaps.sasl.mechanims", "XOAUTH2");
        properties.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, token);
        
        final Session session = Session.getInstance(properties);
        final IMAPSSLStore store = new IMAPSSLStore(session, null);
        
        try {
            store.connect(
                GMAIL_IMAP_HOST,
                GMAIL_IMAP_PORT,
                email,
                ""
            );
            
            System.out.println("Searching for: " +query);
            
            final GmailSearchCommand search = new GmailSearchCommand(query);
            
            final IMAPFolder folder = (IMAPFolder) store.getFolder("Inbox");
            final GmailSearchResponse response = (GmailSearchResponse) folder.doCommand(search);
            
            for (int id : response.getMessageIds()) {
                System.out.println("Message ID: " + id);
            }
            
            System.out.println("Finished, disconnecting.");
            
            store.close();
        }
        
        catch (final MessagingException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
}
