package com.sedmelluq.discord.lavaplayer.source.youtube;

import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

public class YoutubeHttpContextFilter implements HttpContextFilter {
    @Override
    public void onContextOpen(HttpClientContext context) {
        CookieStore cookieStore = context.getCookieStore();

        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
            context.setCookieStore(cookieStore);
        }

        // Reset cookies for each sequence of requests.
        cookieStore.clear();
    }

    @Override
    public void onContextClose(HttpClientContext context) {

    }

    @Override
    public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
        request.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/83.0.4103.116 Safari/537.36");
        request.setHeader("x-youtube-client-name", "1");
        request.setHeader("x-youtube-client-version", "2.20200701.03.01");
        request.setHeader("x-youtube-page-cl", "319190200");
        request.setHeader("x-youtube-page-label", "youtube.ytfe.desktop_20200630_3_RC1");
        request.setHeader("x-youtube-time-zone", "Asia/Tokyo");
        request.setHeader("x-youtube-utc-offset", "540");
        request.setHeader("x-youtube-variants-checksum", "9dbf566520bdead91596fadc55a8deef");
        request.setHeader("accept-language", "ja");
    }

    @Override
    public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
        return false;
    }

    @Override
    public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
        return false;
    }
}
