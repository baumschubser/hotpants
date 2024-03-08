package hotpants;

import java.util.Calendar;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import java.util.Hashtable;
import java.util.Vector;

public class MainForm extends Form implements CommandListener {
    private final StringItem textItem;
    private final Command exit, addTotp, addHotp, editTimeConfig, scanQRcode;
    private final Midlet midlet;
    private Vector entryItems;
  
    public MainForm(String title, Midlet m){
        super(title);
        midlet = m;
        entryItems = new Vector();
        
        scanQRcode = new Command("Scan code", Command.OK, 1);
        addCommand(scanQRcode);

        exit = new Command("Exit", Command.EXIT, 1);
        addCommand(exit);

        addTotp = new Command("Add TOTP", Command.SCREEN, 2);
        addCommand(addTotp);
        
        addHotp = new Command("Add HOTP", Command.SCREEN, 2);
        addCommand(addHotp);
        
        editTimeConfig = new Command("Time configuration", Command.SCREEN, 2);
        addCommand(editTimeConfig);
        
        textItem = new StringItem (null, "no entries");
        append(textItem);
        this.setCommandListener(this);
    }
    
    public void updateEntriesFromConfig() {
        entryItems.removeAllElements();
        Hashtable entries = Configuration.getInstance().getEntries();
        Enumeration keys = entries.keys();
        while (keys.hasMoreElements()) {
            entryItems.addElement(new EntryItem((Otp)entries.get(keys.nextElement()), midlet));
        }
        rebuildList();
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
    
    // sort list alphabetically
    private void sortEntries() {
        Vector sortedEntries = new Vector(entryItems.size());
        while (entryItems.size() > 0) {
            EntryItem lowest = (EntryItem)entryItems.firstElement();
            for (int j = 1; j < entryItems.size(); j++) {
                EntryItem cmp = (EntryItem)entryItems.elementAt(j);
                if (lowest.entry.getLabel().compareTo(cmp.entry.getLabel()) > 0) {
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
            midlet.getEntryForm().setEntry(new TotpEntry(""));
            midlet.showEntryForm();
        } 
        if (c == addHotp) {
            midlet.getEntryForm().setEntry(new HotpEntry(""));
            midlet.showEntryForm();
        }
        if (c == editTimeConfig) {
            midlet.showTimeConfigForm();
        }
        if (c == scanQRcode) {
            midlet.showScanForm();
        }
    }
}
