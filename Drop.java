package scripts;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Item;

import java.util.Random;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Drop extends Task<ClientContext> {
    private int logId = 1511;
    Random rand = null;

    public Drop(ClientContext ctx) {
        super(ctx);

        // Create our random object
        rand = new Random();
    }

    public boolean activate() {
        // Give 0.2% of dropping logs now in order to appear more real
        float choice = rand.nextFloat();

        return ctx.inventory.select().count() == 28 || choice < 0.002f;
    }

    public void execute() {
        for (Item i : ctx.inventory.id(logId)) {
            i.interact("Drop");
        }
    }
}
