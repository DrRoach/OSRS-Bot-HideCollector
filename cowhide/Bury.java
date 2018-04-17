package scripts.cowhide;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Item;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Bury extends Task<ClientContext> {
    private int BONES_BURIED = 0;

    public Bury(ClientContext ctx) {
        super(ctx);
    }

    public boolean activate() {
        // If our inventory is full or we make our choice and we have bones then bury them
        return (ctx.inventory.select().count() == 28 || HideCollector.RAND.nextInt(0, 99) < 9)
                && ctx.inventory.select().id(HideCollector.BONE_ID).count() > 0;
    }

    public void execute() {
        for (Item i : ctx.inventory.id(HideCollector.BONE_ID)) {
            // If we're not already burying then bury
            if (ctx.players.local().animation() == -1) {
                i.interact("Bury");
                BONES_BURIED++;
                break;
            }
        }
    }

    public int bonesBuried() {
        return BONES_BURIED;
    }
}
