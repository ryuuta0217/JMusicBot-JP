/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SearchCmd extends MusicCommand
{
    protected String searchPrefix = "ytsearch:";
    private final OrderedMenu.Builder builder;
    private final String searchingEmoji;

    public SearchCmd(Bot bot, String searchingEmoji)
    {
        super(bot);
        this.searchingEmoji = searchingEmoji;
        this.name = "search";
        this.aliases = new String[]{"ytsearch"};
        this.arguments = "<query>";
        this.help = "提供されたクエリのYouTubeを検索します。";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }
    @Override
    public void doCommand(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("クエリを含めてください。");
            return;
        }
        event.reply(searchingEmoji+" 検索中... `["+event.getArgs()+"]`",
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m,event)));
    }

    private class ResultHandler implements AudioLoadResultHandler
    {
        private final Message m;
        private final CommandEvent event;

        private ResultHandler(Message m, CommandEvent event)
        {
            this.m = m;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track)
        {
            if(bot.getConfig().isTooLong(track))
            {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" このトラック (**"+track.getInfo().title+"**) : `は許可されている最大長より長いです。"
                        +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, event.getAuthor()))+1;
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess()+" 追加しました **"+track.getInfo().title
                    +"** (`"+FormatUtil.formatTime(track.getDuration())+"`) "+(pos==0 ? "再生します"
                    : "このポジションのキューへ  "+pos))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            builder.setColor(event.getSelfMember().getColor())
                    .setText(FormatUtil.filter(event.getClient().getSuccess()+" の検索結果 `"+event.getArgs()+"`:"))
                    .setChoices(new String[0])
                    .setSelection((msg,i) ->
                    {
                        AudioTrack track = playlist.getTracks().get(i-1);
                        if(bot.getConfig().isTooLong(track))
                        {
                            event.replyWarning("このトラック (**"+track.getInfo().title+"**) : `は許可されている最大長よりも長いです。"
                                    +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`");
                            return;
                        }
                        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                        int pos = handler.addTrack(new QueuedTrack(track, event.getAuthor()))+1;
                        event.replySuccess("追加しました **"+track.getInfo().title
                                +"** (`"+FormatUtil.formatTime(track.getDuration())+"`) "+(pos==0 ? "再生します"
                                : " このキューのポジションへ "+pos));
                    })
                    .setCancel((msg) -> {})
                    .setUsers(event.getAuthor())
            ;
            for(int i=0; i<4 && i<playlist.getTracks().size(); i++)
            {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`["+FormatUtil.formatTime(track.getDuration())+"]` [**"+track.getInfo().title+"**]("+track.getInfo().uri+")");
            }
            builder.build().display(m);
        }

        @Override
        public void noMatches()
        {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" の検索結果はありません。 `"+event.getArgs()+"`.")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable)         {

            if(throwable.severity==Severity.COMMON)
                m.editMessage(event.getClient().getError()+" 読み込みエラー: "+throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError()+" 読み込みエラー").queue();
        }
    }
}
