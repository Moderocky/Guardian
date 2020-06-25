package com.moderocky.guardian.command;

import com.moderocky.guardian.Guardian;
import com.moderocky.guardian.api.GuardianAPI;
import com.moderocky.guardian.config.GuardianConfig;
import com.moderocky.guardian.util.Messenger;
import com.moderocky.mask.api.MagicList;
import com.moderocky.mask.command.Commander;
import com.moderocky.mask.template.WrappedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuardianCommand extends Commander<CommandSender> implements WrappedCommand {

    public static GuardianCommand command;
    private final @NotNull GuardianConfig config = Guardian.getInstance().getGuardianConfig();
    private final @NotNull Messenger messenger = Guardian.getMessenger();
    private final @NotNull GuardianAPI api = Guardian.getApi();

    public GuardianCommand() {
        super();
        command = this;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return Arrays.asList("g", "guard");
    }

    @Override
    public @NotNull String getUsage() {
        return "/" + getCommand();
    }

    @Override
    public @NotNull String getDescription() {
        return "The main command for Guardian.";
    }

    @Override
    public @Nullable String getPermission() {
        return "guardian.command." + getCommand();
    }

    @Override
    public @Nullable String getPermissionMessage() {
        return null;
    }

    @Override
    public @NotNull Main create() {
        return command("guardian")
                .arg("flags", sender -> {
                    ComponentBuilder builder = new ComponentBuilder("List of Flags:").color(ChatColor.WHITE);
                    for (BaseComponent[] flag : new MagicList<>(api.getProtectionFlags()).collect((Function<String, BaseComponent[]>) string -> TextComponent.fromLegacyText(string, ChatColor.GRAY))) {
                        builder.append(System.lineSeparator()).append(flag);
                    }
                    messenger.sendMessage(builder.create());
                })
                .arg("about", sender -> messenger.sendMessage(new ComponentBuilder("").color(ChatColor.WHITE)
                        .append("Guardian").color(ChatColor.AQUA)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Visit the website?")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Moderocky/Guardian"))
                        .append(" v" + Guardian.getInstance().getVersion())
                        .append(" by ")
                        .append("@Moderocky")
                        .color(ChatColor.AQUA)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Discord: Moderocky#0001\nGitLab: Moderocky\nGithub: Moderocky\nSpigot: Moderocky")))
                        .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "Moderocky#0001"))
                        .append(System.lineSeparator()).reset().color(ChatColor.WHITE)
                        .append("A lightweight zones/regions system, designed to be easy-to-use and easy for developers to access. ")
                        .color(ChatColor.GRAY).append(System.lineSeparator())
                        .append("New to Guardian? Click ").color(ChatColor.GRAY)
                        .append("here").color(ChatColor.AQUA).underlined(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Getting started guide?")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Moderocky/Guardian/-/wikis/Creating-Zones"))
                        .append(" ").reset().color(ChatColor.GRAY).underlined(false)
                        .append("for a tutorial.").color(ChatColor.GRAY)
                        .color(ChatColor.GRAY).append(System.lineSeparator())
                        .append("If you are interested in developing a Guardian addon, click ").color(ChatColor.GRAY)
                        .append("here").color(ChatColor.AQUA).underlined(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Learn about addon development?")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Moderocky/Guardian/-/wikis/Addon-Creation"))
                        .append(" ").reset().color(ChatColor.GRAY).underlined(false)
                        .append("for information.").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                        .color(ChatColor.GRAY).append(System.lineSeparator())
                        .append("Built on the ").color(ChatColor.GRAY).italic(true)
                        .append("Mask").color(ChatColor.GOLD).italic(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Mask Framework?")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Pandaemonium/Mask"))
                        .append(" framework.").color(ChatColor.GRAY).italic(true)
                        .create(), sender))
                .arg("update", sender -> messenger.sendMessage(new ComponentBuilder()
                        .append("This function is not currently supported.").append(System.lineSeparator())
                        .append("You can manually download the latest version ")
                        .append("here").color(ChatColor.AQUA).underlined(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Guardian Repository")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Moderocky/Guardian/-/tags"))
                        .append(".").retain(ComponentBuilder.FormatRetention.NONE).reset()
                        .create(), sender))
                .arg("reload", sender -> {
                    Guardian.getInstance().getGuardianConfig().load();
                    messenger.sendMessage(new ComponentBuilder()
                            .append("Guardian config reloaded.")
                            .create(), sender);
                })
                .arg("wiki", sender -> messenger.sendMessage(new ComponentBuilder()
                        .append("Click ").color(ChatColor.WHITE).underlined(false)
                        .append("here").color(ChatColor.AQUA).underlined(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to open the Guardian wiki.")))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitlab.com/Moderocky/Guardian/-/wikis/home"))
                        .append(" ").reset().color(ChatColor.WHITE).underlined(false)
                        .append("for the wiki.").color(ChatColor.WHITE)
                        .create(), sender)
                );

    }

    @Override
    public @NotNull Consumer<CommandSender> getDefault() {
        return sender -> messenger.sendMessage(api.getCommandHelpMessage(this), sender);
    }

    @Override
    public @Nullable List<String> getCompletions(int i, @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return api.getTabCompletions(this, args);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return execute(sender, args);
    }
}
