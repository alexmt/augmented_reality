package com.amt.experiments.patternrecognition

import com.amt.common.io.useAutoClosable
import com.amt.experiments.livestreamer.Streamer
import com.google.common.collect.AbstractIterator
import org.bytedeco.javacpp.*
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_videoio.VideoCapture
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun main(args: Array<String>) {
  val extension = "bmp"
  val imageReader = ImageIO.getImageReadersByFormatName(extension).next()!!
  var cascadeBytes = Streamer::class.java.getResource("/haarcascade_frontalface_default.xml")
  val classifier = opencv_objdetect.CascadeClassifier(cascadeBytes.file)

  VideoCapture(0).useAutoClosable { capture ->
    Streamer(640, 480).start(object : AbstractIterator<BufferedImage>() {
      override fun computeNext(): BufferedImage? {
        val frame = Mat()
        capture.read(frame)
        BytePointer().useAutoClosable { pointer ->
          val faces = opencv_core.RectVector()
          classifier.detectMultiScale(frame, faces)
          for (i in 0..faces.size() - 1) {
            val face = faces.get(i)
            opencv_imgproc.rectangle(frame,
              face,
              opencv_core.Scalar.RED,
              2,
              opencv_core.FILLED,
              0)
          }
          opencv_imgcodecs.imencode(".$extension", frame, pointer)
          val result = ByteArray(pointer.limit())
          pointer.get(result)
          imageReader.setInput(ImageIO.createImageInputStream(result.inputStream()), true)
          return imageReader.read(0)
        }
      }
    })
  }
}
