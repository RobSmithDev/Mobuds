/*
 * frmEditAlertProfile.java
 *
 * Created on 09 January 2009, 21:27
 *
 * Edit an alert
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.events.SelectionListener;

/**
 *
 * @author rob smith
 */
public class frmEditAlertProfile extends ModalForm implements SelectionListener  {
    
    private Command cmdOK;
    private Command cmdCancel;
    private GroupLayout layout;
    private int lwidth;
    
    // controls
    private int iStartYear;
    private TextField txtAlertName;
    private Container cboGender;
    private ComboBox cboAgeMode;
    private ComboBox cboAgeFrom;
    private ComboBox cboAgeTo;
    private Container cboStatus;

    private Container cboDrink;
    private Container cboSmoke;
    private Container cboHeight;
    private Container cboBody;
    private Container cboEye;
    private Container cboHair;
    private Container cboSexuality;
    private Container cboSocial;
    private ComboBox cboWearGlasses;
    private ComboBox cboHasFacialHair;
    private Container vecLookingForWith;
    private Container vecLookingFor;
    
    private static final String strDC = "Don't Care";
    
    
    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        if (modalResult==MR_OK) {
            // Need to test some stuff
            String msg="";
            if (txtAlertName.getText().trim().length()<2) msg="Your match name must be at least 2 letters long"; 
            
            if (msg.length()>0) {   
                displayMessage("Match Error",msg,true);
                return false;
            } else return true;
        } else return true;
    }
    
    // Returns a check mask of the ticks
    private static byte getCheckListMask(Container elements) {
        CheckBox cbx;
        byte output = 0;
        for (int a=elements.getComponentCount()-1; a>=0; a--) {
            cbx = (CheckBox)elements.getComponentAt(a);
            output<<=1;
            if (cbx.isSelected()) output|=1;
        }
        return output;
    }

    // Updates the profile supplied and returns it
    public AlertProfile getProfileFromForm(AlertProfile profile) {
        int a;
        CheckBox cbx;
        Vector ages;
                                   
        profile.sAlertName = txtAlertName.getText();
        profile.bGender = getCheckListMask(cboGender);
        profile.bAgeMode = (byte)cboAgeMode.getSelectedIndex();
        profile.bMinAge = (byte)(cboAgeFrom.getSelectedIndex()+MobudsProfile.MINIMUM_AGE);
        profile.bMaxAge = (byte)(cboAgeTo.getSelectedIndex()+MobudsProfile.MINIMUM_AGE);
        profile.bJobStatus = getCheckListMask(cboSocial);
        profile.bDrinks = getCheckListMask(cboDrink); 
        profile.bSmokes = getCheckListMask(cboSmoke); 
        profile.bHeight = getCheckListMask(cboHeight); 
        profile.bBody = getCheckListMask(cboBody); 
        profile.bEyes = getCheckListMask(cboEye); 
        profile.bHair = getCheckListMask(cboHair);
        profile.bWearsGlasses = (byte)cboWearGlasses.getSelectedIndex(); 
        profile.bFacialHair = (byte)cboHasFacialHair.getSelectedIndex(); 
        profile.bMarriedMode = getCheckListMask(cboStatus); 
        profile.bSexuality = getCheckListMask(cboSexuality); 
        profile.bLookingFor = getCheckListMask(vecLookingFor); 
        profile.bLookingForWith = getCheckListMask(vecLookingForWith); 
        return profile;
    }
       
    // Selection changed on a combo box
    public void selectionChanged(int oldSelected, int newSelected) {
        cboAgeFrom.setEnabled(newSelected==1);
        cboAgeTo.setEnabled(newSelected==1);
    }
        
    // Returns a container with the specific elements checked
    private static Container getCheckList(String items[], int checkMask) {
        int a;
        CheckBox cbx;
        Container ll =  new Container(new BoxLayout(BoxLayout.Y_AXIS));
        for (a=0; a<items.length; a++) {
            cbx = new CheckBox(items[a]);
            cbx.setSelected(((1<<a)&checkMask)!=0);
            ll.addComponent(cbx);
        }

        return ll;        
    }
        
    // Creates a new instance of frmEditAlertProfile
    public frmEditAlertProfile(MobudsSystem system, int AlertNumber, AlertProfile profile) {        
        super(system,"Match "+AlertNumber);
        int a;
        CheckBox cbx;
        Vector ages;

        lwidth = /*system.persData.bUseSmallFont?65:*/92;
               
        // OK and Cancel buttons
        addModalCommand("Cancel",system.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Save",system.imgTick,MR_OK,CMD_NONE);
        
        addInfoLine("Use the boxes below to choose the type of person you want to be notified about:");
                    
        // Now add the controls
        startNewSection("Name This Match");
        txtAlertName = new TextField(profile.sAlertName); txtAlertName.setMaxSize(AlertProfile.MAX_ALERT_NAME); addPair("Name:",txtAlertName,lwidth);

        startNewSection("Match Which Genders");
        cboGender = getCheckList(baseconf.strGenders,profile.bGender);
        addPair("Gender:",cboGender,lwidth); 
        
        startNewSection("How Old Are They?");
        cboAgeMode = new ComboBox(new String[]{strDC,"Age Range"});
        addPair("Age:",cboAgeMode,lwidth);  cboAgeMode.setSelectedIndex(profile.bAgeMode);
        ages = new Vector();
        for (a=MobudsProfile.MINIMUM_AGE; a<=MobudsProfile.MAXIMUM_AGE; a++) ages.addElement(a+"");
        cboAgeFrom = new ComboBox(ages);  cboAgeFrom.setSelectedIndex(profile.bMinAge-MobudsProfile.MINIMUM_AGE);
        cboAgeTo = new ComboBox(ages);    cboAgeTo.setSelectedIndex(profile.bMaxAge-MobudsProfile.MINIMUM_AGE);
        addPair("Between:",cboAgeFrom,lwidth);
        addPair("and:",cboAgeTo,lwidth);
        cboAgeMode.addSelectionListener(this);
        selectionChanged(profile.bAgeMode,profile.bAgeMode);
        startNewSection("What Do They Do?");
        cboSocial = getCheckList(baseconf.professionList,profile.bJobStatus); addComponent(cboSocial);
        startNewSection("Drinking Habbits");
        cboDrink = getCheckList(baseconf.habbitTypes,profile.bDrinks); addComponent(cboDrink);
        startNewSection("Smoking Habbits");
        cboSmoke = getCheckList(baseconf.habbitTypes,profile.bSmokes); addComponent(cboSmoke); 
        startNewSection("Height");
        cboHeight = getCheckList(baseconf.heightTypes,profile.bHeight); addComponent(cboHeight);
        startNewSection("Body Type");
        cboBody = getCheckList(baseconf.bodyTypes,profile.bBody); addComponent(cboBody);
        startNewSection("Eye Colour");
        cboEye = getCheckList(baseconf.eyeColours,profile.bEyes); addComponent(cboEye); 
        startNewSection("Hair Colour");
        cboHair = getCheckList(baseconf.hairColours,profile.bHair); addComponent(cboHair); 
        startNewSection("Other Attributes");
        String[] answers = {strDC,"No","Yes"};
        cboWearGlasses = new ComboBox(answers); addPair("Glasses:",cboWearGlasses,lwidth); cboWearGlasses.setSelectedIndex(profile.bWearsGlasses);
        cboHasFacialHair = new ComboBox(answers); addPair("Facial Hair:",cboHasFacialHair,lwidth); cboHasFacialHair.setSelectedIndex(profile.bFacialHair);
        startNewSection("Status");
        cboStatus = getCheckList(baseconf.statusList,profile.bMarriedMode); addComponent(cboStatus);; 
        startNewSection("Sexuality");
        cboSexuality = getCheckList(baseconf.orientationList,profile.bSexuality); addComponent(cboSexuality); 
        startNewSection("Match Type");
        vecLookingFor = getCheckList(baseconf.lookingForList,profile.bLookingFor); addComponent(vecLookingFor); 
        startNewSection("Match With");
        vecLookingForWith = getCheckList(baseconf.lookingWithList,profile.bLookingForWith); addComponent(vecLookingForWith);
    }    
}
