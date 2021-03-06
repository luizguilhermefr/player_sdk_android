package com.sambatech.player.mediasource;

import android.net.Uri;

import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.sambatech.player.offline.SambaDownloadManager;

/**
 * Created by luizbyrro on 28/11/2017.
 */

public class PlayerMediaSourceExtractor extends PlayerMediaSource implements PlayerMediaSourceInterface {

    public PlayerMediaSourceExtractor(PlayerInstanceDefault playerInstanceDefault, String url) {
        super(playerInstanceDefault);
        this.setUrl(url);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        Uri uri = Uri.parse(url);

        DataSource.Factory dataSourceFactory = SambaDownloadManager.getInstance().isConfigured() ? SambaDownloadManager.getInstance().buildDataSourceFactory() : this.playerInstanceDefault.mediaDataSourceFactory;

        setMediaSource(new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri));
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
