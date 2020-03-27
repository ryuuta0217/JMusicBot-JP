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
package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Cosgy Dev (info@cosgy.tokyo)
 */
public class Listener extends ListenerAdapter {
    private final Bot bot;

    public Listener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuilds().isEmpty()) {
            Logger log = LoggerFactory.getLogger("MusicBot");
            log.warn("このボットはグループに入っていません！ボットをあなたのグループに追加するには、以下のリンクを使用してください。");
            log.warn(event.getJDA().asBot().getInviteUrl(JMusicBot.RECOMMENDED_PERMS));
        }
        event.getJDA().getGuilds().forEach((guild) ->
        {
            try {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if (defpl != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            } catch (Exception ignore) {
            }
        });
        if (bot.getConfig().useUpdateAlerts()) {
            bot.getThreadpool().scheduleWithFixedDelay(() ->
            {
                User owner = bot.getJDA().getUserById(bot.getConfig().getOwnerId());
                if (owner != null) {
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if (latestVersion != null && !currentVersion.equalsIgnoreCase(latestVersion) && JMusicBot.CHECK_UPDATE) {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        //NUP = false -> NUS = false -> return
        //NUP = false -> NUS = true -> GO
        //NUP = true -> GO
        if (!bot.getConfig().getNoUserPause())
            if (!bot.getConfig().getNoUserStop()) return;

        Member botMember = event.getGuild().getSelfMember();
        //ボイチャにいる人数が1人、botがボイチャにいるか
        if (event.getChannelLeft().getMembers().size() == 1 && event.getChannelLeft().getMembers().contains(botMember)) {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

            // config.txtの nouserpause が true の場合
            if (bot.getConfig().getNoUserPause()) {
                //⏸
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.info("プレイヤーを一時停止");

                // プレイヤーを一時停止する
                handler.getPlayer().setPaused(true);

                Bot.updatePlayStatus(event.getGuild(), event.getGuild().getSelfMember(), PlayStatus.PAUSED);
                return;
            }

            if (bot.getConfig().getNoUserStop()) {
                //⏹
                handler.stopAndClear();
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!bot.getConfig().getResumeJoined()) return;
        //▶
        Member botMember = event.getGuild().getSelfMember();
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        //ボイチャにいる人数が1人以上、botがボイチャにいるか、再生が一時停止されているか
        if ((event.getChannelJoined().getMembers().size() > 1 && event.getChannelJoined().getMembers().contains(botMember)) && handler.getPlayer().isPaused()) {
            handler.getPlayer().setPaused(false);

            Bot.updatePlayStatus(event.getGuild(), event.getGuild().getSelfMember(), PlayStatus.PLAYING);
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        bot.shutdown();
    }
}
