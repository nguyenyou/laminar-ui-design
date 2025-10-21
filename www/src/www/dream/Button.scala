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

}

object Button {
  implicit val renderableNode: RenderableNode[Button] = RenderableNode(
    _.element
  )

  type Self = Button.type

  sealed trait ButtonModifier {}

  trait PropSetter[V] extends ButtonModifier {
    def prop: Prop[V]
    def initialValue: V
  }

  trait PropUpdater[V] extends ButtonModifier {
    def prop: Prop[V]
    def source: Source[V]
  }

  class Prop[V](val name: String) {
    inline def apply(value: V) = {
      this := value
    }

    def :=(value: V): PropSetter[V] = {
      val self = this
      new PropSetter[V] {
        def prop: Prop[V] = self
        def initialValue: V = value
      }
    }

    def <--(value: Source[V]): PropUpdater[V] = {
      val self = this
      new PropUpdater[V] {
        def prop: Prop[V] = self
        def source: Source[V] = value
      }
    }
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
    lazy val primary = ButtonVariant(Variant.Primary)
    lazy val secondary = ButtonVariant(Variant.Secondary)
  }

  object ButtonSize extends Prop[Size]("size") {
    lazy val xs = ButtonSize(Size.Xs)
    lazy val sm = ButtonSize(Size.Sm)
  }

  lazy val variant: ButtonVariant.type = ButtonVariant
  lazy val size: ButtonSize.type = ButtonSize

  type ButtonMods = Button.type => ButtonModifier

  def apply(mods: ButtonMods*) = {
    val btn = new Button()

    // Iterate through all modifiers and apply them to the button
    mods.foreach { mod =>
      // Call the modifier function with Button companion object to get the ButtonModifier
      val modifier = mod(Button)

      // Pattern match on the modifier type and apply it to the button
      modifier match {
        case setter: PropSetter[?] =>
          // Check which property is being set by comparing the prop instance
          setter.prop match {
            case ButtonVariant =>
              // Cast the value to the correct type and call the variant method
              btn.variant(setter.initialValue)

            case ButtonSize =>
              // Cast the value to the correct type and call the size method
              btn.size(setter.initialValue)

            case _ =>
              // Unknown property - ignore or log warning
              ()
          }

        case updater: PropUpdater[?] =>
          // Handle PropUpdater - for reactive Source-based updates
          updater.prop match {
            case ButtonVariant =>
              // Cast the source to the correct type and call the variant method
              btn.variant(updater.source)

            case ButtonSize =>
              // Cast the source to the correct type and call the size method
              btn.size(updater.source)

            case _ =>
              // Unknown property - ignore or log warning
              ()
          }

        case _ =>
          // Handle any other ButtonModifier types
          ()
      }
    }

    btn
  }
}
