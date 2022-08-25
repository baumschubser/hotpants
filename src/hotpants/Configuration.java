package hotpants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.microedition.rms.*;
import java.util.Hashtable;

public class Configuration {
    private RecordStore recordStore;
    private Hashtable entries;
    public static final byte DELIM = 127;
    public static final byte TOTP = 1;
    public static final byte HOTP = 2;
    public static final byte TimeConfig = 3;
    private int timeOffset = 0;
    private int offsetRecordId = -1;
    
    public Configuration() {
        entries = new Hashtable();
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, false);
            RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                int recId = re.nextRecordId();
                byte[] record = recordStore.getRecord(recId);
                record[record.length-1] = (byte)recId;
                int type = getEntryTypeFromRecordBytes(record);
                if (Configuration.TOTP == type) {
                    TotpEntry e = TotpEntry.fromBytes(record);
                    entries.put(e.getId(), e);
                } else if (Configuration.HOTP == type) {
                    HotpEntry e = HotpEntry.fromBytes(record);
                    entries.put(e.getId(), e);
                    System.out.println("Read entry " + e.getId());
                } else if (Configuration.TimeConfig == type) {
                    //setTimeOffset(record);
                }
            }
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            System.out.println("Could not load configuration: " + e.getMessage());
        }
    }
    
    private void setTimeOffset(byte[] record) {
        ByteArrayOutputStream b_id_baos = new ByteArrayOutputStream();
        DataOutputStream b_id = new DataOutputStream(b_id_baos);
        ByteArrayOutputStream b_sec_baos = new ByteArrayOutputStream();
        DataOutputStream b_sec = new DataOutputStream(b_sec_baos);

        int section = 0;
        try {
            for (int i = 0; i < record.length; i++) {
                if (Configuration.DELIM == record[i]) {
                    section++;
                    if (section == 1)
                        offsetRecordId = ByteBuffer.wrap(b_id_baos.toByteArray()).getInt();
                    continue;
                }
                switch (section) {
                    case 0:
                        b_id.write(record[i]);
                        break;
                    case 4:
                        b_sec.write(record[i]);
                }
            }
            timeOffset = ByteBuffer.wrap(b_sec_baos.toByteArray()).getInt();
            return;
        } catch (IOException e) {
            System.out.println("Could not read offset seconds. No more memory?");
        }
        timeOffset = 0;
        offsetRecordId = -1;
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
        entry.setRecordStoreId(recId);
        entries.put(entry.getId(), entry);
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
            System.out.println("Could not save entry: " + e.getMessage());
        }
        return recordStoreId;
    }
        
    public Otp updateEntry(Otp e) {
        if (e == null || e.getRecordStoreId() == -1) return null;
        updateEntryByte(e.getRecordStoreId(), e.toBytes());
        return e;
    }
    
    public void updateOffsetSeconds(int seconds) {
        timeOffset = seconds;
        byte[] offsetSecondsArray = ByteBuffer.allocateDirect(4).putInt(timeOffset).array();
        
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            if (offsetRecordId == -1)
                offsetRecordId = recordStore.getNextRecordID();
            recordStore.setRecord(offsetRecordId, offsetSecondsArray, 0, offsetSecondsArray.length);
        } catch (RecordStoreException e) {
            System.err.println("Could not update offset seconds. " + e.getMessage());
        }
    }
    
    public int getOffsetSeconds() {
        return timeOffset;
    }
    
    private void updateEntryByte(int recordStoreId, byte[] entryBytes) {
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            recordStore.setRecord(recordStoreId, entryBytes, 0, entryBytes.length);
            recordStore.closeRecordStore();
        } catch (RecordStoreException ex) {
            System.out.println("Could not update entry: " + ex.getMessage());
        }
    }
    
    public void deleteEntry(int recordStoreId) {
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
            recordStore.deleteRecord(recordStoreId);
            recordStore.closeRecordStore();
        } catch (RecordStoreException ex) {
            System.out.println("Could not delete entry: " + ex.getMessage());
        }
    }
    
    public void flushConfiguration() {
        try {
            recordStore = RecordStore.openRecordStore("Hotpants",true);
            recordStore.setMode(RecordStore.AUTHMODE_PRIVATE, true);
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
