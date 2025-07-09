/*
 * A basic popup thing with no specific options
 * and open the template in the editor.
 */

package mobuds;

import java.util.*;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;

/**
 *
 * @author rob smith
 */
/*
 * Popup dialog for actions with people
 */

public class dlgOptions extends Dialog implements ActionListener,FocusListener {
    private MobudsSystem moSystem;

    private int selectedCommand;
    private Vector buttons = new Vector();

    // change the selection
    public void setSelection(int newIndex) {
        selectedCommand = newIndex;
        if ((newIndex>=0)&&(newIndex<buttons.size())) {
            this.scrollComponentToVisible((Component)buttons.elementAt(newIndex));
            this.setFocused((Component)buttons.elementAt(newIndex));
        }
    }

    // Add an option
    public void addOption(String txt) {
        Button b3 = new Button(" "+txt,moSystem.imgDot);
        Style st = b3.getStyle();
        st.setMargin((buttons.size()<1)?6:0,0,5,5);
        st.setBgImage(moSystem.horizSeperatorLine);
        if (moSystem.horizSeperatorLine==null) st.setBgColor(moSystem.horizSeperatorLineDefColor);
        st.setFgColor(0x000000);
        st.setPadding(3,3,5,5);
        b3.setPressedStyle(st);
        b3.setSelectedStyle(st);
        addComponent(b3);
        buttons.addElement(b3);
        b3.addActionListener(this);
        b3.addFocusListener(this);
    }

    // Prepare dialog
    public dlgOptions(MobudsSystem moSys, String sTitle) {
        super(sTitle);
        moSystem=moSys;

        setDialogType(Dialog.TYPE_CONFIRMATION);
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        setScrollable(true);
        setTransitionInAnimator(moSystem.getTransition(true));
        setTransitionOutAnimator(moSystem.getTransition(false));
    }

    // Show the dialog
    public int showOptions() {
        showPacked(BorderLayout.CENTER,true);
        return selectedCommand;
    }

    // Respond to buttons
    public void actionPerformed(ActionEvent evt) {
        selectedCommand = buttons.indexOf(evt.getSource());

        if ((selectedCommand>=0) && (selectedCommand<buttons.size())) dispose();
    }

    // focused
    public void focusGained(Component cmp) {
        selectedCommand = buttons.indexOf(cmp);
        if ((selectedCommand>=0) && (selectedCommand<buttons.size())) {
            for (int a=0; a<buttons.size(); a++)
                ((Button)buttons.elementAt(a)).setIcon((a==selectedCommand)?moSystem.imgDot:moSystem.imgUnDot);
        }
    }

    // focus lost
    public void focusLost(Component cmp) {
    }
}

