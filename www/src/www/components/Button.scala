package www.components

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Button {
  
  enum Variant(val cls: String) {
    case Default extends Variant("")
    case Neutral extends Variant("btn-neutral")
    case Primary extends Variant("btn-primary")
    case Secondary extends Variant("btn-secondary")
  }
  
  def apply(variant: Variant = Variant.Default): HtmlElement = {
    button(
      cls(s"btn ${variant.cls}"),
      "Button"
    )
  }  
}