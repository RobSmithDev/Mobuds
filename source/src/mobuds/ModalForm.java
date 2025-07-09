/*
 * ModalForm.java
 *
 * Created on 01 January 2009, 22:57
 *
 * A modal version of the form
 */

package mobuds;

import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.*;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.Font;

/**
 *
 * @author rob smith
 */
public class ModalForm extends Form implements Painter {

    public static final int MR_QUIT     = -1;  // sent to make this quit
    public static final int MR_NONE     = 0;
    public static final int MR_OK       = 1;
    public static final int MR_CANCEL   = 2;

    public static final int CMD_NONE = 0;
    public static final int CMD_DEFAULT = 1;
    public static final int CMD_BACK = 2;

    private ModalForm nextModalForm = null;
//    private int nextModalResult = MR_NONE;
    private int modalresult;
    private final Object obLocker = new Object();
  //  private Painter horizPainter;
    private boolean reducedHeightRows = false;
    private boolean lastStatus = false;
    private int lastAlertStatus = 0;

    protected MobudsSystem moSystem;

    // Enabel the reduced height mode
    protected void setReducedHeightMode() {
        reducedHeightRows = true;
    }

    // override ot change what happens when the dialog closes
    protected boolean canClose(int modalResult) {
        return true;
    }

    // Displays a message on top of the dialog
    public void displayMessage(String title, String message, boolean bError) {
        Command okCommand = new Command("OK");
        moSystem.displayMessage(title,message,bError);
    }
    
    // show special add user/block error message
    public void showAddError(boolean isFriend) {
        String s = "Unable to add to your "+(isFriend?baseconf.strFriend:baseconf.strBlocks)+" list. You must delete some "+baseconf.strFriend+" or "+baseconf.strBlocks+" first.";
        moSystem.displayMessage("Full",s,true);        
    }

    // Returns TRUE if we're ready to animate
    public boolean animate()  {
        boolean status = moSystem.getMailIconStatus();
        if (lastStatus!=status) {
            lastStatus = status;
            return true;
        } else  {
            int sss = moSystem.getAlertIndex();
            if (sss!=lastAlertStatus) {
                lastAlertStatus = sss;
                return true;
            }
            return false;
        }
    }

        // Background painter class
    public void paint(Graphics g, Rectangle rect) {
        int right = getWidth()-3;
        if (moSystem.miniLogo!=null) g.drawImage(moSystem.miniLogo,1,1);
        if ((moSystem.imgNewMail!=null)&&(moSystem.getMailIconStatus())) {
            g.drawImage(moSystem.imgNewMail,right-moSystem.imgNewMail.getWidth(),2);
            right-=moSystem.imgNewMail.getWidth()+1;
        }

        Image another = null;

        // TODO
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

        if (another!=null) g.drawImage(another,right-another.getWidth(),2);

    }


    /** Creates a new instance of ModalForm */
    public ModalForm(MobudsSystem system, String title) {
        super(title);
        modalresult = MR_NONE;
        moSystem = system;
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        setGlassPane(this);
        this.getTitleComponent().getStyle().setPadding(Component.LEFT,28);
        this.getTitleComponent().getStyle().setPadding(Component.RIGHT,28);
        this.registerAnimated(this);
    }

    // returns the current dialog result
    public int modalResult() {
        return modalresult;
    }

    // Set the modal result
    protected void setModalResult(int result) {
        synchronized(obLocker) {
            modalresult = result;
            obLocker.notify();
        }
    }

    // adds a label so it loks like a new heading
    protected void startNewSection(String title) {
        Label l =  new Label(title);
        Style st = l.getStyle();
        st.setFont(moSystem.headingFont);
        l.setAlignment(Component.RIGHT);
        l.setVerticalAlignment(Component.BOTTOM);
        l.setHeight(l.getPreferredH()+5);

        st.setFgColor(0xD0D0D0);
        Style st2 = l.getSelectedStyle();
        st2.setFont(moSystem.headingFont);
        st2.setFgColor(0xFFFFFF);

        if (moSystem.horizSeperatorLine==null) {
               st.setBgTransparency(255);
               st2.setBgTransparency(255);
               st.setBgColor(MobudsSystem.horizSeperatorLineDefColor,true);
               st2.setBgColor(MobudsSystem.horizSeperatorLineDefColor,true);
        } else {
            st.setBgImage(moSystem.horizSeperatorLine,true);
            st2.setBgImage(moSystem.horizSeperatorLine,true);
        }
        l.setFocusable(true);
        addComponent(l);
    }

    // Add an info line
    protected void addInfoLine(String line, boolean alignTop) {
        wordWrapLabels(line,alignTop);
    }

    // Add an info line
    protected void addInfoLine(String line) {
        wordWrapLabels(line,false);
    }

    // adds a spacer line
    protected void addSpacerLine() {
        Label l = new Label(" ");
        addComponent(l);
    }

    // Adds a pair of controls so you can have a title in front of it
    protected void addPair(String label, Component c, int minWidth) {
        Container pair = new Container(new BorderLayout());
        Label l =  new Label(label);
        if (reducedHeightRows) {
            Style st = l.getStyle(); st.setMargin(3,3,4,4); st.setPadding(0,0,0,0); l.setStyle(st);
            st = c.getStyle(); st.setMargin(3,3,4,4); st.setPadding(0,0,0,0); c.setStyle(st);
        }
        l.setAlignment(Label.RIGHT);
        l.setVerticalAlignment(Label.TOP);
        l.getStyle().setFgColor(0xFFFFFF);
        Dimension d = l.getPreferredSize();
        d.setWidth(Math.max(d.getWidth(), minWidth));
        l.setPreferredSize(d);
        pair.addComponent(BorderLayout.WEST,l);
        pair.addComponent(BorderLayout.CENTER, c);
        addComponent(pair);
    }

    // Add a modal command to the dialog
    public void addModalCommand(String title, Image icon,final int modalResult, int commandType) {
        com.sun.lwuit.Command cmd = new com.sun.lwuit.Command(title,icon) {
                public void actionPerformed(ActionEvent evt) {
                        setModalResult(modalResult);
                };
        };

        addCommand(cmd);
        switch (commandType) {
            case CMD_DEFAULT:setDefaultCommand(cmd); break;
            case CMD_BACK:setBackCommand(cmd); break;
        }
    }

    // for compatability
    public void wordWrapLabels(String text) {
        wordWrapLabels(text,false);
    }

    // Adds text wordwrapped to fit the screen
    public void wordWrapLabels(String text, boolean alignTop) {
        Font fnt = getStyle().getFont();
        String txt;
        Label lbl;
        int iPos,l;
        int w = this.getWidth()-30;
        int maxLines = (this.getHeight()/fnt.getHeight())>>1;
        int ww=0;
        text = text.replace('\r',' ');
        if (maxLines<1) maxLines=1;
        int lineCounter=maxLines;

        lbl = new Label("");
        Style st = lbl.getStyle();
        Style st2 = lbl.getStyle();
        st.setFont(fnt);
        st.setPadding(0,0,0,0);
        st.setMargin(0,0,0,0);
        lbl = null;
        st2.setFgColor(0xFFFFFF);


        while (text.length()>0) {
            txt = text;

            // remove characters until we reach a valid width

            l=1;
            while ((l<txt.length())&&(txt.charAt(l-1)!='\n')&&(fnt.substringWidth(txt,0,l)<w)) l++;

            if (l>=txt.length()) {
                text="";
            } else
            if (txt.charAt(l-1)=='\n') {
                text = txt.substring(l);
                txt = txt.substring(0,l-1);
            } else
            if (fnt.substringWidth(txt,0,l)>=w) {
                iPos = txt.lastIndexOf((char)' ',l+1);  // search for ending space
                if (iPos<0) iPos = l;
                text = txt.substring(iPos).trim();
                txt = txt.substring(0,iPos);
            } else {
                text="";
            }

            lbl = new Label(txt);
            lineCounter++;
            if ((txt.trim().length()>0)&&((lineCounter>=maxLines)||(text.length()<1))) {
                lbl.setFocusable(true);
                lineCounter=0;
            }
            st = lbl.getStyle();
            st.setFont(fnt);
            st.setPadding(0,0,0,0);
            st.setMargin(0,0,0,0);
            lbl.setSelectedStyle(st2);
            if (alignTop) addComponent(BorderLayout.NORTH,lbl); else addComponent(lbl);
        }
    }

    // Called when a sub-modal form closes - the form passesd is the form that closed
    protected void subModalFormClose(ModalForm frm) {

    }

    // Used to show a modal form within a modal form.  This is the only way to do this!
    public void showSubModal(ModalForm fm) {
        nextModalForm = fm;

        synchronized(obLocker) {
            obLocker.notify();
        }
    }

    // Shows the dialog modally
    public void showModal() {
        modalresult = MR_NONE;
        moSystem.activeForm = this;
        show();
        this.show();


        synchronized(obLocker) {
            while (modalresult==MR_NONE) {
                try {
                    obLocker.wait(250);

                    if (nextModalForm!=null) {
                        nextModalForm.showModal();
                        subModalFormClose(nextModalForm);
                        nextModalForm = null;
                        show();  // return to this dialog
                        //System.gc();  // force the dialog out of memory ASAP! - Disabled, not recommended
                    }

                } catch (Exception e) {
                    displayMessage("Error",e.toString(),true);
                };
                
                if (modalresult!=MR_NONE) {
                    if (!canClose(modalresult)) modalresult=MR_NONE;
                }
            }
        }
    }

}
