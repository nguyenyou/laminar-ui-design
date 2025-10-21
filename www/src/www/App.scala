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
      new Button("Click me").variant(_.Primary)
      /*
        Button(
          _.variant.primary
        )("Click me")
      
       */
    )
  }
}