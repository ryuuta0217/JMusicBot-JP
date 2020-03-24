package dev.cosgy.JMusicBot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.cosgy.niconicoSearchAPI.nicoSearchAPI;
import dev.cosgy.niconicoSearchAPI.nicoVideoSearchResult;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class NicoSearchCmd extends Command {
    public static final nicoSearchAPI niconicoAPI = new nicoSearchAPI(true, 100);

    public NicoSearchCmd() {
        this.name = "niconicosearch";
        this.aliases = Bot.INSTANCE.getConfig().getAliases(this.name);
        this.arguments = "<query>";
        this.help = "指定した文字列を使用してニコニコ動画上の動画を検索します。";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void execute(CommandEvent event) {
        if(event.getArgs().isEmpty()) {
            event.reply("使用法: **`" + event.getClient().getPrefix() + "ncsearch <検索語句>`**");
        } else {
            Message m = event.getChannel().sendMessage("<a:processingRing:533493912225054750> ニコニコ動画で " + event.getArgs() + " を検索しています\n" +
                    "**(注: 一部再生できない動画があります。)**").complete();
            LinkedList<nicoVideoSearchResult> results = niconicoAPI.searchVideo(event.getArgs(), 5, true);
            if(results.size() == 0) {
                m.editMessage(event.getArgs() + " の検索結果はありません。").queue();
                return;
            }

            OrderedMenu.Builder builder = new OrderedMenu.Builder()
                    .allowTextInput(true)
                    .useNumbers()
                    .useCancelButton(true)
                    .setEventWaiter(Bot.INSTANCE.getWaiter())
                    .setTimeout(1, TimeUnit.MINUTES)
                    .setCancel(msg -> {})
                    .setText(FormatUtil.filter(event.getClient().getSuccess()+"`"+event.getArgs()+"` の検索結果:"))
                    .setSelection((msg, sel) -> {
                        nicoVideoSearchResult selectedResultVideo = results.get((sel-1));
                        System.out.println("URL = " + selectedResultVideo.getWatchUrl() + ", title = " + selectedResultVideo.getTitle());
                        Bot.INSTANCE.getPlayerManager().loadItemOrdered(event.getGuild(), selectedResultVideo.getWatchUrl(), new ResultHandler(m, event));
                    });

            results.forEach(result -> builder.addChoice("`[" + result.getInfo().getLengthFormatted() + "]` [**" + result.getTitle() + "**](" + result.getWatchUrl() + ")"));
            builder.build().display(m);
        }
    }

    private static class ResultHandler implements AudioLoadResultHandler {
        final Message m;
        final CommandEvent event;
        private ResultHandler(Message m, CommandEvent event)
        {
            this.m = m;
            this.event = event;
        }

        /**
         * Called when the requested item is a track and it was successfully loaded.
         *
         * @param track The loaded track
         */
        @Override
        public void trackLoaded(AudioTrack track) {
            if(Bot.INSTANCE.getConfig().isTooLong(track)) {
                event.reply(FormatUtil.filter(event.getClient().getWarning()+" 楽曲 (**"+track.getInfo().title+"**) は許容されている動画の長さを超えています: `"
                        +FormatUtil.formatTime(track.getDuration())+"` > `" + Bot.INSTANCE.getConfig().getMaxTime() + "`"));
                return;
            }

            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;

            event.reply(FormatUtil.filter(String.format("%s %s **%s** (`%s`) を追加しました", event.getClient().getSuccess(), (pos == 0 ? "再生待ちに" : "再生待ち #" + pos + " に"), track.getInfo().title, FormatUtil.formatTime(track.getDuration()))));
        }

        /**
         * Called when the requested item is a playlist and it was successfully loaded.
         *
         * @param playlist The loaded playlist
         */
        @Override
        public void playlistLoaded(AudioPlaylist playlist) {

        }

        /**
         * Called when there were no items found by the specified identifier.
         */
        @Override
        public void noMatches() {
            event.reply(FormatUtil.filter(event.getClient().getWarning()+" `"+event.getArgs()+"`の検索結果はありません."));
        }

        /**
         * Called when loading an item failed with an exception.
         *
         * @param exception The exception that was thrown
         */
        @Override
        public void loadFailed(FriendlyException exception) {
            if(exception.severity== FriendlyException.Severity.COMMON)
                event.reply(event.getClient().getError()+" 読み込み中にエラーが発生しました: "+exception.getMessage());
            else
                event.reply(event.getClient().getError()+" 楽曲を読み込み中にエラーが発生しました");
        }
    }
}
