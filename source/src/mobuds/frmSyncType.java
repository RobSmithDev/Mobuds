/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobuds;

import com.sun.lwuit.*;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.util.Resources;

/**
 * @author rob smith
 */
public class frmSyncType extends ModalForm implements ActionListener {

    private Button btnBackup;
    private Button btnRestore;

    // Handle an action being ran
    public void actionPerformed(ActionEvent evt)  {
        if (evt.getSource()==btnBackup) this.setModalResult(ModalForm.MR_OK);
        if (evt.getSource()==btnRestore) this.setModalResult(ModalForm.MR_QUIT);
    }

    // Coonstructor
    public frmSyncType(MobudsSystem system) {
        super(system,"Sync Profile");

        Image syncImg = null;

        try {
            Resources resources = Resources.open("/Gfx/quick.res");
            syncImg = resources.getImage("syncgfx");
            resources = null;
        } catch (Exception e) {};

        Graphics gfx;

        Image toNet = Image.createImage(55,16);
        gfx = toNet.getGraphics();
        gfx.setColor(0x00FF00);
        gfx.fillRect(0,0,toNet.getWidth(),toNet.getHeight());  
        gfx.drawImage(syncImg.subImage(0,0,16,16,true),0,0);
        gfx.drawImage(syncImg.subImage(16,0,16,16,true),39,0);
        gfx.drawImage(syncImg.subImage(32,0,12,16,true),22,1);
        toNet=toNet.modifyAlpha((byte)0xFF,0x00FF00);

        Image fromNet = Image.createImage(55,16); 
        gfx = fromNet.getGraphics();
        gfx.setColor(0x00FF00);
        gfx.fillRect(0,0,toNet.getWidth(),toNet.getHeight());
        gfx.drawImage(syncImg.subImage(0,0,16,16,true),39,0);
        gfx.drawImage(syncImg.subImage(16,0,16,16,true),0,0);
        gfx.drawImage(syncImg.subImage(43,0,12,16,true),21,1);
        fromNet=fromNet.modifyAlpha((byte)0xFF,0x00FF00);

        addModalCommand("Close",system.imgCross,MR_CANCEL,CMD_BACK);

        addInfoLine("What would you like to do?");
        addInfoLine(" ");
        startNewSection("Copy to Web");
        addInfoLine("Backup profile and transfer tracking data to the Mobuds website.");

        addInfoLine(" ");
        btnBackup = new Button("Backup",toNet);
        btnBackup.setGap(15);
        btnBackup.addActionListener(this);
        addComponent(btnBackup);

        addInfoLine(" ");
        startNewSection("Copy from Web");
        addInfoLine("Download profile from the Mobuds website (overwrites all settings)");
        addInfoLine(" ");
        btnRestore = new Button("Restore",fromNet);
        btnRestore.setGap(15);
        btnRestore.addActionListener(this);
        addComponent(btnRestore);

    }
}
