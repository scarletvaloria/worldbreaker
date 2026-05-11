    package net.scarletvaloria.worldbreaker.index;

    import net.minecraft.entity.player.PlayerEntity;
    import net.minecraft.nbt.NbtCompound;
    import net.minecraft.registry.RegistryWrapper;
    import net.minecraft.server.network.ServerPlayerEntity;
    import org.ladysnake.cca.api.v3.component.Component;
    import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

    public class FormStateComponent implements Component, AutoSyncedComponent {

        private WorldbreakerState state = WorldbreakerState.NORMAL;
        private final PlayerEntity player;

        public FormStateComponent(PlayerEntity player) {
            this.player = player;
        }

        public WorldbreakerState getState() {
            return state;
        }

        public boolean isActive() {
            return state == WorldbreakerState.WORLDBREAKER;
        }

        public void setState(WorldbreakerState state) {
            this.state = state;
            ModComponents.FORM_STATE.sync(this.player);
        }

        @Override
        public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            this.state = WorldbreakerState.valueOf(nbt.getString("state"));
        }

        @Override
        public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putString("state", this.state.name());
        }

        @Override
        public boolean shouldSyncWith(ServerPlayerEntity player) {
            return true;
        }
    }
