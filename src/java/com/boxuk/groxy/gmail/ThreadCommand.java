
package com.boxuk.groxy.gmail;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInputStream;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom command that uses the X-GM-THRID IMAP extension to search Gmail for
 * all messages in the specified message thread.
 * 
 */
public class ThreadCommand extends BaseCommand implements IMAPFolder.ProtocolCommand
{
    /**
     * @var String
     */
    private long messageId;

    /**
     * @var String
     */
    private String folderName;
    
    /**
     * @param query 
     */
    public ThreadCommand(final String folderName, final long messageId)
    {
        this.folderName = folderName;
        this.messageId = messageId;
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

        try {
            final long threadId = fetchThreadId(protocol);
            final Response[] responses = protocol.command(
                "SEARCH X-GM-THRID " +threadId,
                null
            );
            
            return SearchResponse.parse(responses[1]);
        }
        
        catch (final Exception e) {
            e.printStackTrace();
            throw new ProtocolException(e.getLocalizedMessage());
        }
    }
    
    /**
     * Fetch thread ID information for each message retrieved
     * 
     * @param protocol
     * @param response 
     */
    protected long fetchThreadId(final IMAPProtocol protocol) throws Exception
    {
        final Class cls = com.sun.mail.iap.Protocol.class;
        final Method getInputStream = cls.getDeclaredMethod("getInputStream", (Class<?>) null);
        final String cmd = "FETCH " +messageId+ " (X-GM-THRID)";
        
        protocol.writeCommand(cmd, null);

        getInputStream.setAccessible(true);

        return parseThreadResponse(
            (ResponseInputStream)
                getInputStream.invoke(protocol, (Class<?>) null)
        );
    }

    /**
     * @param in
     * 
     * @return
     * 
     * @throws IOException
     * @throws ProtocolException 
     */
    protected long parseThreadResponse(ResponseInputStream in) throws IOException, ProtocolException
    {
        final String result = new String(in.readResponse().getBytes()).trim();
        final Pattern pattern = Pattern.compile("\\* \\d+ FETCH \\(X-GM-THRID (\\d+)\\)");
        final Matcher matcher = pattern.matcher(result);
        
        if (!matcher.matches()) {
            throw new ProtocolException("Invalid X-GM-THRID response: " +result);
        }

        return Long.parseLong(matcher.group(1));
    }
}
