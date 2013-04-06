
package com.boxuk.groxy;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
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
     * @var String
     */
    private String folderName;
    
    /**
     * @param query 
     */
    public GmailSearchCommand(final String folderName, final String queryString)
    {
        this.folderName = folderName;
        this.queryString = queryString;
    }
    
    /**
     * @param protocol
     * 
     * @return 
     */
    @Override
    public Object doCommand(final IMAPProtocol protocol) throws ProtocolException
    {
        protocol.select(folderName);
        
        final Response[] responses = protocol.command(
            "SEARCH X-GM-RAW",
            argument(queryString)
        );
        
        if (responses.length > 0) {
            return parseSearchResponse(responses[0]);
        }
        
        return new GmailSearchResponse();
    }

    /**
     * Create an argument for a command
     * 
     * @param value
     * 
     * @return 
     */
    protected Argument argument(final String value)
    {
        final Argument arg = new Argument();
        
        arg.writeString(value);
        
        return arg;
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
        final String idString = response.toString();
        
        if (idString.length() > 8) {
            final String ids[] = idString.substring(9).split(" ");
            for (final String id : ids) {
                res.add(Integer.parseInt(id));
            }
        }
        
        return res;
    }
}
