/*
 * Copyright 2018 Cosgy Dev (info@cosgy.jp)
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
package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

public class BotConfig
{
    private final Prompt prompt;
    private final static String CONTEXT = "Config";
    private final static String START_TOKEN = "/// START OF JMUSICBOT-JP CONFIG ///";
    private final static String END_TOKEN = "/// END OF JMUSICBOT-JP CONFIG ///";
    
    private Path path = null;
    // [JMusicBot-JP] added nicoEmail, nicoPass
    private String token, prefix, altprefix, helpWord, playlistsFolder,
            successEmoji, warningEmoji, errorEmoji, loadingEmoji, searchingEmoji, nicoEmail, nicoPass;
    // [JMusicBot-JP] added useNicoNico
    private boolean useNicoNico, stayInChannel, songInGame, npImages, updatealerts, useEval, dbots;
    private long owner, maxSeconds;
    private OnlineStatus status;
    private Game game;
    
    private boolean valid = false;
    
    public BotConfig(Prompt prompt)
    {
        this.prompt = prompt;
    }
    
    public void load()
    {
        valid = false;
        
        // read config from file
        try 
        {
            // get the path to the config, default config.txt
            path = Paths.get(System.getProperty("config.file", System.getProperty("config", "config.txt")));
            if(path.toFile().exists())
            {
                if(System.getProperty("config.file") == null)
                    System.setProperty("config.file", System.getProperty("config", "config.txt"));
                ConfigFactory.invalidateCaches();
            }
            
            // load in the config file, plus the default values
            //Config config = ConfigFactory.parseFile(path.toFile()).withFallback(ConfigFactory.load());
            Config config = ConfigFactory.load();
            
            // set values
            token = config.getString("token");
            prefix = config.getString("prefix");
            altprefix = config.getString("altprefix");
            helpWord = config.getString("help");
            owner = (config.getAnyRef("owner") instanceof String ? 0L : config.getLong("owner"));
            successEmoji = config.getString("success");
            warningEmoji = config.getString("warning");
            errorEmoji = config.getString("error");
            loadingEmoji = config.getString("loading");
            searchingEmoji = config.getString("searching");
            game = OtherUtil.parseGame(config.getString("game"));
            status = OtherUtil.parseStatus(config.getString("status"));
            stayInChannel = config.getBoolean("stayinchannel");
            songInGame = config.getBoolean("songinstatus");
            npImages = config.getBoolean("npimages");
            updatealerts = config.getBoolean("updatealerts");
            useEval = config.getBoolean("eval");
            maxSeconds = config.getLong("maxtime");
            playlistsFolder = config.getString("playlistsfolder");
            dbots = owner == 113156185389092864L;

            // [JMusicBot-JP] new function: support niconico play
            useNicoNico = config.getBoolean("useniconico");
            nicoEmail = config.getString("nicomail");
            nicoPass = config.getString("nicopass");
            // [JMusicBot-JP] End
            
            // we may need to write a new config file
            boolean write = false;

            // validate bot token
            if(token==null || token.isEmpty() || token.matches("(BOT_TOKEN_HERE|Botトークンをここに貼り付け)")) {
                token = prompt.prompt("BOTトークンを入力してください。"
                        + "\nトークンを取得する方法はこちらから:"
                        + "\nhttps://github.com/jagrosh/MusicBot/wiki/Getting-a-Bot-Token."
                        + "\nBOTトークン: ");
                if(token==null)
                {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "トークンが入力されていません！終了します。\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
                    return;
                }
                else
                {
                    write = true;
                }
            }

            System.out.println("Token Check Passed");
            
            // validate bot owner
            if(owner<=0)
            {
                try
                {
                    owner = Long.parseLong(prompt.prompt("所有者のユーザーIDが設定されていない、または有効なIDではありません。"
                        + "\nBOTの所有者のユーザーIDを入力してください。"
                        + "\nユーザーIDの入手方法はこちらから:"
                        + "\nhttps://github.com/jagrosh/MusicBot/wiki/Finding-Your-User-ID"
                        + "\n所有者のユーザーID: "));
                }
                catch(NumberFormatException | NullPointerException ex)
                {
                    owner = 0;
                }
                if(owner<=0)
                {
                    prompt.alert(Prompt.Level.ERROR, CONTEXT, "無効なユーザーIDです！終了します。\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
                    System.exit(0);
                }
                else
                {
                    write = true;
                }
            }
            
            if(write)
            {
                String original = OtherUtil.loadResource(this, "/reference.conf");
                byte[] bytes;
                if(original==null)
                {
                    bytes = ("token = "+token+"\r\nowner = "+owner).getBytes();
                }
                else
                {
                    bytes = original.substring(original.indexOf(START_TOKEN)+START_TOKEN.length(), original.indexOf(END_TOKEN))
                        .replace("BOT_TOKEN_HERE", token).replace("Botトークンをここに貼り付け", token)
                        .replace("0 // OWNER ID", Long.toString(owner)).replace("所有者IDをここに貼り付け", Long.toString(owner))
                        .trim().getBytes();
                }
                try 
                {
                    Files.write(path, bytes);
                }
                catch(IOException ex) 
                {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "新しい設定を書き込めませんでした: "+ex
                        + "\nファイルがデスクトップまたはその他の制限された領域にないことを確認してください。\n\n設定ファイルの場所: "
                        + path.toAbsolutePath().toString());
                }
            }
            
            // if we get through the whole config, it's good to go
            valid = true;
        }
        catch (ConfigException ex)
        {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, ex + ": " + ex.getMessage() + "\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
        }
    }
    
    public boolean isValid()
    {
        return valid;
    }
    
    public String getConfigLocation()
    {
        return path.toFile().getAbsolutePath();
    }
    
    public String getPrefix()
    {
        return prefix;
    }
    
    public String getAltPrefix()
    {
        return "NONE".equalsIgnoreCase(altprefix) ? null : altprefix;
    }
    
    public String getToken()
    {
        return token;
    }
    
    public long getOwnerId()
    {
        return owner;
    }
    
    public String getSuccess()
    {
        return successEmoji;
    }
    
    public String getWarning()
    {
        return warningEmoji;
    }
    
    public String getError()
    {
        return errorEmoji;
    }
    
    public String getLoading()
    {
        return loadingEmoji;
    }
    
    public String getSearching()
    {
        return searchingEmoji;
    }
    
    public Game getGame()
    {
        return game;
    }
    
    public OnlineStatus getStatus()
    {
        return status;
    }
    
    public String getHelp()
    {
        return helpWord;
    }
    
    public boolean getStay()
    {
        return stayInChannel;
    }
    
    public boolean getSongInStatus()
    {
        return songInGame;
    }
    
    public String getPlaylistsFolder()
    {
        return playlistsFolder;
    }
    
    public boolean getDBots()
    {
        return dbots;
    }
    
    public boolean useUpdateAlerts()
    {
        return updatealerts;
    }
    
    public boolean useEval()
    {
        return useEval;
    }
    
    public boolean useNPImages()
    {
        return npImages;
    }
    
    public long getMaxSeconds()
    {
        return maxSeconds;
    }
    
    public String getMaxTime()
    {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }
    
    public boolean isTooLong(AudioTrack track)
    {
        if(maxSeconds<=0)
            return false;
        return Math.round(track.getDuration()/1000.0) > maxSeconds;
    }

    // [JMusicBot-JP] new function: support niconico play
    public boolean isNicoNicoEnabled() {
        return useNicoNico;
    }

    public String getNicoNicoEmailAddress() {
        return nicoEmail;
    }

    public String getNicoNicoPassword() {
        return nicoPass;
    }
    // [JMusicBot-JP] End
}
