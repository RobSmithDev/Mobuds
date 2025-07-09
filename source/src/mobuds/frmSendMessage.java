/*
 * VERY simple form to send a message to someone
 * When the form closes it has ALREADY sent the message!
 */

package mobuds;

import com.sun.lwuit.*;


/**
 *
 * @author rob smith
 */
public class frmSendMessage extends ModalForm {

    private TextField txtBox;
    private String strBlue;

    public frmSendMessage(MobudsSystem moSys, String bluetoothAddress, String strPersonName, String strLastMessage) {
        super(moSys,"Send Message");
        strBlue = bluetoothAddress;

        addModalCommand("Cancel",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Send",moSystem.imgTick,MR_OK,CMD_NONE);
        addPair("To:",new Label(strPersonName),60);
        addPair("Message:",new Label(" "),60);
        
        txtBox = new TextField(strLastMessage);
        txtBox.setMaxSize(baseconf.MAX_MEESSAGE_LENGTH);
        
        addComponent(txtBox);
    }

    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==ModalForm.MR_OK) {
            if (txtBox.getText().length()<2) {
                displayMessage("Error","Your message is too short",true);
                return false;
            } else {
                moSystem.bluetoothAccess.sendMessageToAddress(strBlue,txtBox.getText());
                return true;
            }
        } else return true;
    }
}
