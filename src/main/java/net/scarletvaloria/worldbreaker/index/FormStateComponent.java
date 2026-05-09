    package net.scarletvaloria.worldbreaker.index;

    import net.minecraft.entity.player.PlayerEntity;
    import net.minecraft.nbt.NbtCompound;
    import net.minecraft.registry.RegistryWrapper;
    import net.minecraft.server.network.ServerPlayerEntity;
    import org.ladysnake.cca.api.v3.component.Component;
    import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

    public class FormStateComponent implements Component, AutoSyncedComponent {
        private boolean active = false;
        private final PlayerEntity player;

        public FormStateComponent(PlayerEntity player) { this.player = player; }

        public boolean isActive() { return active; }

        public void setActive(boolean active) {
            this.active = active;
            ModComponents.FORM_STATE.sync(this.player);
        }
            @Override
            public boolean shouldSyncWith(ServerPlayerEntity player) {
                return true;
        }
        @Override
        public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) { this.active = nbt.getBoolean("active"); }
        @Override
        public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) { nbt.putBoolean("active", this.active); }
    }
