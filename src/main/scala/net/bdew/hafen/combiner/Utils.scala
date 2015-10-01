package net.bdew.hafen.combiner

import java.nio.ByteBuffer
import java.nio.channels.{ReadableByteChannel, WritableByteChannel}

object Utils {
  def fullyRead(ch: ReadableByteChannel, buf: ByteBuffer): Unit =
    while (ch.read(buf) >= 0 && buf.position() < buf.limit()) {}

  def fullyWrite(ch: WritableByteChannel, buf: ByteBuffer): Unit =
    while (buf.hasRemaining)
      ch.write(buf)
}
