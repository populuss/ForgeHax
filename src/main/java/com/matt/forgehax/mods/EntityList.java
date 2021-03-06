package com.matt.forgehax.mods;

import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;

import java.util.*;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

/**
 * Created by OverFloyd
 * may 2020
 */
@RegisterMod
public class EntityList extends HudMod {

  public EntityList() {
    super(Category.GUI, "EntityList", false, "Displays a list of all rendered entities");
  }

  private final Setting<SortMode> sortMode =
    getCommandStub()
      .builders()
      .<SortMode>newSettingEnumBuilder()
      .name("sorting")
      .description("alphabetical or length")
      .defaultTo(SortMode.LENGTH)
      .build();

  private final Setting<Boolean> items =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("items")
      .description("Include non-living entities")
      .defaultTo(true)
      .build();

  private final Setting<Boolean> animate =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("animate")
      .description("Add entities to screen one at a time")
      .defaultTo(true)
      .build();

  private final Setting<Boolean> players =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("players")
      .description("Include players")
      .defaultTo(false)
      .build();

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.TOPLEFT;
  }

  @Override
  protected int getDefaultOffsetX() { return 100; }

  @Override
  protected int getDefaultOffsetY() { return 1; }

  @Override
  protected double getDefaultScale() { return 0.5d; }

  @Override
  public boolean isInfoDisplayElement() { return false; }

  private int max_len = 0;

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!MC.gameSettings.showDebugInfo) {
      int align = alignment.get().ordinal();
      List<String> entityList = new ArrayList<>();
	    List<String> text = new ArrayList<>();

      // Prints all the "InfoDisplayElement" mods
      getWorld()
        .loadedEntityList
        .stream()
        .filter(e -> items.get() || EntityUtils.isLiving(e))
        .filter(e -> items.get() || EntityUtils.isAlive(e))
        .filter(e -> players.get() || !EntityUtils.isPlayer(e))
        .filter(
          entity ->
            !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
        .filter(EntityUtils::isValidEntity)
        .map(entity -> { if (entity instanceof EntityItem)
                            return ((EntityItem) entity).getItem().getDisplayName();
                         else
                            return entity.getDisplayName().getUnformattedText();
                       })
        .forEach(name -> entityList.add(name));

	    String buf = "";
	    int num = 0;
	    for (String element : entityList.stream().distinct().collect(Collectors.toList())) {
		    buf = String.format("%s", element);
		    num = Collections.frequency(entityList, element);
		    if (num > 1) buf += String.format(" (x%d)", num);
		    text.add(AlignHelper.getFlowDirX2(align) == 1 ? "> " + buf : buf + " <");
        if (animate.get() && text.size() >= (max_len + 1)) break;
	    }
      max_len = text.size();

      text.sort(sortMode.get().getComparator());

      // Prints on screen
      SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
    }
  }

  public enum SortMode {
    ALPHABETICAL((o1, o2) -> 0), // mod list is already sorted alphabetically
    LENGTH(Comparator.<String>comparingInt(SurfaceHelper::getTextWidth).reversed());

    private final Comparator<String> comparator;

    public Comparator<String> getComparator() {
      return this.comparator;
    }

    SortMode(Comparator<String> comparatorIn) {
      this.comparator = comparatorIn;
    }
  }
}
