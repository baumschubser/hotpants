/*
* Author: Matthias Clausen <matthiasclausen@posteo.de>
* License: GPL 2
*/

package hotpants;

import java.util.Timer;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Midlet extends MIDlet {
    private Configuration config;
    private Display display;
    private MainForm mainForm;
    private EntryForm entryForm;
    private UpdateTask updateTask;
    
    public Midlet() {
    }

    public void startApp() {
        config = new Configuration();
        mainForm = new MainForm("Hotpants", this);
        entryForm = new EntryForm("", this);
        mainForm.setEntries(config.getEntries());
        
        display = Display.getDisplay(this);
        display.setCurrent(mainForm.getForm());
        
        updateTask = new UpdateTask(this);
        new Timer().schedule(updateTask,0, 1000);   
    }
    
    public void showMainForm() {
        Display.getDisplay(this).setCurrent(mainForm);
    }
    
    public void showEntryForm() {
        Display.getDisplay(this).setCurrent(entryForm);
    }
    
    public MainForm getMainForm() {
        return mainForm;
    }
    
    public EntryForm getEntryForm() {
        return entryForm;
    }
    
    public Configuration getConfiguration() {
        return config;
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
