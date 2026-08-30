# UI Framework Documentation

---

## Invariants
- Every successful pointer press has exactly one outcome: *release* or *cancellation*.
- At most one UI element is focused.
- At most one UI element is hovered.
- Each active pointer/button combination has at most one owner.
- A captured pointer bypasses normal hit testing until release or cancellation. 
  (so e.g. a drag over another element doesn't hit that element)
- Removing/hiding/deactivating the owner of an active interaction cancels that interaction before the owner becomes invalid.
- Opening a modal interaction must cancel gameplay pointer interactions that can no longer receive their termination.
- Input is routed from the topmost eligible layer downward until consumed or blocked.
- Hit testing uses the same final geometry/traversal order as rendering.
- Structural UI mutations during event callbacks have defined semantics and cannot invalidate the current dispatch.
- No interaction may remain permanently active from an input event becoming unreachable (i.e. from layer input being blocked).
- When the Game-Window goes out of focus, ALL interactions must be canceled (including drags, button-presses, etc.).

---

## General

### UI Layout
There are 2 distinct logical steps involved in UI Layout, each executed recursively for the whole tree before the next step:
1) ``measure()``: measure & cache dimensions, positions, padding etc.
2) ``layout()``: apply measured layout

For performance, any method that might invalidate a layout should mark the element dirty.
Only dirty elements get re-layout. This avoids layout every frame.

---

## [UIManager](./core/UIManager.java)
Central UI-Manager. Controls the global Layer Stack, as well as UI Input & hit-testing.

Coordinates rendering and updating of layers:
- Input/updates happen top->bottom
- Rendering happens reverse, bottom->top

Each UILayer owns a [Canvas](./components/Canvas.java). Each Canvas has ONE retained UIElement tree.
A UIElements layout is deterministic, its size set by its parent (Canvas being the root).

## [UILayer](./core/UILayer.java)
A UILayer represent an independent Layer in the UI-Stack.
States:
- ``active``:
    - true: Layer receives updates via update()
    - false: Layer does NOT receive updates via update(). Cancels all interactions
- ``visible``:
    - true: Layer is rendered and part of hit-testing
    - false: Layer is NOT rendered and NOT part of hit-testing. Cancels all interactions
- ``modal``: Layer blocks input for all other layers below it in the stack
- ``blocking``: Layer prevents gameplay from receiving input, but allows events to pass to lower layers

## [UIElement](./components/UIElement.java)
  Elements have the following States:
- ``active``: 
    - true: receives updates via update()
    - false: does NOT receive updates via update(). Cancels all interactions
- ``visible``: 
    - true: is rendered and part of hit-testing
    - false: is NOT rendered and is NOT part of hit-testing. Cancels all interactions
- ``enabled``:
  - true: normal UI interactions
  - false: still rendered (using the disabled visuals) and updated, BUT no interactions allowed.
- ``disposed``:
    - false: element is alive and usable 
    - true: element was disposed and is scheduled for destruction. Any calls on a disposed element will throw

## Pointer Capture
A successful pointer press establishes an owner. Until the capture is ended, all input is routed to the owner.
The capture ends if:
- the pointer is released
- the interaction is canceled (e.g. by loosing focus)
- the owner becomes invalid (e.g. by becoming inactive)

## Cancellation
Cancellation ensures all interactions reach a terminal state, even when they can no longer receive events, e.g. from:
- modal layer opening
- screen transition
- layer removal
- element becoming invisible, inactive, disabled
- game-window unfocused

Cancellation releases pointer captures and clears all interactions.
Cancellation is distinct from normal button releases.
