package de.ax.powermode.power.element

import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, Graphics, Graphics2D}

import de.ax.powermode.power.ElementOfPower
import de.ax.powermode.{ImageUtil, Util}

case class PowerFlame(_x: Int,
                      _y: Int,
                      _width: Int,
                      _height: Int,
                      initLife: Long)
  extends ElementOfPower {
  val life = System.currentTimeMillis() + initLife
  var x = _x
  var y = _y
  var width = 0
  var height = 0

  var i = 0
  var currentImage: BufferedImage = null

  override def update(delta: Float): Boolean = {
    if (alive) {
      val flameImages1 = ImageUtil.imagesForPath(powerMode.flameImageFolder)
      if (flameImages1.nonEmpty) {
        currentImage = flameImages1(i % flameImages1.size)
      }
      i += 1
      x = _x
      y = _y
      width = _width
      height = _height
    }
    !alive
  }

  override def render(g: Graphics, dxx: Int, dyy: Int): Unit = {
    if (alive) {

      val g2d: Graphics2D = g.create.asInstanceOf[Graphics2D]
      g2d.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          Util.alpha(0.50f * (1 - lifeFactor))))

      if (currentImage != null)
        g2d.drawImage(currentImage,
          (x + dxx - width/2),
          (y + dyy - height/2),
          width,
          height,
          null)
      g2d.dispose()
    }
  }

}
