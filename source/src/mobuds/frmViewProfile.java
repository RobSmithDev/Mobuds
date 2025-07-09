/*
 * Form to display a persons profile - finally
 */

package mobuds;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;


/**
 * @author rob smith
 */
public class frmViewProfile extends ModalForm implements ActionListener {

    private MobudsProfile profile;
    private String strBlue;
    private dlgActions dlg;
    private int lwidth;
    private Button btPhone;
    private Button btURL;
    private Label status;

    // adds my stuff
    private void addMyPair(String title, String field) {
        Label lbl = new Label(field);
        lbl.setFocusable(field.length()>0);
        addPair(title,lbl,lwidth);
    }

    // Respond to two buttons
    public void actionPerformed(ActionEvent evt) {
        Button bt = (Button)evt.getSource();
        evt.consume();
        try {
            if (bt==btPhone) {
                moSystem.MainApp.platformRequest("tel:"+profile.strMobileNumber);
            } else {
                moSystem.MainApp.platformRequest(profile.strURL);
            }
        } catch (Exception e) {};
    }

    // Refresh status
    private void refreshFriendshipStatus() {
        if (moSystem.persData.isFriend(strBlue)) {status.setText(" "+baseconf.strFriend); status.setIcon(moSystem.imgFriend);} else
        if (moSystem.persData.isBlocked(strBlue)) {status.setText(" "+baseconf.strBlock); status.setIcon(moSystem.imgBlocked);} else {
            status.setIcon(moSystem.imgNotListed);
        }
    }

    // Display the mobuds profile
    public frmViewProfile(MobudsSystem moSys, String title, String strBluetooth, MobudsProfile mp) {
        super(moSys,title);
        Image imgGender;
        Label lbl;
        int tmp;
        String st,st2;
        Button bt;
        lwidth = /*moSys.persData.bUseSmallFont?70:*/98;

        strBlue = strBluetooth;
        profile = mp;

        setReducedHeightMode();

        // Now add the controls
        startNewSection("Details");
        lbl = new Label(mp.strNickName); lbl.setFocusable(true);
        addPair("Name:",lbl,lwidth);

        status = new Label(); status.setFocusable(true);
        refreshFriendshipStatus();
        addPair("Situation:",status,lwidth);
        status = new Label(); status.setFocusable(true);

        int i = moSystem.bluetoothAccess.onlineDistance(strBluetooth);
        switch (i) {
            case -1: lbl = new Label("Out of range"); break;
            case 0: lbl = new Label("less than 10 meters"); break;
            default: lbl = new Label((i*10)+"-"+((i+1)*10)+" meters");
        }
        lbl.setFocusable(true);
        addPair("Distance:",lbl,lwidth);

        if (((mp.lVisibility&MobudsProfile.VISIBLE_NUMBER)!=0)&&(mp.strMobileNumber.length()>0)) {
            btPhone = new Button(mp.strMobileNumber);
            btPhone.setStyle(lbl.getStyle());
            btPhone.addActionListener(this);
            addPair("Phone No:",btPhone,lwidth);
        }

        if (((mp.lVisibility&MobudsProfile.VISIBLE_URL)!=0)&&(mp.strURL.length()>0)) {
            btURL = new Button(mp.strURL);
            btURL.setStyle(lbl.getStyle());
            btURL.addActionListener(this);
            addPair("Homepage:",btURL,lwidth);
        }
        
        startNewSection("Who Are They");
        if ((mp.lVisibility&MobudsProfile.VISIBLE_GENDER)!=0) {
            imgGender = (mp.bGender==MobudsProfile.GENDER_MALE?moSystem.imgMale:moSystem.imgFemale);
            imgGender = imgGender.scaled(16,16);  // make a smaller copy of it
            lbl = new Label(" "+((mp.bGender==MobudsProfile.GENDER_MALE)?baseconf.strGenders[0]:baseconf.strGenders[1]));
            lbl.setFocusable(true); lbl.setIcon(imgGender);
            addPair("Gender:",lbl,lwidth);
        }

        // Star Sign
        tmp = mp.bStarsign*18;
        imgGender=null;
        if (moSystem.imgStarSigns!=null) 
                if (moSystem.imgStarSigns.getWidth()>18) imgGender = moSystem.imgStarSigns.subImage(tmp,0,18,18,false); 
        lbl = new Label(" "+baseconf.starSigns[mp.bStarsign]);
        lbl.setIcon(imgGender); lbl.setFocusable(true); 
        addPair("Star Sign:",lbl,lwidth);

        // date of Birth
        if ((mp.lVisibility&MobudsProfile.VISIBLE_DOB)!=0) {
            addMyPair("Age:",baseconf.getAge(mp.bDay,mp.bMonth,mp.iYear)+" years");
            addMyPair("DOB:",baseconf.getNiceField(mp.bDay)+" "+baseconf.monthNames[mp.bMonth]+" "+mp.iYear);
        }

        // Drinker
        if ((mp.lVisibility&MobudsProfile.VISIBLE_DRINKER)!=0) addMyPair("Drinker:",baseconf.habbitTypes[mp.iDrinker]);
        if ((mp.lVisibility&MobudsProfile.VISIBLE_SMOKER)!=0) addMyPair("Drinker:",baseconf.habbitTypes[mp.iSmoker]);

        if ((mp.lVisibility&(MobudsProfile.VISIBLE_HEIGHT|MobudsProfile.VISIBLE_FACEHAIR|MobudsProfile.VISIBLE_GLASSES|MobudsProfile.VISIBLE_BODYTYPE|MobudsProfile.VISIBLE_EYECOL|MobudsProfile.VISIBLE_HAIRCOL))!=0) {
            startNewSection("Physical Appearance");
            if ((mp.lVisibility&MobudsProfile.VISIBLE_HEIGHT)!=0) addMyPair("Height:",baseconf.heightTypes[mp.iHeight]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_BODYTYPE)!=0) addMyPair("Body Type:",baseconf.bodyTypes[mp.iBodyType]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_EYECOL)!=0) addMyPair("Eye Colour:",baseconf.eyeColours[mp.iEyeColor]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_HAIRCOL)!=0) addMyPair("Hair Colour:",baseconf.hairColours[mp.iHairColor]);
            String[] answers = {"No","Yes"};
            if ((mp.lVisibility&MobudsProfile.VISIBLE_GLASSES)!=0) addMyPair("Wears Glasses:",mp.bWearsGlasses?answers[1]:answers[0]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_FACEHAIR)!=0) addMyPair("Facial Hair:",mp.bFacialHair?answers[1]:answers[0]);
        }

        if ((mp.lVisibility&(MobudsProfile.VISIBLE_STATUS|MobudsProfile.VISIBLE_ORIENTATION|MobudsProfile.VISIBLE_PROFESSION))!=0) {
            startNewSection("Lifestyle");
            if ((mp.lVisibility&MobudsProfile.VISIBLE_STATUS)!=0) addMyPair("Status:",baseconf.statusList[mp.iStatus]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_ORIENTATION)!=0) addMyPair("Sexuality:",baseconf.orientationList[mp.iSexualOrientation]);
            if ((mp.lVisibility&MobudsProfile.VISIBLE_PROFESSION)!=0) addMyPair("I Am:",baseconf.professionList[mp.iProfession]);
        }

        // Looking for
        st="";
        if ((mp.lVisibility&MobudsProfile.VISIBLE_LOOKING)!=0) 
            for (int a=0; a<baseconf.lookingForList.length; a++)
                if ((mp.iLookingFor&baseconf.lookingForMasks[a])!=0) st+=", "+baseconf.lookingForList[a];

        // Looking for with
        st2="";
        if ((mp.lVisibility&MobudsProfile.VISIBLE_LOOKINGFOR)!=0) 
            for (int a=0; a<baseconf.lookingWithList.length; a++)
                if ((mp.iLookingForGender&baseconf.lookingWithMask[a])!=0) st2+=", "+baseconf.lookingWithList[a];

        if ((st.length()>0)&&(st2.length()>0)) {
                startNewSection("...Looking For");
                addInfoLine(st.substring(2)+" with "+st2.substring(2));
        } else if (st.length()>0) {
                startNewSection("...Looking For");
                addInfoLine(st.substring(2));
        } else if (st2.length()>0) {
                startNewSection("...Interact With");
                addInfoLine(st2.substring(2));
        }

        // Description
        if (((mp.lVisibility&MobudsProfile.VISIBLE_MESSAGE)!=0)&&(mp.strDescription.length()>0)) {
            startNewSection("Personality");
            addInfoLine(mp.strDescription);
        }

        addInfoLine(" ");
        addModalCommand("Back",moSystem.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Action",moSystem.imgCross,MR_OK,CMD_NONE);
    }

        // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) {
            runActionPopup();
            return false;
        }
        return true;
    }  
    
    // Run Action
    private void runActionPopup() {
        dlg = new dlgActions(moSystem,strBlue,false,false,false);
        int res = dlg.showOptions();
        dlg = null;

        switch (res) {
            case dlgActions.RESULT_SENDMESSAGE:
                showSubModal(new frmSendMessage(moSystem,strBlue,profile.strNickName,""));
                break;

            case dlgActions.RESULT_ADDREMOVEFRIEND:
                if (moSystem.persData.isFriend(strBlue)) moSystem.persData.delUserFromFriends(strBlue);
                                                    else if (!moSystem.persData.addUserToFriends(strBlue, profile.saveToString(false))) showAddError(true);
                refreshFriendshipStatus();
                break;

            case dlgActions.RESULT_ADDREMOVEBLOCK:
                if (moSystem.persData.isBlocked(strBlue)) moSystem.persData.delUserFromBlocks(strBlue);
                                                    else if (!moSystem.persData.addUserToBlockList(strBlue, profile.saveToString(false))) showAddError(false);
                refreshFriendshipStatus();
                break;
        }
    }
}
