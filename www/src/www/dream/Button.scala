package www.dream

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L
import org.scalajs.dom

import com.raquo.laminar.modifiers.RenderableNode

class Button() {
  lazy val element = button(
    cls("btn")
  )

  def variant(v: Button.Variant.Selector) = {
    println(v(Button.Variant))
    element.amend(
      cls("btn-primary")
    )
    this
  }

  def variant(v: Button.Variant) = {
    element.amend(
      cls("btn-primary")
    )
    this
  }

  def variant(v: Source[Button.Variant]) = {}

  def size(v: Button.Size) = {}

  def size(v: Source[Button.Size]) = {}

  def icon(v: Icon.IconName.Selector) = {}

  def endIcon(v: Icon.IconName.Selector) = {}

  def onClick(observer: Observer[dom.MouseEvent]) = {
    element.amend(
      L.onClick --> observer
    )
    this
  }

  def onClick(fn: dom.MouseEvent => Unit) = {
    element.amend(
      L.onClick --> fn
    )
    this
  }

  def label(v: String) = {
    element.amend(v)
    this
  }

  def label(v: Signal[String]) = {
    element.amend(
      text <-- v
    )
    this
  }

}

object Button {
  implicit val renderableNode: RenderableNode[Button] = RenderableNode(
    _.element
  )

  type Self = Button.type

  // Base trait for all button modifiers
  sealed trait ButtonModifier {
    // Type-safe apply method that each modifier must implement
    def applyTo(button: Button): Unit
  }

  // Concrete modifier types with built-in application logic
  final class PropSetter[V](val prop: Prop[V], val initialValue: V)
      extends ButtonModifier {
    def applyTo(button: Button): Unit = prop.applyValue(button, initialValue)
  }

  final class PropUpdater[V](val prop: Prop[V], val source: Source[V])
      extends ButtonModifier {
    def applyTo(button: Button): Unit = prop.applySource(button, source)
  }

  // Abstract Prop class with type-safe application methods
  abstract class Prop[V](val name: String) {
    // Abstract methods that subclasses must implement for type-safe application
    def applyValue(button: Button, value: V): Unit
    def applySource(button: Button, source: Source[V]): Unit

    inline def apply(value: V): PropSetter[V] = this := value

    def :=(value: V): PropSetter[V] = PropSetter(this, value)

    def <--(source: Source[V]): PropUpdater[V] = PropUpdater(this, source)
  }

  enum Variant {
    case Primary, Secondary
  }

  object Variant {
    type Selector = Variant.type => Variant
  }

  enum Size {
    case Xs, Sm
  }

  object ButtonVariant extends Prop[Variant]("variant") {
    def applyValue(button: Button, value: Variant): Unit = {
      button.variant(value)
      ()
    }

    def applySource(button: Button, source: Source[Variant]): Unit = {
      button.variant(source)
      ()
    }

    lazy val primary = ButtonVariant(Variant.Primary)
    lazy val secondary = ButtonVariant(Variant.Secondary)
  }

  object ButtonSize extends Prop[Size]("size") {
    def applyValue(button: Button, value: Size): Unit = {
      button.size(value)
      ()
    }

    def applySource(button: Button, source: Source[Size]): Unit = {
      button.size(source)
      ()
    }

    lazy val xs = ButtonSize(Size.Xs)
    lazy val sm = ButtonSize(Size.Sm)
  }

  lazy val variant: ButtonVariant.type = ButtonVariant
  lazy val size: ButtonSize.type = ButtonSize

  type ButtonMods = Button.type => ButtonModifier

  def apply(mods: ButtonMods*): Button = {
    val btn = new Button()

    // Apply all modifiers using the type-safe applyTo method
    mods.foreach(_(Button).applyTo(btn))

    btn
  }
}
