/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/
package hotpants;

import javax.microedition.lcdui.Command;


public class EntryCommand extends Command {
    Entry entry;
    
    public EntryCommand(String label, int commandType, int priority) {
        super(label, commandType, priority);
    }
    
    public EntryCommand(String label, int commandType, int priority, Entry e) {
        super(label, commandType, priority);
        entry = e;
    }
    
    public Entry getEntry() {
        return entry;
    }
}
