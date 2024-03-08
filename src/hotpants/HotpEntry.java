package hotpants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HotpEntry implements Otp {
    private byte recordStoreId;
    private String secret;
    private String label;
    private int counter;
    
    public int getOtpType() {
        return Configuration.HOTP;
    }
    
    public HotpEntry(byte[] id, byte[] secret, int counter, byte recordStoreId) {
        this.label = new String(id);
        this.secret = new String(secret);
        this.counter = counter;
        this.recordStoreId = recordStoreId;
    }
    
    public HotpEntry(String id) {
        counter = 0;
        secret = "";
        this.label = id;
        recordStoreId = -1;
    }
    
    public int getCounter() {
        return counter;
    }
    
    public void setCounter(int d) {
        counter = d;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String s) {
        secret = s;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String i) {
        label = i;
    }
    
    public int getRecordStoreId() {
        return recordStoreId;
    }
    
    public void setRecordStoreId(byte n) {
        recordStoreId = n;
    }
    
    public void nextPin() {
        counter += 1;
    }
    
    public byte[] toBytes() {
        byte[] b_label = label.getBytes();
        byte[] b_secret = secret.getBytes();
        byte[] ret = new byte[b_label.length + b_secret.length + 10];
        int count = 0;
        
        // Store id
        System.arraycopy(b_label, 0, ret, count, b_label.length);
        count += b_label.length;
        ret[count++] = Configuration.DELIM;
        
        //Store secret
        System.arraycopy(b_secret, 0, ret, count, b_secret.length);
        count += b_secret.length;
        ret[count++] = Configuration.DELIM;
        
        // Store EntryType
        ret[count++] = Configuration.HOTP;
        ret[count++] = Configuration.DELIM;
        
        //Store counter (4 bytes long)
        System.arraycopy(intToBytesLittleEndian(counter), 0, ret, count, 4);
        count += 4;
        ret[count++] = Configuration.DELIM;
        
        //Store recordStoreId
        ret[count] = recordStoreId;
        
        return ret;
    }
    
    public static HotpEntry fromBytes(byte[] bytes) {
        ByteArrayOutputStream b_label_baos = new ByteArrayOutputStream();
        DataOutputStream b_label = new DataOutputStream(b_label_baos);
        ByteArrayOutputStream b_secret_baos = new ByteArrayOutputStream();
        DataOutputStream b_secret = new DataOutputStream(b_secret_baos);
        ByteArrayOutputStream b_counter_baos = new ByteArrayOutputStream();
        DataOutputStream b_counter = new DataOutputStream(b_counter_baos);

        byte b_recordStoreId = -1;
        byte count = 0;
        try {
            for (int i = 0; i < bytes.length; i++) {
                if (Configuration.DELIM != bytes[i]) {
                    if (count == 0) b_label.write(bytes[i]);
                    if (count == 1) b_secret.write(bytes[i]);
                    if (count == 2) {
                        if (Configuration.HOTP != bytes[i]) {
                            System.err.println("HotpEntry tries to read entry that is not Hotp.");
                            return null;
                        }
                    }
                    if (count == 3) {
                        b_counter.write(bytes[i]);
                    }
                    if (count == 4) b_recordStoreId = bytes[i];
                }
                else count++;
            }
        } catch (IOException e) {
            System.err.println("Could not read Configuration from record store. Out of memory?");
        }
        byte[] counterByteArray = b_counter_baos.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(counterByteArray);
        int c = bb.getInt();
        return new HotpEntry(
                b_label_baos.toByteArray(), 
                b_secret_baos.toByteArray(), 
                c,
                b_recordStoreId);
    }
    
    private static byte[] intToBytesLittleEndian(int i) {
        return new byte[] {
            (byte) (i & 0xff),
            (byte) ((i >>> 8) & 0xff),
            (byte) ((i >>> 16) & 0xff),
            (byte) ((i >>> 24) & 0xff)
        };
    }
}
