/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/
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

/**
 *
 * @author matti
 */
public class EntryItem extends Gauge implements ItemCommandListener{
    Entry entry;
    Midlet midlet;
    
    public EntryItem(String label, boolean interactive, int maxValue, int initialValue) {
        super(label, interactive, maxValue, initialValue);
    }    
    
    public EntryItem(Entry e, Midlet m) {
        super("", false, e.getRefreshSeconds(), e.getRefreshSeconds());
        entry = e;
        midlet = m;
        setLabel(getPinLabel());
    }
    
    public Entry getEntry() {
        return entry;
    }
    
    public void update(Calendar cal) {
        setValue(cal.get(Calendar.SECOND) % getMaxValue());
        if (getValue() < 1) {
            setLabel(getPinLabel());
        }
    }
    
    private String getPinLabel() {
        return entry.getId() + ": " + computePin(entry.getSecret(), null);
    }

    private static String computePin(String secret, Long counter) {
        try {
            final byte[] keyBytes = Base32String.decode(secret);
            Mac mac = new HMac(new SHA1Digest());
            mac.init(new KeyParameter(keyBytes));
            PasscodeGenerator pcg = new PasscodeGenerator(mac);

            String pin;
            if (counter == null) { // time-based totp
                pin =  pcg.generateTimeoutCode();
            } else { // counter-based hotp
                pin = pcg.generateResponseCode(counter.longValue());
            }
            String formattedPin = pin.substring(0, 3) + " "+pin.substring(3);
            return formattedPin;
        } catch (Exception e) {
            return "General security exception: " + e.getMessage();
        }
    }
    
    public void commandAction(Command c, Item item) {
        if (c.getCommandType() == Command.OK) { // Edit
            EntryCommand cmd = (EntryCommand)c;
            midlet.getEntryForm().setEntry(cmd.getEntry());
            midlet.showEntryForm();
        } else if (c.getCommandType() == Command.STOP) { // Delete
            EntryCommand cmd = (EntryCommand)c;
            int recordStoreId = cmd.getEntry().getRecordStoreId();
            midlet.getConfiguration().deleteEntry(recordStoreId);
            midlet.getMainForm().deleteEntry(recordStoreId);
        }
    }    
}

//        f.addCommand(new EntryCommand("Edit", Command.OK, 2, entry));
  //      f.addCommand(new EntryCommand("Delete", Command.STOP, 2, entry));

