package dev.cosgy.JMusicBot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import dev.cosgy.JMusicBot.playlist.MylistLoader;
import dev.cosgy.JMusicBot.util.StackTraceUtil;

import java.io.IOException;
import java.util.List;

public class MylistCmd extends MusicCommand {
    private final Bot bot;

    public MylistCmd(Bot bot) {
        super(bot);
        this.bot = bot;
        this.guildOnly = false;
        this.name = "mylist";
        this.arguments = "<append|delete|make>";
        this.help = "自分専用の再生リストを管理";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new MusicCommand[]{
                new MakelistCmd(bot),
                new DeletelistCmd(bot),
                new AppendlistCmd(bot),
                new ListCmd(bot)
        };
    }

    @Override
    public void doCommand(CommandEvent event) {

        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " マイリスト管理コマンド:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }

    public class MakelistCmd extends DJCommand {
        public MakelistCmd(Bot bot) {
            super(bot);
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "再生リストを新規作成";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String pname = event.getArgs().replaceAll("\\s+", "_");
            String userId = event.getAuthor().getId();

            if (pname.isEmpty()) {
                event.replyError("プレイリスト名を指定してください。");
                return;
            }

            if (bot.getMylistLoader().getPlaylist(userId, pname) == null) {
                try {
                    bot.getMylistLoader().createPlaylist(userId, pname);
                    event.reply(event.getClient().getSuccess() + "マイリスト `" + pname + "` を作成しました");
                } catch (IOException e) {
                    if (event.isOwner() || event.getMember().isOwner()) {
                        event.replyError("曲の読み込み中にエラーが発生しました。\n" +
                                "**エラーの内容: " + e.getLocalizedMessage() + "**");
                        StackTraceUtil.sendStackTrace(event.getTextChannel(), e);
                        return;
                    }

                    event.reply(event.getClient().getError() + " マイリストを作成できませんでした。:" + e.getLocalizedMessage());
                }
            } else {
                event.reply(event.getClient().getError() + " マイリスト `" + pname + "` は既に存在します");
            }
        }
    }

    public class DeletelistCmd extends MusicCommand {
        public DeletelistCmd(Bot bot) {
            super(bot);
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "既存のマイリストを削除";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String pname = event.getArgs().replaceAll("\\s+", "_");
            String userId = event.getAuthor().getId();
            if (!pname.equals("")) {
                if (bot.getMylistLoader().getPlaylist(userId, pname) == null)
                    event.reply(event.getClient().getError() + " マイリストは存在しません:`" + pname + "`");
                else {
                    try {
                        bot.getMylistLoader().deletePlaylist(userId, pname);
                        event.reply(event.getClient().getSuccess() + " マイリストを削除しました:`" + pname + "`");
                    } catch (IOException e) {
                        event.reply(event.getClient().getError() + " マイリストを削除できませんでした: " + e.getLocalizedMessage());
                    }
                }
            } else {
                event.reply(event.getClient().getError() + "マイリストの名前を含めてください");
            }
        }
    }

    public class AppendlistCmd extends MusicCommand {
        public AppendlistCmd(Bot bot) {
            super(bot);
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "既存のマイリストに曲を追加";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        public void doCommand(CommandEvent event) {

            String[] parts = event.getArgs().split("\\s+", 2);
            String userId = event.getAuthor().getId();
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " 追加先のマイリスト名とURLを含めてください。");
                return;
            }
            String pname = parts[0];
            MylistLoader.Playlist playlist = bot.getMylistLoader().getPlaylist(userId, pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " マイリストは存在しません:`" + pname + "`");
            else {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = parts[1].split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length() - 1);
                    builder.append("\r\n").append(u);
                }
                try {
                    bot.getMylistLoader().writePlaylist(userId, pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " 項目をマイリストに追加しました:`" + pname + "`");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " マイリストに追加できませんでした: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class ListCmd extends MusicCommand {
        public ListCmd(Bot bot) {
            super(bot);
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "利用可能なすべてのマイリストを表示";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            String userId = event.getAuthor().getId();

            if (!bot.getMylistLoader().folderUserExists(userId))
                bot.getMylistLoader().createUserFolder(userId);
            if (!bot.getMylistLoader().folderUserExists(userId)) {
                event.reply(event.getClient().getWarning() + " マイリストフォルダが存在しないため作成できませんでした。");
                return;
            }
            List<String> list = bot.getMylistLoader().getPlaylistNames(userId);
            if (list == null)
                event.reply(event.getClient().getError() + " 利用可能なマイリストを読み込めませんでした。");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " マイリストフォルダに再生リストがありません。");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " 利用可能なマイリスト:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
