package hotpants;

import dk.onlinecity.qrr.client.CameraControl;
import hotpants.qr.DecodeCanvas;
import hotpants.qr.QRCodeParser;
import hotpants.qr.CameraCanvas;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;

public class ScanForm extends Form implements CommandListener {
    private final Midlet midlet;
    private Player player;
    private Gauge progress;
    
    public ScanForm(Midlet m) {
        super("Scan QR Code");
        midlet = m;
    }
    
    public void takeSnapshot()
    {
        Display.getDisplay(midlet).setCurrent(this);
        final ScanForm scanForm = this;
        new Thread(new Runnable() {       
            public void run() { 
                dk.onlinecity.qrr.client.CameraControl cameraControl = dk.onlinecity.qrr.client.CameraControl.getInstance();
                byte[] snapShot = cameraControl.getSnapshot();
                Player player = cameraControl.getPlayer();
                try {
                        player.stop();
                } catch (MediaException e) {
                }
                DecodeCanvas decodeCanvas = new DecodeCanvas(midlet, scanForm, snapShot);
                Display.getDisplay(midlet).setCurrent(decodeCanvas);
            } 
        }).run();
    }
    
    public void showCamera()
    {
        dk.onlinecity.qrr.client.CameraControl cameraControl = dk.onlinecity.qrr.client.CameraControl.getInstance();
        hotpants.qr.CameraCanvas cameraCanvas = hotpants.qr.CameraCanvas.getInstance(midlet, this);
        Player player = cameraControl.getPlayer();
        VideoControl videoControl = cameraControl.getVideoControl(cameraCanvas);
        try {
            videoControl.setDisplayFullScreen(true);
            videoControl.setDisplayLocation(0, 0);
            player.start();
            videoControl.setVisible(true);
            System.out.println("Setting camera canvas as current displayable");
            Display.getDisplay(midlet).setCurrent(cameraCanvas);
        } catch (MediaException e) {
            e.printStackTrace();
        }
    }
    
    public void commandAction(Command c, Displayable d) {
    }
    
    public void setThumbnail(Image thumbnail) {
        this.deleteAll();
        this.append(thumbnail);
        progress = new Gauge("Please wait...", false, 100, 0);
        this.append(progress);
    }
    
    public void setProgress(int percentage) {
        progress.setValue(percentage);
    }
    
    public void setOtp(Otp otp) {
        Configuration.getInstance().addEntry(otp);
        midlet.getMainForm().addEntry(otp);
        Display.getDisplay(midlet).setCurrent(new Alert("OTP created: " + otp.getId()), midlet.getMainForm());
    }
    
    private Alert getWarning(String text) {
        return new Alert("QR Code scan", text, null, AlertType.WARNING);
    }
}
