package www.dream

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.given
import org.scalajs.dom

import com.raquo.laminar.modifiers.RenderableNode
import com.raquo.laminar.modifiers.EventListener
import com.raquo.laminar.keys.EventProp

class Button() {
  // Slot-based mechanism for consistent rendering order
  // Using Element type to accommodate both HTML and SVG elements

  object slots {
    lazy val icon = L.Var[Option[L.SvgElement]](None)
    lazy val label = L.Var[Option[String]](None)
    lazy val endIcon = L.Var[Option[L.SvgElement]](None)
  }

  lazy val element = L.button(
    L.cls("btn"),
    // Render slots in consistent order: icon -> label -> endIcon
    L.child.maybe <-- slots.icon.signal,
    L.child.maybe <-- slots.label.signal.map(_.map(text => L.span(text))),
    L.child.maybe <-- slots.endIcon.signal
  )

  def variant(v: Button.Variant.Selector) = {
    println(v(Button.Variant))
    element.amend(
      L.cls("btn-primary")
    )
    this
  }

  def variant(v: Button.Variant) = {
    element.amend(
      L.cls("btn-primary")
    )
    this
  }

  def variant(v: L.Source[Button.Variant]) = {
    // TODO: Implement reactive variant updates
    this
  }

  def size(v: Button.Size) = {
    // TODO: Implement size styling
    this
  }

  def size(v: L.Source[Button.Size]) = {
    // TODO: Implement reactive size updates
    this
  }

  def icon(v: Icon.IconName.Selector) = {
    slots.icon.set(Some(Icon(v)))
    this
  }

  def endIcon(v: Icon.IconName.Selector) = {
    slots.endIcon.set(Some(Icon(v)))
    this
  }

  def onClick[Out](
      eventListener: EventProp[dom.MouseEvent] => EventListener[
        dom.MouseEvent,
        Out
      ]
  ) = {
    element.amend(eventListener(L.onClick))
    this
  }

  def onClick[Out](eventListener: EventListener[dom.MouseEvent, Out]) = {
    element.amend(eventListener)
    this
  }

  def onClick(observer: L.Observer[dom.MouseEvent]) = {
    element.amend(
      L.onClick --> observer
    )
    this
  }

  def attachEventListener[Ev <: dom.Event, Out](
      eventListener: EventListener[Ev, Out]
  ) = {
    element.amend(eventListener)
    this
  }

  def label(v: String) = {
    slots.label.set(Some(v))
    this
  }

  def label(v: L.Source[String]) = {
    // Bind the source to the labelSlot Var
    // Convert Source to Observable to access map method
    element.amend(
      v.toObservable.map(Some(_)) --> slots.label.writer
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

  final class PropUpdater[V](val prop: Prop[V], val source: L.Source[V])
      extends ButtonModifier {
    def applyTo(button: Button): Unit = prop.applySource(button, source)
  }

  // Abstract Prop class with type-safe application methods
  abstract class Prop[V](val name: String) {
    // Abstract methods that subclasses must implement for type-safe application
    def applyValue(button: Button, value: V): Unit
    def applySource(button: Button, source: L.Source[V]): Unit

    inline def apply(value: V): PropSetter[V] = this := value

    def :=(value: V): PropSetter[V] = PropSetter(this, value)

    def <--(source: L.Source[V]): PropUpdater[V] = PropUpdater(this, source)
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

    def applySource(button: Button, source: L.Source[Variant]): Unit = {
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

    def applySource(button: Button, source: L.Source[Size]): Unit = {
      button.size(source)
      ()
    }

    lazy val xs = ButtonSize(Size.Xs)
    lazy val sm = ButtonSize(Size.Sm)
  }

  object ButtonLabel extends Prop[String]("label") {
    def applyValue(button: Button, value: String): Unit = {
      button.label(value)
      ()
    }
    def applySource(button: Button, source: L.Source[String]): Unit = {
      button.label(source)
      ()
    }
  }

  object ButtonIcon extends Prop[Icon.IconName.Selector]("icon") {
    def applyValue(button: Button, value: Icon.IconName.Selector): Unit = {
      button.icon(value)
      ()
    }
    def applySource(
        button: Button,
        source: L.Source[Icon.IconName.Selector]
    ): Unit = {
      // Reactive icon updates - could be implemented if needed
      ()
    }
  }

  object ButtonEndIcon extends Prop[Icon.IconName.Selector]("endIcon") {
    def applyValue(button: Button, value: Icon.IconName.Selector): Unit = {
      button.endIcon(value)
      ()
    }
    def applySource(
        button: Button,
        source: L.Source[Icon.IconName.Selector]
    ): Unit = {
      // Reactive endIcon updates - could be implemented if needed
      ()
    }
  }

  lazy val variant: ButtonVariant.type = ButtonVariant
  lazy val size: ButtonSize.type = ButtonSize
  lazy val label: ButtonLabel.type = ButtonLabel
  lazy val icon: ButtonIcon.type = ButtonIcon
  lazy val endIcon: ButtonEndIcon.type = ButtonEndIcon

  lazy val onClick = L.onClick

  // Custom modifier for Laminar EventListener
  final class EventListenerModifier[Ev <: dom.Event, Out](
      val eventListener: EventListener[Ev, Out]
  ) extends ButtonModifier {
    def applyTo(button: Button): Unit = {
      button.attachEventListener(eventListener)
      ()
    }
  }

  // Implicit conversion from Laminar EventListener to ButtonModifier
  implicit def eventListenerToModifier[Ev <: dom.Event, Out](
      eventListener: EventListener[Ev, Out]
  ): EventListenerModifier[Ev, Out] = {
    new EventListenerModifier(eventListener)
  }

  type ButtonMods = Button.type => ButtonModifier

  def apply(mods: ButtonMods*): Button = {
    val btn = new Button()

    // Apply all modifiers using the type-safe applyTo method
    mods.foreach(_(Button).applyTo(btn))

    btn
  }
}
