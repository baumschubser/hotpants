/*

 */

package hotpants;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.ItemCommandListener;

public class ConfirmationDialog implements CommandListener {
    private Alert yesNoAlert;
    private boolean status;
    private EntryItem entryItem; // to call back
    private Midlet midlet;
    
    public ConfirmationDialog(Midlet midlet, EntryItem entryItem, String title, String text) {
        yesNoAlert = new Alert(title);
        yesNoAlert.setString(text);
        yesNoAlert.addCommand(entryItem.getNoCommand());
        yesNoAlert.addCommand(entryItem.getYesCommand());
        yesNoAlert.setCommandListener(this);
        status = false;
        this.entryItem = entryItem;
        this.midlet = midlet;
    }
    
    public Displayable getDisplayable() {
        return yesNoAlert;
    }

    public boolean getStatus() {
        return status;
    }

    public void commandAction(Command c, Displayable d) {
        status = c.getCommandType() == Command.OK;
        entryItem.commandAction(c, entryItem.getItem());
    } 
}
