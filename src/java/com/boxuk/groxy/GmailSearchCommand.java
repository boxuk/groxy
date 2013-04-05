
package com.boxuk.groxy;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;

/**
 * Custom command that uses the X-GM-RAW IMAP extension to search Gmail and
 * return a response with matched message IDs
 */
public class GmailSearchCommand implements IMAPFolder.ProtocolCommand
{
    /**
     * @var String
     */
    private String queryString;
    
    /**
     * @param query 
     */
    public GmailSearchCommand(final String queryString)
    {
        this.queryString = queryString;
    }
    
    /**
     * @param protocol
     * 
     * @return 
     */
    @Override
    public Object doCommand(final IMAPProtocol protocol)
    {
        final Argument inbox = new Argument();
        inbox.writeString("[Gmail]/All Mail");
        
        final Argument query = new Argument();
        query.writeString(queryString);
        
        protocol.command("SELECT", inbox);
        
        final Response[] responses = protocol.command("SEARCH X-GM-RAW", query);
        
        if (responses.length > 0) {
            return parseSearchResponse(responses[0]);
        }
        
        return new GmailSearchResponse();
    }
    
    /**
     * Return a search response object containing the ID's of all matched messages
     * 
     * @param response
     * 
     * @return 
     */
    protected GmailSearchResponse parseSearchResponse(final Response response)
    {
        final GmailSearchResponse res = new GmailSearchResponse();
        final String idString = response.toString().substring(9);
        final String ids[] = idString.split(" ");
        
        for (final String id : ids) {
            res.add(Integer.parseInt(id));
        }
        
        return res;
    }
}
