package dev.cosgy.JMusicBot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.time.format.DateTimeFormatter;

public class ServerInfo extends Command {
    public ServerInfo() {
        this.name = "serverinfo";
        this.help = "サーバーに関する情報を表示します";
        this.guildOnly = true;
        this.category = new Category("General");
    }

    @Override
    public void execute(CommandEvent event) {
        String GuildName = event.getGuild().getName();
        String GuildIconURL = event.getGuild().getIconUrl();
        String GuildId = event.getGuild().getId();
        String GuildOwner = event.getGuild().getOwner().getUser().getName() + "#" + event.getGuild().getOwner().getUser().getDiscriminator();
        String GuildCreatedDate = event.getGuild().getCreationTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        String GuildRolesCount = String.valueOf(event.getGuild().getRoles().size());
        String GuildMember = String.valueOf(event.getGuild().getMembers().size());
        String GuildCategoryCount = String.valueOf(event.getGuild().getCategories().size());
        String GuildTextChannelCount = String.valueOf(event.getGuild().getTextChannels().size());
        String GuildVoiceChannelCount = String.valueOf(event.getGuild().getVoiceChannels().size());
        String GuildRegion = event.getGuild().getRegionRaw()
                .replace("japan", ":flag_jp: 日本")
                .replace("singapore", ":flag_sg: シンガポール")
                .replace("hongkong", ":flag_hk: 香港")
                .replace("Brazil", ":flag_br: ブラジル")
                .replace("us-central", ":flag_us: 中央アメリカ")
                .replace("us-west", ":flag_us: 西アメリカ")
                .replace("us-east", ":flag_us: 東アメリカ")
                .replace("us-south", ":flag_us: 南アメリカ")
                .replace("sydney", ":flag_au: シドニー")
                .replace("eu-west", ":flag_eu: 西ヨーロッパ")
                .replace("eu-central", ":flag_eu: 中央ヨーロッパ")
                .replace("russia", ":flag_ru: ロシア");

        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor("サーバー " + GuildName + " の情報", null, GuildIconURL);

        eb.addField("サーバーID", GuildId, true);
        eb.addField("サーバーリージョン", GuildRegion, true);
        eb.addField("サーバーオーナー", GuildOwner, true);
        eb.addField("メンバー数", GuildMember, true);
        eb.addField("役職数", GuildRolesCount, true);
        eb.addField("カテゴリの数", GuildCategoryCount, true);
        eb.addField("テキストチャンネルの数", GuildTextChannelCount, true);
        eb.addField("ボイスチャンネルの数", GuildVoiceChannelCount, true);

        eb.setFooter("サーバー作成日時: " + GuildCreatedDate, null);

        event.getChannel().sendMessage(eb.build()).queue();
    }
}
