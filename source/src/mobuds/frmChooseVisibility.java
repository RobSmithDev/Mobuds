/*
 * frmChooseVisibility.java
 *
 * Created on 01 January 2009, 19:30
 *
 * Simple dialog for confuguring which items should be visible to other people
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;

/**
 *
 * @author rob smith
 */
public class frmChooseVisibility extends ModalForm {
    // list of checkboxes
    private Vector checkboxes;
    private Command cmdOK;
    private Command cmdCancel;
    private GroupLayout layout;
           

    // Creates a new instance of frmChooseVisibility
    public frmChooseVisibility(MobudsSystem system, String title, int initialStatus) {
            super(system,"Profile Visibility");                       
            int a;
            CheckBox cbox;
            checkboxes = new Vector();

            addModalCommand("Cancel",system.imgCross,MR_CANCEL,CMD_BACK);
            addModalCommand("Save",system.imgTick,MR_OK,CMD_NONE);
            addInfoLine(title);
            
            for (a=0; a<baseconf.visibilityFields.length; a++) {
                cbox = new CheckBox(baseconf.visibilityFields[a]);
                
                addComponent(cbox);
                
                cbox.setSelected((initialStatus&baseconf.visibilityBitmasks[a])!=0);
                checkboxes.addElement(cbox);
            }
    }
    
    // Returns the current visibiity bitmask
    public int getVisibility() {
        int a,value;
        Enumeration Enum;
        CheckBox cbox;
        
        a=0;   
        value=0;
        Enum = checkboxes.elements();
        
        while (Enum.hasMoreElements()) {               
            cbox = (CheckBox)Enum.nextElement();
            if (cbox.isSelected()) value|=baseconf.visibilityBitmasks[a];
            a++;
        }
        
        return value;
    }
}
