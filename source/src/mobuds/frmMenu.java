/*
 * This is the most important page - its the menu!
 */

package mobuds;

import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.RGBImage;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.util.Resources;

/**
 *
 * @author rob smith
 */
public class frmMenu extends ModalForm implements ActionListener, FocusListener  {
    private static final String[] iconNames = {"Who's Around","Profile","Visibility",baseconf.strFriends,baseconf.strBlocks,"MoMail","Matches","Settings","Sync","Pass On","Quit"};
    private Button[] btn = new Button[iconNames.length+1];
    public int selectedIcon;

    // focused
    public void focusGained(Component cmp) {
        for (int a=0; a<btn.length; a++)
            if (cmp==btn[a]) {
                selectedIcon = a;
                break;
            }
    }

    // focus lost
    public void focusLost(Component cmp) {
    }
    
    // Command running
    private void runCommand(int item) {
        selectedIcon = item;
        setModalResult(ModalForm.MR_OK);
    }

    // Action performed!
    public void actionPerformed(ActionEvent evt) {
        Button bt = (Button)evt.getSource();
        for (int a=0; a<btn.length; a++)
            if (btn[a]==bt) {
                runCommand(a);
                break;
            }
    }
    
    // Makes a kind of lightened image
    private Image makeGrayscale(Image img) {
        int[] im = img.getRGB();
        int a=0,r,g,b;
        for (a=0; a<im.length; a++) {
                r = ((im[a] & 0xFF0000)>>16)&0xFF;
                g = ((im[a] & 0xFF00)>>8)&0xFF;
                b = im[a] & 0xFF;
                r=Math.min((r*140)/100,255);
                g=Math.min((g*140)/100,255);
                b=Math.min((b*140)/100,255);

                im[a] = (im[a]&0xFF000000) | (r<<16) | (g<<8) | b;
        }
        return IndexedImage.pack(RGBImage.createImage(im,img.getWidth(),img.getHeight()));
    }

    // Constructor
    public frmMenu(MobudsSystem system) {
        super(system,"Mobuds Menu");

        selectedIcon=0;
        addModalCommand("Back",system.imgCross,MR_CANCEL,CMD_BACK);
        addModalCommand("Select",system.imgTick,MR_OK,CMD_DEFAULT);

        int width = Display.getInstance().getDisplayWidth(); //get the display width

        int elementWidth = 0;
        Image img,tmp;
        tmp = null;
        try {
            Resources resources = Resources.open("/Gfx/quick.res");
            tmp = resources.getImage("menu");
        } catch (Exception e) {}
        Style s;
        
        btn[iconNames.length] = null;

        for (int i = 0; i < iconNames.length; i++) {
            img = tmp.subImage(i*32,0,32,32,true);
            btn[i] = new Button(iconNames[i], img) {
                public Image getPressedIcon() {
                    Image i = getIcon();
                    int newx = (i.getWidth() * 8) / 10;
                    int newy = (i.getHeight() * 8) / 10;
                    return i.scaled(newx, newy);
                }
                public Image getRolloverIcon() {
                    Image i = getIcon();
                    int newx = (i.getWidth() * 9) / 10;
                    int newy = (i.getHeight() * 9) / 10;
                    return makeGrayscale(i.scaled(newx, newy));                
                }
            };
            s = btn[i].getStyle(); s.setMargin(3,3,3,3); s.setPadding(2,2,2,2); s.setBorder(null);
            s.setBgImage(null); s.setFgColor(0x909090); s.setBgTransparency(0);
            btn[i].setGap(6); btn[i].setAlignment(Label.CENTER); btn[i].setTextPosition(Label.BOTTOM);
            addComponent(btn[i]);
            btn[i].addActionListener(this);
            btn[i].addFocusListener(this);
            elementWidth = Math.max(Math.max(btn[i].getPreferredW(),10+s.getFont().stringWidth(iconNames[i])), elementWidth);
        }
        
        //if (moSystem.persData.bDebugMode)  {
            int i=iconNames.length;
            btn[i] = new Button("Debug", tmp.subImage((i-1)*32,0,32,32,true));
            s = btn[i].getStyle(); s.setMargin(3,3,3,3); s.setPadding(2,2,2,2); s.setBorder(null);
            s.setBgImage(null); s.setFgColor(0x909090); s.setBgTransparency(0); 
            btn[i].setGap(6); btn[i].setAlignment(Label.CENTER); btn[i].setTextPosition(Label.BOTTOM);
            addComponent(btn[i]);
            btn[i].addActionListener(this);
            btn[i].addFocusListener(this);            
       // }

        int cols = width / elementWidth;
        int rows = iconNames.length / cols;
        setLayout(new GridLayout(rows, cols));
    }
}