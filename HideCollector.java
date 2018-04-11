package scripts;

import org.powerbot.script.PaintListener;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.TilePath;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

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

    // Path from field to bank
    public static final Tile[] PATH_FIELD_BANK = {
            new Tile(3258, 3275, 0),
            new Tile(3250, 3266, 0),
            new Tile(3253, 3251, 0),
            new Tile(3260, 3239, 0),
            new Tile(3254, 3225, 0),
            new Tile(3239, 3225, 0),
            new Tile(3227, 3218, 0),
            new Tile(3214, 3218, 0),
            new Tile(3211, 3210, 0)
    };

    public static final Tile[] PATH_STAIRS_BANK = {
        new Tile(3206, 3210, 2),
        new Tile(3209, 3220, 2)
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

    @Override
    public void repaint(Graphics g) {
        Image background = getImage("http://oi46.tinypic.com/2jdkgi1.jpg");
        Font font1 = new Font("Verdana", 0, 20);

        g.drawImage(background, 7, 280, null);

        g.setColor(Color.WHITE);
        g.setFont(font1);
        g.drawString("Hello player", 30, 300);
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
            return State.BURY;
        } else if (ctx.bank.nearest().tile().distanceTo(ctx.players.local()) < 4) {
            return State.BANK;
        } else {
            return State.WALK_TO_BANK;
        }
    }
}
