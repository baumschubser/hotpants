package hotpants.qr;

import hotpants.Configuration;
import hotpants.HotpEntry;
import hotpants.Midlet;
import hotpants.Otp;
import hotpants.TotpEntry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;

public class QRCodeParser implements Runnable {
    private final Midlet midlet;
    private final String result;

    public QRCodeParser(String result, Midlet midlet) {
        this.result = result;
        this.midlet = midlet;
    }
    
    public void parse() {
        if (result == null) {
            Display.getDisplay(midlet).setCurrent(getWarning("No QR code detected"), midlet.getScanForm());
            return;
        }
        Otp res = getOtp();
        if (res == null) {
            Display.getDisplay(midlet).setCurrent(getWarning("QR code did not contain valid OTP info"), midlet.getMainForm());
            return;
        }
        midlet.addEntry(res);
        Display.getDisplay(midlet).setCurrent(new Alert(res.getLabel(), "Import successful", null, AlertType.INFO), midlet.getMainForm());
    }
    
    public Otp getOtp() {
        System.out.print("Getting otp from scan result...");
        try {
            // OTP format: otpauth://totp/OpenVPN?secret=4LRW4HZQCC52QP7NIEMCIT4FXYOLWI75

            if (!result.startsWith("otpauth://")) {
                throw new Exception("QR Code is no one time password key");
            }
            String type = result.substring(10, result.indexOf("/", 11));
            String theRest = result.substring(11 + type.length());
            String label = theRest.substring(0, theRest.indexOf("?"));
            theRest = theRest.substring(label.length() + 1);
            String secret = getURIparameter(theRest, "secret");
            String digits = getURIparameter(theRest, "digits");
            if (digits == null) {
                digits = "6";
            }
            
            if (secret == null || label == null) {
                throw new Exception("Code does not contain all necessary data");
            }
            
            if (type.equalsIgnoreCase("hotp")) {
                String counterString = getURIparameter(theRest, "counter");
                if (counterString == null) {
                    throw new Exception ("HOTP code does not contain all necessary data");
                }
                int counter = Integer.parseInt(counterString);
                HotpEntry hotp = new HotpEntry(label);
                hotp.setSecret(secret);
                hotp.setCounter(counter);
                return hotp;
            } else {
                String periodString = getURIparameter(theRest, "period");
                if (periodString == null) {
                    periodString = "30";
                }
                int period = Integer.parseInt(periodString);
                TotpEntry totp = new TotpEntry(label);
                totp.setSecret(secret);
                totp.setRefreshSeconds((byte)period);
                return totp;
            }
        } catch (NumberFormatException n) {
            Display.getDisplay(midlet).setCurrent(getWarning("Number parsing failed. " + n.getMessage()), midlet.getMainForm());            
        } catch (Exception e) {
            Display.getDisplay(midlet).setCurrent(getWarning(e.getMessage()), midlet.getMainForm());
        }
        return null;
    }
    
    private static String getURIparameter(String uriParameterPart, String key) {
        if (uriParameterPart.indexOf(key) == -1) {
            System.out.println("Key " + key + " not found.");
            return null;
        };
        String tmp = uriParameterPart.substring(uriParameterPart.indexOf(key + "=") + key.length() + 1);
        return tmp.indexOf("&") == -1 ? tmp : tmp.substring(0, tmp.indexOf("&"));
    }
    
    private Alert getWarning(String text) {
        return new Alert("QR Code scan", text, null, AlertType.WARNING);
    }

    public void run() {
        parse();
    }
}
