/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.*;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.ButtonGroup;
import javax.bluetooth.RemoteDevice;
import com.sun.lwuit.util.Resources;

/**
 *
 * @author rob smith
 */
public class frmNonMobudsUserList extends ModalForm {

    private ButtonGroup bg;
    public RemoteDevice selectedDevice = null;
    private Vector thelist;

    // OK!
    private void runOK() {
        if (!bg.isSelected()) displayMessage("Tell Friend","You must choose one of the above first",true); else {
            selectedDevice = (RemoteDevice)thelist.elementAt(bg.getSelectedIndex());
            setModalResult(ModalForm.MR_OK);
        }
    }

    // User list
    public frmNonMobudsUserList(MobudsSystem mosys, Vector devices) {
        super(mosys,"Search Results");
        thelist = devices;

        addModalCommand("Close",mosys.imgCross,MR_CANCEL,CMD_BACK);

        Command cmd = new Command("Tell Friend",mosys.imgAction) {
            public void actionPerformed(ActionEvent evt) {
                        runOK();
            };
        };
        addCommand(cmd);
        
        bg = new ButtonGroup();
        RadioButton rb;
        Image syncImg = null;

        try {
            Resources resources = Resources.open("/Gfx/quick.res");
            syncImg = resources.getImage("syncgfx");
            resources = null;
        } catch (Exception e) {};
        
        if (syncImg!=null) syncImg = syncImg.subImage(16,0,16,16,true);
        String s;

        // Make the list
        for (int a=0; a<devices.size(); a++) {
            try {
                s = ((RemoteDevice)devices.elementAt(a)).getFriendlyName(false);
            } catch (Exception ex) {s="";};
            if (s.length()<1) s=baseconf.UNKNOWN_NAME;

            rb = new RadioButton(s,syncImg);
            bg.add(rb);
            addComponent(rb);
            if (a==0) {
                rb.setSelected(true);
                bg.setSelected(rb);
            }
        }


    }
}
