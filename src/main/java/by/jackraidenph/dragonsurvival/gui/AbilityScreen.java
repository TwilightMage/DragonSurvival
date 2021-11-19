package by.jackraidenph.dragonsurvival.gui;


import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.abilities.DragonAbilities;
import by.jackraidenph.dragonsurvival.abilities.common.ActiveDragonAbility;
import by.jackraidenph.dragonsurvival.abilities.common.DragonAbility;
import by.jackraidenph.dragonsurvival.abilities.common.InnateDragonAbility;
import by.jackraidenph.dragonsurvival.abilities.common.PassiveDragonAbility;
import by.jackraidenph.dragonsurvival.capability.DragonStateProvider;
import by.jackraidenph.dragonsurvival.gui.Buttons.*;
import by.jackraidenph.dragonsurvival.util.DragonType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class AbilityScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_interface.png");
    
    private final int xSize = 256;
    private final int ySize = 256;
    private int guiLeft;
    private int guiTop;
    
    private DragonType type;
    
    public ArrayList<ActiveDragonAbility> unlockAbleSkills = new ArrayList<>();
    
    
    public AbilityScreen() {
        super(new StringTextComponent("AbilityScreenTest"));
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void init(Minecraft p_231158_1_, int width, int height)
    {
        super.init(p_231158_1_, width, height);
        
        int startX = this.guiLeft;
        int startY = this.guiTop;
        
        //Inventory
        addButton(new TabButton(startX + 5, startY - 26, 0, this));
        addButton(new TabButton(startX + 33, startY - 28, 1, this));
        addButton(new TabButton(startX + 62, startY - 26, 2, this));
        addButton(new TabButton(startX + 91, startY - 26, 3, this));
        
        
        addButton(new SkillProgressButton(guiLeft + (int)(219 / 2F), startY + 8, 4, this));
        
        for(int i = 0; i <= 4; i++){
            addButton(new SkillProgressButton(guiLeft + (int)(219 / 2F) - (i * (23 + ((4 - i) / 4))), startY + 8, 4 - i, this));
            addButton(new SkillProgressButton(guiLeft + (int)(219 / 2F) + (i * (23 + ((4 - i) / 4))), startY + 8, 4 + i, this));
        }
        
        //TODO Maybe just manually add the buttons?
        DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
     
            int num = 0;
            for(ActiveDragonAbility ability : DragonAbilities.ACTIVE_ABILITIES.get(cap.getType())){
                if(ability != null) {
                    addButton(new AbilityButton((int)(guiLeft + (92 / 2.0)), (guiTop + 40 + (num * 23)), ability, this));
                    num++;
                }
            }
            
            num = 0;
            for(PassiveDragonAbility ability : DragonAbilities.PASSIVE_ABILITIES.get(cap.getType())){
                if(ability != null) {
                    addButton(new AbilityButton(guiLeft + (int)(219 / 2F), (guiTop + 40 + (num * 23)), ability, this));
                    addButton(new IncreaseLevelButton(guiLeft + (int)(219 / 2F) + 30, (guiTop + 40 + (num * 23)), num, this));
                    addButton(new DecreaseLevelButton(guiLeft + (int)(219 / 2F) - 25, (guiTop + 40 + (num * 23)), num, this));
                    num++;
                }
            }
    
            num = 0;
            for(InnateDragonAbility ability : DragonAbilities.INFORMATION_ABILITIES.get(cap.getType())){
                if(ability != null) {
                    addButton(new AbilityButton(guiLeft + (int)(348 / 2F), (guiTop + 40 + (num * 23)), ability, this));
                    num++;
                }
            }
        });
        
        addButton(new Button(startX + (218 / 2), startY + (263 / 2) - 2, 16, 16, null, (button) -> {}){
            @Override
            public void renderButton(MatrixStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_)
            {
                if(isHovered()){
                    minecraft.getTextureManager().bind(TabButton.buttonTexture);
                    int xP = type == DragonType.SEA ? 0 : type == DragonType.FOREST ? 18 : 36;
                    GL11.glPushMatrix();
                    GL11.glTranslated(0.5, 0, 0);
                    blit(stack, x + 3, y + 3, xP / 2, 204 / 2, 9, 9, 128, 128);
                    GL11.glPopMatrix();
                }
            }
    
            @Override
            public void renderToolTip(MatrixStack stack, int mouseX, int mouseY)
            {
                ArrayList<ITextComponent> description = new ArrayList<>(Arrays.asList(new TranslationTextComponent("ds.skill.help")));
                Minecraft.getInstance().screen.renderComponentTooltip(stack, description, mouseX, mouseY);
            }
        });
    }
    
    @Override
    public void tick()
    {
        DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
            type = cap.getType();
            unlockAbleSkills.clear();
    
            for(ActiveDragonAbility ab : DragonAbilities.ACTIVE_ABILITIES.get(cap.getType())){
                DragonAbility ability = cap.getAbility(ab);
                ActiveDragonAbility db = ability != null ? ((ActiveDragonAbility)ability) : ab;
                
                if(db != null) {
                    if(db.getLevel() < db.getMaxLevel()) {
                        if (Minecraft.getInstance().player.experienceLevel >= (db.getNextRequiredLevel() - 3)) {
                            ActiveDragonAbility newActivty = db.createInstance();
                            newActivty.setLevel(db.getLevel() + 1);
                            unlockAbleSkills.add(newActivty);
                        }
                    }
                }
            }
    
            unlockAbleSkills.sort(Comparator.comparingInt(c -> c.getNextRequiredLevel()));
        });
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft == null)
            return;

        this.renderBackground(stack);

        
        int startX = this.guiLeft;
        int startY = this.guiTop;
        
        this.minecraft.getTextureManager().bind(BACKGROUND_TEXTURE);
        blit(stack, startX, startY, 0, 0, 256, 256);
        
        if(type != null) {
            int barYPos = type == DragonType.SEA ? 198 : type == DragonType.FOREST ? 186 : 192;
    
            minecraft.getTextureManager().bind(TabButton.buttonTexture);
    
            float progress = MathHelper.clamp((minecraft.player.experienceLevel / 50F), 0, 1);
            float progress1 = Math.min(1F, (Math.min(0.5F, progress) * 2F));
            float progress2 = Math.min(1F, (Math.min(0.5F, progress - 0.5F) * 2F));
    
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5F, 0.75F, 0F);
            blit(stack, startX + (23 / 2), startY + 28, 0, 180 / 2, 105, 3, 128, 128);
            blit(stack, startX + (254 / 2), startY + 28,  0, 180 / 2,105, 3, 128, 128);

            blit(stack, startX + (23 / 2), startY + 28, 0, barYPos / 2, (int)(105 * progress1), 3, 128, 128);

            if (progress > 0.5) {
                blit(stack, startX + (254 / 2), startY + 28, 0, barYPos / 2, (int)(105 * progress2), 3, 128, 128);
            }
            
            int expChange = -1;
            
            for(Widget btn : buttons) {
                if (!btn.isHovered()) continue;
    
                if (btn instanceof IncreaseLevelButton) {
                    expChange = ((IncreaseLevelButton)btn).skillCost;
                    break;
                } else if (btn instanceof SkillProgressButton) {
                    expChange = ((SkillProgressButton)btn).skillCost;
                    break;
                }
            }
            
            if(expChange != -1){
                float Changeprogress = MathHelper.clamp((expChange / 50F), 0, 1); //Total exp required to hit level 50
                float Changeprogress1 = Math.min(1F, (Math.min(0.5F, Changeprogress) * 2F));
                float Changeprogress2 = Math.min(1F, (Math.min(0.5F, Changeprogress - 0.5F) * 2F));
                
                blit(stack, startX + (23 / 2), startY + 28, 0, 174 / 2, (int)(105 * Changeprogress1), 3, 128, 128);
    
                if (Changeprogress2 > 0.5) {
                    blit(stack, startX + (254 / 2) - (int)(105 * progress1), startY + 28, 0, 174 / 2, (int)(105 * Changeprogress2), 3, 128, 128);
                }
            }
    
            GL11.glPopMatrix();
    
            //TODO Set font size
            GL11.glPushMatrix();
            ITextComponent textComponent = new StringTextComponent(Integer.toString(minecraft.player.experienceLevel)).withStyle(TextFormatting.DARK_GRAY);
            int xPos = startX + 117 + 1;
            float finalXPos = (float)(xPos - minecraft.font.width(textComponent) / 2);
            minecraft.font.draw(stack, textComponent, finalXPos, startY + 26, 0);
            GL11.glPopMatrix();
        }
    
        super.render(stack, mouseX, mouseY, partialTicks);
        
        for(Widget btn : buttons){
            if(btn.isHovered()){
                btn.renderToolTip(stack, mouseX, mouseY);
            }
        }
    }
    
    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize / 2) / 2;
    }
}
