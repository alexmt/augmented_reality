package com.amt.experiments.livestreamer

import com.xuggle.mediatool.ToolFactory
import com.xuggle.xuggler.ICodec
import com.xuggle.xuggler.IContainerFormat
import com.xuggle.xuggler.io.XugglerIO
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.HandlerList
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Streamer(val width: Int, val height: Int) {
  fun start(source: Iterator<BufferedImage?>) {
    val server = Server(8080)
    val streamHandler = ContextHandler("/stream")
    streamHandler.handler = object : AbstractHandler() {
      override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
        val startTime = System.nanoTime()
        val writer = ToolFactory.makeWriter(XugglerIO.map(response!!.outputStream))
        val containerFormat = IContainerFormat.make()
        containerFormat.setOutputFormat("ogg", null, "application/ogg")
        writer.container.setFormat(containerFormat)
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_THEORA, width, height)

        var next: BufferedImage? = source.next()
        while (next != null) {
          writer.encodeVideo(0, next, System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
          next = source.next()
          response.flushBuffer()
        }
        response.status = HttpServletResponse.SC_OK
        baseRequest!!.isHandled = true
      }
    }

    val pageHandler = ContextHandler("/")
    pageHandler.handler = object : AbstractHandler() {
      override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
        response!!.writer.print("""
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="utf-8">
            <title>Video</title>
          </head>
          <body>
            <video id="video1" style="width:600px;max-width:100%;" controls="">
            <source src="/stream" type="video/ogg">
            Your browser does not support HTML5 video.
          </video>
          </body>
        </html>
        """)
        response.status = HttpServletResponse.SC_OK
        baseRequest!!.isHandled = true
      }

    }

    val handlers = HandlerList()
    handlers.handlers = arrayOf(streamHandler, pageHandler)

    server.handler = handlers

    server.start()
    server.join()
  }
}
