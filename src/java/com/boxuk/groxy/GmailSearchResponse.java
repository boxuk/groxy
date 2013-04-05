
package com.boxuk.groxy;

import java.util.ArrayList;

public class GmailSearchResponse {
    
    /**
     * 
     */
    private final ArrayList<Integer> messageIds;
    
    /**
     * 
     */
    public GmailSearchResponse()
    {
        messageIds = new ArrayList<Integer>();
    }
    
    /**
     * @param id 
     */
    public void add(int id)
    {
        final Integer messageId = new Integer(id);
        
        messageIds.add(messageId);
    }

    /**
     * @return 
     */
    public int[] getMessageIds()
    {
        final int[] ids = new int[messageIds.size()];
        
        for (int i=0; i<messageIds.size(); i++) {
            ids[i] = messageIds.get(i).intValue();
        }
        
        return ids;
    }
    
}
