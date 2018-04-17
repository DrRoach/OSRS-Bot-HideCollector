package scripts.cowhide;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GameObject;

/**
 * Created by Ryan on 18/04/2018.
 */
public class OpenGate extends Task<ClientContext> {
    private final int GATE_ID = 1560;

    public OpenGate(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean activate() {
        return !ctx.objects.select(15).id(GATE_ID).nearest().isEmpty();
    }

    @Override
    public void execute() {
        GameObject gate = ctx.objects.select(15).id(GATE_ID).nearest().poll();

        gate.interact("Open");
    }
}
