package hotpants;

import java.util.Calendar;
import java.util.Date;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.ItemStateListener;

public class TimeConfigForm  extends Form implements CommandListener, ItemStateListener {
    private Midlet midlet;
    private Command cancel, save;
    private TextField secondsInput, minutesInput, hoursInput;
    private Item focusedItem;
    private long offset;
    private Calendar displayedTime;
    private StringItem helpText;
    
    public TimeConfigForm(Midlet m){
        super("Time configuration");
        midlet = m;
        
        helpText = new StringItem("", "Set the correct time as exact as possible and select \"Save\"");
        append(helpText);
        
        cancel = new Command("Cancel", Command.CANCEL, 1);
        addCommand(cancel);

        save = new Command("Save", Command.ITEM, 2);
        addCommand(save);
        
        hoursInput = new TextField("", "0", 2, TextField.NUMERIC);
        hoursInput.setLayout(Item.LAYOUT_SHRINK);
        append(hoursInput);
        
        minutesInput = new TextField("", "0", 2, TextField.NUMERIC);
        minutesInput.setLayout(Item.LAYOUT_SHRINK);
        append(minutesInput);
        
        secondsInput = new TextField("", "0", 3, TextField.NUMERIC);
        secondsInput.setLayout(Item.LAYOUT_SHRINK);
        append(secondsInput);
        
        displayedTime = Calendar.getInstance();
        offset = m.getConfiguration().getOffsetSeconds();
        updateTimeLabel();

        this.setCommandListener(this);
        this.setItemStateListener(this);
    }
    
    public void updateTimeLabel() {
        displayedTime.setTime(new Date(System.currentTimeMillis() - (offset*1000)));
        
        int hours = displayedTime.get(Calendar.HOUR_OF_DAY);
        int minutes = displayedTime.get(Calendar.MINUTE);
        int seconds = displayedTime.get(Calendar.SECOND);
        
        if (!hoursInput.equals(focusedItem))
            hoursInput.setString((hours < 10 ? "0" : "") + hours);
        
        if (!minutesInput.equals(focusedItem))
            minutesInput.setString((minutes < 10 ? "0" : "") + minutes);
        
        if (!secondsInput.equals(focusedItem))
            secondsInput.setString((seconds < 10 ? "0" : "") + seconds);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == save) {
            recalcOffset();
            focusedItem = null;
//            midlet.getConfiguration().updateOffsetSeconds((byte)seconds);
        }
else        midlet.showMainForm();
    }

    public void itemStateChanged(Item item) {
        focusedItem = item;
    }
    
    private void recalcOffset() {
        try {
            Calendar newTime = Calendar.getInstance();
            newTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hoursInput.getString()));
            newTime.set(Calendar.MINUTE, Integer.parseInt(minutesInput.getString()));
            newTime.set(Calendar.SECOND, Integer.parseInt(secondsInput.getString()));
            offset = (System.currentTimeMillis() - newTime.getTime().getTime())/1000;
        } catch (NumberFormatException e) {
            System.err.println("Could not parse time input: " + e.getMessage());
        }
    }
}
