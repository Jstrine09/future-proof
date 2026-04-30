---
name: JumzysNotes
description: Personal knowledge companion — warm, inhabited, deeply personal.
colors:
  bg:           "oklch(11% 0.008 65)"
  surface:      "oklch(17% 0.012 62)"
  surface-2:    "oklch(22% 0.014 60)"
  border:       "oklch(28% 0.016 60)"
  text-dim:     "oklch(50% 0.008 68)"
  text-body:    "oklch(82% 0.007 72)"
  text-display: "oklch(93% 0.005 72)"
  amber:        "oklch(75% 0.13 75)"
  amber-dim:    "oklch(60% 0.10 75)"
  mauve:        "oklch(62% 0.09 350)"
  mauve-bg:     "oklch(22% 0.05 350)"
  mauve-border: "oklch(30% 0.07 350)"
  sage:         "oklch(58% 0.06 150)"
  sage-bg:      "oklch(17% 0.03 150)"
  sage-border:  "oklch(26% 0.05 150)"
typography:
  display:
    fontFamily: "Georgia, 'Times New Roman', serif"
    fontSize: "1.7rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "-0.01em"
  headline:
    fontFamily: "Georgia, 'Times New Roman', serif"
    fontSize: "1.3rem"
    fontWeight: 400
    lineHeight: 1.2
  title:
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 700
    lineHeight: 1.3
  body:
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif"
    fontSize: "0.9rem"
    fontWeight: 400
    lineHeight: 1.72
  label:
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif"
    fontSize: "0.72rem"
    fontWeight: 400
    letterSpacing: "0.01em"
rounded:
  sm:   "8px"
  md:   "12px"
  pill: "99px"
spacing:
  1:  "4px"
  2:  "8px"
  3:  "12px"
  4:  "16px"
  6:  "24px"
  8:  "32px"
  12: "48px"
components:
  btn-primary:
    backgroundColor: "{colors.amber}"
    textColor: "{colors.bg}"
    rounded: "{rounded.sm}"
    padding: "8px 12px"
  btn-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.text-dim}"
    rounded: "{rounded.sm}"
    padding: "8px 12px"
  btn-save:
    backgroundColor: "{colors.amber}"
    textColor: "{colors.bg}"
    rounded: "{rounded.sm}"
    padding: "12px 16px"
    width: "100%"
  tag:
    backgroundColor: "{colors.mauve-bg}"
    textColor: "{colors.mauve}"
    rounded: "{rounded.pill}"
    padding: "1px 8px"
  input:
    backgroundColor: "{colors.bg}"
    textColor: "{colors.text-body}"
    rounded: "{rounded.sm}"
    padding: "12px 16px"
  modal:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.text-display}"
    rounded: "{rounded.md}"
    padding: "32px"
---

# Design System: JumzysNotes

## 1. Overview

**Creative North Star: "The Inhabited Study"**

JumzysNotes looks like a room that belongs to someone. Not installed software, not a SaaS product — a place. The visual language draws from a personal study at night: warm amber lamplight on dark wood, ink on aged paper, a desk that is chaotic to outsiders but perfectly legible to the person who built it. Every surface is tinted warm brown rather than gray, as if the screens were lit by a lamp rather than a fluorescent tube. References: Bear's warmth and typographic care, Obsidian's depth and density, Craft's document-first polish.

The palette is a full, disciplined four-role system. Warm amber (`oklch(75% 0.13 75)`) is the primary voice — rare, deliberate, used only when something truly matters. Dusty mauve (`oklch(62% 0.09 350)`) anchors tags and relational elements with a quiet secondary warmth. Muted sage (`oklch(58% 0.06 150)`) acts as a mode signal: when the user enters the dataset view, the accent color shifts to sage, telling them visually that the context has changed. Warm dark neutrals carry everything else, never reaching pure black.

Typography pairs Georgia serif (display + content headings) with the system humanist sans (all UI chrome). The pairing evokes a journal with footnotes — the serif draws the eye to note titles, the sans keeps navigation frictionless. Motion is responsive: every state change earns a transition, nothing moves for decoration. Modals enter with a 300ms ease-out-quart slide + fade; they exit at 230ms ease-in-quart, slightly faster to feel light.

**Key Characteristics:**
- Full-palette warm dark mode — four deliberate color roles, no accidents
- Georgia serif for display + note titles; system-ui sans for all UI chrome
- Flat tonal surfaces — depth through three background steps, not drop shadows
- Responsive motion — transitions on state, nothing choreographed
- Bear-style list layout for notes — border-bottom dividers, no card grid

## 2. Colors: The Warm Study Palette

A four-role warm dark palette — earthy amber, dusty mauve, calm sage, warm dark neutrals. Every surface is tinted warm brown; pure black and pure white are never used.

**The Full Palette Rule.** Four color roles exist and each has exactly one job. Amber anchors interactive elements and primary attention. Mauve anchors tags and relational elements. Sage anchors dataset contexts only — when the surface shifts to sage, the user knows they are reading data, not notes. Neutrals carry everything else. Never press a role into double duty.

**The Warm Dark Rule.** No surface may be neutral-gray or pure black. Every background and panel is tinted toward warm brown (hue 55–80, chroma 0.008–0.016 in OKLCH). The warmth is subtle — a hint, not a statement — but without it the UI would feel cold and corporate.

### Primary
- **Warm Amber / Honey Gold** (`oklch(75% 0.13 75)`): The primary voice. App name in the header, save buttons, active nav state, note title color on hover, focus ring base, markdown links. Rare enough to carry weight — not scattered. Its dimmer variant (`oklch(60% 0.10 75)`, `--color-amber-dim`) is used for inline code text in markdown notes.

### Secondary
- **Dusty Mauve / Warm Rose** (`oklch(62% 0.09 350)`): Tag chips, relational markers. Warmer than sage, quieter than amber. Signals "categorization" rather than "action." Applied only to the tag text itself — the tag's background is the deeper `mauve-bg` (`oklch(22% 0.05 350)`) with a `mauve-border` (`oklch(30% 0.07 350)`) outline, creating a pill-shaped chip that reads at a glance.

### Tertiary
- **Muted Sage / Stone Green** (`oklch(58% 0.06 150)`): Dataset-context accent only. Dataset item titles shift to sage on hover. The dataset list uses sage borders instead of neutral borders, and the dataset page's header uses a sage bottom border. This deliberate tonal break from amber and mauve tells the user they are in data mode, not notes mode. Never used in the notes view.

### Neutral
- **Deep Warm Black / Lamp-Lit Void** (`oklch(11% 0.008 65)`, `--color-bg`): The page background. Near-black but with a warm brown tint that prevents it from feeling like a void.
- **Warm Dark Surface** (`oklch(17% 0.012 62)`, `--color-surface`): Header, note item hover background, search input background. Slightly lighter than the page.
- **Warm Mid Surface** (`oklch(22% 0.014 60)`, `--color-surface-2`): Modal backgrounds. Lighter still — creates a natural three-step depth from page to surface to modal without shadows.
- **Tonal Boundary** (`oklch(28% 0.016 60)`, `--color-border`): Dividers, borders, the bottom line between note items. The boundary layer.
- **Receded Text / Lamp Shadow** (`oklch(50% 0.008 68)`, `--color-text-dim`): Metadata, timestamps, placeholders, note previews, section labels. Present but stepped back.
- **Warm Body Text** (`oklch(82% 0.007 72)`, `--color-text-body`): Primary reading text. Warm off-white — not stark.
- **Near-White Display** (`oklch(93% 0.005 72)`, `--color-text-display`): Note titles, modal headings, UI labels that demand attention. Near-white, still warm.

## 3. Typography

**Display Font:** Georgia, 'Times New Roman', serif
**Body Font:** -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif
**Mono Font (code blocks only):** 'SF Mono', 'Fira Code', 'Consolas', monospace

**Character:** The pairing evokes a journal with footnotes. Georgia draws the eye to note titles and modal headings with quiet authority — it's not a decorative headline font, it's a legible reading serif. The system sans handles navigation and UI chrome with frictionless warmth. Neither font feels technical or clinical; both feel like they belong on a real desk.

**The No-Monospace Rule.** Monospace is a content voice, not a UI voice. `SF Mono` / `Fira Code` appear only inside code blocks within markdown notes. Navigation, buttons, labels, metadata, and form fields are set entirely in the system humanist sans. Using mono in the UI would make JumzysNotes feel like a terminal, not a journal.

### Hierarchy
- **Display** (Georgia, 400 weight, 1.7rem, line-height 1, letter-spacing −0.01em): The app name "JumzysNotes" in the header. The amber voice of the application.
- **Headline** (Georgia, 400 weight, 1.3rem, line-height 1.2): Modal headings (`<h2>` inside modals), section headings (`<h1>`) inside markdown note content. Present but not theatrical.
- **Title** (system-ui, 700 weight, 1rem, line-height 1.3): Note item titles in the list. The workhorse — what the eye lands on first in the notes list.
- **Body** (system-ui, 400 weight, 0.9rem, line-height 1.72, max 65ch for reading contexts): Modal content body, note markdown paragraphs, UI descriptions. The line height (1.72) is generous — this is reading text, not scan text.
- **Label** (system-ui, 400 weight, 0.72rem, letter-spacing 0.01em): Tags, metadata, timestamps, action button text, sort/filter labels, table column headers in uppercase.

## 4. Elevation

JumzysNotes is tonal by default and nearly shadowless. Depth is expressed through three ascending background color steps — the page background is the darkest, surface is slightly lighter, surface-2 (modals) lighter still — not through drop shadows. The UI is flat and warm, not layered and cool.

No shadows exist on cards, list items, inputs, or buttons at rest. The single elevation moment is the modal backdrop: a `oklch(0% 0 0 / 0.72)` transparent overlay that separates the modal from the underlying content by dimming everything behind it. The modal box itself gains visual separation purely through its lighter background (`--color-surface-2`) against the dimmed backdrop.

**The Tonal Depth Rule.** If you reach for a shadow before trying a background step, you've reached too soon. Three distinct surface levels exist (`--color-bg`, `--color-surface`, `--color-surface-2`). Use them before adding shadow. The only valid shadow in this system is the modal overlay — and even that is implemented as a background color, not `box-shadow`.

### Shadow Vocabulary
- **Modal backdrop** (`background: oklch(0% 0 0 / 0.72)`): Applied to the full-screen overlay behind open modals. Not a `box-shadow` — the depth comes from dimming the world, not lifting the modal.
- **Focus ring** (amber glow: `box-shadow: 0 0 0 2px oklch(75% 0.13 75 / 0.15)`): Used on focused inputs and textareas. Technically a shadow but functionally a state indicator — signals keyboard/focus position, not elevation.

## 5. Components

### Buttons
Three button variants — primary (amber fill), ghost (border only), and save (full-width amber).

- **Primary (`.btn-primary`):** Amber background (`--color-amber`), deep dark text (`--color-bg`), 8px radius, 8px 12px padding, 600 weight, 0.82rem text. Hover: opacity drops to 0.85 — the fill stays amber, the object just recedes slightly. Transition: 150ms ease-out-quart.
- **Ghost (`.btn-ghost`):** No background, 1px solid `--color-border`, dim text. Hover: border shifts to dim text color, text shifts to body text. Signals "available but secondary" — never competes with primary amber.
- **Save (`.btn-save`):** Full-width variant of primary used inside modals. Same amber fill, 12px 16px padding, 100% width. Disabled state: opacity 0.4, `not-allowed` cursor.
- **Action (`.btn-action`):** Inline row actions (Edit / Delete) on note items. Border-only, dim text. Hover Edit: amber border + amber text. Hover Delete: muted red (`oklch(65% 0.14 20)`) border + text. Hidden at rest (opacity 0), revealed when parent `.note-item` is hovered.
- **Nav toggle (`.btn-nav`):** Borderless text buttons for Notes / Datasets view switching. Active state: amber text. Default: dim text. No background at rest; subtle `--color-surface-2` tint on hover.
- **Dataset view (`.btn-sage`, `.btn-view`):** Sage counterparts to primary/ghost used only in dataset contexts.

### Chips / Tags
- **Style:** Mauve pill — `--color-mauve-bg` background, `--color-mauve` text, `--color-mauve-border` 1px border, 99px border-radius. 0.72rem font, 1px 8px padding. Pure display; no click state.
- **Placement:** Below note titles in the meta row, inline after the timestamp and a `·` separator.

### Notes List (`.note-item`) — Signature Component
The core layout pattern. A Bear-style flat list; no card backgrounds at rest.

- **Rest state:** Transparent background, 1px `--color-border` bottom divider. First item has a matching top border.
- **Hover state:** Background shifts to `--color-surface` (16px 12px padding maintained). Note title transitions to amber. Row actions fade in.
- **Row anatomy (top to bottom):** Title row (Georgia title + optional media badge aligned right) → meta row (dim timestamp, `·` separator, mauve tag chips) → preview text (0.82rem dim, 1.55 line-height, markdown-stripped) → action row (Edit / Delete, hidden until hover).
- **Transition:** 150ms ease-out-quart on background and color.

### Modal
Five modals in the system (read note, create, edit, stats, upload). All share the same container style.

- **Box:** `--color-surface-2` background, 1px `--color-border` border, 12px radius, 32px padding, 700px max-width, 88vh max-height with overflow scroll. Close button top-right.
- **Animation enter:** Backdrop fades in (opacity 0→1, 300ms ease-out-quart). Modal slides up + fades (translateY 12px→0, opacity 0→1, 300ms ease-out-quart).
- **Animation exit:** 230ms ease-in-quart, slightly faster — the asymmetry makes the exit feel light rather than labored.
- **Headings:** Georgia 400, 1.3rem, `--color-text-display`, `text-wrap: balance`, right-padded to avoid the close button.

### Inputs / Fields
- **Style:** Recessed background (`--color-bg`, darker than the modal surface to create depth), 1px `--color-border` border, 8px radius, 12px 16px padding, 0.9rem body font.
- **Focus:** Border shifts to `--color-amber`. Amber glow ring: `box-shadow: 0 0 0 2px oklch(75% 0.13 75 / 0.15)`. The glow reinforces amber's role as the "you are here" signal.
- **Placeholder:** `--color-text-dim` — visually lighter than field text, never confused with entered data.

### Navigation
- **Structure:** Left: Georgia amber brand name + dim subtitle (note/tag count). Center: text-style Notes / Datasets nav toggles. Right: ghost action buttons (Stats, New Note, Logout).
- **Active nav:** Amber text, no background or underline — the amber color alone communicates selection.
- **Sticky:** Header is `position: sticky; top: 0; z-index: 10` so the brand and nav stay in view while scrolling the notes list.
- **Responsive (≤640px):** Header padding reduces, brand name scales to `--text-lg`, inline stats and the Stats button hide to reduce crowding.

## 6. Do's and Don'ts

### Do:
- **Do** keep every surface tinted warm — even "neutral" backgrounds must carry a warm brown cast (hue 55–80, chroma ≥0.008 in OKLCH). Check with a color picker; the tint is subtle but must be present.
- **Do** use Georgia only for note titles, modal headings, and content headings rendered from markdown. Every nav label, button, tag, timestamp, and input is system-ui.
- **Do** let amber be rare. It earns its weight through scarcity. The app name, save button, active nav, and hover title state — that is the full inventory. Adding amber to a section header or a secondary badge means amber no longer signals "this matters."
- **Do** use tonal stepping (`--color-bg` → `--color-surface` → `--color-surface-2`) to create depth before reaching for `box-shadow`. The only valid shadow in this system is the modal overlay backdrop.
- **Do** keep tag chips in mauve and dataset accents in sage. Color role discipline is what makes the mode shift legible — when the user sees sage, they know they are in data mode.
- **Do** cap body text at 65ch in reading contexts (modal content, markdown paragraphs). The `max-width: 65ch` on `.modal-content p` is deliberate — comfortable reading length.
- **Do** animate modals with an asymmetric in/out duration: 300ms enter, 230ms exit. The exit should feel lighter than the enter.
- **Do** hide note row actions (Edit / Delete) at rest and reveal them on hover with an opacity transition. Actions should appear when the user signals intent, not before.

### Don't:
- **Don't** use the hero-metric template — big numbers with small labels in a 2×2 grid of identical stat cards. JumzysNotes is not a SaaS dashboard. Stats are contextual and inline, not prominently displayed.
- **Don't** build a card grid for notes. The list is a flat Bear-style divider list — rhythm and variation come from different tag counts, preview lengths, and note densities. Identical bordered cards with fixed heights would kill this.
- **Don't** add panel sprawl: no persistent left sidebar, no nested navigation drawers, no secondary toolbar. One person, one flow. Navigation appears in the sticky header and recedes.
- **Don't** use `border-left` as a colored accent stripe on note items or list rows. The system uses full borders and background tints only.
- **Don't** apply gradient text (`background-clip: text` with a gradient). Emphasis is through weight, size, and color — not gradients. The amber is warm already.
- **Don't** use glassmorphism or `backdrop-filter: blur()` decoratively. The UI is opaque and solid. Blur would undermine the warm-flat register.
- **Don't** use monospace outside of code blocks inside note content. Never for nav, buttons, labels, metadata, table headers, or form fields.
- **Don't** use pure black (`oklch(0%)`) or pure white (`oklch(100%)`). Every surface is warm-tinted. `oklch(11% 0.008 65)` for deepest bg; `oklch(93% 0.005 72)` for brightest text.
- **Don't** put sage anywhere in the notes view, or amber anywhere in the dataset table view. The color shift is intentional mode signaling — mixing roles destroys the signal.
