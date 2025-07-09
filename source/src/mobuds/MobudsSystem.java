/*
 * MobudsSystem.java
 *
 * Created on 21 December 2008, 17:49
 *
 * Mobuds main application in a seperate class for tidyness
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.plaf.*;
import javax.microedition.midlet.*;
import com.sun.lwuit.util.Resources;
import com.sun.lwuit.animations.*;
import com.sun.lwuit.*;
import com.sun.lwuit.Font;
import com.sun.lwuit.Command;

/**
 *
 * @author rob smith
 */
public class MobudsSystem {
    private static final String strForm = "Form";
    private static final String strDialogBody = "DialogBody";
    private static final String strDialog = "Dialog";
    private static final String strMenu = "Menu";
    private static final String strButton = "Button";
    private static final String strSoftButton = "SoftButton";
    private static final String strTitle = "Title";
    private static final String strDialogTitle = "DialogTitle";
    public static final int transitionDuration = 250;
    public static final int horizSeperatorLineDefColor = 0xBC0295;
    private static final int VIBRATE_LENGTH = 800;
    private static final int FLASHBACKLIGHT_LENGTH = 5000;
    public final MIDlet MainApp;
    public final PersistantData persData;
    public Image imgTick;
    public Image imgCross;
    public Image imgAction;
    public Image imgMale;
    public Image imgFemale;
    public Image imgUnknown;   
    public Image imgStarSigns;    
    public Image imgFriend;
    public Image imgBlocked;
    public Image imgNotListed;
    public Image imgNewMail;
    public Image imgOpenMail;
    public Image imgReplyMail;
    public Image imgView;
    public Image imgCancel;
    public Image imgDot;
    public Image imgUnDot;
    public Image imgDotOpt;
    public Image imgSplashLogo;
    public Image imgDeleteMessage;
    public Image imgMatch;
    public Font headingFont;
    public ModalForm activeForm;
    public int TimeTillNextScan = 0;
    protected Image horizSeperatorLine;
    protected Image miniLogo;
    public frmSplash splash;
    public BTManager bluetoothAccess;
    private frmMainScreen mainScreen;
    private int forceJumpTo = -1;
    private clsSfx sfx;
    private int newInSize;
    private int defaultInboxMode=1;
    private int newAlertType = -10;
    private long newAlertSince;
    public String[] errors;
    public String[] debug;
    
    // returns the application version
    public String getVersion() {
            String ver = MainApp.getAppProperty("MIDlet-Version");
            if (ver==null) ver="0.1";

            return ver;
    }   
    
    public Transition getFormTransition(boolean directionIN) {
        /*Transition standardEffect = null;
        if (persData.bUseTransitions) {
            standardEffect = Transition3D.createRotation(transitionDuration,directionIN);
            if (standardEffect==null) standardEffect=CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL,directionIN,transitionDuration,true);
        }
        return standardEffect;*/ return null;
    }

    public Transition getTransition(boolean directionIN) {
        /*Transition standardEffect = null;
        if (persData.bUseTransitions) {
            standardEffect = Transition3D.createStaticRotation(transitionDuration,directionIN);
            if (standardEffect==null) standardEffect=CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL,directionIN,transitionDuration,true);
        }
        return standardEffect;*/ return null;
    }

    // Log a debug message
    public void logDebugMessage(String s) {
        if (baseconf.ISDEBUG) {
            for (int a=1; a<debug.length-1; a++) debug[a]=debug[a+1]+"";
            debug[debug.length-1]=s;
            System.out.println("DEBUG: "+s);
        }
    }
    
    // Do nothing here
    public void logErrorMessage(String s) {
        if (s.length()<1) return;
        for (int a=0; a<errors.length-1; a++) errors[a]=errors[a+1]+"";
        errors[errors.length-1]=s;
        if (baseconf.ISDEBUG) System.out.println("ERROR: "+s);
    }

    
    // new number of users sent
    public void notifyUserlistChanged(int newNumberOfUsers, int newNumberOfMatchedUsers, int newNumberOfFriends, int newNumberOfBlocks) {
        if (mainScreen!=null) {
            mainScreen.numberOfUsers = newNumberOfUsers;
            mainScreen.numberOfMatchedUsers = newNumberOfMatchedUsers;
            mainScreen.numberOfFriends = newNumberOfFriends;
            mainScreen.numberOfBlocks = newNumberOfBlocks;
            mainScreen.bRepaintIt = true;
        }
    }


    // Displays a message on top of the dialog
    public void displayMessage(String title, String message, boolean bError) {
        Command okCommand = new Command("OK",imgTick);
        Dialog.show(title,message,okCommand,new Command[] {new Command("OK",imgTick), okCommand}, (bError?Dialog.TYPE_ERROR:Dialog.TYPE_INFO), null, 0, getTransition(true));
        pause(transitionDuration+150);
    }

    // return the alert type
    public int getAlertIndex() {
        if ((Math.abs(System.currentTimeMillis()-newAlertSince)>15000)||(System.currentTimeMillis()%1000>=500)) return -10;
        return newAlertType;
    }

    // Alert of matched profile
    public void signalNearbyPerson(String title, String message, int alertNumber, MobudsProfile profileMatched) {
        if (persData.bAlertVibrateOnMessage) com.sun.lwuit.Display.getInstance().vibrate(VIBRATE_LENGTH);
        if (persData.bAlertFlashOnMessage) com.sun.lwuit.Display.getInstance().flashBacklight(FLASHBACKLIGHT_LENGTH);
        if (persData.bAlertMakeNoiseOnMessage) sfx.playSound(Dialog.TYPE_ALARM);
        mainScreen.bRepaintIt = true;
        newAlertSince = System.currentTimeMillis();
        newAlertType = alertNumber;

        if ((activeForm == mainScreen) && (persData.bAlertShowMessage)) {
            if (alertNumber>=0) {   /// Alert Triggered
                //displayMessage(title,message,false);
            } else
            if (alertNumber==-1) {   // Friend Near
                displayMessage(title,message,false);
            } else
            if (alertNumber==-2) {   // Blocked Person Near
                displayMessage(title,message,false);
            }
        }
    }
    
    // Notification about messages
    public void setInOut(int newMessages, int inboxSize, int outboxSize, int alertsNew, int alertsTotal) {
        newInSize = newMessages;
        if (mainScreen!=null) {
            mainScreen.inboxSize = inboxSize;
            mainScreen.inboxNew = newMessages;
            mainScreen.outboxSize = outboxSize;
            mainScreen.alertsNew = alertsNew;
            mainScreen.alertsTotal = alertsTotal;
            mainScreen.bRepaintIt = true;
        }
    }
    
    // Force jump to a menu item
    public void jumpToMenu(int menuNum) {
        mainScreen.cancelModal();
        forceJumpTo = menuNum;
    }

    // Returns an int showing the status, false=0ff, true=on
    public boolean getMailIconStatus() {
        if (newInSize>0) return System.currentTimeMillis()%1000<500; else return false;
    }

    // Notification a message was received
    public void signalNewMessage(String msgFrom, int inboxMode) {
        if (persData.bVibrateOnMessage) com.sun.lwuit.Display.getInstance().vibrate(VIBRATE_LENGTH);
        if (persData.bFlashOnMessage) com.sun.lwuit.Display.getInstance().flashBacklight(FLASHBACKLIGHT_LENGTH);
        if (persData.bMakeNoiseOnMessage) sfx.playSound(Dialog.TYPE_ALARM);

        // ensure re-draw which might trigger the animate!
        mainScreen.bRepaintIt = true;

        if ((activeForm == mainScreen) && (persData.bDisplayMessage)) {
            if (displayQuestion("Message Received","Message received from "+msgFrom+".\r\n\r\nGo to MoMail?","Yes","No")) {
                defaultInboxMode = inboxMode;
                jumpToMenu(5);  // Make inbox appear
            }
        } 
    }    
    
    /** Creates a new instance of MobudsSystem */
    public MobudsSystem(MIDlet mainApp) {
        sfx = new clsSfx(mainApp);
        MainApp = mainApp;
        persData = new PersistantData();
    }
    
    // Geta  copy of the inbox
    public Vector getInboxCopy() {
        return bluetoothAccess.getInboxCopy();
    }

    // new mailbox to be saved
    public void saveNewInbox(Vector mailbox) {
        if (bluetoothAccess!=null) bluetoothAccess.saveNewInbox(mailbox);
    }

    // Ask the user a YES/NO question
    public boolean displayQuestion(String title, String message, String sYes, String sNo) {
        Command okCommand = new Command(sYes,imgTick);
        Command cancelCommand = new Command(sNo,imgCross);
        
        boolean ret = Dialog.show(title,message,okCommand,new Command[] {cancelCommand,okCommand}, (Dialog.TYPE_CONFIRMATION), null, 0, getTransition(true))==okCommand;
        pause(transitionDuration+150);
        return ret;
    }
    
    public void pause(int milliseconds) {
        synchronized(MainApp) {
            try {
                MainApp.wait(milliseconds);
            } catch (Exception e) {}
        }
    }

    public void runFrontScreen() {
        int res;
        frmMenu menu;
        int modalRes;


        if (!bluetoothAccess.isOK()) {
            displayMessage("Error","There was an error starting up.  Please check the MoBuds website for support with MoBuds from your make and model of phone.",true);
            return;
        }

        mainScreen = new frmMainScreen(this,imgSplashLogo);
        bluetoothAccess.refreshMailStatistics();

        defaultInboxMode=1;
        do {
            forceJumpTo=-1;
            refreshTransitionSetting(mainScreen);
            mainScreen.showModal();
            if (forceJumpTo<0) {
                menu = new frmMenu(this);
                menu.showModal();
                modalRes = menu.modalResult();
            } else {
                modalRes = ModalForm.MR_OK;
                menu = null;
            }
            activeForm = null;
            if (modalRes==ModalForm.MR_OK) {
                if (menu!=null) res = menu.selectedIcon; else res=forceJumpTo;
                forceJumpTo=-1;
                menu = null;

                if (res==10) {
                    persData.advanceStep(7);
                    if (!this.displayQuestion("Quit","Are you sure you want to quit MoBuds?","Yes","No")) res=0;
                } else {
                    menu = null;
                    switch (res) {
                        case 0: // Browse
                            persData.advanceStep(0);
                            frmScrollPeopleList peopleBrowse;
                            do {
                                peopleBrowse = new frmScrollPeopleList(this,false);
                                peopleBrowse.populateWith(frmScrollPeopleList.BROWSEPEOPLE_VISIBLENOW);
                                peopleBrowse.showModal();
                                System.gc();
                            } while (peopleBrowse.modalResult() == ModalForm.MR_QUIT);
                            peopleBrowse = null;
                            break;
                        case 1: // Profile
                            frmEditProfile frmProfile = new frmEditProfile(this,"",persData.myProfile);
                            persData.advanceStep(4);
                            frmProfile.showModal();
                            if (frmProfile.modalResult()==ModalForm.MR_OK) {
                                persData.myProfile=frmProfile.getProfileFromForm(persData.myProfile);
                                if (persData.myProfile.strDescription.compareTo("mobuds")==0) persData.advanceStep(5); 
                                bluetoothAccess.signalProfileChanged();
                                persData.bSaveUserData(PersistantData.SAVE_PROFILE);
                            }
                            frmProfile = null;
                            break;
                        case 2: // Visbility
                            frmChooseVisibility frmVisibility = new frmChooseVisibility(this,"",persData.myProfile.lVisibility);
                            frmVisibility.showModal();
                            if (frmVisibility.modalResult()==ModalForm.MR_OK) {
                                persData.myProfile.lVisibility=frmVisibility.getVisibility();
                                bluetoothAccess.signalProfileChanged();
                                persData.bSaveUserData(PersistantData.SAVE_PROFILE);
                                persData.advanceStep(6);
                            }
                            frmVisibility = null;
                            break;
                        case 3: // MoMates
                            frmScrollPeopleList matesBrowse = new frmScrollPeopleList(this,true);
                            matesBrowse.populateWith(frmScrollPeopleList.BROWSEPEOPLE_ALLFRIENDS);
                            matesBrowse.showModal();
                            matesBrowse = null;
                            break;
                        case 4: // MoFoe's
                            frmScrollPeopleList foBrowse = new frmScrollPeopleList(this,true);
                            foBrowse.populateWith(frmScrollPeopleList.BROWSEPEOPLE_ALLBLOCKS);
                            foBrowse.showModal();
                            foBrowse = null;
                            break;
                        case 5: // MoMail
                            frmInbox inboxWin = new frmInbox(this,defaultInboxMode);
                            defaultInboxMode=1;
                            inboxWin.showModal();
                            
                            inboxWin = null;
                            break;
                        case 6: // Matches
                            frmAlertSetup matches = new frmAlertSetup(this);
                            persData.advanceStep(3);
                            matches.showModal();
                            if (matches.modalResult()==ModalForm.MR_OK) persData.bSaveUserData(PersistantData.SAVE_PROFILE);
                            matches = null;
                            break;
                        case 7: // Settings
                            persData.advanceStep(1);
                            frmSettings frmSet = new frmSettings(this);
                            frmSet.showModal();
                            if (frmSet.modalResult()==ModalForm.MR_OK) {
                                persData.bSaveUserData(PersistantData.SAVE_PROFILE);
                                refreshTransitionSetting(mainScreen);
                            } else persData.advanceStep(2);
                            frmSet = null;
                            break;
                        case 8: // Sync
                            frmSyncType frmSyncit = new frmSyncType(this);
                            frmSyncit.showModal();
                            modalRes = frmSyncit.modalResult();
                            frmSyncit = null;

                            frmEnterPassword frmPassword;
                            boolean direction = false;
                            
                            if (modalRes==ModalForm.MR_OK) {
                                // Backup
                                frmPassword = new frmEnterPassword(this,"Copy to Web");
                                frmPassword.showModal();
                                direction = true;
                                if (frmPassword.modalResult()!=ModalForm.MR_OK) frmPassword=null;
                            } else
                            if (modalRes==ModalForm.MR_QUIT) {
                                // Restore
                                frmPassword = new frmEnterPassword(this,"Copy from Web");
                                frmPassword.showModal();
                                direction = false;
                                if (frmPassword.modalResult()!=ModalForm.MR_OK) frmPassword=null;
                            } else frmPassword = null;

                            if (frmPassword!=null) {
                                boolean upgradeAvailable = false;
                                boolean connOK = false;
                                String u = frmPassword.getUsername();
                                String p = frmPassword.getPassword();
                                MobudsInternetConnection inet = new MobudsInternetConnection(this);
                                switch (inet.connectToServer(u,p,direction)) {
                                    case MobudsInternetConnection.HTTP_OK: connOK=true; upgradeAvailable=inet.bUpgradeIsRequired; break;
                                    case MobudsInternetConnection.HTTP_ERROR: displayMessage("Error","An unknown error occured. Check your internet connection and try again.",true); break;
                                    case MobudsInternetConnection.HTTP_UNKNOWNUSER: displayMessage("Error","Invalid email address or password. Please check and try again.",true); break;
                                    case MobudsInternetConnection.HTTP_UNKNOWNERROR:  displayMessage("Error","An unknown communication error occured. Please try again.",true); break;
                                }

                                // save it
                                if (connOK) {
                                    persData.bSaveUserData(PersistantData.SAVE_PROFILE|PersistantData.SAVE_FRIENDS|PersistantData.SAVE_BLOCKS);
                                    bluetoothAccess.saveMessagesToDisk();
                                }

                                if (upgradeAvailable) {
                                    if (displayQuestion("Upgrade","A newer version of this Mobuds application is now available.\r\n\r\nWould you like to download it now?","Yes","No")) {
                                        try {
                                            MainApp.platformRequest(baseconf.SOFTWARE_DOWNLOAD_URL);
                                        } catch (Exception e) {upgradeAvailable=false;}
                                    } else upgradeAvailable=false;
                                }

                                inet = null;
                                frmPassword = null;
                            }

                            break;
                        case 9: // Pass On
                            if (displayQuestion("Pass it On","Ensure your friend has Bluetooth enabled and visible.","Continue","Cancel")) {
                                frmBusy buz = new frmBusy(this);
                                buz.changeText("Searching...");
                                buz.show();
                                Vector people = bluetoothAccess.scanForUnknownUsers();
                                if (people.isEmpty()) {
                                    buz = null;
                                    people = null;
                                    displayMessage("Pass it On","No new devices were found.",false);
                                } else {
                                    frmNonMobudsUserList fmul = new frmNonMobudsUserList(this,people);
                                    people = null;
                                    buz = null;
                                    fmul.showModal();
                                    if (fmul.modalResult()==ModalForm.MR_OK) {
                                        buz = new frmBusy(this);
                                        buz.changeText("Sending...");
                                        buz.show();
                                        int errcode = bluetoothAccess.tellAFriend(fmul.selectedDevice);
                                        if (errcode!=0) {
                                            buz.changeText("Please Wait...");
                                            displayMessage("Tell Friend","Unable to send a message to this person. ("+errcode+")",false);
                                        }
                                        fmul = null;
                                        buz = null;
                                    }
                                    fmul = null;
                                }
                            }
                            break;
                        case 11: // Debug
                            boolean bRepeat=true;
                            while (bRepeat)  {
                                frmDebug dbg = new frmDebug(this);
                                dbg.showModal();
                                bRepeat = dbg.modalResult()==ModalForm.MR_CANCEL;
                                dbg = null;
                            }
                            break;
                    }
                }
            } else res=0;
        } while (res!=10);

        persData.bSaveUserData(PersistantData.SAVE_PROFILE|PersistantData.SAVE_FRIENDS|PersistantData.SAVE_BLOCKS);
        persData.closeStorage();
        bluetoothAccess.saveMessagesToDisk();
    }

    // Main application
    public void main() {
        long iStart = System.currentTimeMillis();
        SystemStartup();


        iStart=System.currentTimeMillis()-iStart;
        // This time was: 441 on SE K800i
        //                1500 on Nk80
        //                3016 on Nk 6680


        // Start up
        persData.bLoadUserData();

        if (persData.bLastLicenceShown==0) {
            persData.bLiteMode = iStart>2000;
            if (persData.bLiteMode) prepareLite(null);
        }
        
        clsStartupWizard startup = new clsStartupWizard(this);
        if (!baseconf.ISDEBUG)
            if (!startup.runWizard()) {
                MainApp.notifyDestroyed();
                return;
            }
        startup = null;
        imgSplashLogo = splash.logo;
        splash = null;

        int i = 0;
        //#if JSR179
        String s2 = System.getProperty("microedition.location.version");
        if (s2!=null)
            if (s2.length()>0) i = Integer.parseInt(s2.substring(0,1));
        //#endif

        // Now start the bluetooth and wireless sub-systems
        bluetoothAccess = new BTManager(this,i>0);
        
        if (baseconf.ISDEBUG) persData.myProfile.strNickName = bluetoothAccess.getBluetoothAddress();
        
        runFrontScreen();
        bluetoothAccess.terminateBluetooth();
        bluetoothAccess = null;
        MainApp.notifyDestroyed();
    }
        
    // called when the app wants to close
    public void close() {
        try {
            if (activeForm!=null) activeForm.setModalResult(ModalForm.MR_QUIT);
        } catch (Exception e) {}
    }
    

    // Update the setting of a transition
    private void refreshTransitionSetting(Form frm) {
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();

        lf.setDefaultDialogTransitionIn(getTransition(true));
        lf.setDefaultDialogTransitionOut(getTransition(false));
        lf.setDefaultFormTransitionIn(getFormTransition(true));
        //lf.setDefaultSmoothScrolling(persData.bUseTransitions);

        try {
            Resources resources = Resources.open("/Gfx/loadonce.res");
            Hashtable th = resources.getTheme("main");

            /*if (!persData.bUseSmallFont) {
                Hashtable th2 = resources.getTheme("mainBg");
                Enumeration keylist = th2.keys();
                String s;
                while (keylist.hasMoreElements()) {
                    s = (String)keylist.nextElement();
                    th.put(s,th2.get(s));
                }
                headingFont = resources.getFont("MobudsBig Bold");
            } else*/ headingFont = resources.getFont("Mobuds Bold");
            UIManager.getInstance().setThemeProps(th);
            UIManager.getInstance().setLookAndFeel(lf);

        } catch (Exception e) {}
        if (persData.bLiteMode) prepareLite(frm); else prepareFull(frm);
    }

    // Prepare lite graphics
    private void prepareLite(Form frm) {
            Image imm;
            Style st;
            UIManager ui = UIManager.getInstance();

            st = ui.getComponentStyle(strForm);             st.setBgImage(null,true); st.setBgColor(0x000000,true);        ui.setComponentStyle(strForm, st);
            st = ui.getComponentStyle(strDialogBody);       st.setBgImage(null,true);  st.setBgColor(0x000000,true);        ui.setComponentStyle(strDialogBody, st);
            st = ui.getComponentStyle(strDialog);           st.setBgImage(null,true);  st.setBgColor(0x000000,true);        ui.setComponentStyle(strDialog, st);
            st = ui.getComponentStyle(strMenu);             st.setBgImage(null,true);  st.setBgColor(0x000000,true);        ui.setComponentStyle(strMenu, st);
            st = ui.getComponentStyle(strButton);           st.setBgImage(null,true);  st.setBgColor(0x8FB7C0,true);        ui.setComponentStyle(strButton, st);
            st = ui.getComponentStyle(strSoftButton);       st.setBgImage(null,true);  st.setBgColor(0x8FB7C0,true);        ui.setComponentStyle(strSoftButton, st);
            st = ui.getComponentStyle(strTitle);            st.setBgImage(null,true);  st.setBgColor(0xB1E6F0,true);        ui.setComponentStyle(strTitle, st);
            st = ui.getComponentStyle(strDialogTitle);      st.setBgImage(null,true);  st.setBgColor(0xB1E6F0,true);        ui.setComponentStyle(strDialogTitle, st);

            if (frm!=null) frm.refreshTheme();

            horizSeperatorLine = null;
    }


    // Prepare full graphics
    private void prepareFull(Form frm) {
            Image imm;
            Style st;
            UIManager ui = UIManager.getInstance();
            Graphics gpx;

            imm = Image.createImage(1,23); gpx = imm.getGraphics();
            gpx.setColor(0); gpx.fillRect(0,0,imm.getWidth()+1,imm.getHeight()+1);
            gpx.fillLinearGradient(0xA3CAD2,0x789DA4,0,1,imm.getWidth(),imm.getHeight(),false);
            st = ui.getComponentStyle(strButton);            st.setBgImage(imm,true);        ui.setComponentStyle(strButton, st);
            st = ui.getComponentStyle(strSoftButton);        st.setBgImage(imm,true);        ui.setComponentStyle(strSoftButton, st);

            imm = Image.createImage(1,23); gpx = imm.getGraphics();
            gpx.setColor(0xFFFFFF); gpx.fillRect(0,0,imm.getWidth()+1,imm.getHeight()+1);
            gpx.fillLinearGradient(0xB6E8F2,0xA5DEEA,0,0,imm.getWidth(),imm.getHeight(),false);
            st = ui.getComponentStyle(strTitle);            st.setBgImage(imm,true);        ui.setComponentStyle(strTitle, st);
            st = ui.getComponentStyle(strDialogTitle);        st.setBgImage(imm,true);      ui.setComponentStyle(strDialogTitle, st);

            horizSeperatorLine = Image.createImage(1,24); gpx = horizSeperatorLine.getGraphics();
            gpx.fillLinearGradient(0xFE00C2,0x7A0368,0,0,imm.getWidth()+1,imm.getHeight()+1,false);

            if (frm!=null) frm.refreshTheme();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Private Methods ///////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void SystemStartup() {
        errors = new String[5];
        if (baseconf.ISDEBUG) debug = new String[5];
        for (int a=0; a<errors.length; a++) errors[a]="";
    
        try {
            //persData.bUseSmallFont = false;
            Resources resources = Resources.open("/Gfx/loadonce.res");
            UIManager.getInstance().setThemeProps(resources.getTheme("main"));            
            prepareFull(null);
            refreshTransitionSetting(null);

            splash = new frmSplash(this);
            splash.show();

            imgTick = resources.getImage("tick");
            imgCross = resources.getImage("cross");
            imgAction = resources.getImage("action");            
            miniLogo = resources.getImage("mini");
            imgMale = resources.getImage("male");
            imgFemale = resources.getImage("female");
            imgUnknown = resources.getImage("unknown");
            imgStarSigns = resources.getImage("star");
            imgFriend = resources.getImage("friend");
            imgBlocked = resources.getImage("block");
            imgNotListed = resources.getImage("neutral");
            imgNewMail = resources.getImage("mail");
            imgOpenMail = resources.getImage("mailopen");
            imgReplyMail = resources.getImage("mailreply");
            imgView = resources.getImage("glass");
            imgCancel = resources.getImage("cancel");
            imgDot = resources.getImage("dot");
            imgUnDot = resources.getImage("undot");
            imgDotOpt = resources.getImage("dotopt");
            imgDeleteMessage = resources.getImage("delmail");
            imgMatch = resources.getImage("match");

            resources = null;
        } catch (Exception e) {}

    }
}
