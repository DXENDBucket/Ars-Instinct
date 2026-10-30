package dev.arsinstinct;
@net.minecraftforge.fml.common.Mod("ars_instinct")
public final class ArsInstinct {
    public ArsInstinct() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                dev.arsinstinct.platform.InstinctConfig.SPEC,"ars_instinct-server.toml");
        dev.arsinstinct.platform.InstinctSetup.register();
    }
}
