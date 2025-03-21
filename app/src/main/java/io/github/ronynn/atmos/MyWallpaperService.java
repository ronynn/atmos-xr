package io.github.ronynn.atmos;

import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends Engine {

        private WebView webView;

        public MyEngine() {
            // Ensure that the WebView is initialized on the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    webView = new WebView(MyWallpaperService.this);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.setWebViewClient(new WebViewClient());
                    // Load your fullpage HTML animation from assets
                    webView.loadUrl("file:///android_asset/index.html");
                }
            });
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (webView != null) {
                if (visible) {
                    webView.onResume();
                } else {
                    webView.onPause();
                }
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (webView != null) {
                webView.destroy();
            }
        }
    }
}