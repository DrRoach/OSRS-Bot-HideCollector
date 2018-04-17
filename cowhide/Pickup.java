package scripts.cowhide;

import org.powerbot.script.rt4.*;

import java.util.Random;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Pickup extends Task<ClientContext> {
    public Pickup(ClientContext ctx) {
        super(ctx);
    }

    public boolean activate() {
        // Check to make sure we are wanting to pickup hide
        return ctx.inventory.select().count() < 28
                && !ctx.groundItems.select().id(HideCollector.HIDE_IDS).isEmpty();
    }

    public void execute() {
        GroundItem hide = ctx.groundItems.select().id(HideCollector.HIDE_IDS).nearest().poll();

        if (hide.inViewport()) {
            // 98% of time take hides. 2% take bones
            if (HideCollector.RAND.nextInt(0, 99) >= HideCollector.BONE_PICKUP_RATE) {
                hide.interact("Take", "Cowhide");
            } else {
                hide.interact("Take", "Bones");
            }
        } else {
            ctx.movement.step(hide);
            ctx.camera.turnTo(hide);
        }
    }
}
