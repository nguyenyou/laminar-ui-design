package www

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L
import www.dream.{Button, Icon}

case class App() {
  def apply(): HtmlElement = {
    div(
      cls("p-5"),
      i(dataAttr("lucide") := "menu"),
      Icon(_.ArrowUpRight),
      Icon(_.Send),
      Icon(_.Download),
      new Button().variant(_.Primary).label("Click me"),
      new Button().variant(_.Primary).label(Val("Another day"))
      /*
        Button(
          _.variant.primary
        )("Click me")

       */
    )
  }
}
