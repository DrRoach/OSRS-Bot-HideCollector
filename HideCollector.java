package scripts;

import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.TilePath;

@Script.Manifest(name="Cow Hide Collector", description="Simple lumbridge cow hide collector and banker")

/**
 * Created by Ryan on 11/04/2018.
 */

public class HideCollector extends PollingScript<ClientContext> {
    public enum State {
        BANK,
        COLLECT,
        BURY,
        WALK_TO_BANK,
        WALK_TO_FIELD
    }

    // Our state so that we know what we're doing
    public State state;

    private int boneId = 526;
    private int[] hideId = {1739, 1740};

    // Path from field to bank
    public static final Tile[] PATH_FIELD_BANK = {
            new Tile(3251, 3292, 0),
            new Tile(3258, 3277, 0),
            new Tile(3253, 3267, 0),
            new Tile(3251, 3257, 0),
            new Tile(3253, 3244, 0),
            new Tile(3259, 3236, 0),
            new Tile(3256, 3227, 0),
            new Tile(3247, 3226, 0),
            new Tile(3238, 3226, 0),
            new Tile(3223, 3218, 0),
            new Tile(3215, 3211, 0),
            new Tile(3206, 3210, 0)
    };

    public static final Tile[] PATH_STAIRS_BANK = {
        new Tile(3206, 3210, 2),
        new Tile(3208, 3218, 2)
    };

    private TilePath pathToBank, pathToField, pathStairsToBank, pathBankToStairs;

    @Override
    public void start() {
        pathToBank = ctx.movement.newTilePath(PATH_FIELD_BANK);
        pathToField = ctx.movement.newTilePath(PATH_FIELD_BANK).reverse();
        pathStairsToBank = ctx.movement.newTilePath(PATH_STAIRS_BANK);
        pathBankToStairs = ctx.movement.newTilePath(PATH_STAIRS_BANK).reverse();
    }

    @Override
    public void poll() {
        state = getState();

        switch (state) {
            case BANK:
                Bank bank = new Bank(ctx, pathToBank, pathToField, pathStairsToBank, pathBankToStairs);
                bank.execute();
                break;
            case COLLECT:
                Pickup pickup = new Pickup(ctx);
                pickup.execute();
                break;
            case BURY:
                Bury bury = new Bury(ctx);
                bury.execute();
                break;
            case WALK_TO_BANK:
            case WALK_TO_FIELD:
                Walk walk = new Walk(ctx, state, pathToBank, pathToField, pathStairsToBank, pathBankToStairs);
                walk.execute();
                break;
        }
    }

    private State getState() {
        if (ctx.inventory.select().count() < 28) {
            // Check to see if we are in field
            if (ctx.groundItems.select().id(hideId).nearest().isEmpty()) {
                return State.WALK_TO_FIELD;
            } else {
                return State.COLLECT;
            }
        } else if (ctx.inventory.select().id(boneId).count() > 0) {
            return State.BURY;
        } else if (ctx.bank.nearest().tile().distanceTo(ctx.players.local()) < 5) {
            return State.BANK;
        } else {
            return State.WALK_TO_BANK;
        }
    }
}
