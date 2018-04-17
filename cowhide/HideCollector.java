package scripts.cowhide;

import org.powerbot.script.PaintListener;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Script.Manifest(name="Cow Hide Collector", description="Simple lumbridge cow hide collector and banker")

/**
 * Created by Ryan on 11/04/2018.
 */

public class HideCollector extends PollingScript<ClientContext> implements PaintListener {
    final static int BONE_ID = 526;
    final static int[] HIDE_IDS = {1739, 1740};
    final static int BOTTOM_STAIRS = 16671;
    final static int MIDDLE_STAIRS = 16672;
    final static int TOP_STAIRS = 16673;

    // Keep track of how many hides we've picked up
    private int HIDES_COLLECTED = 0;
    // Keep track of our inventory size
    private int INVENTORY_COUNT = 0;
    // Keep track of our run time
    private long _startTime;
    // The price of hides at start
    private int _hidePrice = 1;
    // Keep track of Bury so we know how many bones we've buried
    private int _bonesBuried = 0 ;

    private Random rand;

    // Path from field to bank
    private static final Tile[] PATH_FIELD_BANK = {
            new Tile(3252, 3286, 0),
            new Tile(3256, 3278, 0),
            new Tile(3259, 3270, 0),
            new Tile(3250, 3264, 0),
            new Tile(3255, 3249, 0),
            new Tile(3257, 3233, 0),
            new Tile(3244, 3226, 0),
            new Tile(3231, 3219, 0),
            new Tile(3215, 3218, 0),
            new Tile(3207, 3210, 0)
    };

    private static final Tile[] PATH_STAIRS_BANK = {
        new Tile(3206, 3210, 2),
        new Tile(3209, 3218, 2),
        new Tile(3209, 3220, 2)
    };

    private TilePath pathToBank, pathToField, pathStairsToBank, pathBankToStairs;

    private List<Task> taskList = new ArrayList<Task>();

    @Override
    public void start() {
        pathToBank = ctx.movement.newTilePath(PATH_FIELD_BANK);
        pathToField = ctx.movement.newTilePath(PATH_FIELD_BANK).reverse();
        pathStairsToBank = ctx.movement.newTilePath(PATH_STAIRS_BANK);
        pathBankToStairs = ctx.movement.newTilePath(PATH_STAIRS_BANK).reverse();

        INVENTORY_COUNT = ctx.inventory.select().count();

        _startTime = System.currentTimeMillis();

        GeItem hide = new GeItem(HIDE_IDS[0]);
        _hidePrice = hide.price;

        rand = new Random();

        taskList.addAll(Arrays.asList(new Bank(ctx), new Bury(ctx), new Pickup(ctx),
                new WalkToBank(ctx, pathToBank, pathStairsToBank), new WalkToField(ctx, pathToField, pathBankToStairs)));
    }

    @Override
    public void poll() {
        // Work out if we have picked up a hide
        if (INVENTORY_COUNT < ctx.inventory.select().count()) {
            Item lastCollected = ctx.inventory.itemAt(INVENTORY_COUNT);

            // Make sure we know we've picked something up
            INVENTORY_COUNT++;

            // Check if our latest item is a hide
            for (int id : HIDE_IDS) {
                if (id == lastCollected.id()) {
                    HIDES_COLLECTED++;
                    break;
                }
            }
        }

        // Check to see if we should turn on running
        if (!ctx.movement.running()) {
            int energyLevel = ctx.movement.energyLevel();
            if (energyLevel > 95) {
                ctx.movement.running(true);
            } else if (energyLevel > 70 && rand.nextFloat() < 0.1f) {
                ctx.movement.running(true);
            } else if (energyLevel > 40 && rand.nextFloat() < 0.01f) {
                ctx.movement.running(true);
            }
        }

        for (Task task : taskList) {
            if (task.activate()) {
                task.execute();
            }
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
        long minutes = ((runTime / 1000) / 60) % 60;
        long seconds = (runTime / 1000) % 60;

        // Work out our p/h stats
        float hidesPH = 0;
        long profitPH = 0;
        if (HIDES_COLLECTED > 0) {
            hidesPH = HIDES_COLLECTED / ((runTime / 1000.0f) / 3600.0f);
            profitPH = _hidePrice * (long) hidesPH;
        }

        // Draw our stats to screen
        g.drawString("" + HIDES_COLLECTED, 259, 353);
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
}
