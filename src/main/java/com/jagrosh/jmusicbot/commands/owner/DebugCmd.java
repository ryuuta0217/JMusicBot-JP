package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

/**
 * @author kosugi_kun (info@cosgy.jp)
 */
public class DebugCmd extends OwnerCommand {
    private final static String[] PROPERTIES = {"java.version", "java.vm.name", "java.vm.specification.version",
            "java.runtime.name", "java.runtime.version", "java.specification.version", "os.arch", "os.name"};

    private final Bot bot;

    public DebugCmd(Bot bot) {
        this.bot = bot;
        this.name = "debug";
        this.help = "デバッグ情報を表示します";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("システムプロパティ:");
        for (String key : PROPERTIES)
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        sb.append("\n\nJMusicBot JPの情報:")
                .append("\n  Version = ").append(OtherUtil.getCurrentVersion())
                .append("\n  Owner = ").append(bot.getConfig().getOwnerId())
                .append("\n  Prefix = ").append(bot.getConfig().getPrefix())
                .append("\n  AltPrefix = ").append(bot.getConfig().getAltPrefix())
                .append("\n  MaxSeconds = ").append(bot.getConfig().getMaxSeconds())
                .append("\n  NPImages = ").append(bot.getConfig().useNPImages())
                .append("\n  SongInStatus = ").append(bot.getConfig().getSongInStatus())
                .append("\n  StayInChannel = ").append(bot.getConfig().getStay())
                .append("\n  UseEval = ").append(bot.getConfig().useEval())
                .append("\n  UpdateAlerts = ").append(bot.getConfig().useUpdateAlerts());
        sb.append("\n\n依存情報:")
                .append("\n  JDA Version = ").append(JDAInfo.VERSION)
                .append("\n  JDA-Utilities Version = ").append(JDAUtilitiesInfo.VERSION)
                .append("\n  Lavaplayer Version = ").append(PlayerLibrary.VERSION);
        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long used = total - (Runtime.getRuntime().freeMemory() / 1024 / 1024);
        sb.append("\n\nランタイム情報:")
                .append("\n  Total Memory = ").append(total)
                .append("\n  Used Memory = ").append(used);
        sb.append("\n\nDiscord情報:")
                .append("\n  ID = ").append(event.getJDA().getSelfUser().getId())
                .append("\n  Guilds = ").append(event.getJDA().getGuildCache().size())
                .append("\n  Users = ").append(event.getJDA().getUserCache().size());
        sb.append("このファイルを開発者に送信する場合はファイルを編集せずに送信するようにお願いします。")
                .append("\nこのファイルは個人情報を特定、アカウントの乗っ取りなどに使用できるデータは含んでいません。");

        if (event.isFromType(ChannelType.PRIVATE)
                || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES))
            event.getChannel().sendFile(sb.toString().getBytes(), "debug_information.txt").queue();
        else
            event.reply("デバッグ情報: ```\n" + sb.toString() + "\n```");
    }
}