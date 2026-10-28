package my.celium.org.morebeaconcolors.client;

import java.lang.reflect.Field;

import my.celium.org.client.MycelScreens;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.data.BeamPainter;
import my.celium.org.morebeaconcolors.data.BeamStyle;
import my.celium.org.client.MycelTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The button beside the beacon screen that opens {@link BeamColorScreen}: a
 * beacon icon over a strip in the beam's current color. Placed just outside
 * the beacon window's top-right corner (left of it on narrow screens).
 * Client only.
 */
public final class BeamButton extends AbstractButton {
    static final int SIZE = 22;

    private final BeaconScreen beacon;
    private final BlockPos pos;
    private final ItemStack icon = new ItemStack(Items.BEACON);

    private BeamButton(BeaconScreen beacon, BlockPos pos, int x, int y) {
        super(x, y, SIZE, SIZE, Component.literal("Beam color"));
        this.beacon = beacon;
        this.pos = pos;
        setTooltip(Tooltip.create(Component.literal("Beam color")));
    }

    /** The button for {@code screen}, or {@code null} when it is not a beacon (or the button is off). */
    public static BeamButton create(Screen screen) {
        try {
            if (!(screen instanceof BeaconScreen beacon) || !MoreBeaconColorsMod.isModEnabled()
                    || !MoreBeaconColorsMod.showButton.get()) {
                return null;
            }
            BlockPos pos = lookedAtBeacon();
            if (pos == null) {
                return null;
            }
            int left = readInt(beacon, "leftPos");
            int top = readInt(beacon, "topPos");
            int imageWidth = readInt(beacon, "imageWidth");
            int x = left + imageWidth + 4;
            if (x + SIZE > beacon.width - 2) {
                x = Math.max(2, left - SIZE - 4);
            }
            return new BeamButton(beacon, pos, x, top + 4);
        } catch (RuntimeException | ReflectiveOperationException e) {
            MoreBeaconColorsMod.debug("Beam button not added: {}", e.toString());
            return null;
        }
    }

    /** The beacon the player opened: the block they are looking at. */
    static BlockPos lookedAtBeacon() {
        Minecraft minecraft = Minecraft.getInstance();
        HitResult hit = minecraft.hitResult;
        if (minecraft.level == null || !(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = blockHit.getBlockPos();
        return minecraft.level.getBlockState(pos).is(Blocks.BEACON) ? pos.immutable() : null;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        MycelScreens.open(new BeamColorScreen(beacon, pos));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float a) {
        int x0 = getX();
        int y0 = getY();
        boolean hot = isHoveredOrFocused();
        gfx.fill(x0, y0, x0 + SIZE, y0 + SIZE, hot ? 0xFF8A8A8A : MycelTheme.BORDER);
        gfx.fill(x0 + 1, y0 + 1, x0 + SIZE - 1, y0 + SIZE - 1, hot ? MycelTheme.BORDER : MycelTheme.SURFACE);
        Minecraft minecraft = Minecraft.getInstance();
        BeamStyle style = minecraft.level == null ? BeamStyle.VANILLA : ClientBeams.style(minecraft.level, pos);
        if (!style.isVanilla()) {
            double ticks = Util.getMillis() / 50.0;
            int rgb = BeamPainter.baseColor(style, (long) ticks, (float) (ticks % 1));
            gfx.fill(x0 + 3, y0 + SIZE - 5, x0 + SIZE - 3, y0 + SIZE - 3, 0xFF000000 | rgb);
        }
        gfx.item(icon, x0 + 3, y0 + 2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    static int readInt(AbstractContainerScreen<?> screen, String name) throws ReflectiveOperationException {
        Field field = AbstractContainerScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(screen);
    }
}
