package dev.cosgy.JMusicBot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.time.format.DateTimeFormatter;

public class UserInfo extends Command {
    public UserInfo() {
        this.name = "userinfo";
        this.help = "指定したユーザーに関する情報を表示します";
        this.arguments = "<ユーザー>";
        this.guildOnly = true;
        this.category = new Category("General");
    }

    @Override
    public void execute(CommandEvent event) {
        Member memb;

        if (event.getArgs().length() > 0) {
            try {
                if (event.getMessage().getMentionedUsers().size() != 0) {
                    memb = event.getMessage().getMentionedMembers().get(0);
                } else {
                    memb = FinderUtil.findMembers(event.getArgs(), event.getGuild()).get(0);
                }
            } catch (Exception e) {
                event.reply("ユーザー \"" + event.getArgs() + "\" は見つかりませんでした。");
                return;
            }
        } else {
            memb = event.getMember();
        }

        EmbedBuilder eb = new EmbedBuilder().setColor(memb.getColor());
        String NAME = memb.getEffectiveName();
        String TAG = "#" + memb.getUser().getDiscriminator();
        String GUILD_JOIN_DATE = memb.getJoinDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String DISCORD_JOINED_DATE = memb.getUser().getCreationTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String ID = memb.getUser().getId();
        String STATUS = memb.getOnlineStatus().getKey().replace("offline", ":x: オフライン").replace("dnd", ":red_circle: 起こさないで").replace("idle", "退席中").replace("online", ":white_check_mark: オンライン");
        String UserURL = null;
        String ROLES = "";
        String GAME;
        String AVATAR = memb.getUser().getAvatarUrl();

        try {
            GAME = memb.getGame().getName();
        } catch (Exception e) {
            GAME = "-/-";
        }

        for (Role r : memb.getRoles()) {
            ROLES += r.getName() + ", ";
        }
        if (ROLES.length() > 0)
            ROLES = ROLES.substring(0, ROLES.length() - 2);
        else
            ROLES = "このサーバーには役職が存在しません";

        if (AVATAR == null) {
            AVATAR = "アイコンなし";
        }

        eb.setAuthor(memb.getUser().getName() + TAG + " のユーザー情報", UserURL, null)
                .addField(":pencil2: 名前/ニックネーム", "**" + NAME + "**", true)
                .addField(":link: DiscordTag", "**" + TAG + "**", true)
                .addField(":1234: ID", "**" + ID + "**", true)
                .addBlankField(false)
                .addField(":signal_strength: 現在のステータス", "**" + STATUS + "**", true)
                .addField(":video_game: プレイ中のゲーム", "**" + GAME + "**", true)
                .addField(":tools: 役職", "**" + ROLES + "**", true)
                .addBlankField(false)
                .addField(":inbox_tray: サーバー参加日", "**" + GUILD_JOIN_DATE + "**", true)
                .addField(":beginner: アカウント作成日", "**" + DISCORD_JOINED_DATE + "**", true)
                .addBlankField(false)
                .addField(":frame_photo: アイコンURL", AVATAR, false);

        if (!AVATAR.equals("アイコンなし")) {
            eb.setAuthor(memb.getUser().getName() + TAG + " のユーザー情報", UserURL, AVATAR);
        }

        event.getChannel().sendMessage(eb.build()).queue();
    }
}
