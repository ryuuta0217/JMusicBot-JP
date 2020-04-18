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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AutoplaylistCmd extends OwnerCommand {
    private final Bot bot;

    public AutoplaylistCmd(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.name = "autoplaylist";
        this.arguments = "<name|NONE|なし>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "サーバーのデフォルトの再生リストを設定します。";
        this.ownerCommand = false;
    }

    @Override
    public void execute(CommandEvent event) {
        if(!event.isOwner() || !event.getMember().isOwner()) return;

        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 再生リスト名またはNONEを含めてください");
            return;
        }
        if (event.getArgs().toLowerCase().matches("(none|なし)")) {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(null);
            event.reply(event.getClient().getSuccess() + " サーバー **" + event.getGuild().getName() + "** の規定の再生リストを削除しました");
            return;
        }
        String pname = event.getArgs().replaceAll("\\s+", "_");
        if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + "`" + pname + ".txt`を見つけることができませんでした!");
        } else {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess() + " サーバー **" + event.getGuild().getName() + "** の規定の再生リストを`" + pname + "`に設定しました。\n"
                    + "再生待ちに曲がないときは、再生リストから曲が再生されます。");
        }
    }
}
