/*
 * frmScrollPeopleList.java
 *
 * Created on 14 January 2009, 20:45
 *
 * This is a base class for a list that scrolls and contains a list of people
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.list.ListCellRenderer;


    
/**
 *
 * @author rob smith
 */
public class frmScrollPeopleList extends ModalForm   {
    
    public static final int BROWSEPEOPLE_VISIBLENOW      = 1;
    public static final int BROWSEPEOPLE_VISIBLEFRIENDS  = 2;
    public static final int BROWSEPEOPLE_VISIBLEBLOCKS   = 3;
    public static final int BROWSEPEOPLE_VISIBLEMATCHES  = 4;
    public static final int BROWSEPEOPLE_VISIBLEMATCHES1 = 5;
    public static final int BROWSEPEOPLE_VISIBLEMATCHES2 = 6;
    public static final int BROWSEPEOPLE_VISIBLEMATCHES3 = 7;
    public static final int BROWSEPEOPLE_ALLFRIENDS      = 8;
    public static final int BROWSEPEOPLE_ALLBLOCKS       = 9;      
    private dlgActions dlg;

    private static final int DELAYTIME = 30000;   // delat between refresh checks

    private List items;
    private int browseMode;
    private int browseModeIndex;
    private boolean fOfflineMode;
    
    // for passing data about what is shown
    private class renderData {
        public String strName;
        public byte gender;
        public byte starSign;
        public byte friendStatus;
    }
    // Inner class for refreshing
    private class clsRefreshCheck extends java.util.TimerTask {
        private java.util.Timer update;

        // startup
        public clsRefreshCheck() {
             update = new java.util.Timer();
             update.schedule(this,DELAYTIME,DELAYTIME);
        }

        // for managing repainting
        public void run() {
            try {
                if (moSystem.bluetoothAccess.hasUserlistChanged()) {
                    setModalResult(ModalForm.MR_QUIT);
                }
            } catch (Exception foo) {}
        }
    }

    private clsRefreshCheck refreshMonitor = null;

    
    // This should be overridden by the child class
    protected renderData getCellRenderData(List list, int index, Object value) {
        if (value==null) return null;
        renderData rd = new renderData();
        MobudsProfile mp = new MobudsProfile();
        String strBlue;

        if ((browseMode==BROWSEPEOPLE_ALLFRIENDS)||(browseMode==BROWSEPEOPLE_ALLBLOCKS)) {
            StoredUser su = (StoredUser)value;
            strBlue = su.sBluetoothAddress;
            mp.loadFromString(su.sProfile);
        } else {
            DiscoveredUsers du = (DiscoveredUsers)value;
            strBlue = du.bluetoothAddress;
            mp.loadFromString(du.userProfile);
        }

        if (moSystem.persData.isFriend(strBlue)) rd.friendStatus=1; else
        if (moSystem.persData.isBlocked(strBlue)) rd.friendStatus=2; else rd.friendStatus=0;
        rd.strName = mp.strNickName;
        
        // Show gender, but based on if its even public
        if ((mp.lVisibility&MobudsProfile.VISIBLE_GENDER)==0) rd.gender = 0; else
            rd.gender = (byte)(mp.bGender==MobudsProfile.GENDER_MALE?1:2);
        
        // and a final bit of info
        rd.starSign = mp.bStarsign;  // starsign is always available
                    
        return rd;
    }    
 
    // Populates the list with what you requested
    public void populateWith(int populateType) {
        browseMode = populateType;
        Vector tmp;
        Enumeration eNum;
        int a;
        this.removeAll();
        items = new List();
        items.getStyle().setBgTransparency(0);
        items.setListCellRenderer(new RowRenderer());
        items.setOrientation(List.VERTICAL);

        this.addComponent(BorderLayout.CENTER, items);

        switch (populateType) {
            case BROWSEPEOPLE_VISIBLENOW:
                         if (refreshMonitor==null) refreshMonitor=new clsRefreshCheck();
                         tmp = moSystem.bluetoothAccess.getUserlistCopy();
                         for (a=0; a<tmp.size(); a++) addItem(tmp.elementAt(a));
/*
                         
                         
                         
                         DiscoveredUsers du = new DiscoveredUsers();
                         du.bluetoothAddress = "0123456789"; du.distance=1;du.iProfileVersion=1;du.txtPersonName="moo Name"; du.userProfile=moSystem.persData.myProfile.saveToString(false);
                                 addItem(du);
                         du = new DiscoveredUsers();
                         du.bluetoothAddress = "10123456789"; du.distance=1;du.iProfileVersion=1;du.txtPersonName="mssssssoo Name"; du.userProfile=moSystem.persData.myProfile.saveToString(false);
                                 addItem(du);
                         
  */                               
                                 
                                 
                         tmp = null;
                         browseModeIndex=0;
                         setTitle("Who's Around");
                         break;
            case BROWSEPEOPLE_VISIBLEFRIENDS:
                         tmp = moSystem.bluetoothAccess.getUserlistCopy();
                         for (a=0; a<tmp.size(); a++) 
                               if (moSystem.persData.isFriend(((DiscoveredUsers)tmp.elementAt(a)).bluetoothAddress)) addItem(tmp.elementAt(a));
                         tmp = null;
                         browseModeIndex=1;
                         setTitle("Nearby "+baseconf.strFriends);
                         break;
            case BROWSEPEOPLE_VISIBLEBLOCKS:
                         tmp = moSystem.bluetoothAccess.getUserlistCopy();
                         for (a=0; a<tmp.size(); a++) 
                               if (moSystem.persData.isBlocked(((DiscoveredUsers)tmp.elementAt(a)).bluetoothAddress)) addItem(tmp.elementAt(a));
                         tmp = null;
                         browseModeIndex=2;
                         setTitle("Nearby "+baseconf.strBlocks);
                         break;
            case BROWSEPEOPLE_VISIBLEMATCHES:
                         setTitle("All Matches");
                         browseModeIndex=3;
            case BROWSEPEOPLE_VISIBLEMATCHES1:
            case BROWSEPEOPLE_VISIBLEMATCHES2:
            case BROWSEPEOPLE_VISIBLEMATCHES3:
                        AlertProfile ap[] = new AlertProfile[3];
                        boolean match;
                        for (int i=0; i<3; i++) 
                            if (moSystem.persData.bEnableAlert[i]) {
                                ap[i] = new AlertProfile(i+1);
                                ap[i].fromString(moSystem.persData.sAlertProfile[i]);
                            } else ap[i] = null;
                        
                        // disable the other matches here if not allowed
                        if (populateType==BROWSEPEOPLE_VISIBLEMATCHES1) {setTitle((ap[0]==null)?"Matches":ap[0].sAlertName+" matches"); browseModeIndex=4; ap[1]=null; ap[2]=null;};
                        if (populateType==BROWSEPEOPLE_VISIBLEMATCHES2) {setTitle((ap[1]==null)?"Matches":ap[0].sAlertName+" matches"); browseModeIndex=5; ap[0]=null; ap[2]=null;};
                        if (populateType==BROWSEPEOPLE_VISIBLEMATCHES3) {setTitle((ap[2]==null)?"Matches":ap[0].sAlertName+" matches"); browseModeIndex=6; ap[0]=null; ap[1]=null;};
                       
                        // are any matches enabled?
                        if ((ap[0]!=null)||(ap[1]!=null)||(ap[2]!=null)) {
                            tmp = moSystem.bluetoothAccess.getUserlistCopy();
                            MobudsProfile mb = new MobudsProfile();
                            for (a=0; a<tmp.size(); a++) {                                
                               if (mb.loadFromString(((DiscoveredUsers)tmp.elementAt(a)).userProfile)) {
                                   match = false;
                                   if ((ap[0]!=null)&&(!match)) if (mb.matchesAlertProfile(ap[0])) match=true;
                                   if ((ap[1]!=null)&&(!match)) if (mb.matchesAlertProfile(ap[1])) match=true;
                                   if ((ap[2]!=null)&&(!match)) if (mb.matchesAlertProfile(ap[2])) match=true;
                                   if (match) addItem(tmp.elementAt(a));
                               }
                            }
                            tmp = null;
                        }
                        break;
            case BROWSEPEOPLE_ALLFRIENDS:
                        setTitle(baseconf.strFriends);
                        eNum = moSystem.persData.myFriends.elements();
                        while (eNum.hasMoreElements()) addItem((StoredUser)eNum.nextElement());
                        break;
            case BROWSEPEOPLE_ALLBLOCKS:
                        setTitle(baseconf.strBlocks);
                        eNum = moSystem.persData.myBlocks.elements();
                        while (eNum.hasMoreElements()) addItem((StoredUser)eNum.nextElement());
                        break;
        }
    }


    // Options button pressed
    private void runOptions() {
        // switch whats viewed depending on what is selected
        dlgOptions opt = new dlgOptions(moSystem,"View");
        opt.addOption("Who's Around");
        opt.addOption("Nearby "+baseconf.strFriends);
        opt.addOption("Nearby "+baseconf.strBlocks);
        if (moSystem.persData.bEnableAlert[0]|moSystem.persData.bEnableAlert[1]|moSystem.persData.bEnableAlert[2]) {
            opt.addOption("All Matches");
            AlertProfile at = new AlertProfile(0);
            if (moSystem.persData.bEnableAlert[0]) {at.sAlertName="Alert 1";at.fromString(moSystem.persData.sAlertProfile[0]);opt.addOption(at.sAlertName+" matches");}
            if (moSystem.persData.bEnableAlert[1]) {at.sAlertName="Alert 2";at.fromString(moSystem.persData.sAlertProfile[1]);opt.addOption(at.sAlertName+" matches");}
            if (moSystem.persData.bEnableAlert[2]) {at.sAlertName="Alert 3";at.fromString(moSystem.persData.sAlertProfile[2]);opt.addOption(at.sAlertName+" matches");}
        }
        opt.setSelection(browseModeIndex);
        browseModeIndex = opt.showOptions();
        if (browseModeIndex==0) populateWith(BROWSEPEOPLE_VISIBLENOW); else
        if (browseModeIndex==1) populateWith(BROWSEPEOPLE_VISIBLEFRIENDS); else
        if (browseModeIndex==2) populateWith(BROWSEPEOPLE_VISIBLEBLOCKS); else
        if (browseModeIndex==3) populateWith(BROWSEPEOPLE_VISIBLEMATCHES); else {
            int tmp = 4;
            if ((browseModeIndex==tmp)&&(moSystem.persData.bEnableAlert[0])) populateWith(BROWSEPEOPLE_VISIBLEMATCHES1); else if (moSystem.persData.bEnableAlert[0]) tmp++;
            if ((browseModeIndex==tmp)&&(moSystem.persData.bEnableAlert[1])) populateWith(BROWSEPEOPLE_VISIBLEMATCHES2); else if (moSystem.persData.bEnableAlert[1]) tmp++;
            if ((browseModeIndex==tmp)&&(moSystem.persData.bEnableAlert[2])) populateWith(BROWSEPEOPLE_VISIBLEMATCHES3);
        }
        repaint();
    }

    
    // Adds an item to the list
    public void addItem(Object value) {
        items.getModel().addItem(value);
    }
    
    // Remove an item at a position
    public void deleteItem(int index) {
        items.getModel().removeItem(index);
    }        

    // Class to render it
    private class RowRenderer extends Container implements ListCellRenderer {
        private Label picture = new Label("");
        private Label name = new Label("");;
        private Label line2 = new Label("");
        private Label focus = new Label("");
        
        public RowRenderer() {
            setLayout(new BorderLayout());
            addComponent(BorderLayout.WEST, picture);
            Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));
            Style st = name.getStyle();
            st.setBgTransparency(0);
            st.setPadding(2,1,3,3);
            st.setMargin(2,0,2,2);
            st.setFont(moSystem.headingFont);
            name.setStyle(st);
            st = picture.getStyle();
            st.setBgTransparency(0);
            st.setPadding(3,3,3,0);
            st.setMargin(2,2,2,2);
            picture.setStyle(st);
            st = line2.getStyle();
            st.setBgTransparency(0);
            st.setPadding(3,3,3,3);
            st.setMargin(0,1,2,2);
            line2.setStyle(st);
            cnt.addComponent(name);
            cnt.addComponent(line2);
            focus.getStyle().setBgTransparency(255);
            focus.getStyle().setBgColor(0x000000);
            focus.getStyle().setBorder(Border.createLineBorder(2,0xFF60FF));
            
            addComponent(BorderLayout.CENTER, cnt);
        }

        // Render
        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
            renderData data;
            if ((index>=0)&&(index<list.size())) data = getCellRenderData(list,index,value); else data = null;
            if (data==null) {
                name.setText("");
                name.setIcon(null);
                line2.setText("");
                line2.setIcon(null);
                picture.setIcon(moSystem.imgUnknown);
            } else {
                String s;
                name.setText("  "+data.strName);
                switch (data.gender) {
                    case 1:picture.setIcon(moSystem.imgMale); s=", Male"; break;
                    case 2:picture.setIcon(moSystem.imgFemale); s=", Female"; break;
                    default: s=""; picture.setIcon(moSystem.imgUnknown); break;
                }
                switch (data.friendStatus) {
                    default: name.setIcon(moSystem.imgNotListed); break;
                    case 1: name.setIcon(moSystem.imgFriend); s+=", "+baseconf.strFriend; break;
                    case 2: name.setIcon(moSystem.imgBlocked); s+=", "+baseconf.strBlock; break;
                }
                
                line2.setText(" "+baseconf.starSigns[data.starSign]+s);
                int xp = data.starSign*18;
                if (moSystem.imgStarSigns!=null) 
                       if (moSystem.imgStarSigns.getWidth()>18) line2.setIcon( moSystem.imgStarSigns.subImage(xp,0,18,18,false));
            }         
            name.getStyle().setFgColor(isSelected?0xFF60FF:0xC0C0C0);            
            return this;
        }

        // Get focus
        public Component getListFocusComponent(List list) {
            return focus;
        }        
    }

    // action popup signalled
    public boolean runActionPopup() {
        int selected = items.getSelectedIndex();
        if ((selected<0)||(selected>=items.size())) {
            runOptions();
            return true;
        }

        String strBlue, strProfile;
        
        if ((browseMode==BROWSEPEOPLE_ALLFRIENDS)||(browseMode==BROWSEPEOPLE_ALLBLOCKS)) {
            StoredUser su = (StoredUser)items.getModel().getItemAt(selected);
            if (su==null) return false;

            strBlue = su.sBluetoothAddress;
            strProfile = su.sProfile;
        } else {
            DiscoveredUsers du = (DiscoveredUsers)items.getModel().getItemAt(selected);
            if (du==null) return false;

            strBlue = du.bluetoothAddress;
            strProfile = du.userProfile;
        }

        dlg = new dlgActions(moSystem,strBlue,true,false,!fOfflineMode);
        int res = dlg.showOptions();
        dlg = null;

        MobudsProfile mb;

        // see the results
        switch (res) {
            case dlgActions.RESULT_CHANGEVIEW:
                return false;                

            case dlgActions.RESULT_VIEWPROFILE:
                mb = new MobudsProfile();
                mb.loadFromString(strProfile);
                showSubModal(new frmViewProfile(moSystem,"View Profile",strBlue,mb));
                break;

            case dlgActions.RESULT_SENDMESSAGE:
                mb = new MobudsProfile();
                mb.loadFromString(strProfile);
                showSubModal(new frmSendMessage(moSystem,strBlue,mb.strNickName,""));
                break;

            case dlgActions.RESULT_ADDREMOVEFRIEND:
                if (moSystem.persData.isFriend(strBlue)) moSystem.persData.delUserFromFriends(strBlue);
                                                    else if (!moSystem.persData.addUserToFriends(strBlue, strProfile)) showAddError(true);
                items.repaint();
                break;

            case dlgActions.RESULT_ADDREMOVEBLOCK:
                if (moSystem.persData.isBlocked(strBlue)) moSystem.persData.delUserFromBlocks(strBlue);
                                                    else if (!moSystem.persData.addUserToBlockList(strBlue, strProfile)) showAddError(false);
                items.repaint();
                break;
        }
        return true;
    }
    
    
    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) {
            if (!runActionPopup()) runOptions();
            return false;
        }
        return true;
    }    
    //

    // A scrollable list base class
    public frmScrollPeopleList(MobudsSystem moSystem, boolean isOffline) {
        super(moSystem," ");
        fOfflineMode = isOffline;
        addModalCommand("Back",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Action",moSystem.imgAction,MR_OK,CMD_DEFAULT);
        
        this.setLayout(new BorderLayout());
        this.setScrollable(false);
    }
}