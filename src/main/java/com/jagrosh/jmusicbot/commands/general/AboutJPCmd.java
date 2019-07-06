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
        .setDescription("**MusicBot-JP**\n" +
        "`2019/7/06更新`\n\n" +
        "**開発**\n" +
        "`CosgyDev`\n\n" +
        "**Botバージョン**\n" +
        "`version 0.10-BETA`\n\n" +
        "**ライセンス**\n" +
        "`GNU General Public License v3.0`\n\n" +
        "**著作権**\n" +
        "`Copyright (c)2019 Cosgy Dev`")
        .setColor(Color.CYAN)
        .build()).queue();



    }

}
