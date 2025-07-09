/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;

import com.sun.lwuit.Dialog;
import javax.microedition.lcdui.AlertType;
import javax.microedition.midlet.MIDlet;


/**
 *
 * @author rob smith
 */
public class clsSfx {
    
    private MIDlet MainApp;

    public clsSfx(MIDlet mainApp) {
        MainApp = mainApp;
    }

        // Play one of the sfx from the DIALOG class
    public void playSound(int sfx) {
        switch (sfx) {
            case Dialog.TYPE_ALARM: AlertType.ALARM.playSound(javax.microedition.lcdui.Display.getDisplay(MainApp)); break;
            case Dialog.TYPE_CONFIRMATION: AlertType.CONFIRMATION.playSound(javax.microedition.lcdui.Display.getDisplay(MainApp)); break;
            case Dialog.TYPE_ERROR: AlertType.ERROR.playSound(javax.microedition.lcdui.Display.getDisplay(MainApp)); break;
            case Dialog.TYPE_INFO: AlertType.INFO.playSound(javax.microedition.lcdui.Display.getDisplay(MainApp)); break;
            case Dialog.TYPE_WARNING: AlertType.WARNING.playSound(javax.microedition.lcdui.Display.getDisplay(MainApp)); break;
        }
    }

}
