package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import static com.matt.forgehax.Helper.*;

@RegisterMod
public class Biome extends ToggleMod {

  public Biome() {
    super(Category.GUI, "Biome", false, "Shows the biome you're currently in");
  }

  public enum Mode {
    PLAYER,
    VIEWENTITY
  }

  public final Setting<Mode> mode =
    getCommandStub()
      .builders()
      .<Mode>newSettingEnumBuilder()
      .name("mode")
      .description("Player or viewentity position (mainly for freecam)")
      .defaultTo(Mode.VIEWENTITY)
      .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  double thisX;
  double thisY;
  double thisZ;

  public String getInfoDisplayText() {
    switch (mode.get()) {
      case VIEWENTITY: {
        Entity viewEntity = getRenderEntity();
        thisX = viewEntity.posX;
        thisY = viewEntity.posY;
        thisZ = viewEntity.posZ;
        break;
      }
      case PLAYER: {
        EntityPlayerSP player = getLocalPlayer();
        thisX = player.posX;
        thisY = player.posY;
        thisZ = player.posZ;
        break;
      }
    }

    BlockPos position = new BlockPos(thisX, thisY ,thisZ);

    return "Biome: " + String.format("%s", MC.world.getBiomeForCoordsBody(position).getBiomeName());
  }
}
