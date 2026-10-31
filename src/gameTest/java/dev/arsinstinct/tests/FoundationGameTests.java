package dev.arsinstinct.tests;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import dev.arsinstinct.tests.shared.FoundationCases;
@GameTestHolder("ars_instinct") @PrefixGameTestTemplate(false)
public final class FoundationGameTests {
 @GameTest(template="empty",timeoutTicks=80) public static void nativeTouch(GameTestHelper h){dev.arsinstinct.tests.shared.TouchCases.nativeInteractions(h,false);}
 @GameTest(template="empty",timeoutTicks=80,batch="trigger_billing") public static void triggeredTouch(GameTestHelper h){dev.arsinstinct.tests.shared.TouchCases.nativeInteractions(h,true);}
 @GameTest(template="empty",timeoutTicks=80) public static void nativeStorage(GameTestHelper h){FoundationCases.nativeStorageSurvivesReloadAndReconfiguration(h);}
 @GameTest(template="empty",timeoutTicks=80) public static void estimates(GameTestHelper h){FoundationCases.estimatesMergePoolsAndKeepUnknownCosts(h);}
 @GameTest(template="empty",timeoutTicks=80) public static void optIn(GameTestHelper h){FoundationCases.optInInstallationIsScopedAndIdempotent(h);}
 @GameTest(template="empty",timeoutTicks=180) public static void nativeMobCombat(GameTestHelper h){FoundationCases.ordinaryMobRegeneratesAttacksHealsAndUsesProviderCooldown(h);}
 @GameTest(template="empty",timeoutTicks=180,batch="trigger_billing") public static void triggeredMobCombat(GameTestHelper h){FoundationCases.ordinaryMobTriggerBilling(h);}
}
