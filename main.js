import "./style.css";
import sprite from "lucide-static/sprite.svg";

if (process.env.NODE_ENV === "development") {
  import("./out/www/fastLinkJS.dest/main.js");
} else {
  import("./out/www/fullLinkJS.dest/main.js");
}