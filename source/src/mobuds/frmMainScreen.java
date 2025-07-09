/*
 * frmMainScreen.java
 *
 * Created on 08 January 2009, 20:48
 *
 * The main screen
 */

package mobuds;

import com.sun.lwuit.*;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.events.ActionEvent;


/**
 *
 * @author rob smith
 */
public class frmMainScreen extends ModalForm implements Painter {
    private Image logo;
    private String verstr;
    private int copyyear;
    public int numberOfUsers,numberOfMatchedUsers,numberOfFriends,numberOfBlocks;
    public int inboxSize,inboxNew,outboxSize,alertsNew,alertsTotal;
    boolean bRepaintIt,bLastStatus;
    int lastOtherStatus;
    private String croppedName,lastName; 
    private java.util.Calendar cal = java.util.Calendar.getInstance();
    private long lastTime = System.currentTimeMillis();
    
    // animate thing
    public boolean animate() {
        boolean bNewStatus = moSystem.getMailIconStatus();
        if (bLastStatus!=bNewStatus) {
            bLastStatus = bNewStatus;
            bRepaintIt = false;
            return true;
        }
        int bNewStat = moSystem.getAlertIndex();
        if (bNewStat!=lastOtherStatus) {
            lastOtherStatus = bNewStat;
            bRepaintIt = false;
            return true;
        }

        if (bRepaintIt) {
            bRepaintIt = false;
            return true;
        }
        
        if (System.currentTimeMillis()-lastTime>500) {
            lastTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    // Cancel
    public void cancelModal() {
        setModalResult(ModalForm.MR_OK);
    }
    
    // Background painter class
    public void paint(Graphics g, Rectangle rect) {
        String s,line2,line3,line4;    
        int a,x1,x2,minw,copyLines,boxtop;
        
        // Draw layout
        g.drawImage(logo,5,6);
        g.setColor(0xA0A0A0);  s = "v"+verstr;   g.drawString(s,((logo.getWidth()>>1)-(g.getFont().stringWidth(s)>>1))+15,logo.getHeight()-3);

        int hh = /*moSystem.persData.bUseSmallFont?26:*/28;
        int ii = /*moSystem.persData.bUseSmallFont?0:*/1;
        if (moSystem.horizSeperatorLine==null) {
            g.setColor(moSystem.horizSeperatorLineDefColor);
            g.fillRect(0,getHeight()-hh,getWidth(),hh);
        } else g.fillLinearGradient(0xDE00A2,0x7A0368,0,getHeight()-hh,getWidth(),hh,false);
        g.setColor(0xD0D0D0); s = "Menu"; x2 = g.getFont().stringWidth(s); x1 = (getWidth()>>1)-(x2>>1); x2+=x1; 
        g.drawString(s,x1,(getHeight()-24+(12-(g.getFont().getHeight()>>1)))-ii);
        g.drawImage(moSystem.imgDot,12,getHeight()-(20+ii));
        g.drawImage(moSystem.imgDot,getWidth()-28,getHeight()-(20+ii));
        
        // Some basic information, that we dont care if it gets lost due to small screens        
        if (lastName.compareTo(moSystem.persData.myProfile.strNickName)!=0) {
            lastName = moSystem.persData.myProfile.strNickName;
            croppedName = moSystem.persData.myProfile.strNickName;
            
            if (g.getFont().stringWidth(croppedName)>getWidth()-(logo.getWidth()+14)) {
                while ((g.getFont().stringWidth(croppedName+"...")>getWidth()-(logo.getWidth()+14))&&(croppedName.length()>0)) croppedName = croppedName.substring(0,croppedName.length()-1);
                croppedName+="...";
            }                
        }
        
        cal.setTime(new java.util.Date());
                
        line3 = cal.get(java.util.Calendar.DAY_OF_MONTH)+" "+baseconf.monthNames[cal.get(java.util.Calendar.MONTH)]+" "+cal.get(java.util.Calendar.YEAR);
        line4 = cal.get(java.util.Calendar.HOUR)+"";
        if (line4.length()<2) line4="0"+line4;
        line4+=":";
        line2 = cal.get(java.util.Calendar.MINUTE)+"";
        if (line2.length()<2) line2="0"+line2;
        line4+=line2+":";
        line2 = cal.get(java.util.Calendar.SECOND)+"";
        if (line2.length()<2) line2="0"+line2;
        line4+=line2+" ";
        if (cal.get(java.util.Calendar.AM_PM)<1) line4+="am"; else line4+="pm";
        
        line2 = croppedName;

        minw = Math.max(g.getFont().stringWidth(line2),Math.max(g.getFont().stringWidth(line3),g.getFont().stringWidth(line4)));
        
        if (getWidth()-minw>15) {
            g.setColor(0xD0D0D0); 
            x1 = logo.getWidth()+10;
            x2 = (getWidth()-x1)>>1;
            x1+=x2;
            x2 = 14;
            g.drawString(line2,x1-(g.getFont().stringWidth(line2)>>1),x2);   x2+=g.getFont().getHeight()-1;
            g.setColor(0x707070); 
            g.drawString(line3,x1-(g.getFont().stringWidth(line3)>>1),x2);   x2+=g.getFont().getHeight()-1;
            g.drawString(line4,x1-(g.getFont().stringWidth(line4)>>1),x2);            
        } else x2 = logo.getHeight()+1;
        boxtop = x2;
    
        line4 = "Copyright (C) 2006-"+((copyyear+"").substring(2))+" Crave Create";
        copyLines=g.getFont().getHeight();
        if (g.getFont().stringWidth(line4)>=getWidth()) {
            line3 = "Copyright (C)";
            line4 = "2006-"+((copyyear+"").substring(2))+" Crave Create";
            copyLines+=g.getFont().getHeight();
            g.drawString(line3,(getWidth()>>1)-(g.getFont().stringWidth(line3)>>1),(getHeight()-29)-(g.getFont().getHeight()*2));            
            g.drawString(line4,(getWidth()>>1)-(g.getFont().stringWidth(line4)>>1),(getHeight()-29)-g.getFont().getHeight());            
        } else g.drawString(line4,(getWidth()>>1)-(g.getFont().stringWidth(line4)>>1),(getHeight()-29)-g.getFont().getHeight());            
            
        // Work out how many lines we can show
        x2 = (getHeight()-(copyLines+boxtop+30));
        x1 = (x2-10)/g.getFont().getHeight();
        
        String[] st = new String[10];
        
        st[0] = "People Close By: "+numberOfUsers;
        st[1] = baseconf.strFriends+" Close By: "+numberOfFriends;
        st[2] = baseconf.strBlocks+" Close By: "+numberOfBlocks;
        st[3] = "Matches Close By: "+numberOfMatchedUsers;
        st[4] = "MoMail: "+inboxNew+" new ("+inboxSize+'/'+PersistantData.SETTING_MAX_SAVED_MESSAGES+")";
        st[5] = "Local Alerts: "+alertsNew+"/"+alertsTotal;
        if (moSystem.persData.bDebugMode) {
            st[6] = "Outbox Sending: "+outboxSize;
            if (x1>7) x1=7;
        } else {
            st[6]="";
            if (x1>6) x1=6;
        }
        x2 = (boxtop)+((x2>>1)-(((g.getFont().getHeight()-1)*x1)>>1));
        g.setColor(0xA0A0A0); 
        minw=0;
        for (a=0; a<x1; a++) minw = Math.max(minw,g.getFont().stringWidth(st[a]));
        
        minw = (getWidth()>>1)+(minw>>1);

        for (a=0; a<x1; a++)  {
            g.drawString(st[a],minw-g.getFont().stringWidth(st[a]),x2);   
            x2+=g.getFont().getHeight()-1;
        }
        
        int right = getWidth()-3;
        if ((moSystem.imgNewMail!=null)&&(moSystem.getMailIconStatus())) {
            g.drawImage(moSystem.imgNewMail,right-moSystem.imgNewMail.getWidth(),1);
            right-=moSystem.imgNewMail.getWidth()+1;
        }

        Image another = null;

        switch (moSystem.getAlertIndex()) {

            case 0:   // Alert 1-3
            case 1:
            case 2:
                another = moSystem.imgMatch;
                break;
            case -1:  // Friend near
                another = moSystem.imgFriend;
                break;
            case -2: // Blocked Person Near
                another = moSystem.imgBlocked;
                break;
        }

        if (another!=null) g.drawImage(another,right-another.getWidth(),1);                
    }

    // Quit signalled
    private void runMenuAction() {
        this.setModalResult(ModalForm.MR_OK);
    }
    
    /** Creates a new instance of frmMainScreen */
    public frmMainScreen(MobudsSystem system, Image logoIn) {
            super(system,"");
            cal.setTimeZone(java.util.TimeZone.getDefault());
            
            logo = logoIn;
            croppedName="";
            lastName="";
            moSystem = system;
            verstr = moSystem.getVersion();
                    
            // Copy year min 2009
            java.util.Calendar d = java.util.Calendar.getInstance();
            copyyear = d.get(java.util.Calendar.YEAR);
            if (copyyear<2009) copyyear=2009;

            Command cmd = (new Command("Menu",null) { public void actionPerformed(ActionEvent evt) { runMenuAction();};});
            addCommand(cmd);
            setDefaultCommand(cmd);
            addCommand(new Command("Menu",null) { public void actionPerformed(ActionEvent evt) { runMenuAction();};});

            this.setGlassPane(this);
    }   
}
