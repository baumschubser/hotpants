package hotpants;

import javax.microedition.rms.*;
import java.util.Hashtable;

public class Configuration {
    private static Configuration instance = null;

    private RecordStore recordStore;
    private Hashtable entries;
    public static final byte DELIM = 127;
    public static final byte TOTP = 1;
    public static final byte HOTP = 2;
    public static final byte TimeConfig = 3;
    private byte timeOffset = 0;
    private int offsetRecordId = -1;
    
    private Configuration() {
        entries = new Hashtable();
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, false);
            RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                byte[] record = re.nextRecord();
                int type = getEntryTypeFromRecordBytes(record);
                if (Configuration.TOTP == type) {
                    TotpEntry e = TotpEntry.fromBytes(record);
                    entries.put(new Integer(e.getRecordStoreId()), e);
                } else if (Configuration.HOTP == type) {
                    HotpEntry e = HotpEntry.fromBytes(record);
                    entries.put(new Integer(e.getRecordStoreId()), e);
                } else if (Configuration.TimeConfig == type) {
                    setTimeOffset(record);
                }
            }
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.err.println("Could not load configuration: " + e.getMessage());
        }
    }
    
    public static Configuration getInstance() {
        if (instance == null) instance = new Configuration();
        return instance;
    }
    
    private void setTimeOffset(byte[] record) {
        if (record.length != 9) {
            System.err.println("Corrupt time offset configuration entry.");
            return;
        }
        timeOffset = record[6];
        offsetRecordId = record[8];
        System.out.println("Time offset from config: " + timeOffset);
    }
    
    private int getEntryTypeFromRecordBytes(byte[] record) {
        int count = 0;
        for (int i = 0; i < record.length; i++) {
                if (count == 2) {
                    if (Configuration.HOTP == record[i]) return Configuration.HOTP;
                    if (Configuration.TOTP == record[i]) return Configuration.TOTP;
                    if (Configuration.TimeConfig == record[i]) return Configuration.TimeConfig;
                    System.err.println("Could not determine entry type");
                    return -1;
                }
                if (Configuration.DELIM == record[i]) {
                    count++;
                }
        }
        System.err.println("Record corrupt");
        return -1;
    }
    
    public Otp addEntry(Otp entry) {
        byte recId = addEntryToRecordStore(entry.toBytes());
        System.out.println("Adding entry " + recId);
        entry.setRecordStoreId(recId);
        entries.put(new Integer(recId), entry);
        return entry;
    }

    public byte addEntryToRecordStore(byte[] entrybytes) {
        byte recordStoreId = -1;
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            recordStoreId = (byte)recordStore.getNextRecordID();
            recordStore.addRecord(entrybytes, 0, entrybytes.length);
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.err.println("Could not save entry: " + e.getMessage());
        }
        return recordStoreId;
    }
        
    public Otp updateEntry(Otp e) {
        System.out.println("Updating entry " + e.getRecordStoreId());
        if (e == null || e.getRecordStoreId() == -1) return null;
        updateEntryByte(e.getRecordStoreId(), e.toBytes());
        entries.put(new Integer(e.getRecordStoreId()), e);
        return e;
    }
    
    public void updateOffsetSeconds(byte seconds) {
        try {
            timeOffset = seconds;
            byte[] record = new byte[9];
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            boolean isNew = offsetRecordId == -1;
            if (isNew)
                offsetRecordId = recordStore.getNextRecordID();

            record[0] = 0;
            record[1] = DELIM;
            record[2] = 0;
            record[3] = DELIM;
            record[4] = TimeConfig;
            record[5] = DELIM;
            record[6] = seconds;
            record[7] = DELIM;
            record[8] = (byte)offsetRecordId;
            
            if (isNew)
                offsetRecordId = recordStore.addRecord(record, 0, record.length);
            else
                recordStore.setRecord(offsetRecordId, record, 0, record.length);
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.err.println("Could not update offset seconds. " + e.getMessage());
        }
    }
    
    public byte getOffsetSeconds() {
        return timeOffset;
    }
    
    private void updateEntryByte(int recordStoreId, byte[] entryBytes) {
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            recordStore.setRecord(recordStoreId, entryBytes, 0, entryBytes.length);
            recordStore.closeRecordStore();
        } catch (RecordStoreException ex) {
            System.err.println("Could not update entry: " + ex.getMessage());
        }
    }
    
    public void deleteEntry(int recordStoreId) {
        System.out.println("Deleting entry " + recordStoreId);
        entries.remove(new Integer(recordStoreId));
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            recordStore.deleteRecord(recordStoreId);
            recordStore.closeRecordStore();
        } catch (RecordStoreException ex) {
            System.err.println("Could not delete entry: " + ex.getMessage());
        }
    }
    
    public Hashtable getEntries() {
        return entries;
    }
}
