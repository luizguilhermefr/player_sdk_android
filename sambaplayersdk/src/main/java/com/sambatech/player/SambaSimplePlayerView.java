package com.sambatech.player;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.sambatech.player.adapter.CaptionsSheetAdapter;
import com.sambatech.player.adapter.OutputSheetAdapter;
import com.sambatech.player.adapter.SpeedSheetAdapter;
import com.sambatech.player.mediasource.PlayerMediaSourceInterface;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.utils.OptionsMenuLayer;
import com.sambatech.player.utils.Util;

import static android.graphics.Typeface.NORMAL;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.accessibility.CaptioningManager.CaptionStyle.EDGE_TYPE_NONE;

/**
 * Created by luizbyrro on 30/11/2017.
 */

public class SambaSimplePlayerView implements View.OnClickListener {

    private Context context;
    private FrameLayout playerContainer;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private OptionsMenuLayer optionsMenuLayer;
    private FrameLayout loadingView;

    //bottom buttons e  progresso
    private LinearLayout progressControls;
    private ImageButton fullscreenButton;

    //barras de controle
    private LinearLayout bottomBar;
    private LinearLayout controlsView;
    private LinearLayout topBar;

    //topbar buttons e texto
    private ImageButton optionsMenuButton;
    private ImageButton liveButton;
    private ImageButton castButton;
    private TextView videoTitle;

    private boolean isFullscreen = false;
    private boolean isReverseLandscape = false;

    private boolean isLive = false;
    private boolean isVideo = false;

    private View outputSheetView;
    private View captionSheetView;
    private View speedSheetView;


    /**
     * The output menu modalsheet.
     */
    private BottomSheetDialog outputSheetDialog;

    /**
     * Captions menu modalsheet
     */
    private BottomSheetDialog captionsSheetDialog;

    /**
     * Speed menu modalsheet
     */
    private BottomSheetDialog speedSheetDialog;

    /**
     * Indicates playback last state before the output menu has open.
     */
    private boolean menuWasPlaying;

    public SambaSimplePlayerView(Context context, FrameLayout playerContainer) {
        this.context = context;
        this.playerContainer = playerContainer;
        playerView = (SimpleExoPlayerView) SimpleExoPlayerView.inflate(context, R.layout.custom_simple_exo_player_view, null);
        bindMethods();
        createMenuView();
        this.originalContainerLayoutParams = this.playerContainer.getLayoutParams();
        this.playerContainer.addView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.playerContainer.setBackgroundColor(Color.BLACK);
        playerView.setBackgroundColor(Color.BLACK);
    }

    public void bindMethods() {
        videoTitle = (TextView) playerView.findViewById(R.id.video_title_text);
        optionsMenuButton = (ImageButton) playerView.findViewById(R.id.topbar_menu_button);
        liveButton = (ImageButton) playerView.findViewById(R.id.topbar_live_button);
        castButton = (ImageButton) playerView.findViewById(R.id.topbar_cast_button);
        fullscreenButton = (ImageButton) playerView.findViewById(R.id.fullscreen_button);
        loadingView = (FrameLayout) playerView.findViewById(R.id.exo_progress_view);

        controlsView = (LinearLayout) playerView.findViewById(R.id.exo_control_bar);
        topBar = (LinearLayout) playerView.findViewById(R.id.exo_top_bar);
        bottomBar = (LinearLayout) playerView.findViewById(R.id.exo_bottom_bar);

        progressControls = (LinearLayout) playerView.findViewById(R.id.exo_progress_controls);


        fullscreenButton.setOnClickListener(this);
        optionsMenuButton.setOnClickListener(this);
    }

    public void createMenuView() {
        View menuPlaceholder = playerView.findViewById(R.id.exo_menu_placeholder);
        ViewGroup parent = ((ViewGroup) menuPlaceholder.getParent());
        this.optionsMenuLayer = new OptionsMenuLayer(context, parent);
        parent.addView(optionsMenuLayer, menuPlaceholder.getLayoutParams());
        optionsMenuLayer.setCallback(optionsMenuCallBack);
    }

    public void configView(boolean isVideo, boolean isLive) {
        this.isLive = isLive;
        this.isVideo = isVideo;
        if (isVideo) {
            playerView.setControllerHideOnTouch(true);
            playerView.setControllerShowTimeoutMs(2 * 1000);
            topBar.setVisibility(View.VISIBLE);
            if (isLive) {
                castButton.setVisibility(View.GONE);
                progressControls.setVisibility(View.GONE);
                liveButton.setVisibility(View.VISIBLE);
            } else {
                castButton.setVisibility(View.VISIBLE);
                progressControls.setVisibility(View.VISIBLE);
                liveButton.setVisibility(View.GONE);
            }
        } else {
            playerView.setControllerHideOnTouch(false);
            playerView.setControllerShowTimeoutMs(-1);
            topBar.setVisibility(View.GONE);
            fullscreenButton.setVisibility(View.GONE);
            if (isLive) {
                progressControls.setVisibility(View.GONE);
            } else {
                progressControls.setVisibility(View.VISIBLE);
            }
        }
        optionsMenuButton.setVisibility(View.GONE);
    }

    private OptionsMenuLayer.OptionsMenuCallback optionsMenuCallBack = new OptionsMenuLayer.OptionsMenuCallback() {
        @Override
        public void onTouchHD() {
            if (outputSheetDialog == null) return;
            outputSheetDialog.show();
        }

        @Override
        public void onTouchCaptions() {
            if (captionsSheetDialog == null) return;
            captionsSheetDialog.show();
        }

        @Override
        public void onTouchSpeed() {
            if (speedSheetDialog == null) return;
            speedSheetDialog.show();
        }

        @Override
        public void onMenuDismiss() {
            if (menuWasPlaying) {
                player.setPlayWhenReady(true);
                playerView.hideController();
            } else {
                playerView.showController();
            }
            if (isFullscreen) {
                ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.topbar_menu_button) {
            optionsMenuLayer.showMenu();
            menuWasPlaying = player.isPlayingAd() || (player.getPlayWhenReady() && (player.getPlaybackState() == Player.STATE_READY || player.getPlaybackState() == Player.STATE_BUFFERING));
            player.setPlayWhenReady(false);
            playerView.hideController();
        } else if (viewId == R.id.topbar_cast_button) {

        } else if (viewId == R.id.topbar_live_button) {

        } else if (viewId == R.id.fullscreen_button) {
            doToggleFullscreen();
        }
    }

    public void setPlayer(@NonNull SimpleExoPlayer simpleExoPlayer) {
        this.player = simpleExoPlayer;
        playerView.setPlayer(player);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            controlsView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            controlsView.setVisibility(View.VISIBLE);
        }
    }

    public void setEnableControls(boolean flag) {
        playerView.setUseController(flag);
    }

    public void setVideoTitle(String title) {
        videoTitle.setText(title);
    }

    public void configureSubTitle(SambaMedia.CaptionsConfig captionsConfig) {
        Typeface typeface = Typeface.create((Typeface) null, NORMAL);
        CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(captionsConfig.color, 0, 0, EDGE_TYPE_NONE, 0, typeface);
        playerView.getSubtitleView().setStyle(captionStyleCompat);
        playerView.getSubtitleView().setFixedTextSize(COMPLEX_UNIT_SP, captionsConfig.size);
    }

    public void setupMenu(PlayerMediaSourceInterface playerMediaSource, Format selectedVideo, Format selectedSubtitle, boolean isAbrEnabled) {
        if (!isVideo) {
            outputSheetView = null;
            outputSheetDialog = null;
            captionSheetView = null;
            captionsSheetDialog = null;
            speedSheetDialog = null;
            speedSheetView = null;
            optionsMenuButton.setVisibility(View.GONE);
            return;
        }
        if (playerMediaSource.getVideoOutputsTracks() != null && playerMediaSource.getVideoOutputsTracks().length > 1) {
            initOutputMenu(playerMediaSource, selectedVideo, isAbrEnabled);
        } else {
            outputSheetView = null;
            outputSheetDialog = null;
        }
        if (playerMediaSource.getSubtitles() != null && playerMediaSource.getSubtitles().length > 1) {
            initCaptionMenu(playerMediaSource, selectedSubtitle);
        } else {
            captionSheetView = null;
            captionsSheetDialog = null;
        }
        if (!isLive) {
            initSpeedMenu();
        } else {
            speedSheetDialog = null;
            speedSheetView = null;
        }
        boolean hasMenu = captionsSheetDialog != null || outputSheetDialog != null || speedSheetDialog != null;
        optionsMenuButton.setVisibility(hasMenu ? View.VISIBLE : View.GONE);
        optionsMenuLayer.setCaptionsButtonVisibility(captionsSheetDialog != null);
        optionsMenuLayer.setHdButtonVisibility(outputSheetDialog != null);
        optionsMenuLayer.setSpeedButtonVisibility(speedSheetDialog != null);
    }

    private void initCaptionMenu(final PlayerMediaSourceInterface playerMediaSource, Format currentCaption) {
        final TrackGroupArray captions = playerMediaSource.getSubtitles();
        captionSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) captionSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.captions));
        final ListView menuList = (ListView) captionSheetView.findViewById(R.id.sheet_list);
        final CaptionsSheetAdapter adapter = new CaptionsSheetAdapter(context, captions);
        menuList.setAdapter(adapter);
        if (currentCaption != null) {
            for (int i = 0; i < captions.length; i++) {
                if (captions.get(i).getFormat(0) == currentCaption) {
                    adapter.currentIndex = i;
                }
            }
        } else {
            adapter.currentIndex = 0;
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeCaptionMenu();
                TrackGroup format = captions.get(position);
                playerMediaSource.setSubtitle(format);
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        captionsSheetDialog = setupMenuDialog(captionSheetView);
    }

    private void initOutputMenu(final PlayerMediaSourceInterface playerMediaSource, Format currentOutput, boolean isAbrEnabled) {
        final TrackGroup outputs = playerMediaSource.getVideoOutputsTracks();
        outputSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) outputSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.output));
        final ListView menuList = (ListView) outputSheetView.findViewById(R.id.sheet_list);
        final OutputSheetAdapter adapter = new OutputSheetAdapter(context, outputs, isAbrEnabled);
        menuList.setAdapter(adapter);
        if (currentOutput != null) {
            adapter.currentIndex = outputs.indexOf(currentOutput) + (isAbrEnabled ? 1 : 0);
        } else {
            adapter.currentIndex = 0;
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeOutputMenu();
                Format format = (Format) adapter.getItem(position);
                playerMediaSource.setVideoOutputTrack(format);
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;
            }
        });
        menuList.deferNotifyDataSetChanged();
        outputSheetDialog = setupMenuDialog(outputSheetView);
    }

    private void initSpeedMenu() {
        speedSheetView = ((Activity) context).getLayoutInflater().inflate(R.layout.action_sheet, null);
        TextView title = (TextView) speedSheetView.findViewById(R.id.action_sheet_title);
        title.setText(context.getString(R.string.speed));
        final ListView menuList = (ListView) speedSheetView.findViewById(R.id.sheet_list);
        final float[] speeds = new float[]{0.25f, 0.5f, 1.0f, 1.5f, 2.0f};
        final float[] audioPitch = new float[]{0.25f, 0.5f, 1.0f, 1.5f, 2.0f};
        final SpeedSheetAdapter adapter = new SpeedSheetAdapter(context, speeds);
        menuList.setAdapter(adapter);
        for (int i = 0; i < speeds.length; i++) {
            if (player.getPlaybackParameters().speed == speeds[i]) {
                adapter.currentIndex = i;
            }
        }
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeSpeedMenu();
                player.setPlaybackParameters(new PlaybackParameters(speeds[position], audioPitch[2]));
                menuList.smoothScrollToPosition(0);
                adapter.currentIndex = position;

            }
        });
        menuList.deferNotifyDataSetChanged();
        speedSheetDialog = setupMenuDialog(speedSheetView);
    }


    /**
     * Sets the adapter for all menus
     *
     * @param view The view for the caption menu
     */
    public BottomSheetDialog setupMenuDialog(View view) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
                if (menuWasPlaying) {
                    player.setPlayWhenReady(true);
                    playerView.hideController();
                } else {
                    playerView.showController();
                }
                if (isFullscreen) {
                    ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                }
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(Integer.MAX_VALUE);
        return bottomSheetDialog;
    }

    /**
     * Closes the output menu.
     */
    public void closeOutputMenu() {
        outputSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    /**
     * Closes the caption menu.
     */
    public void closeCaptionMenu() {
        captionsSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    /**
     * Closes the speed menu.
     */
    public void closeSpeedMenu() {
        speedSheetDialog.dismiss();
        if (optionsMenuLayer != null) optionsMenuLayer.hideMenu();
    }

    public SimpleExoPlayerView getPlayerView() {
        return this.playerView;
    }


    private final int mFullScreenFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;


    /**
     * This is the layout of the container before fullscreen mode has been entered.
     * When we leave fullscreen mode, we restore the layout of the container to this layout.
     */
    private ViewGroup.LayoutParams originalContainerLayoutParams;

    private FullscreenCallback fullscreenCallback;

    public void setFullscreenCallback(FullscreenCallback fullscreenCallback) {
        this.fullscreenCallback = fullscreenCallback;
    }

    public void doToggleFullscreen() {
        setFullscreen(!isFullscreen);
    }


    public void setFullscreen(boolean newValue) {
        setFullscreen(newValue, false);
    }

    /**
     * Fullscreen mode will rotate to landscape mode, hide the action bar, hide the navigation bar,
     * hide the system tray, and make the video player take up the full size of the display.
     * The developer who is using this function must ensure the following:
     * <p>
     * <p>1) Inside the android manifest, the activity that uses the video player has the attribute
     * android:configChanges="orientation".
     * <p>
     * <p>2) Other views in the activity (or fragment) are
     * hidden (or made visible) when this method is called.
     *
     * @param isReverseLandscape Whether orientation is reverse landscape.
     */
    public void setFullscreen(boolean newValue, boolean isReverseLandscape) {
        if (this.isFullscreen == newValue && this.isReverseLandscape == isReverseLandscape) return;
        if (fullscreenCallback == null) return;
        final Activity activity = (Activity) context;
        if (newValue == false) {
            fullscreenCallback.onReturnFromFullscreen();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Make the status bar and navigation bar visible again.
            activity.getWindow().getDecorView().setSystemUiVisibility(0);

            playerContainer.setLayoutParams(originalContainerLayoutParams);
            fullscreenButton.setImageResource(R.drawable.fullscreen);
            this.isFullscreen = newValue;
            this.isReverseLandscape = isReverseLandscape;
        } else {
            fullscreenCallback.onGoToFullscreen();
            activity.setRequestedOrientation(isReverseLandscape ?
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE :
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            activity.getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);

            // Whenever the status bar and navigation bar appear, we want the playback controls to
            // appear as well.
            activity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int i) {
                            // By doing a logical AND, we check if the fullscreen option is triggered (i.e. the
                            // status bar is hidden). If the result of the logical AND is 0, that means that the
                            // fullscreen flag is NOT triggered. This means that the status bar is showing. If
                            // this is the case, then we show the playback controls as well (by calling show()).
                            if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                if (!optionsMenuLayer.isVisible()) playerView.showController();
                            }
                            if (isFullscreen)
                                activity.getWindow().getDecorView().setSystemUiVisibility(mFullScreenFlags);
                        }
                    }
            );
            playerContainer.setLayoutParams(Util.getLayoutParamsBasedOnParent(playerContainer,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            fullscreenButton.setImageResource(R.drawable.fullscreen_exit);
            this.isFullscreen = newValue;
            this.isReverseLandscape = isReverseLandscape;
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public interface FullscreenCallback {

        /**
         * When triggered, the activity should hide any additional views.
         */
        void onGoToFullscreen();

        /**
         * When triggered, the activity should show any views that were hidden when the player
         * went to fullscreen.
         */
        void onReturnFromFullscreen();
    }

    public void destroyInternal() {
        destroyDialogs();
        setEnableControls(false);
        optionsMenuLayer.setCallback(null);
        fullscreenCallback = null;
        originalContainerLayoutParams = null;
        if (optionsMenuLayer != null)
            ((ViewGroup) optionsMenuLayer.getParent()).removeView(optionsMenuLayer);
        if (playerView != null)
            ((ViewGroup) playerView.getParent()).removeView(playerView);
        optionsMenuLayer = null;
        playerView = null;
    }

    private void destroyDialogs() {
        if (outputSheetView != null) {
            ((ListView) outputSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (captionSheetView != null) {
            ((ListView) captionSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (speedSheetView != null) {
            ((ListView) speedSheetView.findViewById(R.id.sheet_list)).setOnItemClickListener(null);
        }
        if (outputSheetDialog != null) outputSheetDialog.cancel();
        if (captionsSheetDialog != null) captionsSheetDialog.cancel();
        if (speedSheetDialog != null) speedSheetDialog.cancel();
        outputSheetView = null;
        captionSheetView = null;
        speedSheetView = null;
        outputSheetDialog = null;
        captionsSheetDialog = null;
        speedSheetDialog = null;
    }
}