package xyz.sziit.mediacodectest;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by Money on 2017/12/12.
 */

public class Encode {

    private MediaCodec codec;
    private IEncoderListener encoderListener;

    private int videoW;
    private int videoH;
    private int videoBitrate;
    private int videoFrameRate;

    private static final String TAG = "Encode";
    private static final String MIME = "Video/AVC";

    public Encode(int videoW, int videoH, int videoBitrate, int videoFrameRate, IEncoderListener encoderListener) {
        this.videoW = videoW;
        this.videoH = videoH;
        this.videoBitrate = videoBitrate;
        this.videoFrameRate = videoFrameRate;
        this.encoderListener = encoderListener;

        initMediaCodec();
    }

    private void initMediaCodec() {
        try {
            codec = MediaCodec.createEncoderByType(MIME);

            MediaFormat format = MediaFormat.createVideoFormat(MIME, videoW, videoH);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encoderYUV420(byte[] input) {
        try {
            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(input);
                codec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[outputBuffer.remaining()];
                outputBuffer.get(outData, 0, outData.length);
                if (encoderListener != null) {
                    encoderListener.onH264(outData);
                }
                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseMediaCodec() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
    }

    public interface IEncoderListener {
        void onH264(byte[] data);
    }
}
