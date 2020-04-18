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
package dev.cosgy.JMusicBot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import dev.cosgy.JMusicBot.settings.RepeatMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh <john.a.grosh@gmail.com> | edit: ryuuta0217
 */
public class RepeatCmd extends DJCommand {
    Logger log = LoggerFactory.getLogger("Repeat");

    public RepeatCmd(Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "再生待ち楽曲の再生が終了したら曲を再追加します";
        this.arguments = "[all|on|single|one|off]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        RepeatMode value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().isEmpty()) {
            value = (settings.getRepeatMode() == RepeatMode.OFF ? RepeatMode.ALL : (settings.getRepeatMode() == RepeatMode.ALL ? RepeatMode.SINGLE : (settings.getRepeatMode() == RepeatMode.SINGLE ? RepeatMode.OFF : settings.getRepeatMode())));
        } else if (event.getArgs().matches("(true|all|on)")) {
            value = RepeatMode.ALL;
        } else if (event.getArgs().matches("(false|off)")) {
            value = RepeatMode.OFF;
        } else if (event.getArgs().matches("(one|single)")) {
            value = RepeatMode.SINGLE;
        } else {
            event.replyError("有効なオプションは\n" +
                    "```\n" +
                    "全曲リピート: true, all, on\n" +
                    "1曲リピート: one, single\n" +
                    "リピートオフ: false, off" +
                    "```\n" +
                    "です\n" +
                    "(または、オプション無しで切り替えが可能です)");
            return;
        }
        settings.setRepeatMode(value);
        log.info(event.getGuild().getName() + "でリピートコマンドを実行し、設定を" + value + "に設定しました。");
        event.replySuccess("リピートを `" + (value == RepeatMode.ALL ? "有効(全曲リピート)" : (value == RepeatMode.SINGLE ? "有効(1曲リピート)" : "無効")) + "` にしました。");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
