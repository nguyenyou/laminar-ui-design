package www

import com.raquo.laminar.api.L.*
import www.dream.Button

case class App() {
  def apply(): HtmlElement = {
    div(
      cls("p-5 space-x-4"),
      Button(
       _.variant <-- Val(Button.Variant.Primary)
      )
    )
  }
}