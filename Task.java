package scripts;

import org.powerbot.script.ClientContext;
import org.powerbot.script.ClientAccessor;

/**
 * Created by Ryan on 10/04/2018.
 */
public abstract class Task<C extends ClientContext> extends ClientAccessor<C> {
    public Task(C ctx) {
        super(ctx);
    }

    public abstract boolean activate();
    public abstract void execute();
}
