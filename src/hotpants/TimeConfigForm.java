package hotpants;

import java.util.Calendar;
import java.util.Date;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;

public class TimeConfigForm  extends Form implements CommandListener, ItemStateListener {
    private final Midlet midlet;
    private final Command cancel, save;
    private final Gauge secondsInput;
    private byte offset;
    private final Calendar displayedTime;
    private final StringItem helpText;
    
    public TimeConfigForm(Midlet m){
        super("Time configuration");
        midlet = m;
        
        helpText = new StringItem("", "Set the correct time as exact as possible and select \"Save\"");
        append(helpText);
        
        secondsInput = new Gauge("0 seconds", true, 240, 120);
        secondsInput.setLayout(Item.LAYOUT_EXPAND);
        append(secondsInput);
        this.setItemStateListener(this);
        
        displayedTime = Calendar.getInstance();
        offset = Configuration.getInstance().getOffsetSeconds();
        updateTimeLabel();

        cancel = new Command("Cancel", Command.CANCEL, 1);
        addCommand(cancel);

        save = new Command("Save", Command.ITEM, 2);
        addCommand(save);

        this.setCommandListener(this);
    }
    
    public void updateTimeLabel() {
        displayedTime.setTime(new Date(System.currentTimeMillis() + (offset*1000)));
        
        int minutes = displayedTime.get(Calendar.MINUTE);
        int seconds = displayedTime.get(Calendar.SECOND);
        
        secondsInput.setLabel(offset + " seconds");
        secondsInput.setLabel(
            (minutes < 10 ? "0" : "") + minutes + ":" + 
            (seconds < 10 ? "0" : "") + seconds +
            (offset > 0 ? "(+" : "(") + offset + " " + "seconds)"
        );
    }

    public void commandAction(Command c, Displayable d) {
        if (c == save) {
            Configuration.getInstance().updateOffsetSeconds(offset);
        }
        midlet.showMainForm();
    }

    public void itemStateChanged(Item item) {
        if (item == secondsInput) {
            offset = (byte)(secondsInput.getValue() - 120);
            updateTimeLabel();
        }
    }
}
