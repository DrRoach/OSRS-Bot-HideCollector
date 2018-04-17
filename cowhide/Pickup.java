package scripts.cowhide;

import org.powerbot.script.rt4.*;

import java.util.Random;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Pickup extends Task<ClientContext> {
    private int hideId = 1739;
    private int boneId = 526;
    private int pickedUp = 0;

    // Random number gen to randomly pickup bones
    private Random rand = null;

    public Pickup(ClientContext ctx) {
        super(ctx);

        rand = new Random();
    }

    public boolean activate() {
        // Check to make sure we are wanting to pickup hide
        return ctx.inventory.select().count() < 28
                && ctx.groundItems.select().id(hideId).isEmpty() == false
                && ctx.players.local().animation() == -1;
    }

    public void execute() {
        GroundItem hide = ctx.groundItems.select().id(hideId).nearest().poll();

        if (hide.inViewport()) {
            // 98% of time take hides. 2% take bones
            if (rand.nextFloat() > 0.02f) {
                this.pickupHide(hide);
            } else {
                this.pickupBones(hide);
            }
        } else {
            ctx.movement.step(hide);
            ctx.camera.turnTo(hide);
        }
    }

    private void pickupHide(GroundItem item) {
        item.interact("Take", "Cowhide");
    }

    private void pickupBones(GroundItem item) {
        item.interact("Take", "Bones");
    }
}
