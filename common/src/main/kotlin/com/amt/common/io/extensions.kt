package com.amt.common.io

public inline fun <T : AutoCloseable, R> T.useAutoClosable(block: (T) -> R): R {
  var closed = false
  try {
    return block(this)
  } catch (e: Exception) {
    closed = true
    try {
      close()
    } catch (closeException: Exception) {    }
    throw e
  } finally {
    if (!closed) {
      close()
    }
  }
}
