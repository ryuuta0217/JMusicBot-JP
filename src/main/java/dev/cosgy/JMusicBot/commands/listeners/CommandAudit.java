package dev.cosgy.JMusicBot.commands.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jmusicbot.JMusicBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandAudit implements CommandListener {
    /**
     * Called when a {@link Command Command} is triggered
     * by a {@link CommandEvent CommandEvent}.
     *
     * @param event   The CommandEvent that triggered the Command
     * @param command 実行されたコマンドオブジェクト
     */
    @Override
    public void onCommand(CommandEvent event, Command command) {
        if(JMusicBot.COMMAND_AUDIT_ENABLED) {
            Logger logger = LoggerFactory.getLogger("CommandAudit");
            logger.info(String.format("%s の #%s で %s#%s (%s) がコマンド %s を実行しました",
                    event.getGuild().getName(),
                    event.getTextChannel().getName(),
                    event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getAuthor().getId(),
                    event.getMessage().getContentDisplay()));
        }
    }
}
