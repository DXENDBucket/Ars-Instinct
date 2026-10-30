package dev.arsinstinct;
@net.neoforged.fml.common.Mod("ars_instinct")
public final class ArsInstinct {
    public ArsInstinct(net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,dev.arsinstinct.platform.InstinctConfig.SPEC,"ars_instinct-server.toml");
        dev.arsinstinct.platform.InstinctSetup.register();
    }
}
