/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/
package hotpants;

import javax.microedition.lcdui.Command;


public class EntryCommand extends Command {
    Otp entry;
    
    public EntryCommand(String label, int commandType, int priority) {
        super(label, commandType, priority);
    }
    
    public EntryCommand(String label, int commandType, int priority, Otp e, int entryType) {
        super(label, commandType, priority);
        entry = e;
    }
    
    public Otp getEntry() {
        return entry;
    }
    
    public int getEntryType() {
        return entry != null ? entry.getOtpType() : -1;
    }
}
