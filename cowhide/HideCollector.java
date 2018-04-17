package scripts.cowhide;

import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GeItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Script.Manifest(name="Cow Hide Collector", description="Simple lumbridge cow hide collector and banker")

/**
 * Created by Ryan on 11/04/2018.
 */

public class HideCollector extends PollingScript<ClientContext> implements PaintListener, ChangeListener {
    final static int BONE_ID = 526;
    final static int[] HIDE_IDS = {1739, 1740};
    final static int BOTTOM_STAIRS = 16671;
    final static int MIDDLE_STAIRS = 16672;
    final static int TOP_STAIRS = 16673;
    final static Random RAND = new Random();
    // Rate at which we pickup bones. This is a %
    public static int BONE_PICKUP_RATE = 2;

    // Keep track of how many hides we've picked up
    private int HIDES_COLLECTED = 0;
    // Keep track of our inventory size
    private int INVENTORY_COUNT = 0;
    // The price of hides at start
    private int HIDE_PRICE = 1;


    private Bury bury;

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

        GeItem hide = new GeItem(HIDE_IDS[0]);
        HIDE_PRICE = hide.price;

        // Display slider to make user choose how often to pickup bones
        JPanel setup = new JPanel();
        JSlider boneRate = new JSlider(JSlider.HORIZONTAL, 0, 50, 5);
        boneRate.addChangeListener(this);
        boneRate.setMajorTickSpacing(10);
        boneRate.setMinorTickSpacing(1);
        boneRate.setPaintTicks(true);
        boneRate.setPaintLabels(true);

        final JFrame frame = new JFrame("Setup");
        JButton okayButton = new JButton("Okay");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });

        setup.add(boneRate);
        setup.add(okayButton);

        setup.setVisible(true);

        frame.setContentPane(setup);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // We need to store Bury so that we know how many bones we've buried in paint
        bury = new Bury(ctx);

        taskList.addAll(Arrays.asList(new Bank(ctx), bury, new Pickup(ctx),
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
            } else if (energyLevel > 70 && HideCollector.RAND.nextInt(0, 99) <= 9) {
                ctx.movement.running(true);
            } else if (energyLevel > 40 && HideCollector.RAND.nextInt(0, 99) <= 1) {
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
        int width = 200;
        int height = 130;

        Color rectBankground = new Color(0, 0, 0, 127);
        g.setColor(rectBankground);

        g.fillRect(10, 10, width, height);

        g.setColor(Color.BLACK);
        g.drawRect(10, 10, width, height);

        g.setColor(Color.WHITE);
        Font font = new Font("Raleway", 0, 12);
        g.setFont(font);

        long runTime = getRuntime();
        long hours = (runTime / 1000) / 3600;
        long minutes = ((runTime / 1000) / 60) % 60;
        long seconds = (runTime / 1000) % 60;

        float hidesPH = 0;
        long profitPH = 0;

        if (HIDES_COLLECTED > 0) {
            hidesPH = HIDES_COLLECTED / ((runTime / 1000.0f) / 3600.0f);
            profitPH = HIDE_PRICE * (long) hidesPH;
        }

        int bonesBuried = 0;
        if (bury != null) {
            bonesBuried = bury.bonesBuried();
        }

        // Draw our stats to screen
        g.drawString("Hides Collected: " + HIDES_COLLECTED, 20, 40);
        g.drawString("Hides PH: " + (int) hidesPH, 20, 55);
        g.drawString("Profit PH: " + profitPH, 20, 70);
        g.drawString("Bones Buried: " + bonesBuried, 20, 85);
        g.drawString("Time Ran: " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds), 20, 100);

        Font byFont = new Font("Raleway",1, 8);
        g.setFont(byFont);
        g.drawString("Made by DrRoach", 20, 125);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();

        if (!source.getValueIsAdjusting()) {
            BONE_PICKUP_RATE = (int) source.getValue();
        }
    }
}
