/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boxuk.groxy.gmail;

import com.sun.mail.iap.Argument;

/**
 *
 * @author rod
 */
abstract public class BaseCommand
{
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
}
