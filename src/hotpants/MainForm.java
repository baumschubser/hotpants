/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/
package hotpants;

import java.util.Calendar;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.util.Hashtable;
import java.util.Vector;

public class MainForm extends Form implements CommandListener {
    private StringItem textItem;
    private Command exit, addTotp, addHotp, delete;
    private Midlet midlet;
    private Vector entryItems;
  
    public MainForm(String title, Midlet m){
        super(title);
        midlet = m;
        entryItems = new Vector();

        exit = new Command("Exit", Command.EXIT, 1);
        addCommand(exit);

        addTotp = new Command("Add TOTP", Command.ITEM, 2);
        addCommand(addTotp);
        
        addHotp = new Command("Add HOTP", Command.ITEM, 2);
        addCommand(addHotp);
        
        textItem = new StringItem (null, "no entries");
        append(textItem);
        this.setCommandListener(this);
    }
    
    public void setEntries(Hashtable entries) {
        deleteAll();
        Enumeration keys = entries.keys();
        while (keys.hasMoreElements()) {
            addEntryItemToForm(new EntryItem((Otp)entries.get(keys.nextElement()), midlet));
        }
    }
    
    public void addEntry(Otp e) {
        if (entryItems.size() == 0) {
            this.deleteAll();
        }
        addEntryItemToForm(new EntryItem(e, midlet));
    }
    
    public void deleteEntry(EntryItem item) {
        for (int i = 0; i < size(); i++) {
            if (get(i) == item.getItem()) {
                int recId = item.getOtp().getRecordStoreId();
                delete(i);
                entryItems.removeElement(item);
                midlet.getConfiguration().deleteEntry(recId);
                break;
            }
        }
        if (this.size() == 0) append(textItem);
    }
    
    private void addEntryItemToForm(EntryItem e) {
        entryItems.addElement(e);
        Item item = e.getItem();
        append(item);
        int otypType = e.getOtp().getOtpType();
        item.setDefaultCommand(new EntryCommand("Edit", Command.OK, 2, e.getOtp(), otypType));
        item.addCommand(new EntryCommand("Delete", Command.STOP, 3, e.getOtp(), otypType));
        if (Configuration.HOTP == otypType) {
            item.setDefaultCommand(new EntryCommand("New PIN", Command.SCREEN, 1, e.getOtp(), otypType));
        }
        item.setItemCommandListener(e);
    }
    
    public Form getForm() {
        return (Form)this;
    }
    
    public void updateEntry(Otp e) {
        if (e == null) return;
        for (int i = 0; i < entryItems.size(); i++) {
            EntryItem item = (EntryItem)entryItems.elementAt(i);
            if (item.getOtp().getRecordStoreId() == e.getRecordStoreId()) {
                item.setOtp(e);
                //EntryItem newItem = new EntryItem(e, midlet);
                //entryItems.setElementAt(newItem, i);
                //this.set(i, newItem.getItem());
            }
        }
    }
    
    public void refreshAllEntries(Calendar cal) {
        for (int i = 0; i < entryItems.size(); i++) {
            // Only affects TOTP entries
            ((EntryItem)entryItems.elementAt(i)).update(cal);
        }
    }
    
    public void commandAction(Command c, Displayable d){
        if(c == exit) {
            midlet.notifyDestroyed(); 
        }
        if (c == addTotp) {
            midlet.getEntryForm().newTotpEntry();
        } 
        if (c == addHotp) {
            midlet.getEntryForm().newHotpEntry();
        }
        midlet.showEntryForm();
    }
}
