package www

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L
import www.dream.{Button, Icon}
import org.scalajs.dom

case class App() {
  def apply(): HtmlElement = {
    div(
      cls("p-5"),
      i(dataAttr("lucide") := "menu"),
      Icon(_.ArrowUpRight),
      Icon(_.Send),
      Icon(_.Download),
      new Button()
        .variant(_.Primary)
        .label("Click meeee")
        .onClick(
          onClick --> Observer[dom.MouseEvent](e => println("Clicked!"))
        ),
      new Button()
        .variant(_.Primary)
        .label(Val("Another day"))
        .onClick(_.mapToUnit --> Observer(_ => println("hello"))),
      Button(_.label := "Hello"),
      Button(_.onClick --> Observer[dom.MouseEvent](e => println("Clicked!"))),
      Button(_.label <-- Val("World!"))
      /*
        Button(
          _.variant.primary
        )("Click me")

       */
    )
  }
}
