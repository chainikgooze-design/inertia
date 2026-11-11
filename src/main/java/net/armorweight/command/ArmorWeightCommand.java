package net.armorweight.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.armorweight.ArmorWeightMod;
import net.armorweight.api.EncumbranceData;
import net.armorweight.config.WeightClientConfig;
import net.armorweight.util.WeightCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(modid = ArmorWeightMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ArmorWeightCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_A_PLAYER =
            new SimpleCommandExceptionType(Component.literal("This command must be executed by a player when omitting target."));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("armorweight")
                        .then(Commands.literal("check")
                                .executes(ArmorWeightCommand::executeSelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(ArmorWeightCommand::executeTarget)
                                )
                        )
        );
    }

    private static int executeSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            throw ERROR_NOT_A_PLAYER.create();
        }
        return reportPlayerWeight(source, player, true);
    }

    private static int executeTarget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        return reportPlayerWeight(source, target, false);
    }

    private static int reportPlayerWeight(CommandSourceStack source, ServerPlayer target, boolean isSelf) {
        EncumbranceData data = WeightCalculator.calculate(target);
        String unit = WeightClientConfig.DISPLAY_UNIT.get();

        String formattedWeight = String.format(Locale.ROOT, "%.1f", data.totalWeight());
        String formattedSpeedPenalty = String.format(Locale.ROOT, "%.1f", data.speedPenaltyFraction() * 100.0D);
        String formattedJumpPenalty = String.format(Locale.ROOT, "%.1f", data.jumpPenaltyFraction() * 100.0D);
        String formattedArmor = String.format(Locale.ROOT, "%.1f", data.rawArmor());
        String formattedToughness = String.format(Locale.ROOT, "%.1f", data.rawToughness());

        Component header = isSelf
                ? Component.translatable(
                        "command.armor_weight.check.self",
                        formattedWeight,
                        unit,
                        formattedSpeedPenalty,
                        formattedJumpPenalty
                ).withStyle(ChatFormatting.AQUA)
                : Component.translatable(
                        "command.armor_weight.check.target",
                        target.getName().getString(),
                        formattedWeight,
                        unit,
                        formattedSpeedPenalty,
                        formattedJumpPenalty
                ).withStyle(ChatFormatting.GOLD);

        Component details = Component.translatable(
                "command.armor_weight.check.details",
                formattedArmor,
                formattedToughness
        ).withStyle(ChatFormatting.GRAY);

        source.sendSuccess(() -> header, false);
        source.sendSuccess(() -> details, false);

        return 1;
    }

    private ArmorWeightCommand() {
    }
}
