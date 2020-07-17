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
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlaynextCmd extends DJCommand {
    Logger log = LoggerFactory.getLogger("Playnext");
    private final String loadingEmoji;

    public PlaynextCmd(Bot bot) {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "playnext";
        this.arguments = "<title|URL>";
        this.help = "次にこの曲を再生する";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            event.replyWarning("曲のタイトルまたはURLを入力してください。");
            return;
        }
        String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();
        log.info(event.getGuild().getName() + "で[" + args + "]のロードを開始しました。");
        event.reply(loadingEmoji + "`[" + args + "]`をロード中です...", m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message m, CommandEvent event, boolean ytsearch) {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        private void loadSingle(AudioTrack track) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + "(**" + track.getInfo().title + "**) このトラックは許可されている最大長よりも長いです: `"
                        + FormatUtil.formatTime(track.getDuration()) + "` > `" + FormatUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrackToFront(new QueuedTrack(track, event.getAuthor())) + 1;
            String addMsg = FormatUtil.filter(event.getClient().getSuccess() + "**" + track.getInfo().title
                    + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "を再生待ちに追加しました。" : "を" + pos + "番目の再生待ちに追加しました。"));
            m.editMessage(addMsg).queue();

            log.info(event.getGuild().getName() + track.getInfo().title
                    + "(" + FormatUtil.formatTime(track.getDuration()) + ") " + (pos == 0 ? "を再生待ちに追加しました。" : "を" + pos + "番目の再生待ちに追加しました。"));

        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            AudioTrack single;
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
                single = playlist.getSelectedTrack() == null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            else if (playlist.getSelectedTrack() != null)
                single = playlist.getSelectedTrack();
            else
                single = playlist.getTracks().get(0);
            loadSingle(single);
        }

        @Override
        public void noMatches() {
            if (ytsearch)
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " この検索結果はありません `" + event.getArgs() + "`.")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:" + event.getArgs(), new ResultHandler(m, event, true));
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == FriendlyException.Severity.COMMON)
                m.editMessage(event.getClient().getError() + " 読み込みエラー: " + throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError() + " 曲の読み込み中にエラーが発生しました。").queue();
                log.info(event.getGuild().getName() + "で読み込みエラーが発生しました。");
        }
    }
}
