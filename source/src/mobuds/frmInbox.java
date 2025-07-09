/*
 * Inbox browsing for alerts and messages
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
public class frmInbox extends ModalForm {

    private List items;
    private int inboxMode = 1;
    private String[] titles = {"All Messages","MoMail","Alerts","Promotions","Local News","Venue News"};

    class renderData {
        String name;
        String someMessage;
        int messageStatus;   // 0=New, 1=Read, 2=Replied
        int iconStatus;      //
    }
    
    public void refreshDisplay() {
        // Remove previous
        this.removeAll();
        items = new List();
        items.getStyle().setBgTransparency(0);
        items.setListCellRenderer(new RowRenderer());
        items.setOrientation(List.VERTICAL);

        this.addComponent(BorderLayout.CENTER, items);

        Vector list = moSystem.bluetoothAccess.getInboxCopy();
        Enumeration en = list.elements();
        TransmittedMessage tm;
        boolean addit;

        setTitle(titles[inboxMode]);

        for (int a=list.size()-1; a>=0; a--) 
            try {
                tm = (TransmittedMessage)list.elementAt(a);

                switch (inboxMode) {
                    default: addit=true; break;
                    case 1:addit=((tm.bMessageType==baseconf.COMMUNICATION_SENDMESSAGE)||(tm.bMessageType==baseconf.COMMUNICATION_RXFAIL)||(tm.bMessageType==baseconf.COMMUNICATION_RXOK));break;
                    case 2:addit=(tm.bMessageType==baseconf.COMMUNICATION_LOCALERT);
                    case 3:addit=(tm.bMessageType==baseconf.COMMUNICATION_LOCPROMO);
                    case 4:addit=(tm.bMessageType==baseconf.COMMUNICATION_LOCGENNEWS);
                    case 5:addit=(tm.bMessageType==baseconf.COMMUNICATION_LOCVENNEWS);
                }
                if (addit) items.addItem(tm);
            } catch (Exception e) {}
     }


    // Action!
    private boolean runActionPopup() {
        int selected = items.getSelectedIndex();
        if ((selected<0)||(selected>=items.size())) {
            runOptions();
            return true;
        }
        int selectedi = selected;

        PersonMessage tm = (PersonMessage)items.getModel().getItemAt(selected);
        if (tm==null) return false;
        MobudsProfile mb = new MobudsProfile();
        boolean mbProfile = mb.loadFromString(tm.sSourceProfile);

        dlgMailActions dlg = new dlgMailActions(moSystem,tm.sFromBluetooth,mbProfile,true);
        selected = dlg.showOptions();
        dlg = null;


        switch (selected) {
            case dlgMailActions.RESULT_CHANGEVIEW:
                runOptions();
                return false;
                
            case dlgMailActions.RESULT_VIEWMESSAGE:
                showSubModal(new frmViewMessage(moSystem,tm));
                break;

            case dlgMailActions.RESULT_DELETEMESSAGE:
                moSystem.bluetoothAccess.removeMessage(tm);
                items.getModel().removeItem(selectedi);
                moSystem.bluetoothAccess.refreshMailStatistics();
                break;


            case dlgMailActions.RESULT_ADDREMOVEFRIEND:
                if (mbProfile)
                    if (moSystem.persData.isFriend(tm.sFromBluetooth)) moSystem.persData.delUserFromFriends(tm.sFromBluetooth);
                                                    else if (!moSystem.persData.addUserToFriends(tm.sFromBluetooth, mb.saveToString(false))) showAddError(true);
                items.repaint();
                break;
            case dlgMailActions.RESULT_ADDREMOVEBLOCK:
                if (mbProfile)
                    if (moSystem.persData.isBlocked(tm.sFromBluetooth)) moSystem.persData.delUserFromBlocks(tm.sFromBluetooth);
                                                    else if (!moSystem.persData.addUserToBlockList(tm.sFromBluetooth,mb.saveToString(false))) showAddError(false);
                items.repaint();
                break;
        }
        return true;
    }

    // Options button pressed
    private void runOptions() {
        // switch whats viewed depending on what is selected
        dlgOptions opt = new dlgOptions(moSystem,"View");
        opt.addOption("All");
        opt.addOption("Inbox");
        opt.addOption("Alerts");
        opt.addOption("Promotions");
        opt.addOption("Local News");
        opt.addOption("Venue News");
        opt.setSelection(inboxMode);
        inboxMode = opt.showOptions();
        refreshDisplay();
        repaint();
    }

    // A scrollable list base class
    public frmInbox(MobudsSystem moSystem, int defaultInboxMode) {
        super(moSystem," ");

        inboxMode = defaultInboxMode;
        addModalCommand("Back",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Action",moSystem.imgAction,MR_OK,CMD_DEFAULT);

        this.setLayout(new BorderLayout());
        this.setScrollable(false);

        refreshDisplay();
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

        // Class to render it
    private class RowRenderer extends Container implements ListCellRenderer {
        private Label name = new Label("");;
        private Label line2 = new Label("");
        private Label focus = new Label("");

        public RowRenderer() {
            setLayout(new BorderLayout());
            Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));

            Style st = name.getStyle();
            st.setBgTransparency(0);
            st.setPadding(0,4,2,2);
            st.setMargin(0,2,4,2);
            st.setFont(moSystem.headingFont);
            name.setStyle(st);

            st = line2.getStyle();
            st.setBgTransparency(0);
            st.setPadding(4,0,2,2);
            st.setMargin(2,1,4,2);
            line2.setStyle(st);

            cnt.addComponent(line2);
            cnt.addComponent(name);
            focus.getStyle().setBgTransparency(255);
            focus.getStyle().setBgColor(0x000000);
            focus.getStyle().setBorder(Border.createLineBorder(2,0xFF60FF));

            addComponent(BorderLayout.CENTER, cnt);
        }

        // Render
        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
            renderData data;
            if ((index>=0)&&(index<list.size())) {
                PersonMessage pm = (PersonMessage)value;
                MobudsProfile mb = new MobudsProfile();
                clsMohubServer mh;

                data = new renderData();

                if (mb.loadFromString(pm.sSourceProfile)) {
                    data.name = mb.strNickName;
                    if (moSystem.persData.isFriend(pm.sFromBluetooth)) data.iconStatus=1; else
                    if (moSystem.persData.isBlocked(pm.sFromBluetooth)) data.iconStatus=2; else data.iconStatus=0;
                } else {
                   mh = new clsMohubServer();
                   if (mh.updateProfile("",pm.sFromBluetooth,pm.sSourceProfile)) 
                        data.name = mh.strMohubName; else data.name="";
                   data.iconStatus=0;
                }
                data.someMessage = pm.sMessage;
                if (pm.hasReplied)  data.messageStatus = 2; else
                if (pm.hasBeenRead) data.messageStatus = 1; else
                                    data.messageStatus = 0;
            } else data = null;

            if (data==null) {
                name.setText("");
                name.setIcon(null);
                line2.setText("");
                line2.setIcon(null);
            } else {
                name.setText("  "+data.name);
                line2.setText("  "+data.someMessage);
                switch (data.messageStatus) {
                    case 2:line2.setIcon(moSystem.imgReplyMail); break;
                    case 1:line2.setIcon(moSystem.imgOpenMail); break;
                    default: line2.setIcon(moSystem.imgNewMail); break;
                }
                switch (data.iconStatus) {
                    default: name.setIcon(moSystem.imgNotListed); break;
                    case 1: name.setIcon(moSystem.imgFriend); break;
                    case 2: name.setIcon(moSystem.imgBlocked); break;
                }
            }
            name.getStyle().setFgColor(isSelected?0xFF60FF:0xC0C0C0);
 
            return this;
        }

        // Get focus
        public Component getListFocusComponent(List list) {
            return focus;
        }
    }
}
