package hotpants;

import java.util.Calendar;
import java.util.TimerTask;

public class UpdateTask extends TimerTask {
    private Midlet midlet;
    
    public UpdateTask(Midlet m) {
        super();
        midlet = m;
    }
    
    public final void run() {
        midlet.refreshAllEntries(Calendar.getInstance());
    }
}
 