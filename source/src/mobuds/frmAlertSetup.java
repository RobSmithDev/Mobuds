/*
 * frmAlertSetup.java
 *
 * Created on 12 January 2009, 22:09
 *
 * Alert Settings
 */

package mobuds;

import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 *
 * @author rob smith
 */
public class frmAlertSetup extends ModalForm implements ActionListener {
    
    private CheckBox cbxAlertVibrate;
    private CheckBox cbxAlertFlashing;
    private CheckBox cbxAlertPlaySound;
    private CheckBox cbxAlertPopup;
    private CheckBox cbxFriendClose;
    private CheckBox cbxBlockClose;
    private CheckBox cbxAlert1;
    private CheckBox cbxAlert2;
    private CheckBox cbxAlert3;
    private Button btnAlert1;
    private Button btnAlert2;
    private Button btnAlert3;
    private String[] strAlertBackup = new String[3];    
    private int lastAlertIndex;

   // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) saveSettings();
        return true;
    }

    // Save all this back to the alert
    private void saveSettings() {
        moSystem.persData.bAlertVibrateOnMessage = cbxAlertVibrate.isSelected(); 
        moSystem.persData.bAlertFlashOnMessage = cbxAlertFlashing.isSelected(); 
        moSystem.persData.bAlertMakeNoiseOnMessage = cbxAlertPlaySound.isSelected(); 
        moSystem.persData.bAlertShowMessage = cbxAlertPopup.isSelected();         
        moSystem.persData.bAlertFriends = cbxFriendClose.isSelected(); 
        moSystem.persData.bAlertBlocks = cbxBlockClose.isSelected(); 
        moSystem.persData.bEnableAlert[0] = cbxAlert1.isSelected(); 
        moSystem.persData.sAlertProfile[0] = strAlertBackup[0];
        moSystem.persData.bEnableAlert[1] = cbxAlert2.isSelected(); 
        moSystem.persData.sAlertProfile[1] = strAlertBackup[1];
        moSystem.persData.bEnableAlert[2] = cbxAlert3.isSelected(); 
        moSystem.persData.sAlertProfile[2] = strAlertBackup[2];
        
    }
        
    // Called when a sub-modal form closes - the form passesd is the form that closed
    protected void subModalFormClose(ModalForm frm) {
        if (frm.modalResult()==ModalForm.MR_OK) {
            AlertProfile profile = new AlertProfile(lastAlertIndex+1);
            profile.fromString(strAlertBackup[lastAlertIndex]);
            profile = ((frmEditAlertProfile)frm).getProfileFromForm(profile);
            strAlertBackup[lastAlertIndex] = profile.toString();
        }
    }
    
    // Edit a specific alert
    private void editAlert(int alert) {
        lastAlertIndex = alert;
        AlertProfile profile = new AlertProfile(alert+1);
        profile.fromString(strAlertBackup[alert]);
        frmEditAlertProfile dlg = new frmEditAlertProfile(moSystem,alert+1,profile);
        showSubModal(dlg);
    }
    
    // Respond to buttons
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==btnAlert1) editAlert(0); else
        if (evt.getSource()==btnAlert2) editAlert(1); else
        if (evt.getSource()==btnAlert3) editAlert(2);
    }


    private Button makeButton(String title, Image image) {
        Button btn = new Button(" "+title,image);
        btn.addActionListener(this);
        addComponent(btn);
        Style st = btn.getStyle();
        Style st2 = btn.getSelectedStyle();
        st.setMargin(0,0,5,5);
        st2.setMargin(0,0,5,5);
        st.setPadding(5,5,5,5);
        st2.setPadding(5,5,5,5);
        st2.setFgColor(0xFFFFFF);
        st2.setBorder(st.getBorder());
        st2.setBgImage(st.getBgImage());
        return btn;
    }
    
    /** Creates a new instance of frmAlertSetup */
    public frmAlertSetup(MobudsSystem moSys) {
        super(moSys,"Profile Matches");
        
        addInfoLine("Mobuds can notify you about the people around you.");

        // Buttons
        addModalCommand("Cancel",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Save",moSystem.imgTick,MR_OK,CMD_NONE);
        
        startNewSection("Alert Me By");
        cbxAlertVibrate = new CheckBox("Vibrating"); cbxAlertVibrate.setSelected(moSys.persData.bAlertVibrateOnMessage); addComponent(cbxAlertVibrate);
        cbxAlertFlashing = new CheckBox("Flashing"); cbxAlertFlashing.setSelected(moSys.persData.bAlertFlashOnMessage); addComponent(cbxAlertFlashing);
        cbxAlertPlaySound = new CheckBox("Playing Sound"); cbxAlertPlaySound.setSelected(moSys.persData.bAlertMakeNoiseOnMessage); addComponent(cbxAlertPlaySound);
        cbxAlertPopup = new CheckBox("Popup Message"); cbxAlertPopup.setSelected(moSys.persData.bAlertShowMessage); addComponent(cbxAlertPopup);
        
        startNewSection("Alert Me When");
        cbxFriendClose = new CheckBox(baseconf.strFriends+" are near"); cbxFriendClose.setSelected(moSys.persData.bAlertFriends); addComponent(cbxFriendClose);
        cbxBlockClose = new CheckBox(baseconf.strBlocks+" are near"); cbxBlockClose.setSelected(moSys.persData.bAlertBlocks); addComponent(cbxBlockClose);

        startNewSection("Alert Match 1");
        cbxAlert1 = new CheckBox("Alert When Matched"); cbxAlert1.setSelected(moSys.persData.bEnableAlert[0]); addComponent(cbxAlert1);
        btnAlert1 = makeButton("Edit Match...",moSystem.imgMatch);

        strAlertBackup[0] = new String(moSystem.persData.sAlertProfile[0]);

        startNewSection("Alert Match 2");
        cbxAlert2 = new CheckBox("Alert When Matched"); cbxAlert2.setSelected(moSys.persData.bEnableAlert[1]); addComponent(cbxAlert2);
        btnAlert2 = makeButton("Edit Match...",moSystem.imgMatch);
        strAlertBackup[1] = new String(moSystem.persData.sAlertProfile[1]);

        startNewSection("Alert Match 3");
        cbxAlert3 = new CheckBox("Alert When Matched"); cbxAlert3.setSelected(moSys.persData.bEnableAlert[2]); addComponent(cbxAlert3);
        btnAlert3 = makeButton("Edit Match...",moSystem.imgMatch);
        strAlertBackup[2] = new String(moSystem.persData.sAlertProfile[2]);
    }
}
