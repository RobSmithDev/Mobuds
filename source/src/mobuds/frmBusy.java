/*
 * frmBusy.java
 *
 * Created on 04 January 2009, 21:32
 *
 * A busy form, not like the rest of the normal screens
 */

package mobuds;

import com.sun.lwuit.*;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.util.Resources;


/**
 *
 * @author rob smith
 */
public class frmBusy extends Form implements Painter {   
    private static final int ANIM_TIMEPERIOD = 40;
    private Image logo;
    private StaticAnimation spinner;
    private MobudsSystem mosystem;
    private int progressMax;
    private int progressPos;
    private Image imgAdvert;
    private int i;
    private long currentTime;
    private String overridedText = "";

    public void changeText(String overrideText) {
        overridedText = overrideText;
        imgAdvert = null;
    }

    /** Creates a new instance of frmBusy */
    public frmBusy(MobudsSystem system) {
        super("");
        
        progressMax = 2;
        progressPos = 2;
        imgAdvert = null;
        
        currentTime = System.currentTimeMillis();
        
        // setup
        mosystem = system;
        
        // load image logo and animation
        try {
            Resources busyResources = Resources.open("/Gfx/busy.res");
            logo =  busyResources.getImage("logo");
            spinner =  busyResources.getAnimation("spinner");
            imgAdvert =  busyResources.getImage("busy");
        } catch (Exception e) {}
        
        // Set the painter
        this.setGlassPane(this);
        this.registerAnimated(this);
    }
    
    // New graphic loaded, this will be an advert.  If we fail to load it, we return FALSE
    public boolean changeAdvertGraphic(byte[] newAdvert) {        
        try {
            imgAdvert = Image.createImage(newAdvert,0,newAdvert.length);
            invalidate();
        } catch (Exception e) {
            imgAdvert = null;
        }
        
        return (imgAdvert!=null);
    }
        
    // set the current position and maximum
    public void setProgress(int pos,int max) {
        progressMax = max;
        progressPos = pos;
        invalidate();
    }
    
    // animate thing
    public boolean animate() {
        boolean b = spinner.animate();
        if (System.currentTimeMillis()-currentTime>ANIM_TIMEPERIOD) b=true;
        return b;
    }
    
    // amount of space left for an advert
    public int getImageSpace() {
            int y1 = 0;
            int y2 = this.getHeight()-40;
            
            if (logo!=null) y1+=10+logo.getHeight()+getStyle().getFont().getHeight();
            if (spinner!=null) y2-=spinner.getHeight()+10;
            
            return y2-y1;
    }
    
    // Background painter class
    public void paint(Graphics g, Rectangle rect) {
            String s;
            int y;
            
            // Draw the logo on the screen            
            y = 10;
            if (logo!=null) {
                    g.drawImage(logo,(this.getWidth()>>1) - (logo.getWidth()>>1),y);
                    y+=logo.getHeight();
            }
            s = "...Please Wait...";
            if (overridedText.length()>0) s=overridedText;
            g.setColor(0xC0C0C0);
            g.drawString(s,(this.getWidth()>>1)-(g.getFont().stringWidth(s)>>1),y);
            
            // Draw the spinning animation
            if (spinner!=null) {
                    y=this.getHeight() - (spinner.getHeight()+30);
                    g.drawImage(spinner,(this.getWidth()>>1) - (spinner.getWidth()>>1),y);                
            }
            // message at bottom of screen
            y = this.getHeight();

            int x,w,nw,h;
            // Draw the progress bar
            x = rect.getX()+10;
            h = 10;
            w = getWidth()-20;
            y = getHeight()-20;
            g.setColor(0xC0C0C0);
            g.drawRect(x,y,w,h);
            g.setColor(0x82ABB3);
            nw = ((w-1)*progressPos)/progressMax;
            if (nw<1) nw=1;
            if (nw>w-1) nw=w-2;
            g.fillRect(x+1,y+1,nw,h-1);

            if (System.currentTimeMillis()-currentTime>ANIM_TIMEPERIOD) {
                w = (int)((System.currentTimeMillis()-currentTime)/ANIM_TIMEPERIOD)*4;
                if (nw>0) i=(i+w)%nw; else i=0;
                currentTime = System.currentTimeMillis();
            }

            int rr,gg,bb,e,c;
            int xx = i;
            for (int ii=0; ii<30; ii++) {
                if (ii<15) c=ii; else c=30-ii;
                e = 15-c;
                rr = ((0x82 * e) + (0xC2 * c))/15;
                gg = ((0xAB * e) + (0xEB * c))/15;
                bb = ((0xB3 * e) + (0xF3 * c))/15;
                g.setColor( (rr << 16 ) | (gg << 8) | bb );

                g.drawLine(xx+x+1,y+1,xx+x+1,y+h-1);
                xx = (xx+1)%nw;
            }

            y = 10+g.getFont().getHeight();
            if (logo!=null) y+=10+logo.getHeight();
            if (imgAdvert!=null) {
                h = getImageSpace();
                y+= (h>>1)-(imgAdvert.getHeight()>>1);
                g.drawImage(imgAdvert,(this.getWidth()>>1) - (imgAdvert.getWidth()>>1),y);
            }
    }    
}
