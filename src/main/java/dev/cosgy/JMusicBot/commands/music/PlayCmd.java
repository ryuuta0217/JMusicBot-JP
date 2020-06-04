package dev.cosgy.JMusicBot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import dev.cosgy.JMusicBot.playlist.MylistLoader;
import dev.cosgy.JMusicBot.playlist.PubliclistLoader;

public class PlayCmd {
    public static class MylistCmd extends MusicCommand {
        public MylistCmd(Bot bot) {
            super(bot);
            this.name = "mylist";
            this.aliases = new String[]{"ml"};
            this.arguments = "<name>";
            this.help = "マイリストを再生します";
            this.beListening = true;
            this.bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            String userId = event.getAuthor().getId();
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + " マイリスト名を含めてください。");
                return;
            }
            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, event.getArgs());
            if (playlist == null) {
                event.replyError("`" + event.getArgs() + ".txt `を見つけられませんでした ");
                return;
            }
            event.getChannel().sendMessage(":calling: マイリスト **" + event.getArgs() + "**を読み込んでいます... (" + playlist.getItems().size() + " 曲)").queue(m ->
            {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + " 楽曲がロードされていません。"
                            : event.getClient().getSuccess() + "**" + playlist.getTracks().size() + "**　曲、読み込みました。");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\n以下の楽曲をロードできませんでした:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if (str.length() > 2000)
                        str = str.substring(0, 1994) + " (以下略)";
                    m.editMessage(FormatUtil.filter(str)).queue();
                });
            });
        }
    }

    public static class PublistCmd extends MusicCommand {
        public PublistCmd(Bot bot) {
            super(bot);
            this.name = "publist";
            this.aliases = new String[]{"pul"};
            this.arguments = "<name>";
            this.help = "マイリストを再生します";
            this.beListening = true;
            this.bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + " 再生リスト名を含めてください。");
                return;
            }
            PubliclistLoader.Playlist playlist = bot.getPublistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
                event.replyError("`" + event.getArgs() + ".txt `を見つけられませんでした ");
                return;
            }
            event.getChannel().sendMessage(":calling: 再生リスト **" + event.getArgs() + "**を読み込んでいます... (" + playlist.getItems().size() + " 曲)").queue(m ->
            {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + " 楽曲がロードされていません。"
                            : event.getClient().getSuccess() + "**" + playlist.getTracks().size() + "**　曲、読み込みました。");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\n以下の楽曲をロードできませんでした:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if (str.length() > 2000)
                        str = str.substring(0, 1994) + " (以下略)";
                    m.editMessage(FormatUtil.filter(str)).queue();
                });
            });
        }
    }
}
