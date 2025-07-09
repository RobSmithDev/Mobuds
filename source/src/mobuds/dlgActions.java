/*
 * Popup dialog for actions with people
 */

package mobuds;


import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 *
 * @author rob smith
 */
public class dlgActions extends Dialog implements ActionListener {
    private MobudsSystem moSystem;

    public static final int RESULT_VIEWPROFILE        = 0;
    public static final int RESULT_SENDMESSAGE        = 1;
    public static final int RESULT_ADDREMOVEFRIEND    = 2;
    public static final int RESULT_ADDREMOVEBLOCK     = 3;
    public static final int RESULT_CANCEL             = 4;
    public static final int RESULT_CHANGEVIEW         = 5;
    private int selectedCommand;
    private int buttonCount;

    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;
    private Button b5;
    private Button b6;

    // Add an option
    private Button addOption(String txt, Image icon) {
        Button bz = new Button(" "+txt,icon);
        Style st = bz.getStyle();
        st.setMargin((buttonCount==0)?6:2,0,5,5);
        st.setPadding(5,5,5,5);
        Style st2 = bz.getSelectedStyle();
        st2.setMargin((buttonCount==0)?6:2,0,5,5);
        st2.setPadding(5,5,5,5);
        st2.setFgColor(0xFFFFFF);
        st2.setBorder(st.getBorder());
        st2.setBgImage(st.getBgImage());
        bz.setPressedStyle(st2);
        bz.setSelectedStyle(st2);
        bz.addActionListener(this);
        addComponent(bz);
        buttonCount++;
        return bz;
    }


    // Pass in the bluetooth address of this user
    public dlgActions(MobudsSystem moSys, String strBlue, boolean allowViewProfile, boolean replyMode, boolean addChangeView) {
        super("Which Action?");
        moSystem=moSys;
        buttonCount=0;

        setDialogType(Dialog.TYPE_CONFIRMATION);
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        setScrollable(true);
        boolean blocked = moSystem.persData.isBlocked(strBlue);

        if (allowViewProfile) b1 = addOption("View Profile",moSystem.imgView);
        if (!blocked) b2 = addOption(replyMode?"Reply":"Send Message",replyMode?moSystem.imgReplyMail:moSystem.imgNewMail);
        b3 = addOption((moSystem.persData.isFriend(strBlue)?"Remove from ":"Add to ")+baseconf.strFriends,moSystem.imgFriend);
        b4 = addOption((blocked?"Remove from ":"Add to ")+baseconf.strBlocks,moSystem.imgBlocked);
        if (addChangeView) b6 = addOption("Change View",moSystem.imgDot);
        b5 = addOption("Cancel",moSystem.imgCancel); 
        setTransitionInAnimator(moSystem.getTransition(true));
        setTransitionOutAnimator(moSystem.getTransition(false));
    }
    
    // Show the dialog
    public int showOptions() {
        selectedCommand = 4;
        showPacked(BorderLayout.CENTER,true);
        return selectedCommand;
    }

    // Respond to buttons
    public void actionPerformed(ActionEvent evt) {
        Button bt = (Button)evt.getSource();

        if (bt==b1) selectedCommand=0; else
        if (bt==b2) selectedCommand=1; else
        if (bt==b3) selectedCommand=2; else
        if (bt==b4) selectedCommand=3; else
        if (bt==b5) selectedCommand=4; else
        if (bt==b6) selectedCommand=5;

        dispose();
    }
}
