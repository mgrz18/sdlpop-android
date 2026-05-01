package com.mgrz18.sdlpop;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.libsdl.app.SDLActivity;

public class PoPActivity extends SDLActivity {

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL2",
            "SDL2_image",
            "main"
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AssetExtractor.extractIfNeeded(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TouchOverlayView overlay = new TouchOverlayView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        addContentView(overlay, params);
    }
}
