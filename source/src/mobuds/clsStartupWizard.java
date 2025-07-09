/*
 * clsStartupWizard.java
 *
 * Created on 03 January 2009, 20:31
 *
 * Wizard for application startup - if required
 */

package mobuds;

/**
 *
 * @author rob smith
 */
public class clsStartupWizard {
    
    private static int CURRENT_LICENCE_VERSION = 3;
    private MobudsSystem mosystem;
    public boolean upgradeAvailable;
    
    /** Creates a new instance of clsStartupWizard */
    public clsStartupWizard(MobudsSystem mosys) {
            mosystem = mosys;
    }
    
    // Runs the startup wizard. Returns TRUE if continue, false if we should could
    public boolean runWizard() {
        boolean newLic = false;
        
    
        // ensure licence version set
        if (mosystem.persData.bLastLicenceShown<CURRENT_LICENCE_VERSION) {
                frmLicenceAgreement frm = new frmLicenceAgreement(mosystem,"Licence Agreement");
                frm.showModal();
                if (frm.modalResult()!=ModalForm.MR_OK) return false;    
                mosystem.persData.bLastLicenceShown = CURRENT_LICENCE_VERSION;
                mosystem.splash.show();  // re-show the splash form
                frm = null;
                newLic = true;
        }
        mosystem.splash.show();  // re-show the splash form
        mosystem.splash.show();  // re-show the splash form
        mosystem.splash.show();  // re-show the splash form
                
        // check if profile is valid
        if (mosystem.persData.shouldWizardRun()) {
            mosystem.splash.show();  // re-show the splash form
            if (mosystem.displayQuestion("Welcome","Do you want to recover your profile from the Mobuds website?","Yes","No")) {
                mosystem.splash.show();  // re-show the splash form        
                MobudsInternetConnection inet;
                    String u;
                    String p;
                    frmEnterPassword frmPassword;
                    newLic = false;
                    while (!newLic) {     
                        inet = new MobudsInternetConnection(mosystem);
                        frmPassword = new frmEnterPassword(mosystem,"Sign in to Mobuds");
                        frmPassword.showModal();
                        if (frmPassword.modalResult()!=ModalForm.MR_OK) return false;
                        frmPassword.shouldSavePassword();  // jsut force it to if it can!
                        u = frmPassword.getUsername();
                        p = frmPassword.getPassword();
                        frmPassword = null;

                        switch (inet.connectToServer(u,p,false)) {
                            case MobudsInternetConnection.HTTP_OK:newLic=true;upgradeAvailable=inet.bUpgradeIsRequired; break;
                            case MobudsInternetConnection.HTTP_ERROR: mosystem.displayMessage("Error","An unknown error occured. Check your internet connection and try again.",true); break;
                            case MobudsInternetConnection.HTTP_UNKNOWNUSER: mosystem.displayMessage("Error","Invalid email address or password. Please check and try again.",true); break;
                            case MobudsInternetConnection.HTTP_UNKNOWNERROR:  mosystem.displayMessage("Error","An unknown communication error occured. Please try again.",true); break;
                        }
                        
                        if (upgradeAvailable) {
                            if (mosystem.displayQuestion("Upgrade","A newer version of this Mobuds application is now available.\r\n\r\nWould you like to download it now?","Yes","No")) {                                    
                                try {
                                    mosystem.MainApp.platformRequest(baseconf.SOFTWARE_DOWNLOAD_URL);
                                } catch (Exception e) {upgradeAvailable=false;}
                            } else upgradeAvailable=false;
                        }
                    }                    
            } else {
                mosystem.splash.show();  // re-show the splash form
                frmEditProfile profile = new frmEditProfile(mosystem,"Please fill in the details of your profile:",mosystem.persData.myProfile);
                profile.showModal();                
                if (profile.modalResult()!=ModalForm.MR_OK) return false;
                mosystem.splash.show();  // re-show the splash form
                mosystem.persData.myProfile = profile.getProfileFromForm(mosystem.persData.myProfile);
                profile = null;
                frmChooseVisibility vis = new frmChooseVisibility(mosystem,"Please choose which parts of your profile others should see:",mosystem.persData.myProfile.lVisibility);
                vis.showModal();
                if (vis.modalResult()!=ModalForm.MR_OK) return false;
                mosystem.splash.show();  // re-show the splash form                        
                mosystem.persData.myProfile.lVisibility = vis.getVisibility();
                vis = null;
                newLic = true;
            }            
        } 
        mosystem.splash.show();  // re-show the splash form
        
        if (newLic) {
            mosystem.persData.bSaveUserData(PersistantData.SAVE_PROFILE);
        }
        
        // quit after upgrade
        if (upgradeAvailable) return false;
        
        return true;
    }
    
}
