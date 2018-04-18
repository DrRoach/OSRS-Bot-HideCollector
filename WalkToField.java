package scripts.cowhide;

import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.TilePath;

/**
 * Created by Ryan on 17/04/2018.
 */
public class WalkToField extends Task<ClientContext> {
    private int middleStairs = 0;
    private int topStairs = 0;

    private TilePath pathToField;
    private TilePath pathBankToStairs;

    public WalkToField(ClientContext ctx, TilePath pathToField, TilePath pathBankToStairs) {
        super(ctx);

        this.pathToField = pathToField;
        this.pathBankToStairs = pathBankToStairs;
    }

    @Override
    public boolean activate() {
        return ctx.inventory.select().count() < 28
                && ctx.groundItems.select().id(HideCollector.HIDE_IDS).nearest().isEmpty();
    }

    @Override
    public void execute() {
        GameObject mStairs = ctx.objects.select(5).id(HideCollector.MIDDLE_STAIRS).nearest().poll();
        GameObject tStairs = ctx.objects.select(5).id(HideCollector.TOP_STAIRS).nearest().poll();

        if (mStairs.id() == -1 && tStairs.id() == -1) {
            pathToField.traverse();
        } else if (tStairs.id() != -1) {
            if (tStairs.inViewport()) {
                tStairs.interact("Climb-down", "Staircase");
            } else {
                ctx.camera.turnTo(tStairs);
            }
        } else if (mStairs.id() != -1) {
            if (mStairs.inViewport()) {
                mStairs.interact("Climb-down", "Staircase");
            } else {
                ctx.camera.turnTo(mStairs);
            }
        } else {
            pathBankToStairs.traverse();
        }
    }
}
