package www.dream

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L

object Icon {
  enum IconName(val name: String) {
    case ArrowUpRight extends IconName("arrow-up-right")
    case Download extends IconName("download")
    case Send extends IconName("send")
  }
  object IconName {
    type Selector = IconName.type => IconName
  }
  def apply(name: IconName.Selector) = {
    L.svg.svg(
      L.svg.className := "myicon",
      L.svg.use(
        L.svg.href := s"/sprite.svg#${name(IconName).name}"
      )
    )
  }
}