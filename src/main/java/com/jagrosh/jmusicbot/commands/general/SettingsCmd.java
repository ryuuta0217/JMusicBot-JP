/*
 * Copyright 2018-2020 Cosgy Dev
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettingsCmd extends Command {
    private final static String EMOJI = "\uD83C\uDFA7"; // 🎧

    public SettingsCmd(Bot bot) {
        this.name = "settings";
        this.help = "Botの設定を表示します";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        MessageBuilder builder = new MessageBuilder()
                .append(EMOJI + " **")
                .append(FormatUtil.filter(event.getSelfUser().getName()))
                .append("** の設定:");
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        Role role = s.getRole(event.getGuild());
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription("コマンド実行用チャンネル: " + (tchan == null ? "なし" : "**#" + tchan.getName() + "**")
                        + "\n専用ボイスチャンネル: " + (vchan == null ? "なし" : "**" + vchan.getName() + "**")
                        + "\nDJ 権限: " + (role == null ? "未設定" : "**" + role.getName() + "**")
                        //+ "\nリピート: **" + (s.getRepeatMode() ? "有効" : "無効") + "**"
                        + "\nデフォルトプレイリスト: " + (s.getDefaultPlaylist() == null ? "なし" : "**" + s.getDefaultPlaylist() + "**")
                )
                //TODO ここの日本語訳を変更する予定
                .setFooter(String.format(
                        "%s 個のサーバーに参加 | %s 個のボイスチャンネルに接続",
                        event.getJDA().getGuilds().size(),
                        event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()),
                        null);
        event.getChannel().sendMessage(builder.setEmbed(ebuilder.build()).build()).queue();
    }

}
