package hotpants;

import hotpants.qr.DecodeCanvas;
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
    private Gauge progress;
    private final Command backCmd;
    
    public ScanForm(Midlet m) {
        super("Scan QR Code");
        backCmd = new Command("Back", Command.BACK, 1);
        this.addCommand(backCmd);
        this.setCommandListener(this);
        midlet = m;
    }
    
    public void takeSnapshot()
    {
        Display.getDisplay(midlet).setCurrent(this);
        final ScanForm scanForm = this;
        new Thread(new Runnable() {       
            public void run() { 
                try {
                    dk.onlinecity.qrr.client.CameraControl cameraControl = dk.onlinecity.qrr.client.CameraControl.getInstance();
                    byte[] snapShot = cameraControl.getSnapshot();
                    Player player = cameraControl.getPlayer();
                    player.stop();
                    DecodeCanvas decodeCanvas = new DecodeCanvas(midlet, scanForm, snapShot);
                    Display.getDisplay(midlet).setCurrent(decodeCanvas);
                } catch (Exception e) {
                    showCamNotPossible(e.getMessage());
                }
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
            showCamNotPossible(e.getMessage());
        }
    }
    
    private void showCamNotPossible(String msg) {
        deleteAll();
        append("Scan not possible. " + msg);
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

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            midlet.showMainForm();
        }
    }
}
