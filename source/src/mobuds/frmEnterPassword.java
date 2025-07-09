/*
 * frmEnterPassword.java
 *
 * Created on 04 January 2009, 20:53
 *
 * Form for entering username/password into the system
 */

package mobuds;

import com.sun.lwuit.TextField;
import com.sun.lwuit.CheckBox;

/**
 *
 * @author rob smith
 */
public class frmEnterPassword extends ModalForm {
    
    private int lwidth = 60;
    TextField txtUsername;
    TextField txtPassword;
    CheckBox cbxRemember;
    
    // gets the username from the dialog
    public String getUsername() {
        return txtUsername.getText();
    }
    
    // gets the password from the dialog
    public String getPassword() {
        return txtPassword.getText();
    }
    
    // should we save the password?
    public boolean shouldSavePassword() {
        moSystem.persData.sUsername = txtUsername.getText();

        if (cbxRemember.isSelected()) {
            moSystem.persData.sPassword = txtPassword.getText();            
            return true;
        } else return false;
    }
    
     protected boolean canClose(int modalResult) {
         shouldSavePassword();
         return true;
     }
    
    // Creates a username/password entry form
    public frmEnterPassword(MobudsSystem mosystem, String title) {
        super(mosystem,title);
        lwidth = /*mosystem.persData.bUseSmallFont?60:*/84;
        addInfoLine("Please enter your username and password used on the Mobuds website (at http://www.mobuds.com)");
        addSpacerLine();
        
        addModalCommand("Cancel",moSystem.imgCross,MR_CANCEL,CMD_BACK);        
        addModalCommand("Sign In",moSystem.imgTick,MR_OK,CMD_NONE);
        
        txtUsername = new TextField(moSystem.persData.sUsername);
        txtPassword = new TextField(moSystem.persData.sPassword);
        cbxRemember = new CheckBox("Remember Password");
        cbxRemember.setSelected(moSystem.persData.sPassword.length()>0);
        
        txtUsername.setConstraint(TextField.EMAILADDR);
        txtPassword.setConstraint(TextField.PASSWORD);
        txtUsername.setMaxSize(120);
        txtPassword.setMaxSize(40);
        
        addPair("Email:",txtUsername,lwidth);
        addPair("Password:",txtPassword,lwidth);
        addPair("",cbxRemember,lwidth);
    }
    
}