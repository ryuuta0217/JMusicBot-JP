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
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettcCmd extends AdminCommand {
    public SettcCmd(Bot bot) {
        this.name = "settc";
        this.help = "ボットのコマンドチャンネルを設定します";
        this.arguments = "<チャンネル名|NONE|なし>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        Logger log = LoggerFactory.getLogger("SettcCmd");
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "チャンネルまたはNONEを含めてください。");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none|なし)")) {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "音楽コマンドは現在どのチャンネルでも使用できます。");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "一致するチャンネルが見つかりませんでした \"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            else {
                s.setTextChannel(list.get(0));
                log.info("音楽コマンド用のチャンネルを設定しました。");
                event.reply(event.getClient().getSuccess() + "音楽コマンドは現在<#" + list.get(0).getId() + ">でしか使用できません");
            }
        }
    }

}
