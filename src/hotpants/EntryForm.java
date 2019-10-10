/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/
package hotpants;

import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.util.Hashtable;
import java.util.Vector;

public class EntryForm extends Form implements CommandListener {
    private TextField idItem, secretItem, refreshSecondsItem, digitCountItem;
    private Command cancel, save;
    private Midlet midlet;
    private Entry entry;
    private Alert alert;
    
    public EntryForm(String title, Midlet m){
        super(title);
        
        midlet = m;
        
        alert = new Alert("Error");
        alert.setTimeout(2000);
        
        cancel = new Command("Cancel", Command.CANCEL, 1);
        addCommand(cancel);

        save = new Command("Save", Command.ITEM, 2);
        addCommand(save);
        
        idItem = new TextField("Name", "", 80, TextField.ANY);
        append(idItem);
        
        secretItem = new TextField("Secret", "", 126, TextField.ANY);
        append(secretItem);
        
        refreshSecondsItem = new TextField("Token period in seconds", "30", 3, TextField.NUMERIC);
        append(refreshSecondsItem);
        
        digitCountItem = new TextField("Number of digits", "6", 2, TextField.NUMERIC);
        append(digitCountItem);        

        this.setCommandListener(this);
    }
    
    public void setEntry(Entry e) {
        entry = e;
        idItem.setString(e.getId());
        secretItem.setString(e.getSecret());
        refreshSecondsItem.setString(Integer.toString(e.getRefreshSeconds()));
        digitCountItem.setString(Integer.toString(e.getDigitCount()));
    }
        
    public Form getForm() {
        defaultValues();
        return (Form)this;
    }
    
    private void defaultValues() {
        idItem.setString("");
        secretItem.setString("");
        digitCountItem.setString("6");
        refreshSecondsItem.setString("30");
        entry = null;
    }

    public void commandAction(Command c, Displayable d){
        if(c.getCommandType() == Command.CANCEL){
            defaultValues();
            midlet.showMainForm();
        } else if (c.getCommandType() == Command.ITEM) {
            if (alertSanitation()) {
                if (entry == null) { // new entry
                    midlet.getMainForm().addEntry(midlet.getConfiguration().addEntry(entryFromInputs()));
                } else { // update
                    midlet.getMainForm().updateEntry(midlet.getConfiguration().updateEntry(entryFromInputs()));
                }
                defaultValues();
                midlet.showMainForm();
            }
        }
    }
    
    private boolean alertSanitation() {
        boolean ok = true;
        if (idItem.getString().length() == 0) {
            alert.setString("Name must not be empty");
            ok = false;
        } else if (secretItem.getString().length() < 5) {
            alert.setString("Secret must be 5 characters or more");
            ok = false;
        } 
        if (ok) {
            try {
                int refrSec = Integer.parseInt(refreshSecondsItem.getString());
                if (refrSec < 1) {
                    alert.setString("Interval must be at least 1 second");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                alert.setString("Interval must be an integer");
                ok = false;
            }
        }
        if (ok) {
            try {
                int digCount = Integer.parseInt(digitCountItem.getString());
                if (digCount < 1) {
                    alert.setString("Number of digits must be at least 6");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                alert.setString("Number of digits must be an integer");
                ok = false;
            }
        }
        if (!ok) {
            Display.getDisplay(midlet).setCurrent(alert, this);
        }
        return ok;
    }

    
    private Entry entryFromInputs() {
        Entry e = new Entry(idItem.getString());
        e.setId(idItem.getString());
        e.setDigitCount((byte)Integer.parseInt(digitCountItem.getString()));
        e.setRefreshSeconds((byte)Integer.parseInt(refreshSecondsItem.getString()));
        e.setSecret(secretItem.getString());
        if (entry != null) e.setRecordStoreId((byte)entry.getRecordStoreId());
        return e;
    }
}
