/*
 * Copyright 2015 Baptiste Mesta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ax.powermode.power.management

import java.awt._

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{EditorFactoryAdapter, EditorFactoryEvent}
import de.ax.powermode.power.sound.PowerSound
import de.ax.powermode.{Power, PowerMode, Util}
import javax.swing._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
 * @author Baptiste Mesta
 */
class ElementOfPowerContainerManager extends EditorFactoryAdapter with Power {
  def ForceTry[X](f: => X): Try[X] = {
    try {
      Success(f).filter(_ != null)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        logger.error("error doing ForceTry", e)
        Failure(e)
    }
  }

  val elementsOfPowerContainers =
    mutable.Map.empty[Editor, ElementOfPowerContainer]

  private lazy val triedSound: Try[PowerSound] =
    powerMode.mediaPlayerExists.flatMap { _ =>
      Try {
        new PowerSound(powerMode.soundsFolder, powerMode.valueFactor)
      }
    }
  lazy val sound = triedSound

  val elementsOfPowerUpdateThread = new Thread(new Runnable() {
    def run {
      while (true) {
        try {
          if (powerMode != null) {
            updateContainers
            try {
              Thread.sleep(1000 / powerMode.frameRate)
            } catch {
              case ignored: InterruptedException => {}
            }
          }
        } catch {
          case e => PowerMode.logger.error(e.getMessage, e)
        }
      }
    }

    def updateContainers: Unit = {
      elementsOfPowerContainers.values.foreach(_.updateElementsOfPower())
    }

  })
  elementsOfPowerUpdateThread.start()

  override def editorCreated(event: EditorFactoryEvent) {
    val editor: Editor = event.getEditor
    val isActualEditor = Try {
      Util.isActualEditor(editor)
    }.getOrElse(false)
    if (isActualEditor) {
      elementsOfPowerContainers
        .put(editor, new ElementOfPowerContainer(editor))
    }
  }

  override def editorReleased(event: EditorFactoryEvent) {
    elementsOfPowerContainers.remove(event.getEditor)
  }

  def initializeAnimation(editor: Editor, pos: Point) {
    if (powerMode.isEnabled) {
      SwingUtilities.invokeLater(new Runnable() {
        def run {
          initializeInUI(editor, pos)
        }
      })
    }
  }

  private def initializeInUI(editor: Editor, pos: Point) {
    elementsOfPowerContainers.get(editor).foreach(_.initializeAnimation(pos))
  }

  def dispose {
    elementsOfPowerUpdateThread.interrupt()
    elementsOfPowerContainers.clear
  }
}
