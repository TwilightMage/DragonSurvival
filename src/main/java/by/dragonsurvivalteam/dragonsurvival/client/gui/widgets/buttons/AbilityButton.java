package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.magic.MagicDragonRender;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicCap;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class AbilityButton extends Button implements TooltipRender{
	public static final ResourceLocation BLANK_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/blank.png");
	public static final ResourceLocation BLANK_1_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/blank1.png");
	public static final ResourceLocation BLANK_2_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/blank2.png");
	public static final ResourceLocation BLANK_3_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/blank3.png");

	private final Screen screen;
	private AbstractDragonType type;
	private int slot;
	public int skillType;

	public DragonAbility ability;

	public AbilityButton(int x, int y, int skillType, int slot, Screen screen){
		super(x, y, 16, 16, null, button -> {});
		this.slot = slot;
		this.skillType = skillType;
		this.screen = screen;
	}

	public boolean dragging = false;

	@Override
	protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY){
		super.onDrag(pMouseX, pMouseY, pDragX, pDragY);

		if(skillType == 0){
			dragging = true;

			screen.renderables.forEach(s-> {
				if(s instanceof AbilityButton btn){
					if(btn != this && btn.skillType == 0){
						btn.onRelease(pMouseX, pMouseY);
					}
				}
			});
		}
	}

	@Override
	public void onClick(double pMouseX, double pMouseY){
		super.onClick(pMouseX, pMouseY);

		if(skillType == 0){
			screen.renderables.forEach(s-> {
				if(s instanceof AbilityButton btn){
					if(btn != this && btn.skillType == 0 && btn.dragging){
						MagicCap cap = DragonUtils.getHandler(Minecraft.getInstance().player).getMagicData();
						btn.onRelease(pMouseX, pMouseY);
						DragonAbility ab1 = cap.getAbilityFromSlot(btn.slot);
						DragonAbility ab2 = cap.getAbilityFromSlot(slot);
						cap.activeDragonAbilities.put(slot, ab1.getName());
						cap.activeDragonAbilities.put(btn.slot, ab2.getName());
						NetworkHandler.CHANNEL.sendToServer(new SyncMagicCap(Minecraft.getInstance().player.getId(), cap));
					}
				}
			});
		}
	}

	@Override
	public void onRelease(double pMouseX, double pMouseY){
		super.onRelease(pMouseX, pMouseY);

		if(skillType == 0){
			dragging = false;
		}
	}

	@Override
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float p_230431_4_){
		DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
			DragonAbility ab =
				skillType == 0 ? cap.getMagicData().getAbilityFromSlot(slot) :
					skillType == 1 ? cap.getMagicData().getPassiveAbilityFromSlot(slot) :
						skillType == 2 ? cap.getMagicData().getInnateAbilityFromSlot(slot) : null;

			if(ab != null)
				ability = ab;

			type = cap.getType();
		});

		boolean isDragging = false;

		if(skillType == 0){
			for(Widget s : screen.renderables){
				if(s instanceof AbilityButton btn){
					if(btn != this && btn.skillType == 0 && btn.dragging){
						isDragging = true;
						break;
					}
				}
			}
		}

		RenderSystem.setShaderTexture(0, isDragging ? BLANK_3_TEXTURE : ability instanceof PassiveDragonAbility ? BLANK_2_TEXTURE : BLANK_1_TEXTURE);
		blit(stack, x - 1, y - 1, 0, 0, 20, 20, 20, 20);


		if(ability != null && !dragging){
			RenderSystem.setShaderTexture(0, ability.getIcon());
			blit(stack, x, y, 0, 0, 18, 18, 18, 18);

			if(ability.isDisabled()){
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, MagicDragonRender.INVALID_ICON);
				blit(stack, x, y, 0, 0, 18, 18, 18, 18);
				RenderSystem.disableBlend();
			}
		}
	}

	@Override
	public void renderToolTip(PoseStack stack, int mouseX, int mouseY){
		if(ability != null){
			FormattedText desc = ability.getDescription();

			if(ability.getInfo().size() > 0){
				desc = FormattedText.composite(desc, new TextComponent("\n\n"));
			}

			List<FormattedCharSequence> description = Minecraft.getInstance().font.split(desc, 143);
			int yPos = y - description.size() * 7;

			MagicDragonRender.drawAbilityHover(stack, x + width * 2, yPos, ability);
		}
	}
}