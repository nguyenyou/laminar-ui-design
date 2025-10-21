package www.dream

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L
import org.scalajs.dom

import com.raquo.laminar.modifiers.RenderableNode

class Button() {
  lazy val element = button(
    cls("btn"),
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

  def variant(v: Source[Button.Variant]) = {

  }

  def size(v: Button.Size) = {

  }

  def size(v: Source[Button.Size]) = {

  }
  
  def icon(v: Icon.IconName.Selector) = {
    
  }
  
  def endIcon(v: Icon.IconName.Selector) = {
    
  }
  
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
  implicit val renderableNode: RenderableNode[Button] = RenderableNode(_.element)

  type Self = Button.type

  sealed trait ButtonModifier {

  }

  trait PropSetter[V] extends ButtonModifier {

  }

  trait PropUpdater[V] extends ButtonModifier {

  }

  class Prop[V](val name: String) {
    inline def apply(value: V) = {
      this := value
    }

    def :=(value: V): PropSetter[V] = {
      new PropSetter[V] {

      }
    }

    def <--(value: Source[V]): PropUpdater[V] = {
      new PropUpdater[V] {

      }
    }
  }

  enum Variant {
    case Primary, Secondary
  }
  
  object Variant {
    type Selector = Variant.type  => Variant
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
    button("Click MEEEE")
  }
}