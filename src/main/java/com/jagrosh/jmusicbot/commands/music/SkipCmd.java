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
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.core.entities.User;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipCmd extends MusicCommand {
    public SkipCmd(Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "現在流れている曲のスキップをリクエスト";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        // 再生中の曲のリクエスト者が送信者かどうか
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            event.reply(event.getClient().getSuccess() + "**" + handler.getPlayer().getPlayingTrack().getInfo().title + "** をスキップしました。");
            handler.getPlayer().stopTrack();
        } else {
            // ボイチャにいる人数 (Bot, スピーカーミュートは含まず)
            int listeners = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened() && m.getUser().getIdLong() != handler.getRequester()).count();

            // 送信するメッセージ
            String msg;

            // 現在の投票を取得して、メッセージの送信者が含まれているかどうか
            if (handler.getVotes().contains(event.getAuthor().getId())) {
                msg = event.getClient().getWarning() + " 再生中の曲はスキップリクエスト済みです。 `[";
            } else {
                msg = event.getClient().getSuccess() + "現在の曲をスキップリクエストしました。`[";
                handler.getVotes().add(event.getAuthor().getId());
            }

            // ボイチャにいる人の中から、スキップすることに投票している人数を取得する
            int skippers = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();

            // 必要な投票数 (ボイチャにいる人数 × 0.55)
            int required = (int) Math.ceil(listeners * .55);

            // 必要投票数が、ボイチャにいる人数と相違する場合
            if(required != listeners) {
                // メッセージを付加する
                msg += "スキップリクエスト数は、" + skippers + "です。スキップするには、" + required + "/" + listeners + "必要です。]`";
            }

            // 現在の投票者数が、必要投票数に達しているかどうか
            if (skippers >= required) {
                // 達していたらスキップする
                User u = event.getJDA().getUserById(handler.getRequester());
                msg += "\n" + event.getClient().getSuccess() + "**" + handler.getPlayer().getPlayingTrack().getInfo().title + "**をスキップしました 。\n" + (handler.getRequester() == 0 ? "" : " (" + (u == null ? "この曲は誰かがリクエストしました。" : "この曲は**" + u.getName() + "**がリクエストしました。") + ")");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }

}
