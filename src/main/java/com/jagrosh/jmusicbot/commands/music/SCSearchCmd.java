package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jmusicbot.Bot;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SCSearchCmd extends SearchCmd {
    public SCSearchCmd(Bot bot) {
        super(bot);
        this.searchPrefix = "scsearch:";
        this.name = "scsearch";
        this.help = "指定されたクエリをSoundcloudで検索します";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
}