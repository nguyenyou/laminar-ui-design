package www.dream

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L

object Icon {
  sealed trait IconName {
    val name: String
  }

  object IconName {
    type Selector = IconName.type => IconName

    object ArrowUpRight extends IconName { val name = "arrow-up-right" }
    object Download extends IconName { val name = "download" }
    object Send extends IconName { val name = "send" }
  }
  def apply(name: IconName.Selector) = {
    L.svg.svg(
      L.svg.className := "myicon",
      L.svg.use(
        L.svg.href := s"/sprite.svg#${name(IconName).name}"
      )
    )
  }

  def apply(name: IconName) = {
    L.svg.svg(
      L.svg.className := "myicon",
      L.svg.use(
        L.svg.href := s"/sprite.svg#${name.name}"
      )
    )
  }
}
