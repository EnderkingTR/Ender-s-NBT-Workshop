package com.nadir.endersnbt.client.gui;

import com.nadir.endersnbt.network.SaveNbtC2SPacket;
import com.nadir.endersnbt.network.SendNbtS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;

public class NbtEditorScreen extends Screen {
    private final SendNbtS2CPacket initialPayload;
    private boolean isHand;
    private int activeTab = 0;

    private NbtTreeListWidget list;
    private net.minecraft.client.gui.components.EditBox editBox;
    private NbtNode editingNode;

    private net.minecraft.client.gui.components.EditBox nameBox;
    private net.minecraft.client.gui.components.Checkbox unbreakableBox;
    private EnchantmentListWidget enchantmentList;
    private Button btnAddEnchant;

    public NbtEditorScreen(SendNbtS2CPacket payload) {
        super(Component.literal("NBT Editor"));
        this.initialPayload = payload;
    }

    @Override
    protected void init() {
        super.init();
        isHand = initialPayload.targetType() == com.nadir.endersnbt.network.TargetType.HAND;
        activeTab = isHand ? 0 : 1;

        if (isHand) {
            this.addRenderableWidget(Button.builder(Component.literal("Quick Edit"), btn -> switchTab(0))
                    .bounds(10, 10, 80, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Raw NBT"), btn -> switchTab(1))
                .bounds(isHand ? 95 : 10, 10, 80, 20).build());

        this.list = new NbtTreeListWidget(this, this.minecraft, this.width, this.height, 40, 20, initialPayload.tag());
        this.addRenderableWidget(this.list);

        this.editBox = new net.minecraft.client.gui.components.EditBox(this.font, 0, 0, 200, 16, Component.empty());
        this.editBox.setVisible(false);
        this.addRenderableWidget(this.editBox);

        if (isHand) {
            initQuickEdit();
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> saveAndClose())
                .bounds(this.width / 2 - 105, this.height - 30, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> this.onClose())
                .bounds(this.width / 2 + 5, this.height - 30, 100, 20).build());

        switchTab(activeTab);
    }

    private void initQuickEdit() {
        NbtNode components = list.getRootNode().getOrCreateChild("components", new CompoundTag());

        String initialName = "";
        NbtNode nameNode = components.getChild("minecraft:custom_name");
        if (nameNode != null) {
            String json = nameNode.tag.getAsString();
            try {
                Component comp = Component.Serializer.fromJson(json, this.minecraft.level.registryAccess());
                if (comp != null) initialName = comp.getString();
            } catch (Exception e) {
                initialName = json;
            }
        }

        boolean initialUnbreakable = components.getChild("minecraft:unbreakable") != null;

        this.nameBox = new net.minecraft.client.gui.components.EditBox(this.font, 10, 50, 250, 20, Component.literal("Name"));
        this.nameBox.setValue(initialName);
        this.nameBox.setResponder(val -> {
            if (!val.isEmpty()) {
                String json = "{\"text\":\"" + val.replace("\"", "\\\"") + "\"}";
                components.getOrCreateChild("minecraft:custom_name", StringTag.valueOf(json)).tag = StringTag.valueOf(json);
            } else {
                NbtNode n = components.getChild("minecraft:custom_name");
                if (n != null) components.children.remove(n);
            }
        });
        this.addRenderableWidget(this.nameBox);

        this.unbreakableBox = net.minecraft.client.gui.components.Checkbox.builder(Component.literal("Unbreakable"), this.font)
                .pos(10, 80).selected(initialUnbreakable).build();
        this.addRenderableWidget(this.unbreakableBox);

        this.enchantmentList = new EnchantmentListWidget(this, this.minecraft, 250, this.height - 150, 110, 24);
        this.enchantmentList.setX(10);
        this.addRenderableWidget(this.enchantmentList);

        NbtNode enchantNode = components.getOrCreateChild("minecraft:enchantments", new CompoundTag());
        NbtNode levelsNode = enchantNode.getOrCreateChild("levels", new CompoundTag());
        this.enchantmentList.rebuild(levelsNode);

        this.btnAddEnchant = this.addRenderableWidget(Button.builder(Component.literal("Add Enchantment"), btn -> openEnchantDropdown(levelsNode))
                .bounds(10, this.height - 40, 250, 20).build());
    }

    private void openEnchantDropdown(NbtNode levelsNode) {
        this.minecraft.setScreen(new EnchantmentSelectionScreen(this, levelsNode));
    }

    public void rebuildEnchantmentList() {
        if (enchantmentList != null) {
            NbtNode components = list.getRootNode().getChild("components");
            if (components != null) {
                NbtNode enchantNode = components.getChild("minecraft:enchantments");
                if (enchantNode != null) {
                    NbtNode levelsNode = enchantNode.getChild("levels");
                    enchantmentList.rebuild(levelsNode);
                }
            }
        }
    }

    private void switchTab(int tab) {
        this.activeTab = tab;
        boolean quick = tab == 0;

        if (this.nameBox != null) this.nameBox.visible = quick;
        if (this.unbreakableBox != null) this.unbreakableBox.visible = quick;
        if (this.enchantmentList != null) this.enchantmentList.visible = quick;
        if (this.btnAddEnchant != null) this.btnAddEnchant.visible = quick;

        if (this.list != null) this.list.visible = !quick;
        if (this.editBox != null) this.editBox.setVisible(false);
    }

    public void openEditBox(NbtNode node, int x, int y, int width) {
        if (activeTab != 1) return;
        this.editingNode = node;
        this.editBox.setX(x);
        this.editBox.setY(y);
        this.editBox.setWidth(width);
        this.editBox.setValue(node.tag.toString());
        this.editBox.setVisible(true);
        this.setFocused(this.editBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.editBox != null && this.editBox.isVisible() && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            try {
                CompoundTag parsed = net.minecraft.nbt.TagParser.parseTag("{v:" + this.editBox.getValue() + "}");
                this.editingNode.tag = parsed.get("v");
            } catch (Exception e) {}
            this.editBox.setVisible(false);
            this.setFocused(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveAndClose() {
        if (isHand && this.unbreakableBox != null) {
            NbtNode components = list.getRootNode().getOrCreateChild("components", new CompoundTag());
            if (this.unbreakableBox.selected()) {
                CompoundTag unb = new CompoundTag();
                unb.putBoolean("show_in_tooltip", true);
                components.getOrCreateChild("minecraft:unbreakable", unb).tag = unb;
            } else {
                NbtNode n = components.getChild("minecraft:unbreakable");
                if (n != null) components.children.remove(n);
            }
        }

        CompoundTag newTag = (CompoundTag) this.list.getRootNode().rebuildTag();
        ClientPlayNetworking.send(new SaveNbtC2SPacket(
                initialPayload.targetType(),
                initialPayload.pos(),
                initialPayload.entityId(),
                initialPayload.hand(),
                newTag
        ));
        
        if (this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(Component.literal("§aNBT successfully saved!"), false);
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        if (activeTab == 0 && isHand) {
            guiGraphics.drawString(this.font, "Quick Edit:", 10, 35, 0xAAAAAA, false);
        }
    }
}
