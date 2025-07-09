/*
 * frmSettings.java
 *
 * Created on 13 January 2009, 21:51
 *
 * Configure Mobuds to your requirements
 */

package mobuds;

import com.sun.lwuit.*;

/**
 * @author rob smith
 */
public class frmSettings extends ModalForm {
    
    private CheckBox cbxAlertVibrate;
    private CheckBox cbxAlertFlashing;
    private CheckBox cbxAlertPlaySound;
    private CheckBox cbxAlertPopup;
    
    private CheckBox cbxEnableMessages;
    private CheckBox cbxOnlyFromFriends;
    
    private CheckBox cbxRememberSurroundings;
    private CheckBox cbxRememberLocations;

    private CheckBox cbxLocAdverts;
    private CheckBox cbxLocPromo;
    private CheckBox cbxLocVenNews;
    private CheckBox cbxLocGenNews;

    private CheckBox cbxUseLiteMode;

   // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) saveSettings();
        return true;
    }

    // Save all this back to the alert
    private void saveSettings() {
        moSystem.persData.bVibrateOnMessage = cbxAlertVibrate.isSelected(); 
        moSystem.persData.bFlashOnMessage = cbxAlertFlashing.isSelected(); 
        moSystem.persData.bMakeNoiseOnMessage = cbxAlertPlaySound.isSelected(); 
        moSystem.persData.bDisplayMessage = cbxAlertPopup.isSelected();         
        
        moSystem.persData.bAlertFriends = cbxEnableMessages.isSelected(); 
        moSystem.persData.bAlertBlocks = cbxOnlyFromFriends.isSelected(); 

        moSystem.persData.bMonitorBluetooth = cbxRememberSurroundings.isSelected(); 
        moSystem.persData.bMonitorGPS = cbxRememberLocations.isSelected(); 

        moSystem.persData.blocAdverts = cbxLocAdverts.isSelected(); 
        moSystem.persData.blocPromos = cbxLocPromo.isSelected(); 
        moSystem.persData.blocVenNews = cbxLocVenNews.isSelected(); 
        moSystem.persData.blocGenNews = cbxLocGenNews.isSelected();

        moSystem.persData.bLiteMode = cbxUseLiteMode.isSelected();
    }
        
    
    /** Creates a new instance of frmAlertSetup */
    public frmSettings(MobudsSystem moSys) {
        super(moSys,"Settings");
        
        addInfoLine("Setup Mobuds to work how you want it to:");

        // Buttons
        addModalCommand("Cancel",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Save",moSystem.imgTick,MR_OK,CMD_NONE);

        startNewSection("Tracking");
        cbxRememberSurroundings = new CheckBox("Remember everyone seen"); cbxRememberSurroundings.setSelected(moSys.persData.bMonitorBluetooth); addComponent(cbxRememberSurroundings);
        cbxRememberLocations = new CheckBox("Remember your locations"); cbxRememberLocations.setSelected(moSys.persData.bMonitorGPS); addComponent(cbxRememberLocations);
        cbxRememberLocations.setEnabled(false);   // not supported *yet*

        startNewSection("Message Received");
        cbxAlertVibrate = new CheckBox("Vibrate"); cbxAlertVibrate.setSelected(moSys.persData.bVibrateOnMessage); addComponent(cbxAlertVibrate);
        cbxAlertFlashing = new CheckBox("Flash Screen"); cbxAlertFlashing.setSelected(moSys.persData.bFlashOnMessage); addComponent(cbxAlertFlashing);
        cbxAlertPlaySound = new CheckBox("Play Sound"); cbxAlertPlaySound.setSelected(moSys.persData.bMakeNoiseOnMessage); addComponent(cbxAlertPlaySound);
        cbxAlertPopup = new CheckBox("Popup Message"); cbxAlertPopup.setSelected(moSys.persData.bDisplayMessage); addComponent(cbxAlertPopup);
        
        startNewSection("Receive Messages");
        cbxEnableMessages = new CheckBox("Enable Messages"); cbxEnableMessages.setSelected(moSys.persData.bAlertFriends); addComponent(cbxEnableMessages);
        cbxOnlyFromFriends = new CheckBox("Only From "+baseconf.strFriends); cbxOnlyFromFriends.setSelected(moSys.persData.bAlertBlocks); addComponent(cbxOnlyFromFriends);

        startNewSection("Location Messages");
        addInfoLine("While running Mobuds you can be sent messages directly from where you are.  These might include local news, promotions, offers or advertising.  Tick what you would like to be able to receive:");
        cbxLocAdverts = new CheckBox("Local Adverts"); cbxLocAdverts.setSelected(moSys.persData.blocAdverts); addComponent(cbxLocAdverts);
        cbxLocPromo = new CheckBox("Local Promotions"); cbxLocPromo.setSelected(moSys.persData.blocPromos); addComponent(cbxLocPromo);
        cbxLocVenNews = new CheckBox("Local News"); cbxLocVenNews.setSelected(moSys.persData.blocVenNews); addComponent(cbxLocVenNews);
        cbxLocGenNews = new CheckBox("General News"); cbxLocGenNews.setSelected(moSys.persData.blocGenNews); addComponent(cbxLocGenNews);

        startNewSection("Settings");
        addInfoLine("Changing these settings will change performance of this application.");

        cbxUseLiteMode = new CheckBox("Use Lite Graphics"); cbxUseLiteMode.setSelected(moSys.persData.bLiteMode); addComponent(cbxUseLiteMode);
    }
}
