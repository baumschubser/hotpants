/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/

package hotpants;

import javax.microedition.rms.*;
import java.util.Hashtable;

public class Configuration {
    private RecordStore recordStore;
    private Hashtable entries;
    
    public Configuration() {
        entries = new Hashtable();
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                Entry e = Entry.fromBytes(recordStore.getRecord(re.nextRecordId()));
                    entries.put(e.getId(), e);
                    System.out.println("Read entry " + e.getId());
            }
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.out.println("Could not load configuration: " + e.getMessage());
        }
    }
    
    public Entry addEntry(Entry entry) {
        entries.put(entry.getId(), entry);
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            entry.setRecordStoreId((byte)recordStore.getNextRecordID());
            byte[] entryBytes = entry.toBytes();
            recordStore.addRecord(entryBytes, 0, entryBytes.length);
            recordStore.closeRecordStore();
            System.out.println("Entry "+ entry.getId() + " saved to record store.");
        } catch (RecordStoreException e) {
            System.out.println("Could not save entry " + entry.getId() + ": " + e.getMessage());
        }
        return entry;
    }
    
    public Entry addEntry(String id) {
        Entry entry = new Entry(id);
        entry.setSecret("secret");
        return addEntry(entry);
    }
    
    public Entry updateEntry(Entry e) {
        if (e == null || e.getRecordStoreId() == -1) return null;
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            byte[] entryBytes = e.toBytes();
            recordStore.setRecord(e.getRecordStoreId(), entryBytes, 0, entryBytes.length);
            recordStore.closeRecordStore();
            System.out.println("Entry "+ e.getId() + " updated to record store.");
        } catch (RecordStoreException ex) {
            System.out.println("Could not update entry " + e.getId() + ": " + ex.getMessage());
        }
        return e;
    }
    
    public void deleteEntry(int recordStoreId) {
        if (recordStoreId == -1) return;
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.deleteRecord(recordStoreId);
            recordStore.closeRecordStore();
        } catch (RecordStoreException ex) {
            System.out.println("Could not delete entry: " + ex.getMessage());
        }
    }
    
    public void flushConfiguration() {
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                recordStore.deleteRecord(re.nextRecordId());
            }
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.out.println("Could not flush configuration: " + e.getMessage());
        }
    }
    
    public Hashtable getEntries() {
        return entries;
    }
}
