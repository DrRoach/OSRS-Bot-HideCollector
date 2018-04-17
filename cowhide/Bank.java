package scripts.cowhide;

import org.powerbot.script.rt4.ClientContext;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Bank extends Task<ClientContext> {
    public Bank(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean activate() {
        return ctx.inventory.select().count() == 28
                && ctx.inventory.id(HideCollector.BONE_ID).count() == 0
                && ctx.bank.nearest().tile().distanceTo(ctx.players.local()) < 4;
    }

    @Override
    public void execute() {
        if (!ctx.bank.opened()) {
            ctx.bank.open();
        } else {
            ctx.bank.depositInventory();
            ctx.bank.close();
        }
    }
}
