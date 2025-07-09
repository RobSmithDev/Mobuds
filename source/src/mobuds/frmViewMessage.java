/*
 * A simple "View Message" form
 */

package mobuds;

import java.util.Calendar;
import java.util.*;
import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;


/**
 *
 * @author rob smith
 */
public class frmViewMessage extends ModalForm {

    private int lwidth;
    private TransmittedMessage transMessage;
    private MobudsProfile mp = new MobudsProfile();
    private clsMohubServer mh = new clsMohubServer();
    private dlgActions dlg;
    private String strLastMessage;
    private Label fromField;
    private boolean mohubmode;


    // refresh the from field
    private void refreshFrom() {
        if (moSystem.persData.isFriend(transMessage.sFromBluetooth)) fromField.setIcon(moSystem.imgFriend); else
        if (moSystem.persData.isBlocked(transMessage.sFromBluetooth)) fromField.setIcon(moSystem.imgBlocked); else fromField.setIcon(moSystem.imgNotListed);
    }

    // Private message for receiving action click
    private void runActionPopup() {
        if (mp==null) return;

        dlg = new dlgActions(moSystem,transMessage.sFromBluetooth,!mohubmode,!mohubmode,false);
        int res = dlg.showOptions();
        dlg = null;

        switch (res) {
            case dlgActions.RESULT_VIEWPROFILE:
                if (!mohubmode) showSubModal(new frmViewProfile(moSystem,"View Profile",transMessage.sFromBluetooth,mp));
                break;

            case dlgActions.RESULT_SENDMESSAGE:
                if (!mohubmode)
                    showSubModal(new frmSendMessage(moSystem,transMessage.sFromBluetooth,mp.strNickName,""));
                break;

            case dlgActions.RESULT_ADDREMOVEFRIEND:
                if (!mohubmode)
                    if (moSystem.persData.isFriend(transMessage.sFromBluetooth)) moSystem.persData.delUserFromFriends(transMessage.sFromBluetooth);
                                                    else if (!moSystem.persData.addUserToFriends(transMessage.sFromBluetooth, mp.saveToString(false))) showAddError(true);
                refreshFrom();
                break;

            case dlgActions.RESULT_ADDREMOVEBLOCK:
                if (!mohubmode)
                    if (moSystem.persData.isBlocked(transMessage.sFromBluetooth)) moSystem.persData.delUserFromBlocks(transMessage.sFromBluetooth);
                                                    else if (!moSystem.persData.addUserToBlockList(transMessage.sFromBluetooth, mp.saveToString(false))) showAddError(false);
                refreshFrom();
                break;
        }

    }

    // Called when a sub-modal form closes - the form passesd is the form that closed
    protected void subModalFormClose(ModalForm frm) {
        if (frm.modalResult()==ModalForm.MR_OK)
            if (frm instanceof frmSendMessage)
                try {
                    PersonMessage pm = (PersonMessage)transMessage;
                    if (!pm.hasReplied) {
                        pm.hasReplied = true;
                        moSystem.bluetoothAccess.refreshMailStatistics();
                    }
                } catch (Exception e) {};
    }


     // Creates a new instance of frmEditProfile
    public frmViewMessage(MobudsSystem system, TransmittedMessage tm) {
        super(system,"View Message");
        transMessage = tm;
        lwidth = /*system.persData.bUseSmallFont?40:*/65;
        
        PersonMessage pm;
        try {pm = (PersonMessage)tm; }catch (Exception e) {pm=null;}
        if (!mp.loadFromString(tm.sSourceProfile)) {
            mp = null;
            if (!mh.updateProfile("",tm.sFromBluetooth,tm.sSourceProfile)) mh=null;
        } else mh=null;

        if (pm!=null) {
            pm.hasBeenRead = true;
            moSystem.bluetoothAccess.refreshMailStatistics();
        }
        mohubmode = (mh!=null);

        strLastMessage="";
        transMessage = tm;
        Calendar d = Calendar.getInstance();
        Date dd = new Date();

        startNewSection("Details");
        if (mp!=null) {
            fromField = new Label(" "+mp.strNickName);                    
            addPair("From:",fromField,lwidth);
            refreshFrom();
        } else
        if (mh!=null) addPair("From:",new Label(mh.strMohubName),lwidth);
        if (pm!=null) {
            try {dd.setTime(pm.lTime);} catch (Exception e) {};
            d.setTime(dd);
            addPair("Date:",new Label(baseconf.getNiceField(d.get(Calendar.DAY_OF_MONTH))+" "+baseconf.monthNames[d.get(Calendar.MONTH)]+" "+d.get(Calendar.YEAR)+" at "+baseconf.twoWide(d.get(Calendar.HOUR))+":"+baseconf.twoWide(d.get(Calendar.MINUTE))+" "+(d.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM")),lwidth);
        }

        switch (tm.bMessageType) {
            case baseconf.COMMUNICATION_LOCALERT:startNewSection("Local Alert"); break;
            case baseconf.COMMUNICATION_LOCPROMO:startNewSection("Local Promotion"); break;
            case baseconf.COMMUNICATION_LOCVENNEWS:startNewSection("Venue News"); break;
            case baseconf.COMMUNICATION_LOCGENNEWS:startNewSection("Local News"); break;
            default: startNewSection("Message"); break;
        }
        
        switch (tm.bErrorCode) {

            default:
                if (pm!=null) {
                    addInfoLine(pm.sMessage);
                    strLastMessage=pm.sMessage;
                }
         }

        addInfoLine(" ");
        addModalCommand("Close",moSystem.imgCross,MR_CANCEL,CMD_BACK);

        if (tm.bMessageType==baseconf.COMMUNICATION_SENDMESSAGE) addModalCommand("Action",moSystem.imgCross,MR_OK,CMD_DEFAULT);
    }
    
    
    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) {
            runActionPopup();
            return false;
        }
        return true;
    }    

}
