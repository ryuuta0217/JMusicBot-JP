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
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetvcCmd extends AdminCommand {
    public SetvcCmd() {
        this.name = "setvc";
        this.help = "再生に使用する音声チャンネルを固定します。";
        this.arguments = "<チャンネル名|NONE|なし>";
    }

    @Override
    protected void execute(CommandEvent event) {
        Logger log = LoggerFactory.getLogger("SetvcCmd");
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "音声チャンネルまたはNONEを含めてください。");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none|なし)")) {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + "音楽はどの音声チャンネルでも再生できます。");
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "一致する音声チャンネルが見つかりませんでした \"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfVChannels(list, event.getArgs()));
            else {
                s.setVoiceChannel(list.get(0));
                log.info("音楽チャンネルを設定しました。");
                event.reply(event.getClient().getSuccess() + "音楽は**" + list.get(0).getName() + "**でのみ再生できるようになりました。");
            }
        }
    }
}
