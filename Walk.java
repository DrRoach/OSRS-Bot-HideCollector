package scripts;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.TilePath;

/**
 * Created by Ryan on 11/04/2018.
 */
public class Walk extends Task<ClientContext> {
    private HideCollector.State state;

    private TilePath pathToBank, pathToField, pathStairsToBank, pathBankToStairs;

    private int groundStairs = 16671;
    private int middleStairs = 16672;
    private int topStairs = 16673;

    public Walk(ClientContext ctx, HideCollector.State state, TilePath pathToBank, TilePath pathToField,
                TilePath pathStairsToBank, TilePath pathBankToStairs) {
        super(ctx);

        this.state = state;
        this.pathToBank = pathToBank;
        this.pathToField = pathToField;
        this.pathStairsToBank = pathStairsToBank;
        this.pathBankToStairs = pathBankToStairs;
    }

    public boolean activate() {
        return false;
    }

    public void execute() {
        switch (state) {
            case WALK_TO_BANK:
                walkToBank();
                break;
            case WALK_TO_FIELD:
                walkToField();
                break;
        }
    }

    private void walkToBank() {
        GameObject botStairs = ctx.objects.select(5).id(groundStairs).nearest().poll();
        GameObject midStairs = ctx.objects.select(5).id(middleStairs).nearest().poll();
        GameObject tStairs = ctx.objects.select(5).id(topStairs).nearest().poll();

        if (botStairs.id() == -1 && midStairs.id() == -1 && tStairs.id() == -1) {
            pathToBank.traverse();
        } else if (ctx.objects.select().id(groundStairs).nearest().isEmpty() == false && ctx.inventory.select().count() == 28) {
            if (botStairs.inViewport()) {
                botStairs.interact("Climb-up", "Staircase");
            } else {
                ctx.camera.turnTo(botStairs);
            }
        } else if (ctx.objects.select().id(middleStairs).nearest().isEmpty() == false && ctx.inventory.select().count() == 28) {
            if (midStairs.inViewport()) {
                midStairs.interact("Climb-up", "Staircase");
            } else {
                ctx.camera.turnTo(midStairs);
            }
        } else {
            pathStairsToBank.traverse();
        }
    }

    private void walkToField() {
        GameObject botStairs = ctx.objects.select().id(groundStairs).nearest().poll();
        GameObject midStairs = ctx.objects.select().id(middleStairs).nearest().poll();
        GameObject tStairs = ctx.objects.select().id(topStairs).nearest().poll();

        if (botStairs.id() == -1 && midStairs.id() == -1 && tStairs.id() == -1) {
            pathToField.traverse();
        } else if (ctx.objects.select().id(topStairs).nearest().isEmpty() == false && ctx.inventory.select().count() < 28) {
            if (tStairs.inViewport()) {
                tStairs.interact("Climb-down", "Staircase");
            } else {
                ctx.movement.step(tStairs);
                ctx.camera.turnTo(tStairs);
            }
        } else if (ctx.objects.select().id(middleStairs).nearest().isEmpty() == false && ctx.inventory.select().count() < 28) {
            if (midStairs.inViewport()) {
                midStairs.interact("Climb-down", "Staircase");
            } else {
                ctx.movement.step(midStairs);
                ctx.camera.turnTo(midStairs);
            }
        } else {
            pathBankToStairs.traverse();
        }
    }
}
