/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor;

/**
 *
 * @author dwimsey
 */
public class DGSProcessingException extends java.lang.Exception {
    public DGSProcessingException(String errMsg)
    {
        super(errMsg);
    }
    
    public DGSProcessingException(String errMsg, Exception e)
    {
        super(errMsg, e);
    }
}