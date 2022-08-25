package hotpants;

public interface Otp {
    public int getOtpType();
    public String getSecret();
    public void setSecret(String s);
    public String getId();
    public void setId(String i);
    public int getRecordStoreId();
    public void setRecordStoreId(byte n);
    public byte[] toBytes();
}
