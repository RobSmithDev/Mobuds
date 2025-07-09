/*
 * frmSplash.java
 *
 * Created on 28 December 2008, 21:04
 *
 * This is a simple splash screen form shown during application startup
 */

package mobuds;

import com.sun.lwuit.*;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.util.Resources;

/**
 *
 * @author rob smith
 */
public class frmSplash extends Form implements Painter {    
    public Image logo;
    private StaticAnimation spinner;
    private MobudsSystem mosystem;
    private String verstr;
    private int copyyear;
    
    // animate thing
    public boolean animate() {
        return spinner.animate();
    }
    
    // Background painter class
    public void paint(Graphics g, Rectangle rect) {
            String s;
            int y;
            
            // Draw the logo on the screen            
            if (logo!=null) {
                    y = (this.getHeight() >> 2) - (logo.getHeight()>>1);
                    if (y<0) y=0;
                    g.drawImage(logo,(this.getWidth()>>1) - (logo.getWidth()>>1),y);
                    g.setColor(0xA0A0A0); 
                    s = "v"+verstr;   g.drawString(s,((this.getWidth()>>1)-(g.getFont().stringWidth(s)>>1))+10,y+logo.getHeight()-7);
            }
            
            // Draw the spinning animation
            if (spinner!=null) {
                    y=(this.getHeight() >> 1)+10;
                    
                    g.drawImage(spinner,(this.getWidth()>>1) - (spinner.getWidth()>>1),y);                
            }
            
            // message at bottom of screen     
            g.setColor(0x707070); 
            y = (this.getHeight()-(g.getFont().getHeight()*3))-2;
            s = "Copyright (C) 2006-"+((copyyear+"").substring(2));     g.drawString(s,(this.getWidth()>>1)-(g.getFont().stringWidth(s)>>1),y);
            g.setColor(0x606060); y+=g.getFont().getHeight();
            s = "Crave Create Ltd";       g.drawString(s,(this.getWidth()>>1)-(g.getFont().stringWidth(s)>>1),y);
            g.setColor(0x505050); y+=g.getFont().getHeight();
            s = "Patent Pending";       g.drawString(s,(this.getWidth()>>1)-(g.getFont().stringWidth(s)>>1),y);
    }
    
    // Instance of our splash screen
    public frmSplash(MobudsSystem system) {
        super("");

        // Copy year min 2009
        java.util.Calendar d = java.util.Calendar.getInstance();
        d.setTime(new java.util.Date());
        copyyear = d.get(java.util.Calendar.YEAR);
        if (copyyear<2009) copyyear=2009;
        
        // setup
        mosystem = system;
        verstr = mosystem.getVersion();
        
        // load image logo and animation
        try {
            Resources busyResources = Resources.open("/Gfx/busy.res");
            logo = busyResources.getImage("logo");
            spinner = busyResources.getAnimation("spinner");
            busyResources = null;
        } catch (Exception e) {}
        
        // Set the painter
        this.setGlassPane(this);
        
        this.registerAnimated(this);
    }
    
}
