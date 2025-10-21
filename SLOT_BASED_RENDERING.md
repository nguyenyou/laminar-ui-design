# Slot-Based Rendering Implementation

## Problem Statement

When using the Button modifier system, modifiers can be applied in any order:

```scala
Button(_.label := "Submit", _.icon(_.Send), _.endIcon(_.ArrowUpRight))
Button(_.icon(_.Send), _.endIcon(_.ArrowUpRight), _.label := "Submit")
Button(_.endIcon(_.ArrowUpRight), _.label := "Submit", _.icon(_.Send))
```

However, the visual layout of a button should **always** follow a consistent structure:

```
[icon] [label] [endIcon]
```

Without a slot-based mechanism, the elements would be rendered in the order the modifiers are applied, leading to inconsistent layouts.

---

## Solution: Slot-Based Mechanism

### Architecture

The Button class uses **three reactive slots** to ensure consistent rendering order:

```scala
class Button() {
  // Slot-based mechanism for consistent rendering order
  private val iconSlot = Var[Option[Element]](None)
  private val labelSlot = Var[Option[String]](None)
  private val endIconSlot = Var[Option[Element]](None)

  lazy val element = button(
    cls("btn"),
    // Render slots in consistent order: icon -> label -> endIcon
    child.maybe <-- iconSlot.signal,
    child.maybe <-- labelSlot.signal.map(_.map(text => span(text))),
    child.maybe <-- endIconSlot.signal
  )
}
```

### Key Design Decisions

#### 1. **Using `Var[Option[Element]]` for Icon Slots**

```scala
private val iconSlot = Var[Option[Element]](None)
private val endIconSlot = Var[Option[Element]](None)
```

**Why `Element` instead of `HtmlElement`?**
- The `Icon` component returns `ReactiveSvgElement[SVGSVGElement]`
- `Element` is the common supertype that accommodates both HTML and SVG elements
- This allows the slots to hold any type of element

**Why `Option[Element]`?**
- Allows slots to be empty (None) when no icon is set
- Enables conditional rendering with `child.maybe`

#### 2. **Using `Var[Option[String]]` for Label Slot**

```scala
private val labelSlot = Var[Option[String]](None)
```

**Why store String instead of Element?**
- More flexible: can wrap in different elements (span, div, etc.)
- Easier to update reactively
- Lighter weight than creating elements upfront

**Rendering:**
```scala
child.maybe <-- labelSlot.signal.map(_.map(text => span(text)))
```
- `labelSlot.signal` produces `Signal[Option[String]]`
- `.map(_.map(text => span(text)))` transforms to `Signal[Option[HtmlElement]]`
- `child.maybe` renders the element only when `Some(element)` is present

#### 3. **Using `child.maybe` for Conditional Rendering**

```scala
child.maybe <-- iconSlot.signal
```

**What is `child.maybe`?**
- Laminar's API for conditionally rendering a single child
- When the signal emits `None`, no child is rendered
- When the signal emits `Some(element)`, that element is rendered

**Benefits:**
- Clean, declarative syntax
- Automatic DOM updates when slots change
- No manual element management needed

---

## Implementation Details

### Icon Methods

```scala
def icon(v: Icon.IconName.Selector) = {
  iconSlot.set(Some(Icon(v)))
  this
}

def endIcon(v: Icon.IconName.Selector) = {
  endIconSlot.set(Some(Icon(v)))
  this
}
```

**How it works:**
1. `Icon(v)` creates an SVG element
2. `Some(Icon(v))` wraps it in an Option
3. `iconSlot.set(...)` updates the Var
4. The signal automatically propagates to the DOM
5. `child.maybe` renders the new icon

### Label Methods

#### Static Label

```scala
def label(v: String) = {
  labelSlot.set(Some(v))
  this
}
```

**How it works:**
1. Sets the label slot to `Some(v)`
2. The signal propagates: `Signal[Option[String]]`
3. `.map(_.map(text => span(text)))` creates a span element
4. `child.maybe` renders the span

#### Reactive Label

```scala
def label(v: Source[String]) = {
  element.amend(
    v.toObservable.map(Some(_)) --> labelSlot.writer
  )
  this
}
```

**How it works:**
1. `v.toObservable` converts `Source[String]` to `Observable[String]`
2. `.map(Some(_))` transforms to `Observable[Option[String]]`
3. `--> labelSlot.writer` binds the observable to the Var's writer
4. Each emission updates the label slot
5. The signal chain automatically updates the DOM

**Why `toObservable`?**
- `Source` is a trait without `map` method
- `toObservable` provides access to transformation methods
- Works with any Source implementation (Signal, EventStream, Val, etc.)

---

## Rendering Order Guarantee

The button element structure is defined **once** at initialization:

```scala
lazy val element = button(
  cls("btn"),
  child.maybe <-- iconSlot.signal,      // Position 1: Icon
  child.maybe <-- labelSlot.signal.map(_.map(text => span(text))),  // Position 2: Label
  child.maybe <-- endIconSlot.signal    // Position 3: End Icon
)
```

**Key insight:**
- The order of `child.maybe` bindings determines the DOM order
- This order is **fixed** and **independent** of when modifiers are applied
- Modifiers only update the slot values, not the structure

### Example Flow

```scala
Button(
  _.endIcon(_.ArrowUpRight),  // Applied first
  _.label := "Submit",         // Applied second
  _.icon(_.Send)               // Applied third
)
```

**Execution order:**
1. `new Button()` creates element with three empty slots
2. `endIcon(_.ArrowUpRight)` → `endIconSlot.set(Some(Icon(...)))`
3. `label := "Submit"` → `labelSlot.set(Some("Submit"))`
4. `icon(_.Send)` → `iconSlot.set(Some(Icon(...)))`

**DOM structure (always):**
```html
<button class="btn">
  <svg><!-- Send icon --></svg>
  <span>Submit</span>
  <svg><!-- ArrowUpRight icon --></svg>
</button>
```

---

## Benefits

### 1. **Consistent Layout**
- Elements always render in the same order
- Predictable visual structure
- No layout shifts based on modifier order

### 2. **Reactive Updates**
- Slots can be updated at any time
- DOM automatically reflects changes
- No manual DOM manipulation needed

### 3. **Type Safety**
- `Element` type accommodates both HTML and SVG
- Compiler ensures correct types
- No runtime type errors

### 4. **Clean API**
- Users don't need to think about rendering order
- Modifiers can be applied in any order
- Intuitive and ergonomic

### 5. **Performance**
- Laminar's reactive system is efficient
- Only changed slots trigger DOM updates
- No unnecessary re-renders

---

## Extending the Pattern

### Adding a New Slot

To add a new slot (e.g., for a badge or tooltip):

```scala
class Button() {
  private val iconSlot = Var[Option[Element]](None)
  private val labelSlot = Var[Option[String]](None)
  private val badgeSlot = Var[Option[String]](None)  // New slot
  private val endIconSlot = Var[Option[Element]](None)

  lazy val element = button(
    cls("btn"),
    child.maybe <-- iconSlot.signal,
    child.maybe <-- labelSlot.signal.map(_.map(text => span(text))),
    child.maybe <-- badgeSlot.signal.map(_.map(text => span(cls("badge"), text))),  // New slot
    child.maybe <-- endIconSlot.signal
  )

  def badge(v: String) = {
    badgeSlot.set(Some(v))
    this
  }
}
```

### Complex Slot Content

For more complex slot content, you can use different element types:

```scala
private val tooltipSlot = Var[Option[HtmlElement]](None)

lazy val element = button(
  cls("btn"),
  // ... other slots ...
  child.maybe <-- tooltipSlot.signal
)

def tooltip(text: String) = {
  tooltipSlot.set(Some(
    div(
      cls("tooltip"),
      span(text),
      i(cls("tooltip-arrow"))
    )
  ))
  this
}
```

---

## Comparison: Before vs After

### Before (Without Slots)

```scala
def label(v: String) = {
  element.amend(v)  // Appends to end of button
  this
}

def icon(v: Icon.IconName.Selector) = {
  element.amend(Icon(v))  // Appends to end of button
  this
}
```

**Problem:**
```scala
Button(_.label := "Submit", _.icon(_.Send))
// Renders: <button>Submit<svg>Send</svg></button>

Button(_.icon(_.Send), _.label := "Submit")
// Renders: <button><svg>Send</svg>Submit</button>
```
❌ **Inconsistent order!**

### After (With Slots)

```scala
def label(v: String) = {
  labelSlot.set(Some(v))  // Updates label slot
  this
}

def icon(v: Icon.IconName.Selector) = {
  iconSlot.set(Some(Icon(v)))  // Updates icon slot
  this
}
```

**Result:**
```scala
Button(_.label := "Submit", _.icon(_.Send))
// Renders: <button><svg>Send</svg><span>Submit</span></button>

Button(_.icon(_.Send), _.label := "Submit")
// Renders: <button><svg>Send</svg><span>Submit</span></button>
```
✅ **Consistent order!**

---

## Technical Notes

### Why `lazy val element`?

```scala
lazy val element = button(...)
```

- Ensures slots are initialized before element is created
- Prevents null reference errors
- Allows circular dependencies (element references slots, slots referenced in element)

### Why `private` slots?

```scala
private val iconSlot = Var[Option[Element]](None)
```

- Encapsulation: internal implementation detail
- Users interact through public methods (`icon`, `label`, etc.)
- Prevents direct slot manipulation
- Allows changing implementation without breaking API

### Memory Considerations

Each Button instance creates:
- 3 Var instances (lightweight)
- 3 Signal instances (derived from Vars, also lightweight)
- Laminar manages subscriptions automatically
- No memory leaks from reactive bindings

---

## Conclusion

The slot-based rendering mechanism provides:

✅ **Consistent visual layout** regardless of modifier order  
✅ **Reactive updates** with automatic DOM synchronization  
✅ **Type safety** with proper Element types  
✅ **Clean API** that's intuitive to use  
✅ **Extensibility** for adding new slots easily  

This pattern is a best practice for building component libraries with Laminar, ensuring predictable behavior and excellent developer experience.

