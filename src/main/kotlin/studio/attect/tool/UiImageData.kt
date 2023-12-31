package studio.attect.tool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import studio.attect.tool.ComputeBackgroundColor.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.absoluteValue

/**
 * 显示在UI上的图片数据
 */
object UiImageData {

    /**
     * 预览的图片
     */
    var previewImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /**
     * 预览的图片的数据，实际也为计算结果
     */
    var previewImageData = ByteArray(0)
        set(value) {
            previewImage = if (value.isEmpty()) {
                null
            } else {
                loadImageBitmap(ByteArrayInputStream(value))
            }
            field = value
        }

    /**
     * 预览图片缩放
     */
    var previewImageScale by mutableStateOf(1f)

    /**
     * 预览图片显示横轴偏移
     */
    var previewImageOffsetX by mutableStateOf(0f)

    /**
     * 预览图片显示纵轴偏移
     */
    var previewImageOffsetY by mutableStateOf(0f)

    /**
     * 预览图片显示旋转角度
     */
    var previewImageRotate by mutableStateOf(0f)

    var previewMouseRotationLock = false

    var previewMouseRotationStartX = Float.MIN_VALUE

    /**
     * 用于显示的黑色背景图片
     */
    var blackBackgroundImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /**
     * 用于处理的黑色背景图片数据
     */
    var blackBackgroundImageData = ByteArray(0)
        set(value) {
            blackBackgroundImage = if (value.isNotEmpty()) {
                loadImageBitmap(ByteArrayInputStream(value))
            } else {
                null
            }
            field = value
        }


    /**
     * 用于显示的白色背景图片
     */
    var whiteBackgroundImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /**
     * 用于处理的白色背景图片数据
     */
    var whiteBackgroundImageData = ByteArray(0)
        set(value) {
            whiteBackgroundImage = if (value.isNotEmpty()) {
                loadImageBitmap(ByteArrayInputStream(value))
            } else {
                null
            }
            field = value
        }

    /**
     * 黑白地图计算平衡
     */
    var whiteBlackBalance by mutableStateOf(0.5f)


    /**
     * 图片A应透明背景色
     */
    var colorA by mutableStateOf(GREEN)

    /**
     * 用于显示的纯色背景图片A
     */
    var colorABackgroundImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /**
     * 用于处理的纯色背景图片A数据
     */
    var colorABackgroundImageData = ByteArray(0)
        set(value) {
            colorABackgroundImage = if (value.isNotEmpty()) {
                loadImageBitmap(ByteArrayInputStream(value))
            } else {
                null
            }
            field = value
        }

    /**
     * 图片B应透明背景色
     */
    var colorB by mutableStateOf(BLUE)

    /**
     * 用于显示的纯色背景图片B
     */
    var colorBBackgroundImage by mutableStateOf<ImageBitmap?>(null)
        private set

    /**
     * 用于处理的纯色背景图片B数据
     */
    var colorBBackgroundImageData = ByteArray(0)
        set(value) {
            colorBBackgroundImage = if (value.isNotEmpty()) {
                loadImageBitmap(ByteArrayInputStream(value))
            } else {
                null
            }
            field = value
        }


    /**
     * 计算时容许与指定颜色的偏差值
     */
    var colorBackgroundTolerance by mutableStateOf(0f)

    /**
     * 自定义预览背景
     */
    var previewBackgroundImage by mutableStateOf<ImageBitmap?>(null)

    /**
     * 自定义背景图片的数据
     * <br>
     * 实际上不持有，只用于影响[previewBackgroundImage]
     */
    var previewBackgroundImageData = ByteArray(0)
        set(value) {
            previewBackgroundImage = if (value.isNotEmpty()) {
                loadImageBitmap(ByteArrayInputStream(value))
            } else {
                null
            }
        }

    /**
     * 计算前的数据检查
     */
    private fun checkBeforeCompute(): Boolean {
        if (whiteBackgroundImageData.isEmpty() && blackBackgroundImageData.isEmpty()) {
            globalHint = "请至少提供白色或黑色背景的图片"
            return false
        }
        if (colorABackgroundImageData.isEmpty() && colorBBackgroundImageData.isEmpty()) {
            globalHint = "请至少提供一张有色背景（红、绿、蓝）的图片"
            return false
        }

        val currentWhiteBackgroundImage = whiteBackgroundImage
        val currentBlackBackgroundImage = blackBackgroundImage
        val currentColorABackgroundImage = colorABackgroundImage
        val currentColorBBackgroundImage = colorBBackgroundImage

        if (currentColorABackgroundImage == null && currentColorBBackgroundImage == null) {
            globalHint = "请至少提供一张有色背景（红、绿、蓝）的图片"
            return false
        }

        if (currentBlackBackgroundImage != null && currentWhiteBackgroundImage != null && !currentWhiteBackgroundImage.compareSize(currentBlackBackgroundImage)) {
            globalHint = "黑白底色的两张图片尺寸不一致，无法处理"
            return false
        }

        if (currentColorABackgroundImage != null) {
            if (currentWhiteBackgroundImage != null) {
                if (!currentColorABackgroundImage.compareSize(currentWhiteBackgroundImage)) {
                    globalHint = "有色背景（${colorA}）图片与白色背景图片尺寸不一致，无法处理"
                    return false
                }
            }
            if (currentBlackBackgroundImage != null) {
                if (!currentColorABackgroundImage.compareSize(currentBlackBackgroundImage)) {
                    globalHint = "有色背景（${colorA}）图片与黑色背景图片尺寸不一致，无法处理"
                    return false
                }
            }
        }

        if (currentColorBBackgroundImage != null) {
            if (currentWhiteBackgroundImage != null) {
                if (!currentColorBBackgroundImage.compareSize(currentWhiteBackgroundImage)) {
                    globalHint = "有色背景（${colorB}）图片与白色背景图片尺寸不一致，无法处理"
                }
            }
            if (currentBlackBackgroundImage != null) {
                if (!currentColorBBackgroundImage.compareSize(currentBlackBackgroundImage)) {
                    globalHint = "有色背景（${colorB}）图片与黑色背景图片尺寸不一致，无法处理"
                }
            }
        }

        return true
    }

    /**
     * 重置预览图片的相关参数
     */
    fun resetPreview() {
        previewImageScale = 1f
        previewImageOffsetX = 0f
        previewImageOffsetY = 0f
        previewImageRotate = 0f
    }

    fun compute() {
        if (!checkBeforeCompute()) return
        if (((whiteBackgroundImage == null && blackBackgroundImage != null) || (whiteBackgroundImage != null && blackBackgroundImage == null))
            && !(colorABackgroundImage == null && colorBBackgroundImage == null)
        ) {
            compute2ImageMode()
        } else if (colorABackgroundImage == null || colorBBackgroundImage == null) {
            compote3ImageMode()
        } else if (whiteBackgroundImage != null && blackBackgroundImage != null && colorABackgroundImage != null && colorBBackgroundImage != null) {
            compute4ImageBitmap()
        } else {
            globalHint = "请补全必要的素材"
        }
    }

    private fun compute2ImageMode() {
        println("compute2ImageMode")
        val currentColorBackgroundColor: ComputeBackgroundColor

        val colorBackgroundImage: ImageBitmap = if (colorABackgroundImage != null) {
            currentColorBackgroundColor = colorA
            colorABackgroundImage ?: throw IllegalStateException("colorABackgroundImage为null，存在其它线程修改了值？")
        } else if (colorBBackgroundImage != null) {
            currentColorBackgroundColor = colorB
            colorBBackgroundImage ?: throw IllegalStateException("colorBBackgroundImage为null，存在其它线程修改了值？")
        } else {
            throw IllegalStateException("有色图片数据均为null")
        }

        val sourceBackgroundImage = whiteBackgroundImage ?: blackBackgroundImage ?: throw IllegalStateException("whiteBackgroundImage 和 whiteBackgroundImage 均为null，存在其它线程修改了值？")

        val tolerance = (colorBackgroundTolerance * 100).toInt()
        val fullTransparentColor = ComputePixel(0, 0, 0, 0).toInt()

        val computeImage = BufferedImage(colorBackgroundImage.width, colorBackgroundImage.height, BufferedImage.TYPE_INT_ARGB)

        foreach2ImageBitmap(sourceBackgroundImage, colorBackgroundImage) { x, y, sourcePixel, colorPixel ->
            if (colorPixel.alpha == 255 && colorPixel.isWithinTolerance(currentColorBackgroundColor, tolerance)) {
                computeImage.setRGB(x, y, fullTransparentColor)
            } else if (colorPixel == sourcePixel) {
                computeImage.setRGB(x, y, sourcePixel.toInt())
            } else if (colorPixel.isWithinTolerance(sourcePixel, currentColorBackgroundColor, tolerance)) {
                val alphaValue = when (currentColorBackgroundColor) {
                    RED -> ComputePixel::blue
                    GREEN -> ComputePixel::red
                    BLUE -> ComputePixel::green
                }

                val alpha = (255 - (alphaValue.get(colorPixel) - alphaValue(sourcePixel)).absoluteValue)

                val pixel = ComputePixel(
                    alpha = alpha,
                    red = sourcePixel.red,
                    green = sourcePixel.green,
                    blue = sourcePixel.blue,
                )
                computeImage.setRGB(x, y, pixel.toInt())
            } else {
                computeImage.setRGB(x, y, sourcePixel.toInt())
            }
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(computeImage, "png", outputStream)
        previewImageData = outputStream.toByteArray()
        resetPreview()
    }

    private fun compote3ImageMode() {
        println("compute3ImageMode")
        val currentColorBackgroundColor: ComputeBackgroundColor

        val colorBackgroundImage: ImageBitmap = if (colorABackgroundImage != null) {
            currentColorBackgroundColor = colorA
            colorABackgroundImage ?: throw IllegalStateException("colorABackgroundImage为null，存在其它线程修改了值？")
        } else if (colorBBackgroundImage != null) {
            currentColorBackgroundColor = colorB
            colorBBackgroundImage ?: throw IllegalStateException("colorBBackgroundImage为null，存在其它线程修改了值？")
        } else {
            throw IllegalStateException("有色图片数据均为null")
        }

        val currentWhiteBackgroundImage = whiteBackgroundImage ?: throw IllegalStateException("whiteBackgroundImage为null，存在其它线程修改了值？")
        val currentBlackBackgroundImage = blackBackgroundImage ?: throw IllegalStateException("blackBackgroundImage为null，存在其它线程修改了值？")


        val tolerance = (colorBackgroundTolerance * 100).toInt()
        val fullTransparentColor = ComputePixel(0, 0, 0, 0).toInt()

        val computeImage = BufferedImage(currentWhiteBackgroundImage.width, currentWhiteBackgroundImage.height, BufferedImage.TYPE_INT_ARGB)

        foreach3ImageBitmap(currentWhiteBackgroundImage, currentBlackBackgroundImage, colorBackgroundImage) { x, y, whitePixel, blackPixel, colorPixel ->
            if (colorPixel.alpha == 255 && colorPixel.isWithinTolerance(currentColorBackgroundColor, tolerance)) {
                computeImage.setRGB(x, y, fullTransparentColor)
            } else if (colorPixel == whitePixel) {
                computeImage.setRGB(x, y, whitePixel.toInt())
            } else if (colorPixel.isWithinTolerance(whitePixel, currentColorBackgroundColor, tolerance)) {
                val alphaAValue = when (currentColorBackgroundColor) {
                    RED -> ComputePixel::blue
                    GREEN -> ComputePixel::red
                    BLUE -> ComputePixel::green
                }
                val alphaBValue = when (currentColorBackgroundColor) {
                    RED -> ComputePixel::green
                    GREEN -> ComputePixel::blue
                    BLUE -> ComputePixel::red
                }

                val alphaA = (255 - (alphaAValue(colorPixel) - alphaAValue(whitePixel)).absoluteValue)
                val alphaB = (255 - (alphaBValue(colorPixel) - alphaBValue(whitePixel)).absoluteValue)

                val whiteBalance = whiteBlackBalance
                val blackBalance = 1 - whiteBalance

                val pixel = ComputePixel(
                    alpha = ((alphaA * whiteBalance) + (alphaB * blackBalance)).toInt(),
                    red = (whitePixel.red - (whitePixel.red - ((whitePixel.red * whiteBalance) + (blackPixel.red * blackBalance)))).toInt(),
                    green = (whitePixel.green - (whitePixel.green - ((whitePixel.green * whiteBalance) + (blackPixel.green * blackBalance)))).toInt(),
                    blue = (whitePixel.blue - (whitePixel.blue - ((whitePixel.blue * whiteBalance) + (blackPixel.blue * blackBalance)))).toInt(),
                )
                computeImage.setRGB(x, y, pixel.toInt())
            } else {
                computeImage.setRGB(x, y, whitePixel.toInt())
            }
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(computeImage, "png", outputStream)
        previewImageData = outputStream.toByteArray()
        resetPreview()
    }


    private fun compute4ImageBitmap() {
        println("compute4ImageMode")
        val currentWhiteBackgroundImage = whiteBackgroundImage ?: throw IllegalStateException("whiteBackgroundImage为null，存在其它线程修改了值？")
        val currentBlackBackgroundImage = blackBackgroundImage ?: throw IllegalStateException("blackBackgroundImage为null，存在其它线程修改了值？")
        val currentColorABackgroundImage = colorABackgroundImage ?: throw IllegalStateException("colorABackgroundImage为null，存在其它线程修改了值？")
        val currentColorBBackgroundImage = colorBBackgroundImage ?: throw IllegalStateException("colorABackgroundImage为null，存在其它线程修改了值？")
        val currentColorA = colorA
        val currentColorB = colorB

        val tolerance = (colorBackgroundTolerance * 100).toInt()
        val fullTransparentColor = ComputePixel(0, 0, 0, 0).toInt()

        val computeImage = BufferedImage(currentWhiteBackgroundImage.width, currentWhiteBackgroundImage.height, BufferedImage.TYPE_INT_ARGB)

        foreach4ImageBitmap(currentWhiteBackgroundImage, currentBlackBackgroundImage, currentColorABackgroundImage, currentColorBBackgroundImage) { x, y, whitePixel, blackPixel, aPixel, bPixel ->
            if (whitePixel.alpha == 255
                && blackPixel.alpha == 255
                && aPixel.isWithinTolerance(colorA, tolerance)
                && bPixel.isWithinTolerance(colorB, tolerance)
            ) {
                computeImage.setRGB(x, y, fullTransparentColor)
            } else if (whitePixel == blackPixel && blackPixel == aPixel && aPixel == bPixel) {
                computeImage.setRGB(x, y, whitePixel.toInt())
            } else if (
                aPixel.isWithinTolerance(whitePixel, currentColorA, tolerance)
                || aPixel.isWithinTolerance(blackPixel, currentColorA, tolerance)
                || bPixel.isWithinTolerance(whitePixel, currentColorB, tolerance)
                || bPixel.isWithinTolerance(blackPixel, currentColorB, tolerance)
            ) {
                val whiteAAlphaValue = when (colorA) {
                    RED -> ComputePixel::blue
                    GREEN -> ComputePixel::red
                    BLUE -> ComputePixel::green
                }
                val blackAAlphaValue = when (colorA) {
                    RED -> ComputePixel::red
                    GREEN -> ComputePixel::green
                    BLUE -> ComputePixel::blue
                }
                val whiteBAlphaValue = when (colorB) {
                    RED -> ComputePixel::green
                    GREEN -> ComputePixel::blue
                    BLUE -> ComputePixel::red
                }
                val blackBAlphaValue = when (colorB) {
                    RED -> ComputePixel::red
                    GREEN -> ComputePixel::green
                    BLUE -> ComputePixel::blue
                }

                val whiteAlpha = 255 - (((whiteAAlphaValue(aPixel) - whiteAAlphaValue(whitePixel)).absoluteValue + (whiteBAlphaValue(bPixel) - whiteBAlphaValue(whitePixel)).absoluteValue) / 2)
                val blackAlpha = 255 - (((blackAAlphaValue(aPixel) - blackAAlphaValue(blackPixel)).absoluteValue + (blackBAlphaValue(bPixel) - blackBAlphaValue(blackPixel)).absoluteValue) / 2)

                val whiteBalance = whiteBlackBalance
                val blackBalance = 1 - whiteBalance

                val pixel = ComputePixel(
                    alpha = ((whiteAlpha * whiteBalance) + (blackAlpha * blackBalance)).toInt(),
                    red = ((whitePixel.red * whiteBalance) + (blackPixel.red * blackBalance)).toInt(),
                    green = ((whitePixel.green * whiteBalance) + (blackPixel.green * blackBalance)).toInt(),
                    blue = ((whitePixel.blue * whiteBalance) + (blackPixel.blue * blackBalance)).toInt(),
                )
                computeImage.setRGB(x, y, pixel.toInt())
            } else {
                computeImage.setRGB(x, y, whitePixel.toInt())
            }
        }


        val outputStream = ByteArrayOutputStream()
        ImageIO.write(computeImage, "png", outputStream)
        previewImageData = outputStream.toByteArray()
        resetPreview()
    }

    private fun foreach2ImageBitmap(
        imageBitmapA: ImageBitmap,
        imageBitmapB: ImageBitmap,
        block: (x: Int, y: Int, pixelA: ComputePixel, pixelB: ComputePixel) -> Unit
    ) {
        val width = imageBitmapA.width
        val height = imageBitmapA.height

        val bufferSize = width * height
        val bufferA = IntArray(bufferSize)
        val bufferB = IntArray(bufferSize)

        imageBitmapA.readPixels(bufferA, 0, 0, width, height)
        imageBitmapB.readPixels(bufferB, 0, 0, width, height)

        repeat(height) { y ->
            repeat(width) { x ->
                val position = x + (y * width)
                val pixelA = ComputePixel(bufferA[position])
                val pixelB = ComputePixel(bufferB[position])

                block(x, y, pixelA, pixelB)
            }
        }
    }

    private fun foreach3ImageBitmap(
        imageBitmapA: ImageBitmap,
        imageBitmapB: ImageBitmap,
        imageBitmapC: ImageBitmap,
        block: (x: Int, y: Int, pixelA: ComputePixel, pixelB: ComputePixel, pixelC: ComputePixel) -> Unit
    ) {
        val width = imageBitmapA.width
        val height = imageBitmapA.height

        val bufferSize = width * height
        val bufferA = IntArray(bufferSize)
        val bufferB = IntArray(bufferSize)
        val bufferC = IntArray(bufferSize)

        imageBitmapA.readPixels(bufferA, 0, 0, width, height)
        imageBitmapB.readPixels(bufferB, 0, 0, width, height)
        imageBitmapC.readPixels(bufferC, 0, 0, width, height)

        repeat(height) { y ->
            repeat(width) { x ->
                val position = x + (y * width)
                val pixelA = ComputePixel(bufferA[position])
                val pixelB = ComputePixel(bufferB[position])
                val pixelC = ComputePixel(bufferC[position])

                block(x, y, pixelA, pixelB, pixelC)
            }
        }

    }

    private fun foreach4ImageBitmap(
        imageBitmapA: ImageBitmap,
        imageBitmapB: ImageBitmap,
        imageBitmapC: ImageBitmap,
        imageBitmapD: ImageBitmap,
        block: (x: Int, y: Int, pixelA: ComputePixel, pixelB: ComputePixel, pixelC: ComputePixel, pixelD: ComputePixel) -> Unit
    ) {
        val width = imageBitmapA.width
        val height = imageBitmapA.height

        val bufferSize = width * height
        val bufferA = IntArray(bufferSize)
        val bufferB = IntArray(bufferSize)
        val bufferC = IntArray(bufferSize)
        val bufferD = IntArray(bufferSize)

        imageBitmapA.readPixels(bufferA, 0, 0, width, height)
        imageBitmapB.readPixels(bufferB, 0, 0, width, height)
        imageBitmapC.readPixels(bufferC, 0, 0, width, height)
        imageBitmapD.readPixels(bufferD, 0, 0, width, height)

        repeat(height) { y ->
            repeat(width) { x ->
                val position = x + (y * width)
                val pixelA = ComputePixel(bufferA[position])
                val pixelB = ComputePixel(bufferB[position])
                val pixelC = ComputePixel(bufferC[position])
                val pixelD = ComputePixel(bufferD[position])

                block(x, y, pixelA, pixelB, pixelC, pixelD)
            }
        }

    }


}

/**
 * 比较两个ImageBitmap宽高是否一致
 */
private fun ImageBitmap.compareSize(otherImageBitmap: ImageBitmap): Boolean {
    return width == otherImageBitmap.width && height == otherImageBitmap.height
}