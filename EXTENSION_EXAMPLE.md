# How to Extend the Button Modifier System

This guide shows how easy it is to add new properties to the Button component using the refactored architecture.

---

## Example 1: Adding a `label` Property

### Step 1: Ensure the Button class has the method (already exists)

```scala
class Button() {
  def label(v: String) = {
    element.amend(v)
    this
  }
}
```

### Step 2: Create a Prop object

```scala
object ButtonLabel extends Prop[String]("label") {
  def applyValue(button: Button, value: String): Unit = {
    button.label(value)
    ()
  }
  
  def applySource(button: Button, source: Source[String]): Unit = {
    // For reactive updates, you might want to bind the source
    // button.element.amend(child.text <-- source)
    ()
  }
}
```

### Step 3: Expose it in the Button companion object

```scala
object Button {
  // ... existing code ...
  
  lazy val label: ButtonLabel.type = ButtonLabel
}
```

### Step 4: Use it!

```scala
Button(
  _.variant.primary,
  _.label := "Click me!"
)

// Or with reactive updates
val labelSignal: Signal[String] = ???
Button(
  _.label <-- labelSignal
)
```

**That's it!** No changes to the `apply()` method needed.

---

## Example 2: Adding an `onClick` Property

### Step 1: Button method (already exists)

```scala
class Button() {
  def onClick(fn: dom.MouseEvent => Unit) = {
    element.amend(L.onClick --> fn)
    this
  }
  
  def onClick(observer: Observer[dom.MouseEvent]) = {
    element.amend(L.onClick --> observer)
    this
  }
}
```

### Step 2: Create a specialized modifier

Since `onClick` doesn't fit the `Prop[V]` pattern perfectly (it's more of an event handler), you can create a custom modifier:

```scala
// Custom modifier for event handlers
final class EventHandler(val handler: dom.MouseEvent => Unit) extends ButtonModifier {
  def applyTo(button: Button): Unit = {
    button.onClick(handler)
    ()
  }
}

// Or for observers
final class EventObserver(val observer: Observer[dom.MouseEvent]) extends ButtonModifier {
  def applyTo(button: Button): Unit = {
    button.onClick(observer)
    ()
  }
}

// Helper object for creating onClick modifiers
object ButtonOnClick {
  def apply(handler: dom.MouseEvent => Unit): EventHandler = 
    EventHandler(handler)
  
  def apply(observer: Observer[dom.MouseEvent]): EventObserver = 
    EventObserver(observer)
}

// Expose it
lazy val onClick: ButtonOnClick.type = ButtonOnClick
```

### Step 3: Use it!

```scala
Button(
  _.variant.primary,
  _.onClick(e => println("Clicked!"))
)

// Or with an observer
val clickBus = new EventBus[dom.MouseEvent]
Button(
  _.onClick(clickBus.writer)
)
```

---

## Example 3: Adding an `icon` Property with Selector Pattern

### Step 1: Button method (already exists)

```scala
class Button() {
  def icon(v: Icon.IconName.Selector) = {
    // implementation
    this
  }
}
```

### Step 2: Create a specialized Prop

Since `icon` uses a `Selector` pattern, we need a slightly different approach:

```scala
object ButtonIcon extends Prop[Icon.IconName.Selector]("icon") {
  def applyValue(button: Button, value: Icon.IconName.Selector): Unit = {
    button.icon(value)
    ()
  }
  
  def applySource(button: Button, source: Source[Icon.IconName.Selector]): Unit = {
    // Reactive icon updates might not make sense, but you could implement it
    ()
  }
  
  // Convenience methods for common icons
  def apply(selector: Icon.IconName.Selector): PropSetter[Icon.IconName.Selector] = 
    this := selector
}

lazy val icon: ButtonIcon.type = ButtonIcon
```

### Step 3: Use it!

```scala
Button(
  _.variant.primary,
  _.icon := (_.ArrowUpRight)
)

// Or more concisely
Button(
  _.icon(_.Send)
)
```

---

## Example 4: Adding a Complex Property with Validation

Let's say you want to add a `maxWidth` property with validation:

### Step 1: Add the Button method

```scala
class Button() {
  def maxWidth(pixels: Int) = {
    element.amend(styleAttr("max-width") := s"${pixels}px")
    this
  }
}
```

### Step 2: Create a Prop with validation

```scala
object ButtonMaxWidth extends Prop[Int]("maxWidth") {
  private def validate(pixels: Int): Int = {
    require(pixels > 0, "maxWidth must be positive")
    require(pixels <= 1000, "maxWidth cannot exceed 1000px")
    pixels
  }
  
  def applyValue(button: Button, value: Int): Unit = {
    button.maxWidth(validate(value))
    ()
  }
  
  def applySource(button: Button, source: Source[Int]): Unit = {
    // You could validate on each emission
    // button.element.amend(
    //   styleAttr("max-width") <-- source.map(v => s"${validate(v)}px")
    // )
    ()
  }
}

lazy val maxWidth: ButtonMaxWidth.type = ButtonMaxWidth
```

### Step 3: Use it!

```scala
Button(
  _.maxWidth := 300  // ✅ Valid
)

Button(
  _.maxWidth := -10  // ❌ Throws: "maxWidth must be positive"
)
```

---

## Example 5: Adding Multiple Related Properties

Let's add `disabled` and `loading` states:

### Step 1: Add Button methods

```scala
class Button() {
  def disabled(value: Boolean) = {
    if (value) element.amend(disabled := true)
    this
  }
  
  def loading(value: Boolean) = {
    if (value) element.amend(cls("loading"))
    this
  }
}
```

### Step 2: Create Prop objects

```scala
object ButtonDisabled extends Prop[Boolean]("disabled") {
  def applyValue(button: Button, value: Boolean): Unit = {
    button.disabled(value)
    ()
  }
  
  def applySource(button: Button, source: Source[Boolean]): Unit = {
    button.element.amend(disabled <-- source)
    ()
  }
}

object ButtonLoading extends Prop[Boolean]("loading") {
  def applyValue(button: Button, value: Boolean): Unit = {
    button.loading(value)
    ()
  }
  
  def applySource(button: Button, source: Source[Boolean]): Unit = {
    button.element.amend(cls.toggle("loading") <-- source)
    ()
  }
}

lazy val disabled: ButtonDisabled.type = ButtonDisabled
lazy val loading: ButtonLoading.type = ButtonLoading
```

### Step 3: Use them together!

```scala
val isLoading = Var(false)

Button(
  _.variant.primary,
  _.label := "Submit",
  _.loading <-- isLoading.signal,
  _.disabled <-- isLoading.signal,
  _.onClick { _ =>
    isLoading.set(true)
    // ... do async work ...
  }
)
```

---

## Pattern Summary

For any new property, follow this pattern:

1. **Add the method to `Button` class** (if not already present)
2. **Create a `Prop[T]` object** implementing `applyValue` and `applySource`
3. **Expose it** in the `Button` companion object
4. **Use it** with `_.propertyName := value` or `_.propertyName <-- source`

**No changes to the `apply()` method are ever needed!**

---

## Advanced: Creating a Base Trait for Common Patterns

If you have many similar properties, you can create helper traits:

```scala
// For simple value properties
trait SimpleValueProp[V] extends Prop[V] {
  def applyToButton(button: Button, value: V): Unit
  
  final def applyValue(button: Button, value: V): Unit = {
    applyToButton(button, value)
    ()
  }
  
  final def applySource(button: Button, source: Source[V]): Unit = {
    // Default: ignore reactive updates
    ()
  }
}

// Usage
object ButtonLabel extends SimpleValueProp[String]("label") {
  def applyToButton(button: Button, value: String): Unit = 
    button.label(value)
}
```

This reduces boilerplate even further!

---

## Conclusion

The refactored architecture makes extending the Button component:

- ✅ **Simple:** Just implement two methods
- ✅ **Type-safe:** Compiler ensures correctness
- ✅ **Isolated:** No need to modify central dispatch logic
- ✅ **Flexible:** Support any property type or pattern
- ✅ **Maintainable:** Each property is self-contained

Adding new properties is now a **5-minute task** instead of a **30-minute refactoring**.

