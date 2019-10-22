/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hotpants;

/**
 *
 * @author matti
 */
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
