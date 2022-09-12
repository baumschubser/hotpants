package hotpants;

import com.google.authenticator.blackberry.Base32String;
import com.google.authenticator.blackberry.PasscodeGenerator;
import java.util.Calendar;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class EntryItem implements ItemCommandListener {
    Otp entry;
    Midlet midlet;
    Item item;
    Command confirmationYes, confirmationNo;
    
    public EntryItem(String label, boolean interactive, int maxValue, int initialValue) {
        item = new Gauge(label, interactive, maxValue, initialValue);
    }    
    
    public EntryItem(Otp e, Midlet m) {
        setupItem(e);

        entry = e;
        midlet = m;
        item.setLabel(getPinLabel());
        confirmationYes = new Command("Yes", Command.OK, 1);
        confirmationNo = new Command("No", Command.CANCEL, 2);
    }
    
    public Item getItem() {
        return item;
    }
    
    public Otp getOtp() {
        return entry;
    }
    
    public void setOtp(Otp o) {
        if (entry.getOtpType() != o.getOtpType()) return;
        entry = o;
        setupItem(o);
        item.setLabel(getPinLabel());
    }
    
    private void setupItem(Otp e) {
        if (Configuration.TOTP == e.getOtpType()) {
            TotpEntry t = (TotpEntry)e;
            item = new Gauge("", false, t.getRefreshSeconds(), t.getRefreshSeconds());
        } else if (Configuration.HOTP == e.getOtpType()) {
            HotpEntry h = (HotpEntry)e;
            item = new StringItem(e.getId(), "");
            item.setDefaultCommand(new EntryCommand("New PIN", Command.SCREEN, 1, e, e.getOtpType()));
        }
        item.setDefaultCommand(new EntryCommand("Edit", Command.OK, 2, e, e.getOtpType()));
        item.addCommand(new EntryCommand("Delete", Command.STOP, 3, e, e.getOtpType()));
        item.setItemCommandListener(this);
    }
    
    public void update(Calendar cal) {
        if (Configuration.TOTP == entry.getOtpType()) {
            Gauge g = (Gauge)item;
            g.setValue(cal.get(Calendar.SECOND) % g.getMaxValue());
            if (g.getValue() < 1) {
                item.setLabel(getPinLabel());
            }
        } 
    }
    
    public Command getYesCommand() {
        return confirmationYes;
    }
    
    public Command getNoCommand() {
        return confirmationNo;
    }
    
    private String getPinLabel() {
        int counter = -1;
        if (Configuration.HOTP == entry.getOtpType()) {
            counter = ((HotpEntry)entry).getCounter();
        }
        return entry.getId() + ": " + computePin(entry.getSecret(), counter);
    }

    private String computePin(String secret, int counter) {
        try {
            final byte[] keyBytes = Base32String.decode(secret);
            Mac mac = new HMac(new SHA1Digest());
            mac.init(new KeyParameter(keyBytes));
            PasscodeGenerator pcg = new PasscodeGenerator(mac);

            String pin;
            long currentTimeMillis = System.currentTimeMillis() + (midlet.getConfiguration().getOffsetSeconds() * 1000);
            if (counter == -1) { // time-based totp
                pin =  pcg.generateTimeoutCode(currentTimeMillis);
            } else { // counter-based hotp
                pin = pcg.generateResponseCode(new Long(counter).longValue(), currentTimeMillis);
            }
            String formattedPin = pin.substring(0, 3) + " "+pin.substring(3);
            return formattedPin;
        } catch (Exception e) {
            return "General security exception: " + e.getMessage();
        }
    }
    
    public void commandAction(Command c, Item item) {
        if (c == confirmationYes) { // confirmed item delete
            midlet.getMainForm().deleteEntry(this);
            Display.getDisplay(midlet).setCurrent(midlet.getMainForm());
            return;
        } else if (c == confirmationNo) { // no item delete
            Display.getDisplay(midlet).setCurrent(midlet.getMainForm());
            return;
        }

        if (c.getCommandType() == Command.OK) { // Edit
            EntryCommand cmd = (EntryCommand)c;
            if (Configuration.TOTP == cmd.getEntryType()) {
                midlet.getEntryForm().setEntry((TotpEntry)cmd.getEntry());
            } else if (Configuration.HOTP == cmd.getEntryType()) {
                midlet.getEntryForm().setEntry(cmd.getEntry());
            }
            midlet.showEntryForm();
        } else if (c.getCommandType() == Command.STOP) { // Delete
            Display.getDisplay(midlet).setCurrent(new ConfirmationDialog(midlet, this, "Attention", "Do you want to delete this entry?").getDisplayable());
            //midlet.getMainForm().deleteEntry(this);
        } else if (c.getCommandType() == Command.SCREEN) { // Next HOTP PIN
            if (Configuration.HOTP == getOtp().getOtpType()) {
                ((HotpEntry)entry).nextPin();
                item.setLabel(getPinLabel());
                midlet.getConfiguration().updateEntry(entry);
            }
        }
    }
}

