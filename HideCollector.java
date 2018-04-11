package scripts;

import org.powerbot.script.PaintListener;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.util.Timer;

@Script.Manifest(name="Cow Hide Collector", description="Simple lumbridge cow hide collector and banker")

/**
 * Created by Ryan on 11/04/2018.
 */

public class HideCollector extends PollingScript<ClientContext> implements PaintListener {
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

    // Keep track of how many hides we've picked up
    private int _hidesCollected = 0;
    // Keep track of our inventory size
    private int _inventoryCount = 0;
    // Keep track of our run time
    long _startTime;
    // The price of hides at start
    private int _hidePrice = 1;
    // Keep track of Bury so we know how many bones we've buried
    private int _bonesBuried = 0 ;

    // Path from field to bank
    public static final Tile[] PATH_FIELD_BANK = {
            new Tile(3247, 3292, 0),
            new Tile(3256, 3280, 0),
            new Tile(3257, 3269, 0),
            new Tile(3259, 3267, 0),
            new Tile(3250, 3259, 0),
            new Tile(3254, 3247, 0),
            new Tile(3256, 3234, 0),
            new Tile(3245, 3226, 0),
            new Tile(3235, 3220, 0),
            new Tile(3222, 3219, 0),
            new Tile(3215, 3215, 0),
            new Tile(3208, 3210, 0)
    };

    public static final Tile[] PATH_STAIRS_BANK = {
        new Tile(3206, 3210, 2),
        new Tile(3209, 3218, 2),
        new Tile(3209, 3220, 2)
    };

    private TilePath pathToBank, pathToField, pathStairsToBank, pathBankToStairs;

    @Override
    public void start() {
        pathToBank = ctx.movement.newTilePath(PATH_FIELD_BANK);
        pathToField = ctx.movement.newTilePath(PATH_FIELD_BANK).reverse();
        pathStairsToBank = ctx.movement.newTilePath(PATH_STAIRS_BANK);
        pathBankToStairs = ctx.movement.newTilePath(PATH_STAIRS_BANK).reverse();

        _inventoryCount = ctx.inventory.select().count();

        _startTime = System.currentTimeMillis();

        GeItem hide = new GeItem(hideId[0]);
        _hidePrice = hide.price;
    }

    @Override
    public void poll() {
        // Work out if we have picked up a hide
        if (_inventoryCount < ctx.inventory.select().count()) {
            Item lastCollected = ctx.inventory.itemAt(_inventoryCount);

            // Make sure we know we've picked something up
            _inventoryCount++;

            // Check if our latest item is a hide
            for (int i = 0; i < hideId.length; i++) {
                if (hideId[i] == lastCollected.id()) {
                    _hidesCollected++;
                    break;
                }
            }

        }

        // Work out our state and execute corresponding task
        state = getState();

        switch (state) {
            case BANK:
                Bank bank = new Bank(ctx);
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
            case WALK_TO_FIELD:
                // Make sure that our inventory count is reset
                _inventoryCount = 0;
            case WALK_TO_BANK:
                Walk walk = new Walk(ctx, state, pathToBank, pathToField, pathStairsToBank, pathBankToStairs);
                walk.execute();
                break;
        }
    }

    @Override
    public void repaint(Graphics g) {
        Image background = getImage("http://oi68.tinypic.com/aaem35.jpg");
        Font font1 = new Font("Raleway", 0, 20);

        g.drawImage(background, 0, 295, null);

        // Setup our font
        g.setColor(Color.WHITE);
        g.setFont(font1);

        // Work out how long we've been running for
        long runTime = System.currentTimeMillis() - _startTime;
        long hours = (runTime / 1000) / 3600;
        long minutes = ((runTime / 1000) / 60) % 3600;
        long seconds = (runTime / 1000) % 60;

        // Work out our p/h stats
        float hidesPH = 0;
        long profitPH = 0;
        if (_hidesCollected > 0) {
            hidesPH = _hidesCollected / ((runTime / 1000.0f) / 3600.0f);
            profitPH = _hidePrice * (long) hidesPH;
        }

        // Draw our stats to screen
        g.drawString("" + _hidesCollected, 259, 353);
        g.drawString("" + (int) hidesPH, 259, 378);
        g.drawString("" + profitPH, 259, 403);
        g.drawString("" + _bonesBuried, 423, 351);
        g.drawString(hours + ":" + minutes + ":" + seconds, 386, 374);
    }

    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
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
            if (ctx.players.local().animation() == -1) {
                _bonesBuried++;
                return State.BURY;
            }
        } else if (ctx.bank.nearest().tile().distanceTo(ctx.players.local()) < 3) {
            return State.BANK;
        } else {
            return State.WALK_TO_BANK;
        }

        return state;
    }
}
