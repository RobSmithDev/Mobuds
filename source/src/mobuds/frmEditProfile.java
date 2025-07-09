/*
 * frmEditProfile.java
 *
 * Created on 02 January 2009, 10:46
 *
 * This is the edit profile form - for editing your phone profile
 */

package mobuds;

import java.util.*;
import java.util.Calendar;
import com.sun.lwuit.*;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.layouts.*;



/**
 *
 * @author rob smith
 */
public class frmEditProfile extends ModalForm {
    private Command cmdOK;
    private Command cmdCancel;
    private GroupLayout layout;
    private int lwidth;
    
    // controls
    private int iStartYear;
    private TextField txtNickName;
    private TextField txtPhoneNumber;
    private TextField txtWebsiteURL;
    private ComboBox cboGender;
    private ComboBox cboDOB_Day;
    private ComboBox cboDOB_Month;
    private ComboBox cboDOB_Year;
    private ComboBox cboDrink;
    private ComboBox cboSmoke;
    private ComboBox cboHeight;
    private ComboBox cboBody;
    private ComboBox cboEye;
    private ComboBox cboHair;
    private ComboBox cboStatus;
    private ComboBox cboSexuality;
    private ComboBox cboSocial;
    private ComboBox cboWearGlasses;
    private ComboBox cboHasFacialHair;
    private Vector vecLookingWith;
    private Vector vecLookingFor;
    private TextField txtDescription;
    
    
    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) {
            // Need to test some stuff
            String msg="";
            if (txtNickName.getText().trim().length()<2) msg="Your nick name must be at least 2 letters long"; else {
                try {
                    int age = baseconf.getAge(cboDOB_Day.getSelectedIndex()+1,cboDOB_Month.getSelectedIndex()+1,cboDOB_Year.getSelectedIndex()+iStartYear);                    
                    if (age<MobudsProfile.MINIMUM_AGE)  msg="You must be at least "+MobudsProfile.MINIMUM_AGE+" years old to use Mobuds"; 
                } catch (Exception e) {
                    msg = "Your Date of Birth is not valid";
                }
            }
            
            if (msg.length()>0) {
                displayMessage("Profile",msg,true);
                return false;
            } else return true;
        } else return true;
    }

    // Updates the profile supplied and returns it
    public MobudsProfile getProfileFromForm(MobudsProfile profile) {
         int a;
         CheckBox cbx;
         Enumeration Enum;

         profile.strNickName = txtNickName.getText();
         profile.strMobileNumber = txtPhoneNumber.getText();
         profile.strURL = txtWebsiteURL .getText();
         profile.bGender = (cboGender.getSelectedIndex()==0?MobudsProfile.GENDER_MALE:MobudsProfile.GENDER_FEMALE);
        
         Calendar d = Calendar.getInstance();
         d.set(Calendar.YEAR,cboDOB_Year.getSelectedIndex()+iStartYear);
         d.set(Calendar.MONTH,cboDOB_Month.getSelectedIndex());
         d.set(Calendar.DAY_OF_MONTH,cboDOB_Day.getSelectedIndex()+1);
         profile.iYear = d.get(Calendar.YEAR);
         profile.bMonth = (byte)d.get(Calendar.MONTH);
         profile.bDay = (byte)d.get(Calendar.DAY_OF_MONTH);      
        
         profile.iDrinker = (byte)cboDrink.getSelectedIndex();
         profile.iSmoker = (byte)cboSmoke.getSelectedIndex();
        
         profile.iHeight = (byte)cboHeight.getSelectedIndex(); 
         profile.iBodyType = (byte)cboBody.getSelectedIndex(); 
         profile.iEyeColor = (byte)cboEye.getSelectedIndex(); 
         profile.iHairColor = (byte)cboHair.getSelectedIndex(); 

         profile.bWearsGlasses = cboWearGlasses.getSelectedIndex()==1; 
         profile.bFacialHair = cboHasFacialHair.getSelectedIndex()==1; 

         profile.iStatus = (byte)cboStatus.getSelectedIndex(); 
         profile.iSexualOrientation = (byte)cboSexuality.getSelectedIndex(); 
         profile.iProfession = (byte)cboSocial.getSelectedIndex(); 
         
         Enum = vecLookingFor.elements();
         profile.iLookingFor=0;
         a=0;
         while (Enum.hasMoreElements()) {
             cbx = (CheckBox)Enum.nextElement();
             if (cbx.isSelected()) profile.iLookingFor|=baseconf.lookingForMasks[a];
             a++;
         }

         Enum = vecLookingWith.elements();
         profile.iLookingForGender=0;
         a=0;
         while (Enum.hasMoreElements()) {
             cbx = (CheckBox)Enum.nextElement();
             if (cbx.isSelected()) profile.iLookingFor|=baseconf.lookingWithMask[a];
             a++;
         }

        profile.strDescription = txtDescription.getText();
        
        return profile;
    }
    
    // Creates a new instance of frmEditProfile
    public frmEditProfile(MobudsSystem system, String title, MobudsProfile profile) {
        super(system,"Edit Profile");
        int a;
        CheckBox cbx;
        lwidth = /*system.persData.bUseSmallFont?65:*/88;

        
        // OK and Cancel buttons
        addModalCommand("Cancel",system.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Save",system.imgTick,MR_OK,CMD_NONE);
        
        addInfoLine(title);
                    
        // Now add the controls
        startNewSection("Contact Details");
        txtNickName = new TextField(profile.strNickName); txtNickName.setMaxSize(MobudsProfile.MAX_NICKNAME_LENGTH); addPair("Nick Name:",txtNickName,lwidth);
        txtPhoneNumber = new TextField(profile.strMobileNumber); txtPhoneNumber.setMaxSize(MobudsProfile.MAX_PHONE_LENGTH); txtPhoneNumber.setConstraint(TextArea.PHONENUMBER); addPair("Phone No:",txtPhoneNumber,lwidth);
        txtWebsiteURL = new TextField(profile.strURL); txtWebsiteURL.setMaxSize(MobudsProfile.MAX_URL_LENGTH); txtWebsiteURL.setConstraint(TextArea.URL); addPair("Homepage:",txtWebsiteURL,lwidth);

        startNewSection("Who Are You");
        cboGender = new ComboBox(baseconf.strGenders); addPair("Gender:",cboGender,lwidth); cboGender.setSelectedIndex((profile.bGender==MobudsProfile.GENDER_MALE)?0:1);
        
        // DOB
        Vector days; days = new Vector(); for (a=1; a<=31; a++) days.addElement(a+"    ");  cboDOB_Day = new ComboBox(days);
        cboDOB_Month = new ComboBox(baseconf.shortMonthNames);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new java.util.Date());
        int currentyear = cal.get(java.util.Calendar.YEAR);

        iStartYear = currentyear-120; Vector years; years = new Vector(); for (a=currentyear-120; a<=currentyear-MobudsProfile.MINIMUM_AGE; a++) years.addElement(a+"");
        cboDOB_Year = new ComboBox(years);   Dimension d = cboDOB_Day.getPreferredSize(); d.setWidth(d.getWidth()+15); cboDOB_Day.setPreferredSize(d);
        Container dob =  new Container(new BorderLayout()); dob.addComponent(BorderLayout.WEST, cboDOB_Day);  dob.addComponent(BorderLayout.CENTER, cboDOB_Month);
        cboDOB_Year.setSelectedIndex(profile.iYear-iStartYear);
        cboDOB_Month.setSelectedIndex(profile.bMonth);
        cboDOB_Day.setSelectedIndex(profile.bDay-1);
       
        if (this.getWidth()<180) {
            addPair("Birthday:",cboDOB_Day,lwidth);
            addPair("",cboDOB_Month,lwidth);
        } else {
            cboDOB_Day.setWidth(cboDOB_Day.getWidth()+60);
            cboDOB_Month.setWidth(cboDOB_Month.getWidth()+30);
            addPair("Birthday:",dob,lwidth); 
        }
        addPair("",cboDOB_Year,lwidth);
        
        cboDrink = new ComboBox(baseconf.habbitTypes); addPair("I Drink:",cboDrink,lwidth); cboDrink.setSelectedIndex(profile.iDrinker);
        cboSmoke = new ComboBox(baseconf.habbitTypes); addPair("I Smoke:",cboSmoke,lwidth); cboSmoke.setSelectedIndex(profile.iSmoker);
        
        startNewSection("Physical Appearance");
        cboHeight = new ComboBox(baseconf.heightTypes); addPair("Height:",cboHeight,lwidth); cboHeight.setSelectedIndex(profile.iHeight);
        cboBody = new ComboBox(baseconf.bodyTypes); addPair("Body Type:",cboBody,lwidth); cboBody.setSelectedIndex(profile.iBodyType);
        cboEye = new ComboBox(baseconf.eyeColours); addPair("Eye Colour:",cboEye,lwidth); cboEye.setSelectedIndex(profile.iEyeColor);
        cboHair = new ComboBox(baseconf.hairColours); addPair("Hair Colour:",cboHair,lwidth); cboHair.setSelectedIndex(profile.iHairColor);
        
        String[] answers = {"No","Yes"};
        cboWearGlasses = new ComboBox(answers); addPair("Glasses:",cboWearGlasses,lwidth); cboWearGlasses.setSelectedIndex(profile.bWearsGlasses?1:0);
        cboHasFacialHair = new ComboBox(answers); addPair("Facial Hair:",cboHasFacialHair,lwidth); cboHasFacialHair.setSelectedIndex(profile.bFacialHair?1:0);
        
        startNewSection("Lifestyle");
        cboStatus = new ComboBox(baseconf.statusList); addPair("Status:",cboStatus,lwidth); cboStatus.setSelectedIndex(profile.iStatus);
        cboSexuality = new ComboBox(baseconf.orientationList); addPair("Sexuality:",cboSexuality,lwidth); cboSexuality.setSelectedIndex(profile.iSexualOrientation);
        cboSocial = new ComboBox(baseconf.professionList); addPair("I Am:",cboSocial,lwidth); cboSocial.setSelectedIndex(profile.iProfession);
        
        startNewSection("...Looking For");
        Container ll =  new Container(new BoxLayout(BoxLayout.Y_AXIS));
        vecLookingFor = new Vector();
        for (a=0; a<baseconf.lookingForList.length; a++) {
            cbx = new CheckBox(baseconf.lookingForList[a]);
            cbx.setSelected((profile.iLookingFor&baseconf.lookingForMasks[a])!=0);
            vecLookingFor.addElement(cbx);
            ll.addComponent(cbx);
        }
        addComponent(ll);
        startNewSection("...With");
        ll =  new Container(new BoxLayout(BoxLayout.Y_AXIS));
        vecLookingWith = new Vector();
        for (a=0; a<baseconf.lookingWithList.length; a++) {
            cbx = new CheckBox(baseconf.lookingWithList[a]);
            cbx.setSelected((profile.iLookingForGender&baseconf.lookingWithMask[a])!=0);
            vecLookingWith.addElement(cbx);
            ll.addComponent(cbx);
        }
        addComponent(ll);
                
        startNewSection("Personality");
        txtDescription = new TextField(profile.strDescription); txtDescription.setMaxSize(MobudsProfile.MAX_DESCRIPTION_LENGTH); addPair("Personality:",txtDescription,lwidth);
    }    
}
