/*
 * Mobuds.java
 *
 * Created on 09 December 2006, 18:36
 */

package mobuds;

import javax.microedition.midlet.*;
import com.sun.lwuit.Display;

/**
 *
 * @author rob smith
 */

public class Mobuds extends MIDlet {
    
    // Reference to th emobuds subsystem
    private MobudsSystem moSys;

    // Start the main application
    public void startApp() {
        Display.init(this);
                
        moSys = new MobudsSystem(this);        
        moSys.main();
    }

    public void pauseApp() {            
    }

    public void destroyApp(boolean unconditional) {         
        moSys.close();
    }
}