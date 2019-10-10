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
    private Command exit, add, delete;
    private Midlet midlet;
    private Vector entryItems;
  
    public MainForm(String title, Midlet m){
        super(title);
        midlet = m;
        entryItems = new Vector();

        exit = new Command("Exit", Command.EXIT, 1);
        addCommand(exit);

        add = new Command("Add", Command.ITEM, 2);
        addCommand(add);
        
        textItem = new StringItem (null, "no entries");
        append(textItem);
        this.setCommandListener(this);
    }
    
    public void setEntries(Hashtable entries) {
        deleteAll();
        Enumeration keys = entries.keys();
        while (keys.hasMoreElements()) {
            addEntryItemToForm(new EntryItem((Entry)entries.get(keys.nextElement()), midlet));
        }
    }
    
    public void addEntry(Entry e) {
        if (size() == 1 && get(0) instanceof StringItem) {
            this.delete(0);
        }
        addEntryItemToForm(new EntryItem(e, midlet));
    }
    
    public void updateEntry(Entry e) {
        int itemId = findItemIdByRecordStoreId(e.getRecordStoreId());
        if (itemId == -1) return;
        
        EntryItem newItem = new EntryItem(e, midlet);
        newItem.setDefaultCommand(new EntryCommand("Edit", Command.OK, 2, newItem.getEntry()));
        newItem.addCommand(new EntryCommand("Delete", Command.STOP, 2, newItem.getEntry()));
        newItem.setItemCommandListener(newItem);
        this.set(itemId, newItem);
        System.out.println("Entry item " + e.getId() + " updated in form.");
    }
    
    public void deleteEntry(int recordStoreId) {
        int itemId = findItemIdByRecordStoreId(recordStoreId);
        if (itemId == -1) return;
        this.delete(itemId);
        if (this.size() == 0) append(textItem);
    }
    
    private int findItemIdByRecordStoreId(int recordStoreId) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i) instanceof EntryItem) {
                Entry itemEntry = ((EntryItem)this.get(i)).getEntry();
                if (itemEntry.getRecordStoreId() == recordStoreId) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private void addEntryItemToForm(EntryItem e) {
        entryItems.addElement(e);
        append(e);
        e.setDefaultCommand(new EntryCommand("Edit", Command.OK, 2, e.getEntry()));
        e.addCommand(new EntryCommand("Delete", Command.STOP, 2, e.getEntry()));
        e.setItemCommandListener(e);
    }
    
    public Form getForm() {
        return (Form)this;
    }
    
    public void updateAllEntries(Calendar cal) {
        System.out.println("Update");
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i) instanceof EntryItem) {
                ((EntryItem)get(i)).update(cal);
            }
        }
    }
    
    public void commandAction(Command c, Displayable d){
        if(c.getCommandType() == Command.EXIT){ // Exit
            midlet.notifyDestroyed(); 
        } else if (c.getCommandType() == Command.ITEM) { // Add
            midlet.showEntryForm();
        } else if (c.getCommandType() == Command.OK || c.getCommandType() == Command.STOP) {
            EntryCommand e = (EntryCommand)c;
            System.out.println("Command received: " + e.getEntry().getId());
        } 
    }
}
