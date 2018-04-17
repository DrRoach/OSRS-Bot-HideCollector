package scripts.cowhide;

import org.powerbot.script.rt4.ClientContext;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Bank extends Task<ClientContext> {
    private int boneId = 526;
    private int stairId = 16671;

    public Bank(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean activate() {
        return ctx.inventory.select().count() == 28
                && ctx.inventory.id(boneId).count() == 0;
    }

    @Override
    public void execute() {
        // Bank all items
        if (ctx.bank.opened() == false) {
            ctx.bank.open();
        } else {
            ctx.bank.depositInventory();
            ctx.bank.close();
        }
    }
}
