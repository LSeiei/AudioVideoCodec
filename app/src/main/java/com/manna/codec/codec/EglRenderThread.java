package com.manna.codec.codec;

import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.manna.codec.MediaCodecConstant;
import com.manna.codec.surface.EglManager;
import com.manna.codec.surface.EglSurfaceView;

/**
 * EglRender thread
 */
public class EglRenderThread extends Thread {

    private boolean isStart;
    private boolean isStop;

    private EglManager eglManager;

    private Surface surface;
    private EGLContext eglContext;
    private EglSurfaceView.Render surfaceRender;
    private int renderMode;
    private int width;
    private int height;

    public EglRenderThread(Surface surface, EGLContext eglContext, EglSurfaceView.Render surfaceRender,
                           int renderMode, int width, int height) {
        this.surface = surface;
        this.eglContext = eglContext;
        this.surfaceRender = surfaceRender;
        this.renderMode = renderMode;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        super.run();
        isStart = false;
        isStop = false;
        eglManager = new EglManager();
        eglManager.init(surface, eglContext);

        while (!isStop) {
            if (isStart) {
                if (renderMode == GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
                    try {
                        Thread.sleep(1000 / 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (MediaCodecConstant.surfaceCreate && surfaceRender != null) {
                surfaceRender.onSurfaceCreated();
                MediaCodecConstant.surfaceCreate = false;
            }
            if (MediaCodecConstant.surfaceChange && surfaceRender != null) {
                surfaceRender.onSurfaceChanged(width, height);
                MediaCodecConstant.surfaceChange = false;
            }

            if (surfaceRender != null) {
                surfaceRender.onDrawFrame();
                if (!isStart) {
                    //首次调用
                    surfaceRender.onDrawFrame();
                }
            }
            //OpenGL ES绘制完毕后调用swapBuffer,将前台的FrameBuffer和后台FrameBuffer进行交换
            eglManager.swapBuffer();
            isStart = true;
        }
        isStop = true;
        eglManager.release();
        eglManager = null;
    }

    public void stopEglRender() {
        isStop = true;
    }
}
