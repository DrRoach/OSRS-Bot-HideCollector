package scripts.cowhide;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.TilePath;

/**
 * Created by Ryan on 17/04/2018.
 */
public class WalkToBank extends Task<ClientContext> {
    private TilePath pathToBank;
    private TilePath pathStairsToBank;

    public WalkToBank(ClientContext ctx, TilePath pathToBank, TilePath pathStairsToBank) {
        super(ctx);

        this.pathToBank = pathToBank;
        this.pathStairsToBank = pathStairsToBank;
    }

    @Override
    public boolean activate() {
        return ctx.inventory.select().count() == 28
                && ctx.bank.nearest().tile().distanceTo(ctx.players.local()) > 4;
    }

    @Override
    public void execute() {
        GameObject bStairs = ctx.objects.select(5).id(HideCollector.BOTTOM_STAIRS).nearest().poll();
        GameObject mStairs = ctx.objects.select(5).id(HideCollector.MIDDLE_STAIRS).nearest().poll();
        GameObject tStairs = ctx.objects.select(5).id(HideCollector.TOP_STAIRS).nearest().poll();

        // If we can't see any stairs then we need to walk to the bank
        if (bStairs.id() == -1 && mStairs.id() == -1 && tStairs.id() == -1) {
            pathToBank.traverse();
        } else if (bStairs.id() != -1) {
            // We can see the bottom staircase so perform correct action
            if (bStairs.inViewport()) {
                bStairs.interact("Climb-up", "Staircase");
            } else {
                ctx.camera.turnTo(bStairs);
            }
        } else if (mStairs.id() != -1) {
            // We can see middle stairs so perform correct action
            if (mStairs.inViewport()) {
                mStairs.interact("Climb-up", "Staircase");
            } else {
                ctx.camera.turnTo(mStairs);
            }
        } else {
            // Walk from top stairs to bank
            pathStairsToBank.traverse();
        }
    }
}
