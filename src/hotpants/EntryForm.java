package hotpants;

import javax.microedition.lcdui.*;

public class EntryForm extends Form implements CommandListener {
    private TextField idItem, secretItem, refreshSecondsItem, digitCountItem, counterItem;
    private Command cancel, save;
    private Midlet midlet;
    private TotpEntry totpEntry;
    private HotpEntry hotpEntry;
    private int entryType;
    private int recordStoreId;
    
    public EntryForm(String title, Midlet m){
        super(title);
        midlet = m;
        
        cancel = new Command("Cancel", Command.CANCEL, 1);
        addCommand(cancel);

        save = new Command("Save", Command.ITEM, 2);
        addCommand(save);
        
        this.setCommandListener(this);
    }
    
    private void setCommonItems(String id, String secret) {
        deleteAll();
        idItem = new TextField("Name", id, 80, TextField.ANY);
        append(idItem);
        
        secretItem = new TextField("Secret", secret, 126, TextField.ANY);
        append(secretItem);
    }

    private void setHotpItems(HotpEntry e) {
        counterItem = new TextField("Counter", Integer.toString(e.getCounter()), 6, TextField.NUMERIC);
        append(counterItem);
    }
    
    private void setTotpItems(TotpEntry e) {
        refreshSecondsItem = new TextField("Token period in seconds", Integer.toString(e.getRefreshSeconds()), 3, TextField.NUMERIC);
        append(refreshSecondsItem);
        
        digitCountItem = new TextField("Number of digits", Integer.toString(e.getDigitCount()), 2, TextField.NUMERIC);
        append(digitCountItem);             
    }
    
    public void setEntry(Otp e) {
        if (e == null) return;

        entryType = e.getOtpType();
        recordStoreId = e.getRecordStoreId();

        deleteAll();
        setCommonItems(e.getLabel(), e.getSecret());
        if (Configuration.TOTP == e.getOtpType()) {
            hotpEntry = null;
            totpEntry = (TotpEntry)e;
            setTotpItems((TotpEntry)e);
        } else if (Configuration.HOTP == e.getOtpType()) {
            totpEntry = null;
            hotpEntry = (HotpEntry)e;
            setHotpItems((HotpEntry)e);
        }
    }
        
    public void reset() {
        deleteAll();
        idItem = null;
        secretItem = null;
        refreshSecondsItem = null;
        digitCountItem = null;
        counterItem = null;
        totpEntry = null;
        hotpEntry = null;
        entryType = -1;
        recordStoreId = -1;
    }
        
    public void commandAction(Command c, Displayable d){
        if(c == cancel) {
            reset();
            midlet.showMainForm();
        } else if (c == save) {
            if (sanitation()) {
                Configuration cfg = Configuration.getInstance();
                if (
                        (totpEntry == null || totpEntry.getRecordStoreId() == -1) &&
                        (hotpEntry == null || hotpEntry.getRecordStoreId() == -1)) {
                    midlet.addEntry(newOtpFromInputs());
                } else { // update
                    midlet.updateEntry(updateOtpFromInputs());
                }
            }
            reset();
            midlet.showMainForm();
        }
    }
    
    private boolean sanitation() {
        if (idItem.getString().length() == 0) {
            midlet.alertError("Name must not be empty", this);
            return false;
        }
        if (secretItem.getString().length() < 5) {
            midlet.alertError("Secret must be 5 characters or more", this);
            return false;
        } 
        if (entryType == Configuration.TOTP) {
            try {
                int refrSec = Integer.parseInt(refreshSecondsItem.getString());
                if (refrSec < 1) {
                    midlet.alertError("Interval must be at least 1 second", this);
                    return false;
                }
            } catch (NumberFormatException e) {
                midlet.alertError("Interval must be an integer", this);
                return false;
            }
            
            try {
                int digCount = Integer.parseInt(digitCountItem.getString());
                if (digCount < 1) {
                    midlet.alertError("Number of digits must be at least 6", this);
                    return false;
                }
            } catch (NumberFormatException e) {
                midlet.alertError("Number of digits must be an integer", this);
                return false;
            }
        } else if (entryType == Configuration.HOTP) {
            try {
                int counter = Integer.parseInt(counterItem.getString());
                if (counter < 0) {
                    midlet.alertError("Counter must be positive", this);
                    return false;
                }
            } catch (NumberFormatException e) {
                midlet.alertError("Counter must be an integer", this);
                return false;
            }
        }
        return true;
    }

    private Otp newOtpFromInputs() {
        // It is assumed there was already sanitation
        if (Configuration.TOTP == entryType) {
            totpEntry = new TotpEntry("");
            return updateOtpFromInputs();
        } else if (Configuration.HOTP == entryType) {
            hotpEntry = new HotpEntry("");
            return updateOtpFromInputs();
        }
        return null;
    }
    
    private Otp updateOtpFromInputs() {
        // It is assumed there was already sanitation
        String id = idItem.getString();
        String secret = secretItem.getString();
        
        if (Configuration.TOTP == entryType && totpEntry != null) {
            totpEntry.setDigitCount((byte)Integer.parseInt(digitCountItem.getString()));
            totpEntry.setRefreshSeconds((byte)Integer.parseInt(refreshSecondsItem.getString()));
            totpEntry.setLabel(id);
            totpEntry.setSecret(secret);
            return totpEntry;
        } else if (Configuration.HOTP == entryType && hotpEntry != null) {
            hotpEntry.setCounter(Integer.parseInt(counterItem.getString()));
            hotpEntry.setLabel(id);
            hotpEntry.setSecret(secret);
            return hotpEntry;
        }
        return null;
    }
}
