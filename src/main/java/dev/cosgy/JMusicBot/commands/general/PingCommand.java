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
package dev.cosgy.JMusicBot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;

import java.time.temporal.ChronoUnit;

/**
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
        name = {"Ping", "Pong"},
        description = "ボットのレイテンシを確認します"
)
@Author("John Grosh (jagrosh)")
public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "ボットのレイテンシをチェックします";
        this.guildOnly = false;
        this.aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Ping: ...", m -> {
            long ping = event.getMessage().getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS);
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getPing() + "ms").queue();
        });
    }

}
