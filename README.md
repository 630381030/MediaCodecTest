## MediaCodec原理

- 参考Android官方：[https://developer.android.com/reference/android/media/MediaCodec.html](https://developer.android.com/reference/android/media/MediaCodec.html)

### MediaCode编码的流程

#### 编码器初始化

创建编码器

```java
codec = MediaCodec.createEncoderByType(MIME);
```

创建媒体编码格式

```java
MediaFormat format = MediaFormat.createVideoFormat(MIME, videoW, videoH);
format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate);
format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
```

配置编码器

```java
codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
```

启动编码器

```java
codec.start();
```

#### 将原始数据提交给编码器

查询编码器可用输入缓冲区索引

```java
int inputBufferIndex = codec.dequeueInputBuffer(-1);
```

根据输入缓冲区索引获取输入缓冲区

```java
ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
```

将编码数据填充到输入缓冲区

```java
inputBuffer.clear();
inputBuffer.put(input);
```

将填充好的输入缓冲器的索引提交给编码器，注意第四个参数是缓冲区的时间戳，微秒单位，后一帧的时间应该大于前一帧

```java
codec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
```

#### 从编码器获取已经编码好的数据

查询编码好的输出缓冲区索引

```java
MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
```

根据索引获取输出缓冲区

```java
ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
```

从缓冲区获取编码成H264的byte[]

```java
byte[] outData = new byte[outputBuffer.remaining()];
outputBuffer.get(outData, 0, outData.length);
```

根据输出缓冲区的索引释放该输出缓冲区

```java
codec.releaseOutputBuffer(outputBufferIndex, false);
```

#### 发送H264给VLC

创建UDP的Socket

```java
socket = new DatagramSocket();
```

初始化VLC播放器地址

```java
address = InetAddress.getByName(VLC_HOST);
```

通过UDP,将编码成H264的数据传输给VLC播放器

```java
DatagramPacket packet = new DatagramPacket(data, 0, data.length, address, VLC_PORT);
socket.send(packet);
```

#### 释放编码器

```java
if (codec != null) {
    codec.stop();
    codec.release();
    codec = null;
}
```

---

## 设置VLC播放器

首先将VLC的去复用模块设置为**H264视频去复用器**，然后打开网络串流，监听UDP流，具体设置流程如下面图片所示。

![](https://raw.githubusercontent.com/630381030/MarkdownImages/master/20171213/TIM%E6%88%AA%E5%9B%BE20171213112251.png)

![](https://raw.githubusercontent.com/630381030/MarkdownImages/master/20171213/TIM%E6%88%AA%E5%9B%BE20171213112335.png)

![](https://raw.githubusercontent.com/630381030/MarkdownImages/master/20171213/TIM%E6%88%AA%E5%9B%BE20171213112402.png)

![](https://raw.githubusercontent.com/630381030/MarkdownImages/master/20171213/TIM%E6%88%AA%E5%9B%BE20171213112424.png)

![](https://raw.githubusercontent.com/630381030/MarkdownImages/master/20171213/TIM%E6%88%AA%E5%9B%BE20171213112455.png)

---

## 示例源码

- [https://github.com/630381030/MediaCodecTest](https://github.com/630381030/MediaCodecTest)