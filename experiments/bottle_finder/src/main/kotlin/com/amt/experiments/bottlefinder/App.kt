package com.amt.experiments.bottlefinder

import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgproc

fun getImgContours(img: opencv_core.Mat, threshold: Double): opencv_core.MatVector {
  val grayScaleImg = img.clone()
  opencv_imgproc.cvtColor(img, grayScaleImg, org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY)

  val filteredImg = img.clone()
  opencv_imgproc.Canny(grayScaleImg, filteredImg, 0.0, threshold)

  val contours = opencv_core.MatVector()
  opencv_imgproc.findContours(
    filteredImg, contours, opencv_imgproc.CV_RETR_EXTERNAL, opencv_imgproc.CV_CHAIN_APPROX_NONE)

  return contours
}

fun main(args: Array<String>) {
  val srcImg = opencv_imgcodecs.imread(String::class.java.getResource("/bottles.jpg").file)
  val templateContours = getImgContours(
    opencv_imgcodecs.imread(String::class.java.getResource("/bottle.jpg").file), 1000.0)
  if(templateContours.size() < 1) {
    throw RuntimeException("Cannot extract contours from template image.")
  }

  var templateContour = templateContours.get(0)

  val srcImgContours = getImgContours(srcImg, 1000.0)

  val matchingContours = arrayListOf<opencv_core.Mat>()
  for(i in 0..srcImgContours.size() - 1) {
    val imgContour = srcImgContours.get(i)
    if(opencv_imgproc.matchShapes(
      templateContour, imgContour, opencv_imgproc.CV_CONTOURS_MATCH_I1, 0.0) > 0 ) {
      matchingContours.add(imgContour)
    }
  }

  var result = opencv_core.Mat(srcImg.rows(), srcImg.cols(), srcImg.type(), opencv_core.Scalar(0.0, 0.0, 0.0, 0.0))

  opencv_imgproc.drawContours(
    result, opencv_core.MatVector(*matchingContours.toTypedArray()), -1, opencv_core.Scalar(255.0, 255.0, 0.0, 1.0))

  opencv_imgcodecs.imwrite("/tmp/result.jpg", result)
}
