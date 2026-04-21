# Design System Strategy: The Digital Sanctuary

## 1. Overview & Creative North Star
This design system moves away from the cold, industrial tropes of traditional security software. Instead of steel, dark mode, and rigid grids, we are building **"The Digital Sanctuary."** 

The creative objective is to evoke a sense of calm and breathing room. We achieve this through "Organic Editorialism"—pairing high-fashion typography (Newsreader) with hyper-rounded, pill-shaped containers that feel like smooth river stones. This system prioritizes negative space and tonal shifts over structural lines, creating a UI that feels less like a database and more like a private, high-end botanical library.

---

## 2. Colors: Tonal Atmosphere
The palette is a sophisticated blend of lavender, mint, and pale blue, anchored by deep slate neutrals.

*   **Primary (`#535a97`):** Use for brand presence and key actions.
*   **Secondary (`#3a6759`):** Represents organic growth and security status (e.g., "Safe" or "Verified").
*   **Tertiary (`#396478`):** Use for utility elements and supportive information.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to define sections or containers. 
Structure must be achieved through:
1.  **Background Shifts:** Place a `surface-container-low` card on a `surface` background.
2.  **Negative Space:** Use the Spacing Scale (specifically 8, 10, or 12) to create clear content groupings.

### Surface Hierarchy & Nesting
Treat the UI as a series of nested physical layers. 
- **Base Layer:** `surface` (`#f7f9ff`).
- **Secondary Layer:** `surface-container` (`#e9eef7`) for main content areas.
- **Top Layer:** `surface-container-lowest` (`#ffffff`) for the most important interactive cards.
Nesting should follow a logical "inward" progression: as a user dives deeper into a detail view, the container lightens to pull the content forward.

### The "Glass & Gradient" Rule
To add visual "soul," use subtle linear gradients for large hero backgrounds or primary CTAs (e.g., transitioning from `primary` to `primary_container`). For floating modals or navigation bars, apply **Glassmorphism**: use `surface` at 70% opacity with a `20px` backdrop blur to allow the organic background colors to bleed through.

---

## 3. Typography: The Editorial Voice
This system relies on a high-contrast pairing to balance "Premium Editorial" with "Functional Utility."

*   **Display & Headlines (Newsreader):** Use for page titles and high-level summaries. The serif adds a human, authoritative, and elegant touch that differentiates the product from "standard" tech tools.
*   **Titles & Body (Manrope):** Use for all functional data. Manrope’s geometric clarity ensures that complex passwords and usernames remain highly legible.

**Hierarchy Strategy:**
- Use **Display-LG** for empty states or "Welcome" screens to create an intentional focal point.
- Use **Label-MD** in `on_surface_variant` for metadata to keep the interface feeling airy and light.

---

## 4. Elevation & Depth
Depth is achieved through **Tonal Layering** rather than heavy shadows.

*   **The Layering Principle:** A `surface-container-highest` object should sit atop a `surface-container-low` background. This creates a soft, natural lift that mimics fine paper stocks.
*   **Ambient Shadows:** If a card must "float" (e.g., a floating action button or a dragged item), use a shadow with a blur radius of `24px` to `40px` and an opacity of 6%. The shadow color should be a tinted version of `on_surface` to maintain a natural, sunlit feel.
*   **The "Ghost Border" Fallback:** If a container lacks enough contrast against its background, use a "Ghost Border": the `outline_variant` token at **15% opacity**. Never use a 100% opaque border.
*   **Radius:** Follow the high-roundness mandate. Most cards should use `xl` (3rem) or `full` (9999px) to maintain the "organic" feel of the reference plant app.

---

## 5. Components

### Buttons
- **Primary:** Pill-shaped (`full` radius). Use a subtle gradient of `primary` to `primary_dim`. Text is `on_primary` (Manrope, bold).
- **Secondary:** Pill-shaped. Background is `secondary_container`, text is `on_secondary_container`.
- **Tertiary:** No background. Text is `primary` with a small icon.

### Input Fields
- **Container:** Use `surface_container_highest` with an `xl` radius.
- **States:** On focus, the background shifts to `surface_container_lowest` with a "Ghost Border" of `primary` at 20%. Forbid the standard "bottom line" input style.

### Cards & Lists
- **Forbid Dividers:** Do not use horizontal lines between list items. 
- **The Grouping Method:** Use vertical white space (Spacing 4 or 6) or subtle alternating background shifts (e.g., `surface-container-low` vs `surface-container-high`) to define separate entries.
- **Visuals:** Add organic, soft-colored icons (lavender/mint) in the leading slot to maintain the "Sanctuary" vibe.

### Vault Progress Chips
- Use the `full` radius scale. Backgrounds should use soft pastels (e.g., `tertiary_container`) to indicate password strength or category without looking like an "alert."

---

## 6. Do's and Don'ts

### Do:
- **Do** use intentional asymmetry. A large serif headline on the left with a pill-shaped "Add" button on the far right creates a premium, custom feel.
- **Do** embrace white space. If you think there is enough space, add one more level from the Spacing Scale.
- **Do** use `9999px` (full) rounding for any component that is smaller than 48px in height.

### Don't:
- **Don't** use pure black (`#000000`) for text. Always use `on_surface` (`#2c333c`) to keep the "soft" feel.
- **Don't** use 1px dividers or borders. This is the fastest way to make the design feel "generic."
- **Don't** cram information. If a vault entry has too much data, use a progressive disclosure pattern (nesting) rather than a dense grid.
- **Don't** use standard Material 3 "elevated" shadows. Stick to Tonal Layering.