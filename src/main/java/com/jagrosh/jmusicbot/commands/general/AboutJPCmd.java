package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class AboutJPCmd extends Command {

    public AboutJPCmd() {

        this.name = "about-jp";
        this.help = "CosgyDevのスタッフ一覧を表示します";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        event.getTextChannel().sendMessage(new EmbedBuilder()
        .setTitle("JMusicBot JP", null)
        .setColor(Color.blue)
        .setDescription("2019/07/06更新")
        .addField("ボットバージョン", "0.1.0-BETA", false)
        .addField("ライセンス", "Apache License 2.0", false)
        .addField("著作権","Copyright 2017 John Grosh")
        .addField("日本語化","Cosgy Dev")
        .setAuthor("JMusicBot JP", null, "https://avatars1.githubusercontent.com/u/42630152?s=400&u=3bc25d4a1d13b627f715829ab66ee82cc48a93f9&v=4")
        .setFooter("JMusicBot JP", "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png")

/*
        .setDescription("**MusicBot-JP**\n" +
        "`2019/7/06更新`\n\n" +
        "**日本語化**\n" +
        "`CosgyDev`\n\n" +
        "**Botバージョン**\n" +
        "`version 0.10-BETA`\n\n" +
        "**ライセンス**\n" +
        "`Apache License 2.0`\n\n" +
        "**著作権**\n" +
        "`Copyright 2017 John Grosh`")
        .setColor(Color.CYAN)
*/
        .build()).queue();



    }

}
