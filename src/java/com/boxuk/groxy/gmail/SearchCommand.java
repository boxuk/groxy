
package com.boxuk.groxy.gmail;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * Custom command that uses the X-GM-RAW IMAP extension to search Gmail and
 * return a response with matched message IDs and extra metadata.
 * 
 */
public class SearchCommand extends BaseCommand implements IMAPFolder.ProtocolCommand
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
    public SearchCommand(final String folderName, final String queryString)
    {
        this.folderName = folderName;
        this.queryString = queryString;
    }
    
    /**
     * @param protocol
     * 
     * @return int[]
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
            return SearchResponse.parse(responses[0]);
        }
        
        return new int[] {};
    }
}
