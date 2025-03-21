package io.github.ronynn.atmos;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends Engine {

        private WebView webView;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Runnable drawRunnable = new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };

        public MyEngine() {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    webView = new WebView(MyWallpaperService.this);
                    // Apply all required settings
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setAllowFileAccess(true);
                    webView.getSettings().setAllowContentAccess(true);
                    webView.getSettings().setAllowFileAccessFromFileURLs(true);
                    webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    
                    // Force hardware acceleration
                    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    
                    // Set clients to help with debugging/logging.
                    webView.setWebViewClient(new WebViewClient());
                    webView.setWebChromeClient(new WebChromeClient());
                    
                    // Load index.html from assets.
                    webView.loadUrl("file:///android_asset/index.html");
                    webView.loadUrl("javascript:startVanta();");
                }
            });
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null && webView != null) {
                    int width = canvas.getWidth();
                    int height = canvas.getHeight();
                    webView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                    );
                    webView.layout(0, 0, width, height);
                    webView.draw(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            // Schedule the next frame (about 30 FPS)
            handler.postDelayed(drawRunnable, 33);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                handler.post(drawRunnable);
            } else {
                handler.removeCallbacks(drawRunnable);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            handler.removeCallbacks(drawRunnable);
            if (webView != null) {
                webView.destroy();
            }
        }
    }
}