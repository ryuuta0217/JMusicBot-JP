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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RemoveCmd extends MusicCommand {
    public RemoveCmd(Bot bot) {
        super(bot);
        this.name = "remove";
        this.help = "再生再生待ちから曲を削除します";
        this.arguments = "<再生待ち番号|すべて|ALL>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.replyError("再生待ちには何もありません。");
            return;
        }
        if (event.getArgs().toLowerCase().matches("(all|すべて)")) {
            int count = handler.getQueue().removeAll(event.getAuthor().getIdLong());
            if (count == 0)
                event.replyWarning("再生待ちに曲がありません。");
            else
                event.replySuccess(count + "曲を削除しました。");
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            pos = 0;
        }
        if (pos < 1 || pos > handler.getQueue().size()) {
            event.replyError(String.format("1から%sまでの有効な数字を入力してください!", handler.getQueue().size()));
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));
        QueuedTrack qt = handler.getQueue().get(pos - 1);
        if (qt.getIdentifier() == event.getAuthor().getIdLong()) {
            handler.getQueue().remove(pos - 1);
            event.replySuccess("**" + qt.getTrack().getInfo().title + "**をキューから削除しました。");
        } else if (isDJ) {
            handler.getQueue().remove(pos - 1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            } catch (Exception e) {
                u = null;
            }
            event.replySuccess("**" + qt.getTrack().getInfo().title
                    + "**を再生待ちから削除しました。\n(この曲は" + (u == null ? "誰かがリクエストしました。" : "**" + u.getName() + "**がリクエストしました。") + ")");
        } else {
            event.replyError("**" + qt.getTrack().getInfo().title + "** を削除できませんでした。理由: DJ権限を持っていますか？自分のリクエスト以外は削除できません。");
        }
    }
}
