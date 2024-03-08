package hotpants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TotpEntry implements Otp {
    private byte refreshSeconds, digitCount, recordStoreId;
    private String secret;
    private String id;
    
    public int getOtpType() {
        return Configuration.TOTP;
    }
    
    public TotpEntry(byte[] id, byte[] secret, byte refreshSeconds, byte digitCount, byte recordStoreId) {
        this.id = new String(id);
        this.secret = new String(secret);
        this.refreshSeconds = refreshSeconds;
        this.digitCount = digitCount;
        this.recordStoreId = recordStoreId;
    }
    
    public TotpEntry(String id) {
        refreshSeconds = 30;
        digitCount = 6;
        secret = "";
        this.id = id;
        recordStoreId = -1;
    }
    
    public int getRefreshSeconds() {
        return refreshSeconds;
    }   
    
    public void setRefreshSeconds(byte s) {
        refreshSeconds = s;
    }
    
    public int getDigitCount() {
        return digitCount;
    }
    
    public void setDigitCount(byte d) {
        digitCount = d;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String s) {
        secret = s;
    }
    
    public String getLabel() {
        return id;
    }
    
    public void setLabel(String i) {
        id = i;
    }
    
    public int getRecordStoreId() {
        return recordStoreId;
    }
    
    public void setRecordStoreId(byte n) {
        recordStoreId = n;
    }
    
    public byte[] toBytes() {
        byte[] b_id = id.getBytes();
        byte[] b_secret = secret.getBytes();
        byte[] ret = new byte[b_id.length + b_secret.length + 9];
        
        // Store id
        System.arraycopy(b_id, 0, ret, 0, b_id.length);
        ret[b_id.length] = Configuration.DELIM;
        
        //Store secret
        System.arraycopy(b_secret, 0, ret, b_id.length+1, b_secret.length);
        ret[b_id.length+b_secret.length + 1] = Configuration.DELIM;
        
        //Store entryType
        ret[b_id.length+b_secret.length + 2] = Configuration.TOTP;
        ret[b_id.length+b_secret.length + 3] = Configuration.DELIM;
        
        //Store refreshSeconds
        ret[b_id.length+b_secret.length + 4] = refreshSeconds;
        ret[b_id.length+b_secret.length + 5] = Configuration.DELIM;
        
        //Store digitCount
        ret[b_id.length+b_secret.length + 6] = digitCount;
        ret[b_id.length+b_secret.length + 7] = Configuration.DELIM;
        
        //Store recordStoreId
        ret[b_id.length+b_secret.length + 8] = recordStoreId;

        return ret;
    }
    
    public static TotpEntry fromBytes(byte[] bytes) {
        ByteArrayOutputStream b_label_baos = new ByteArrayOutputStream();
        DataOutputStream b_label = new DataOutputStream(b_label_baos);
        ByteArrayOutputStream b_secret_baos = new ByteArrayOutputStream();
        DataOutputStream b_secret = new DataOutputStream(b_secret_baos);
        ByteArrayOutputStream b_counter_baos = new ByteArrayOutputStream();

        byte b_refreshSeconds = 30, b_digitCount = 6, b_recordStoreId = -1;
        byte count = 0;
        
        try {
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] != Configuration.DELIM) {
                    if (count == 0) b_label.write(bytes[i]);
                    if (count == 1) b_secret.write(bytes[i]);
                    if (count == 2) {
                        if (Configuration.TOTP != bytes[i]) {
                            System.err.println("TotpEntry tries to read entry that is not TOTP.");
                            return null;
                        }
                    }
                    if (count == 3) b_refreshSeconds = bytes[i];
                    if (count == 4) b_digitCount = bytes[i];
                    if (count == 5) b_recordStoreId = bytes[i];
                }
                else count++;
            }
        } catch (IOException e) {
            System.err.println("Could not read Configuration from record store. Out of memory?");
        }
        return new TotpEntry(
                b_label_baos.toByteArray(), 
                b_secret_baos.toByteArray(), 
                b_refreshSeconds, 
                b_digitCount, 
                b_recordStoreId);
    }
}
