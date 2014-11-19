
package com.boxuk.groxy.gmail;

import com.sun.mail.iap.Response;
import java.util.ArrayList;

public class SearchResponse {
    
    /**
     * Parse a search response to an array of message IDs
     * 
     * @param responses
     * 
     * @return 
     */
    public static int[] parse(final Response response)
    {
        final String idString = response.toString();
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        
        if (idString.length() > 8) {
            final String sids[] = idString.substring(9).split(" ");
            for (final String id : sids) {
                ids.add(0, Integer.parseInt(id));
            }
        }
        
        return toArray(ids);
    }   
    
    /**
     * @return 
     */
    protected static int[] toArray(final ArrayList<Integer> intIds)
    {
        final int[] ids = new int[intIds.size()];
        
        for (int i=0; i<intIds.size(); i++) {
            ids[i] = intIds.get(i).intValue();
        }
        
        return ids;
    }
}
