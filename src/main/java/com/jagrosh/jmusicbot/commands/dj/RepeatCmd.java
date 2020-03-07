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
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RepeatCmd extends DJCommand {
    Logger log = LoggerFactory.getLogger("Repeat");
    public RepeatCmd(Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "再生待ち楽曲の再生が終了したら曲を再追加します";
        this.arguments = "[on|off]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        boolean value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().isEmpty()) {
            value = !settings.getRepeatMode();
        } else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        } else if (event.getArgs().equalsIgnoreCase("false") || event.getArgs().equalsIgnoreCase("off")) {
            value = false;
        } else {
            event.replyError("有効なオプションは `on`か`off` です(または空白で切り替えができます)");
            return;
        }
        settings.setRepeatMode(value);
        log.info(event.getGuild().getName() + "でリピートコマンドを実行し、設定を" + (value ? "ON" : "OFF") + "に設定しました。");
        event.replySuccess("リピートを `" + (value ? "有効" : "無効") + "` にしました。");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
