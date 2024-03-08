package hotpants;

import java.util.Calendar;
import java.util.Timer;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Midlet extends MIDlet {
    private Display display;
    private MainForm mainForm;
    private EntryForm entryForm;
    private ScanForm scanForm;
    private TimeConfigForm timeConfigForm;
    private UpdateTask updateTask;
    private Alert alert;
    
    public Midlet() {
    }

    public void startApp() {
        mainForm = new MainForm("Hotpants", this);
        entryForm = new EntryForm("", this);
        scanForm = new ScanForm(this);
        timeConfigForm = new TimeConfigForm(this);
        mainForm.setEntriesFromConfig();
        
        display = Display.getDisplay(this);
        display.setCurrent(mainForm.getForm());
        
        updateTask = new UpdateTask(this);
        new Timer().schedule(updateTask,0, 1000);   
    }
    
    public void refreshAllEntries(Calendar c) {
        Displayable current = Display.getDisplay(this).getCurrent();
        if (current == mainForm)
            mainForm.refreshAllEntries(c);
        else if (current == timeConfigForm)
            timeConfigForm.updateTimeLabel();
    }
    
    public void showMainForm() {
        Display.getDisplay(this).setCurrent(mainForm);
    }
    
    public void showEntryForm() {
        Display.getDisplay(this).setCurrent(entryForm);
    }
    
    public void showHotpEntryForm() {
        Display.getDisplay(this).setCurrent(entryForm);
    }
    
    public void showTimeConfigForm() {
        Display.getDisplay(this).setCurrent(timeConfigForm);
    }
    
    public void showScanForm() {
        Display.getDisplay(this).setCurrent(scanForm);
        scanForm.showCamera();
    }
    
    public ScanForm getScanForm() {
        return scanForm;
    }
    
    public MainForm getMainForm() {
        return mainForm;
    }
    
    public EntryForm getEntryForm() {
        return entryForm;
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
    public void alertError(String msg, Displayable next) {
        alert = new Alert("Error");
        alert.setTimeout(2000);
        alert.setString(msg);
        Display.getDisplay(this).setCurrent(alert, next);
    }
}
