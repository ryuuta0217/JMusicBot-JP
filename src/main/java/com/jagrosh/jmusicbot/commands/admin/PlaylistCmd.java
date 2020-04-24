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
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import dev.cosgy.JMusicBot.util.StackTraceUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlaylistCmd extends AdminCommand {
    private final Bot bot;

    public PlaylistCmd(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault>";
        this.help = "再生リスト管理";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new AdminCommand[]{
                new ListCmd(),
                new AppendlistCmd(),
                new DeletelistCmd(),
                new MakelistCmd(),
                new DefaultPlaylistCmd(bot)
        };
    }

    @Override
    public void execute(CommandEvent event) {

        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " 再生リスト管理コマンド:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }

    public static class DefaultPlaylistCmd extends AutoplaylistCmd {
        public DefaultPlaylistCmd(Bot bot) {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<playlistname|NONE>";
            this.guildOnly = true;
            this.ownerCommand = false;
        }
    }

    public class MakelistCmd extends AdminCommand {
        public MakelistCmd() {
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "再生リストを新規作成";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        protected void execute(CommandEvent event) {

            String pname = event.getArgs().replaceAll("\\s+", "_");
            String guildId = event.getGuild().getId();

            if (pname.isEmpty()) {
                event.replyError("プレイリスト名を指定してください。");
                return;
            }

            if (bot.getPlaylistLoader().getPlaylist(guildId, pname) == null) {
                try {
                    bot.getPlaylistLoader().createPlaylist(guildId, pname);
                    event.reply(event.getClient().getSuccess() + "再生リスト `" + pname + "` を作成しました");
                } catch (IOException e) {
                    if (event.isOwner() || event.getMember().isOwner()) {
                        event.replyError("曲の読み込み中にエラーが発生しました。\n" +
                                "**エラーの内容: " + e.getLocalizedMessage() + "**");
                        StackTraceUtil.sendStackTrace(event.getTextChannel(), e);
                        return;
                    }

                    event.reply(event.getClient().getError() + " 再生リストを作成できませんでした。:" + e.getLocalizedMessage());
                }
            } else {
                event.reply(event.getClient().getError() + " 再生リスト `" + pname + "` は既に存在します");
            }
        }
    }

    public class DeletelistCmd extends AdminCommand {
        public DeletelistCmd() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "既存の再生リストを削除";
            this.arguments = "<name>";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        protected void execute(CommandEvent event) {

            String pname = event.getArgs().replaceAll("\\s+", "_");
            String guildid = event.getGuild().getId();
            if (!pname.equals("")) {
                if (bot.getPlaylistLoader().getPlaylist(guildid, pname) == null)
                    event.reply(event.getClient().getError() + " 再生リストは存在しません:`" + pname + "`");
                else {
                    try {
                        bot.getPlaylistLoader().deletePlaylist(guildid, pname);
                        event.reply(event.getClient().getSuccess() + " 再生リストを削除しました:`" + pname + "`");
                    } catch (IOException e) {
                        event.reply(event.getClient().getError() + " 再生リストを削除できませんでした: " + e.getLocalizedMessage());
                    }
                }
            } else {
                event.reply(event.getClient().getError() + "再生リストの名前を含めてください");
            }
        }
    }

    public class AppendlistCmd extends AdminCommand {
        public AppendlistCmd() {
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "既存の再生リストに曲を追加";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        protected void execute(CommandEvent event) {

            String[] parts = event.getArgs().split("\\s+", 2);
            String guildid = event.getGuild().getId();
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " 追加先の再生リスト名とURLを含めてください。");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(guildid, pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " 再生リストは存在しません:`" + pname + "`");
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
                    bot.getPlaylistLoader().writePlaylist(guildid, pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + urls.length + " 項目を再生リストに追加しました:`" + pname + "`");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " 再生リストに追加できませんでした: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class ListCmd extends AdminCommand {
        public ListCmd() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "利用可能なすべての再生リストを表示";
            this.guildOnly = true;
            this.ownerCommand = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String guildId = event.getGuild().getId();

            if (!bot.getPlaylistLoader().folderGuildExists(guildId))
                bot.getPlaylistLoader().createGuildFolder(guildId);
            if (!bot.getPlaylistLoader().folderGuildExists(guildId)) {
                event.reply(event.getClient().getWarning() + " 再生リストフォルダが存在しないため作成できませんでした。");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames(guildId);
            if (list == null)
                event.reply(event.getClient().getError() + " 利用可能な再生リストを読み込めませんでした。");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " 再生リストフォルダに再生リストがありません。");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " 利用可能な再生リスト:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
