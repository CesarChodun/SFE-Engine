package core.resources;

/**
 * 
 * Interface for classes that allocate
 * memory and should free it at the end
 * of their lifetime.
 * 
 * @author Cezary Chodun
 * @since 01.10.2019
 */
public interface Destroyable {

    /**
     * Frees allocated data.
     * After this call the object
     * <b>should</b> not be used.
     */
    public void destroy();
    
}
