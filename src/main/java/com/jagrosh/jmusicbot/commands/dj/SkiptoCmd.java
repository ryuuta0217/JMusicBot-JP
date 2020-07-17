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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkiptoCmd extends DJCommand {
    Logger log = LoggerFactory.getLogger("Skip");

    public SkiptoCmd(Bot bot) {
        super(bot);
        this.name = "skipto";
        this.help = "指定された曲にスキップします";
        this.arguments = "<position>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int index = 0;
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            event.reply(event.getClient().getError() + " `" + event.getArgs() + "` は有効な整数ではありません。");
            return;
        }
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (index < 1 || index > handler.getQueue().size()) {
            event.reply(event.getClient().getError() + " 1から" + handler.getQueue().size() + "の間の整数でないといけません!");
            return;
        }
        handler.getQueue().skip(index - 1);
        log.info(event.getGuild().getName() + "で曲を" + handler.getQueue().get(0).getTrack().getInfo().title + "にスキップしました。");
        event.reply(event.getClient().getSuccess() + " **" + handler.getQueue().get(0).getTrack().getInfo().title + "にスキップしました。**");
        handler.getPlayer().stopTrack();
    }
}
