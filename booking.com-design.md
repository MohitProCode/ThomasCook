version: alpha
name: Booking.com Blue
description: A trust-first travel booking system with compact typography, rounded controls, and a strong blue accent.
colors:
  primary: "#006CE4"
  secondary: "#1A1A1A"
  tertiary: "#F5F5F5"
  neutral: "#FFFFFF"
  surface: "#FFFFFF"
  on-surface: "#1A1A1A"
  error: "#D71920"
  border: "#E7E7E7"
  muted: "#6B7280"
  accent-soft: "#EAF3FF"
typography:
  headline-display:
    fontFamily: "Avenir Next, BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: 0px
  headline-lg:
    fontFamily: "Avenir Next, BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 32px
    letterSpacing: 0px
  headline-md:
    fontFamily: "Avenir Next, BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 22px
    fontWeight: 700
    lineHeight: 32px
    letterSpacing: 0px
  headline-sm:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 20px
    fontWeight: 700
    lineHeight: 24px
    letterSpacing: 0px
  headline-xs:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 18px
    fontWeight: 600
    lineHeight: 22px
    letterSpacing: 0px
  body-lg:
    fontFamily: "Avenir Next, BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 18px
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: 0px
  body-md:
    fontFamily: "Avenir Next, BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 32px
    letterSpacing: 0px
  body-sm:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0px
  label-lg:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px
    letterSpacing: 0px
  label-md:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0px
  label-sm:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0px
  caption:
    fontFamily: "BlinkMacSystemFont, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
    letterSpacing: 0px
rounded:
  none: 0px
  sm: 4px
  md: 8px
  lg: 12px
  xl: 16px
  full: 9999px
spacing:
  xs: 2px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  gutter: 24px
  section: 32px
  page: 40px
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.neutral}"
    typography: "{typography.label-md}"
    rounded: "{rounded.full}"
    padding: "4px 12px"
    height: "44px"
  button-secondary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.neutral}"
    typography: "{typography.label-md}"
    rounded: "{rounded.full}"
    padding: "4px 12px"
    height: "44px"
  button-tertiary:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-md}"
    rounded: "{rounded.none}"
    padding: "0px"
    height: "auto"
  button-link:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-md}"
    rounded: "{rounded.none}"
    padding: "0px"
  card:
    backgroundColor: "{colors.tertiary}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: "16px"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: "12px"
    height: "44px"
  modal:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: "24px"
  chip:
    backgroundColor: "{colors.accent-soft}"
    textColor: "{colors.primary}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    padding: "4px 12px"
---

# Booking.com Blue

## Overview
This interface feels utilitarian, trustworthy, and highly conversion-focused, with just enough warmth to keep travel planning approachable. The visual tone is professional and familiar rather than editorial, using a dense but controlled layout that prioritizes search, booking actions, and reassurance content. Blue is the primary confidence signal, while white surfaces and soft gray panels keep the experience clear and transactional.

## Colors
- **Primary (#006CE4):** A vivid booking blue used for core actions, links, selected states, and brand moments. It should always read as the highest-attention interactive color.
- **Secondary (#1A1A1A):** A near-black text color for headlines, body copy, and high-contrast UI labels. It anchors the interface and keeps dense information readable.
- **Tertiary (#F5F5F5):** A light neutral panel color used for cards, sections, and low-emphasis containers. It supports layering without adding visual noise.
- **Neutral (#FFFFFF):** The main canvas color for pages, modals, and button text. It preserves clarity and makes blue accents feel crisp.
- **Surface (#FFFFFF):** The default component surface for elevated or floating elements like dialogs and inputs. It should remain clean and uninterrupted.
- **On-surface (#1A1A1A):** The standard text color on white and pale surfaces. Use it for most readable content and iconography.
- **Error (#D71920):** A strong red reserved for validation states and destructive emphasis. It should appear sparingly.
- **Border (#E7E7E7):** A soft divider color for cards, fields, and outlined regions. It creates structure without harsh contrast.
- **Muted (#6B7280):** A subdued gray for secondary metadata, helper text, and low-priority labels. Use it to reduce hierarchy without disappearing.
- **Accent-soft (#EAF3FF):** A pale blue tint for selected chips, subtle highlights, and informational emphasis. It reinforces the brand without competing with primary blue.

## Typography
The system uses Avenir Next for major headings and body text, with BlinkMacSystemFont/system sans fallbacks to preserve a clean product feel across platforms. Headings are bold and compact, with no visible uppercase treatment or decorative letter spacing, reinforcing speed and clarity over personality. Body copy is comfortable and highly legible, while labels and controls lean slightly heavier to support dense booking workflows.

- `headline-display` and `headline-lg` are used for hero messaging and prominent section titles.
- `headline-md`, `headline-sm`, and `headline-xs` handle progressive hierarchy in content modules and dialogs.
- `body-lg` and `body-md` support explanatory copy; `body-md` is the standard default for most paragraphs.
- `body-sm`, `caption`, and the label styles are reserved for UI chrome, metadata, and helper content.
- `label-md` is the most important control text style and maps well to buttons, tabs, and filters.
- `label-sm` works well for chips, badges, and secondary inline controls.

## Layout
The layout is center-aligned and content-rich, with a strong hero region followed by stacked promotional and reassurance modules. Spacing is compact but not cramped: the rhythm is built from small increments like 2px, 8px, 12px, 16px, and 20px, with larger section breaks for major page transitions. Cards and search controls use consistent internal padding and generous horizontal framing, creating a fixed-width-feeling experience on desktop even when sections span the page.

Use `spacing.gutter`, `spacing.section`, and `spacing.page` to separate major regions and keep grouped controls visually organized. Inputs and buttons sit tightly aligned in search bars to reduce wasted space and keep the primary conversion task obvious. Cards generally rely on modest padding rather than large whitespace, since the UI is optimized for scanning many options at once.

## Elevation & Depth
The design is mostly flat, with hierarchy created by color, borders, and contrast rather than heavy shadows. The clearest depth cue is the modal: it floats above the page on a white surface with rounded corners and soft shadowing implied by separation from the dimmed backdrop. Most cards remain lightweight, using `border` and `tertiary` backgrounds to define boundaries without making the interface feel bulky.

Avoid exaggerated shadow systems; they would fight the pragmatic booking aesthetic. If additional emphasis is needed, prefer stronger contrast, a subtle border, or a slight tonal shift before adding depth.

## Shapes
The shape language is friendly and highly rounded for interactive controls, especially the pill-shaped primary buttons. Cards and modals use moderate corner radii, while utility elements like links and some text controls stay sharp or unrounded. Overall, the system blends softness with efficiency: rounded actions feel approachable, but the content containers stay restrained.

Use `rounded.full` for buttons and chips, `rounded.md` for cards and inputs, and `rounded.lg` for dialogs or larger floating panels. Sharp corners are acceptable for simple text links and structural separators.

## Components
**Buttons**
- Primary actions should use `button-primary` with a blue fill, white text, `rounded.full`, and compact horizontal padding.
- Secondary actions in the same blue context can reuse `button-secondary`, but should remain visually consistent with the main CTA family.
- Tertiary and link-style actions should use `button-tertiary` or `button-link`, with no border and minimal padding.
- Button height should stay at `44px` to preserve tap targets.
- Keep button text concise and label-like; avoid sentence-case verbosity where possible.

**Cards**
- Cards use `card` with a pale background, `border`, and `rounded.md`.
- Internal spacing should be moderate, around `16px`, so cards can carry compact promotional content without feeling crowded.
- Keep visual treatment simple: no heavy shadows, gradients, or decorative framing.

**Inputs**
- Inputs should feel like part of the search experience: white or very light surfaces, visible borders, and `rounded.md`.
- Use generous height and clear placeholder text to support quick scanning.
- The field chrome should be strong enough to stand out in dense hero areas, but not so strong that it dominates the page.

**Modal**
- Use `modal` for sign-in prompts and account-related overlays.
- The modal should be white, centered, and visually dominant, with rounded corners and ample padding.
- Close controls should be minimal and unobtrusive, typically a simple icon in the top-right corner.

**Chips / Tabs**
- Chips should use `chip` for lightweight filters and stateful selections.
- Active chips may use an accent-tinted background with blue text, while inactive chips should be quieter and border-led.
- Keep chip content short and horizontally compact.

**Navigation**
- Top navigation should remain lightweight and text-first, with icon + label patterns where needed.
- Active states can be represented with a rounded outline or pill treatment rather than filled backgrounds alone.

## Do's and Don'ts
- Do keep primary actions blue, rounded, and easy to scan.
- Do use restrained borders and flat surfaces to maintain the clean travel-booking feel.
- Do keep typography bold and compact for headings, with readable but not oversized body copy.
- Do favor consistent 44px-tall interactive targets for buttons and controls.
- Don't introduce heavy shadows or dramatic elevation across standard cards.
- Don't use excessive color variety; blue should remain the dominant brand signal.
- Don't make panels overly spacious; the system works best with dense, efficient grouping.
- Don't add decorative type treatments, uppercase-heavy labels, or wide tracking unless a specific utility requires it.