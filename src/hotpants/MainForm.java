package hotpants;

import java.util.Calendar;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.util.Hashtable;
import java.util.Vector;

public class MainForm extends Form implements CommandListener {
    private StringItem textItem;
    private Command exit, addTotp, addHotp, delete, editTimeConfig;
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
        
        editTimeConfig = new Command("Time configuration", Command.SCREEN, 2);
        addCommand(editTimeConfig);
        
        textItem = new StringItem (null, "no entries");
        append(textItem);
        this.setCommandListener(this);
    }
    
    public void setEntries(Hashtable entries) {
        deleteAll();
        Enumeration keys = entries.keys();
        while (keys.hasMoreElements()) {
            entryItems.addElement(new EntryItem((Otp)entries.get(keys.nextElement()), midlet));
        }
        rebuildList();
    }
    
    public void addEntry(Otp e) {
        entryItems.addElement(new EntryItem(e, midlet));
        rebuildList();
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
    
    private void rebuildList() {
        deleteAll();
        sortEntries();
        for (int i = 0; i < entryItems.size(); i++) {
            addEntryToForm((EntryItem)entryItems.elementAt(i));
        }
    }
    
    private void addEntryToForm(EntryItem entry) {
            append(entry.item);        
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
            }
        }
        rebuildList();
    }
    
    // sort list alphabetically
    private void sortEntries() {
        Vector sortedEntries = new Vector(entryItems.size());
        while (entryItems.size() > 0) {
            EntryItem lowest = (EntryItem)entryItems.firstElement();
            for (int j = 1; j < entryItems.size(); j++) {
                EntryItem cmp = (EntryItem)entryItems.elementAt(j);
                if (lowest.entry.getId().compareTo(cmp.entry.getId()) > 0) {
                    lowest = cmp;
                }
            }
            sortedEntries.addElement(lowest);
            entryItems.removeElement(lowest);
        }
        entryItems = sortedEntries;
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
            midlet.showEntryForm();
        } 
        if (c == addHotp) {
            midlet.getEntryForm().newHotpEntry();
            midlet.showEntryForm();
        }
        if (c == editTimeConfig) {
            midlet.showTimeConfigForm();
        }
    }
}
