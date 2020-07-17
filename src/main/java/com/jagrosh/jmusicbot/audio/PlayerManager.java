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
package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cosgy Dev (info@cosgy.jp)
 */
public class PlayerManager extends DefaultAudioPlayerManager {
    private final Bot bot;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PlayerManager(Bot bot) {
        this.bot = bot;
    }

    public void init() {
        if (bot.getConfig().isNicoNicoEnabled()) {
            registerSourceManager(
                    new NicoAudioSourceManager(
                            bot.getConfig().getNicoNicoEmailAddress(),
                            bot.getConfig().getNicoNicoPassword())
            );
        }

        AudioSourceManagers.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);

        if (getConfiguration().getOpusEncodingQuality() != 10) {
            logger.debug("OpusEncodingQuality is " + getConfiguration().getOpusEncodingQuality() + "(< 10)" + ", Setting Quality to 10.");
            getConfiguration().setOpusEncodingQuality(10);
        }

        if (getConfiguration().getResamplingQuality() != AudioConfiguration.ResamplingQuality.HIGH) {
            logger.debug("ResamplingQuality is " + getConfiguration().getResamplingQuality().name() + "(not HIGH), Setting Quality to HIGH");
            getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        }
    }

    public Bot getBot() {
        return bot;
    }

    public boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }

    public AudioHandler setUpHandler(Guild guild) {
        AudioHandler handler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        } else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}
