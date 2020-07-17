package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.convertToMapLayout;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultYoutubeTrackDetailsLoader implements YoutubeTrackDetailsLoader {

    @Override
    public YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId) {
        try {
            return load(httpInterface, videoId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private YoutubeTrackDetails load(HttpInterface httpInterface, String videoId) throws IOException {
        String url = "https://www.youtube.com/watch?v=" + videoId + "&pbj=1&hl=ja";

        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(url))) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("ビデオページ応答の無効なステータスコード： " + statusCode);
            }

            String responseText = EntityUtils.toString(response.getEntity(), UTF_8);

            try {
                JsonBrowser json = JsonBrowser.parse(responseText);
                JsonBrowser playerInfo = JsonBrowser.NULL_BROWSER;
                JsonBrowser statusBlock = JsonBrowser.NULL_BROWSER;

                for (JsonBrowser child : json.values()) {
                    if (child.isMap()) {
                        if (!child.get("player").isNull()) {
                            playerInfo = child.get("player");
                        } else if (!child.get("playerResponse").isNull()) {
                            statusBlock = child.get("playerResponse").get("playabilityStatus");
                        }
                    }
                }

                switch (checkStatusBlock(statusBlock)) {
                    case INFO_PRESENT:
                        if (playerInfo.isNull()) {
                            throw new RuntimeException("プレーヤー情報ブロックはありません。");
                        }

                        return new DefaultYoutubeTrackDetails(videoId, playerInfo);
                    case REQUIRES_LOGIN:
                        return new DefaultYoutubeTrackDetails(videoId, getTrackInfoFromEmbedPage(httpInterface, videoId));
                    case DOES_NOT_EXIST:
                        return null;
                }

                return new DefaultYoutubeTrackDetails(videoId, playerInfo);
            } catch (FriendlyException e) {
                throw e;
            } catch (Exception e) {
                throw new FriendlyException("YouTubeから予期しない応答を受け取りました。", SUSPICIOUS,
                        new RuntimeException("解析できませんでした： " + responseText, e));
            }
        }
    }

    protected InfoStatus checkStatusBlock(JsonBrowser statusBlock) {
        if (statusBlock.isNull()) {
            throw new RuntimeException("再生可能性ステータスブロックはありません。");
        }

        String status = statusBlock.get("status").text();

        if (status == null) {
            throw new RuntimeException("再生可能性ステータスフィールドはありません。");
        } else if ("OK".equals(status)) {
            return InfoStatus.INFO_PRESENT;
        } else if ("ERROR".equals(status)) {
            String reason = statusBlock.get("reason").text();

            if ("Video unavailable".equals(reason)) {
                return InfoStatus.DOES_NOT_EXIST;
            } else {
                throw new FriendlyException(reason, COMMON, null);
            }
        } else if ("UNPLAYABLE".equals(status)) {
            String unplayableReason = getUnplayableReason(statusBlock);
            throw new FriendlyException(unplayableReason, COMMON, null);
        } else if ("LOGIN_REQUIRED".equals(status)) {
            return InfoStatus.REQUIRES_LOGIN;
        } else {
            throw new FriendlyException("この動画は匿名で表示できません。", COMMON, null);
        }
    }

    protected String getUnplayableReason(JsonBrowser statusBlock) {
        JsonBrowser playerErrorMessage = statusBlock.get("errorScreen").get("playerErrorMessageRenderer");
        String unplayableReason = statusBlock.get("reason").text();

        if (!playerErrorMessage.get("subreason").isNull()) {
            JsonBrowser subreason = playerErrorMessage.get("subreason");

            if (!subreason.get("simpleText").isNull()) {
                unplayableReason = subreason.get("simpleText").text();
            } else if (!subreason.get("runs").isNull() && subreason.get("runs").isList()) {
                StringBuilder reasonBuilder = new StringBuilder();
                subreason.get("runs").values().forEach(
                        item -> reasonBuilder.append(item.get("text").text()).append('\n')
                );
                unplayableReason = reasonBuilder.toString();
            }
        }

        return unplayableReason;
    }

    protected JsonBrowser getTrackInfoFromEmbedPage(HttpInterface httpInterface, String videoId) throws IOException {
        JsonBrowser basicInfo = loadTrackBaseInfoFromEmbedPage(httpInterface, videoId);
        basicInfo.put("args", loadTrackArgsFromVideoInfoPage(httpInterface, videoId, basicInfo.get("sts").text()));
        return basicInfo;
    }

    protected JsonBrowser loadTrackBaseInfoFromEmbedPage(HttpInterface httpInterface, String videoId) throws IOException {
        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet("https://www.youtube.com/embed/" + videoId))) {
            HttpClientTools.assertSuccessWithContent(response, "embed video page response");

            String html = EntityUtils.toString(response.getEntity(), UTF_8);
            String configJson = DataFormatTools.extractBetween(html, "'PLAYER_CONFIG': ", "});writeEmbed();");

            if (configJson != null) {
                return JsonBrowser.parse(configJson);
            }
        }

        throw new FriendlyException("トラック情報は利用できません。", SUSPICIOUS,
                new IllegalStateException("埋め込みページに必要なプレーヤー設定がありません。"));
    }

    protected Map<String, String> loadTrackArgsFromVideoInfoPage(HttpInterface httpInterface, String videoId, String sts) throws IOException {
        String videoApiUrl = "https://youtube.googleapis.com/v/" + videoId;
        String encodedApiUrl = URLEncoder.encode(videoApiUrl, UTF_8.name());
        String url = "https://www.youtube.com/get_video_info?video_id=" + videoId + "&eurl=" + encodedApiUrl +
                "hl=ja";

        if (sts != null) {
            url += "&sts=" + sts;
        }

        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(url))) {
            HttpClientTools.assertSuccessWithContent(response, "video info response");
            return convertToMapLayout(URLEncodedUtils.parse(response.getEntity()));
        }
    }

    protected enum InfoStatus {
        INFO_PRESENT,
        REQUIRES_LOGIN,
        DOES_NOT_EXIST
    }
}
