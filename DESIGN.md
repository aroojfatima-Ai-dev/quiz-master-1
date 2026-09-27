# DESIGN.md — Rounds (Quiz Master for Teachers & Students)

> This document describes the design system **as implemented** in this repository
> (`src/main/resources/static/css/rounds.css` + Thymeleaf templates). It is the source of truth for
> any visual change. Change tokens here first, then in `rounds.css`.

---

## 1. Design Thesis & Aesthetic Direction

**Rounds** is a classroom quiz platform built with the structural discipline of **Shopify Polaris**
and the material honesty of an **exam-board answer booklet on a laminated teacher's desk**.

### Core principles

1. **Operational clarity over marketing fluff.** A teacher opening the app at 08:55 before period 1
   needs immediate situational awareness: which class codes exist, which tests are live, how many
   submissions came in, what the class average is. Tables are the hero, not banners.
2. **Paper vs. Slate — two deliberate modes.**
   - **Paper mode** (teacher admin, student dashboard, review, auth): warm matte paper grey canvas,
     crisp white cards, `1px` hairline rules, near-black primary actions.
   - **Slate stage mode** (the student test runner `/student/attempts/{id}/q/{n}`): full-bleed dark
     chalkboard slate engineered for focus and legibility, accented with desk-bell amber for the
     timer and the selected answer.
3. **Zero AI slop / zero purple.**
   - **Strictly forbidden:** purple, indigo, violet, neon gradients, glowing orbs, glassmorphic
     blurred blobs, 3D mascots, generic SaaS hero banners, emoji as icons.
   - **Colour anchor:** deep chalkboard green `#0A5C3F` + warm desk-bell amber `#D98200` + ink `#1A1A1A`.
4. **Bilingual by design.** Every text surface (prompts, options, topics, review) supports Urdu:
   RTL direction and a Nastaliq typeface are applied automatically via the `.urdu` utility when a
   test's language is `URDU`.
5. **Numbers never jitter.** All scores, timers, percentages, counts and codes use tabular numerals.

---

## 2. Colour System & Surface Tokens

Defined as CSS custom properties on `:root` in `rounds.css`.

### Surfaces & hairlines

| Token | Hex | Role |
|---|---|---|
| `--paper` | `#F1F1F0` | Application background canvas (warm paper grey) |
| `--paper-2` | `#F7F7F6` | Sidebar background, table headers, recessed wells (`.well`) |
| `--card` | `#FFFFFF` | Elevated surface for cards, tables, inputs |
| `--rule` | `#E1E1DE` | Standard `1px` structural hairline |
| `--rule-2` | `#CFCFCB` | Emphasised divider, input borders |

### Ink (typography hierarchy)

| Token | Hex | Role |
|---|---|---|
| `--ink` | `#1A1A1A` | Body copy, headings, primary button fill |
| `--ink-2` | `#616161` | Secondary descriptions, metadata, inactive nav |
| `--ink-3` | `#8A8A85` | Muted captions, `kbd`, uppercase eyebrows |

### Brand & semantic accents

| Token | Hex | Role |
|---|---|---|
| `--board` | `#0A5C3F` | Brand green — links, progress bars, active nav icon, correct answers, positive deltas, brand mark |
| `--board-dark` | `#063C2A` | Deep hover/selection green |
| `--board-tint` | `#E4F1EB` | Soft fill for correct-answer chips and positive badges |
| `--amber` | `#D98200` | Live signal, countdown drain bar, selected answer on stage, "Resume" button, overtime flag |
| `--amber-tint` | `#FFF3E0` | Warm tint for Live / Overtime pills |
| `--slate` | `#08130F` | Full-bleed dark background of the test runner |
| `--slate-2` | `#0F211B` | Elevated dark container on the stage (answer tiles, side card) |
| `--red` | `#8C2F2F` | Danger text, incorrect answers, sub-40 % bars |
| `--red-tint` | `#FDECEC` | Danger / incorrect fill |
| `--red-rule` | `#F3D3D3` | Danger border |
| *(stage only)* | `#FF7A66` | Clock and drain bar colour in the final 30 seconds / overtime |

### Status pill tones (`.pill`)

| Class | Background | Text | Border | Used for |
|---|---|---|---|---|
| `.pill-live` | `#FFF3E0` | `#8A4B00` | `#F5D9AE` | Live test, Overtime (+ `.dot.pulse`) |
| `.pill-pos` | `#E4F1EB` | `#0A5C3F` | `#C4E2D5` | Correct, Ended, Easy, best score |
| `.pill-info` | `#EAF0F6` | `#264766` | `#D3E0EC` | Medium difficulty, visibility (Public / Class only) |
| `.pill-warn` | `#FDECEC` | `#8C2F2F` | `#F3D3D3` | Hard difficulty, Incorrect |
| `.pill-neutral` | `#EFEFED` | `#616161` | `#E1E1DE` | Draft, Skipped, Auto-submitted, topic tags |

### Elevation

| Token | Value |
|---|---|
| `--shadow-card` | `0 1px 0 rgba(26,26,26,.04), 0 0 0 1px rgba(26,26,26,.02)` |
| `--shadow-btn` | `0 1px 0 rgba(26,26,26,.08)` |
| `--shadow-pop` | `0 8px 24px -12px rgba(26,26,26,.35)` |

Focus ring (inputs): `border-color: var(--board); box-shadow: 0 0 0 3px rgba(10,92,63,.12)`.

---

## 3. Typography & Numeric Discipline

### Font stacks

| Token | Stack | Use |
|---|---|---|
| `--sans` | `"Archivo", "Helvetica Neue", Helvetica, Arial, sans-serif` | UI, tables, forms, badges, dense operational copy |
| `--display` | `"Instrument Serif", "Iowan Old Style", Georgia, serif` | Page titles, KPI figures, class codes, question prompts, clock |
| `--urdu` | `"Noto Nastaliq Urdu", "Jameel Noori Nastaleeq", serif` | Any `.urdu` element (RTL, `line-height: 2`) |

Fonts are loaded from Google Fonts at the top of `rounds.css`; the stacks degrade gracefully offline.

### Scale & utility classes

| Element | Class | Spec |
|---|---|---|
| Page title | `.serif.page-title` | `40px`, `line-height 1.02`, weight 400, `letter-spacing -.01em` (32px < 1024px) |
| KPI figure | `.serif.kpi.num` | `38px`, `line-height .9` |
| Class code (large) | `.roomcode.num` | `46px`, `letter-spacing .18em` |
| Class code (inline) | `.roomcode.sm.num` | `22px`, `letter-spacing .14em` |
| Stage prompt | `.prompt` | `clamp(28px, 4.2vw, 52px)`, `line-height 1.08`, `max-width 22ch` |
| Stage prompt (Urdu) | `.prompt.urdu` | `clamp(24px, 3.4vw, 40px)`, `line-height 1.9`, no max-width |
| Stage clock | `.clock.num` | `40px` serif (32px on mobile), `.danger` → `#FF7A66` |
| Section heading | `.title` | `15px`, weight 600 |
| Eyebrow | `.eyebrow` | `10.5px`, weight 600, uppercase, `letter-spacing .14em`, `--ink-3` |
| Body | `body` | `13px`, `line-height 1.5` |
| Interactive rows / buttons | — | `13.5px`; `.btn-lg` `14.5px` |
| Table header | `.t th` | `11px`, uppercase, `letter-spacing .08em` |
| Keyboard hint | `kbd` | `10.5px`, 1px border, 2px bottom border |

**Tabular numerals** — `.num` sets `font-variant-numeric: tabular-nums; font-feature-settings: "tnum"`.
Apply to every score, percentage, countdown, question counter, date and class code.

---

## 4. Layout Architecture & Spatial System

### 4.1 Application shell (`templates/fragments/layout.html`)

- **Desktop sidebar** — `236px`, sticky, `--paper-2` background, right hairline.
  - Brand mark: `28px` rounded square (`7px` radius) in `--board`, custom geometric SVG
    (line-line-dot; dot is amber) + "Rounds" + role label ("Teacher desk" / "Student desk").
  - Nav groups with eyebrow labels. Teacher: *Teaching* → Overview, Tests, Classes, Results;
    *Department* → Help centre. Student: *Learning* → Available tests, My results.
  - Active item: white card fill, `1px --rule` border, `--shadow-card`, icon stroke `--board`.
  - Footer: signed-in name/email + full-width "Sign out" button.
- **Sticky top bar** — `56px`, `rgba(241,241,240,.85)` + `backdrop-filter: blur(10px)`, bottom hairline.
  Global search input (`max-width 440px`) with `⌘K` kbd; avatar chip (initials in a `--board` circle + name).
- **Mobile (< 1024px)** — sidebar hidden; horizontal scrollable pill nav sticks under the top bar.
- **Main** — `padding 24px 24px 64px`, `max-width 1320px` (16px padding on mobile).

### 4.2 Grid primitives

| Class | Definition |
|---|---|
| `.grid` | `display:grid; gap:16px` |
| `.g2 / .g3 / .g4` | 2 / 3 / 4 equal columns (g4 → 2 cols ≤ 1100px; all → 1 col ≤ 640px) |
| `.split` | `minmax(0,1fr) 318px` — primary column + right rail (stacks ≤ 1100px) |
| `.rail` | `position: sticky; top: 72px; flex column; gap 16px` |
| `.stack` | vertical flex, `gap 12px` |
| Spacing utilities | `.mt8 .mt16 .mt24 .mb16 .hr` |

### 4.3 Page patterns

**Teacher overview (`teacher/dashboard.html`) — table-as-hero**
1. Header row: date eyebrow → serif greeting ("Good morning, Ruth.") → action pair
   (`New class` secondary, `Create test` primary).
2. 4-column KPI strip: Tests created, Average score, Questions banked, Students enrolled.
3. `.split`: left = *Recent tests* ruled table (title + class/language, status pill, questions,
   avg-score mini bar, submissions, updated) and *Latest submissions* table; right rail =
   *Class codes* card (serif codes) and *How a round works* numbered guide.

**Test detail (`teacher/test_detail.html`)**
- Header: back link, serif title, pills (Live/Draft with pulsing dot, visibility), metadata line;
  actions `Results (n)` + `Publish`/`Unpublish` (publish uses `.btn-board`).
- Left: *Questions* list (`.qitem` rows: numbered square `.qnum`, prompt, 2-col options with the
  correct option in green, topic/difficulty pills, inline `<details>` edit form, Remove);
  *Add a question* form; *Bulk import* card (file input **or** paste textarea → preview list → Import/Discard; collapsible format guide).
- Right rail: *At a glance* mini-KPIs + class code; *Settings* form; danger-zone delete.

**Student dashboard (`student/dashboard.html`)**
- 3 KPIs (Tests taken, Average, Best), *Available tests* table with Start / Resume (amber) / Retake,
  *Recent results* table; rail = *Join a class* with oversized `.code-input`
  (`34px` serif, `letter-spacing .3em`, uppercase, centred) and *My classes* list.

**Auth (`auth/login.html`, `auth/register.html`) — split screen**
- Left `.auth-photo`: slate panel with a radial green wash + faint ruled-paper lines (pure CSS, no image),
  brand row, serif headline *"Six characters and you're in."* (teacher variant: *"Set the paper. Skip the marking."*), one-line description.
- Right `.auth-form`: `max-width 400px` paper form; register has a 2-segment role switch (`.seg`).
- Collapses to single column ≤ 900px.

**Review (`review.html`) — shared by teacher and student**
- Left: `.rev` rows — numbered prompt, result pill (Correct / Incorrect / Skipped), 2×2 option grid
  where the correct option is `.good` (green) and a wrong pick is `.bad` (red), with "your answer" /
  "correct answer" micro-labels.
- Rail: conic-gradient `.score-ring` (96px) beside the serif score, then Time taken / Limit /
  Overtime / Submission-type.

---

## 5. Slate Stage — the Student Test Runner (`student/question.html`)

Rendered **outside** the shell (no sidebar/top bar) so nothing competes with the question.

- **Atmosphere:** `--slate` background; `::before` layers a radial `rgba(10,92,63,.45)` wash from the
  top and faint 32px ruled lines (`rgba(255,255,255,.025)`). No photography required.
- **Drain timer:** fixed `6px` bar at the very top; amber fill whose `width` is set every second by JS
  (`transition: width 1s linear`). Turns `#FF7A66` (`.danger`) at ≤ 30 s. Reads `0%` in overtime.
- **Stage header:** class eyebrow + test title · amber tracked class code (`.stage-code`) · student name ·
  serif clock right-aligned (`+m:ss` and red when in overtime).
- **Body grid:** `minmax(0,1fr) 300px`, `max-width 1120px`, `gap 32px` (single column ≤ 960px).
- **Main:** "Question n of N" eyebrow + difficulty/topic pills + hidden *Overtime* pill →
  serif `.prompt` → `.answers` 2×2 grid of `<label>` tiles (`min-height 86px`, `--slate-2` fill,
  `1px rgba(255,255,255,.14)` border, `34px` serif key square). Selected tile: amber border,
  `rgba(217,130,0,.14)` fill, key square becomes solid amber with dark text.
  Actions: `← Previous` (`.btn-stage` outline), `Save & next →` / `Submit test` (`.btn-amber`),
  `Finish early` pushed right. Hint line: keys 1-4 / A-D select, Enter continues.
- **Side card (`.stage-card`):** answered counter (serif), `.progress-dots` (14px squares; green =
  answered, amber outline = current), time-limit note, marking note ("the reveal comes at the end of
  the round").
- **Behaviour contract:** answers save on *next/prev/submit*; correctness is never shown during the
  run; the server re-checks the clock on every request (client timer is cosmetic).

---

## 6. Component Specifications (`rounds.css`)

### Cards
- `.card` — `bg --card`, `1px --rule`, `radius 12px`, `--shadow-card`, `padding 20px`.
- `.card.flush` — no padding, `overflow hidden` (for full-bleed tables/lists).
- `.card-head` — flex row, bottom hairline, `padding-bottom 12px; margin-bottom 16px`;
  `.title` + optional muted `<p>` + right-aligned action slot.
- `.well` — recessed `--paper-2` box, `radius 8px`, used for mini-stats and format guides.

### Buttons (`.btn`, height `34px`, radius `8px`, `13.5px/500`)
| Variant | Spec |
|---|---|
| default (secondary) | `bg --card`, `border --rule`, hover `#F7F7F6`, active `#EFEFED`, `--shadow-btn` |
| `.btn-primary` | `bg --ink`, white text, hover `#303030`, active `#111` (Polaris-style dark primary) |
| `.btn-board` | `bg --board`, white — *Publish*, *Import N questions* |
| `.btn-amber` | `bg --amber`, dark text, weight 600 — *Resume*, stage *Save & next* / *Submit* |
| `.btn-ghost` | transparent, `--ink-2`, hover `#EFEFED` |
| `.btn-danger` | `--red` text, `#E7C9C9` border, hover `--red-tint` |
| `.btn-stage` | transparent, white text, `rgba(255,255,255,.25)` border (dark stage only) |
| Sizes | `.btn-sm` 28px/12.5px · `.btn-lg` 42px/14.5px · `.btn-block` 100% width |

### Forms
- `.input` — `36px`, `1px --rule-2`, `radius 8px`, `13.5px`; textarea auto-height, `padding 8px 10px`.
- `label.f` — `12.5px/500`, `margin-bottom 5px`; `.help` — `12px --ink-3`; `.field` — `margin-bottom 14px`.
- `.check` — inline checkbox row; `.code-input` — class-code entry (see §4.3).
- Urdu tests set `dir="rtl"` and `.urdu` on prompt/option inputs automatically.

### Tables (`table.t`)
- Header cells `--paper-2`, uppercase `11px`; body cells `12px 16px` padding with bottom hairline;
  row hover `#FAFAF9`; `.r` right-aligns numerics; `a.rowlink` is ink-coloured, weight 500;
  `.sub` is a `12px --ink-3` second line.
- `.bar` — `5px` inline progress (`--board` fill; `.amber`, `.red` variants; min-width 60px).
- `.empty` — centred placeholder (`36px 20px`), bold first line in ink, explanation in `--ink-3`.

### Feedback
- `.flash-ok` (green tint) / `.flash-err` (red tint) — `10px 14px`, `radius 8px`, above page content.
- `.pill` — `20px` tall, `radius 99px`, `11.5px/500`; optional `.dot` (6px) with `.pulse` (1.4 s opacity loop).
- `.delta-up` / `.delta-down` — `▲` green / `▼` red tabular deltas.

### Question & review rows
- `.qitem` — `14px 20px`, hairline separated; `.qnum` 26px numbered square; `.opts` 2-col, `.ok` = green.
- `.rev .opt` — bordered option chip with `.k` letter square; `.good` / `.bad` states (see §4.3).
- `details.edit` — marker hidden; summary turns green when open.

---

## 7. Motion

- Keep motion functional and brief. Only three animations exist:
  1. `.dot.pulse` — 1.4 s opacity pulse on Live/Overtime pills.
  2. Drain bar `width` transition — `1s linear`, matching the tick.
  3. Answer tile `border-color`/`background` — `.12s`.
- No page transitions, parallax, or entrance animations.

---

## 8. Iconography & Imagery

- Icons are inline SVG, `16px`, `stroke-width 1.7`, round caps/joins, `currentColor`
  (active nav icon takes `--board`). No icon fonts, no emoji.
- The brand mark is a geometric "A-frame + dot" (two strokes meeting at an apex, amber dot at the base).
- The implementation is **imagery-free** — atmosphere on the auth panel and the stage is produced with
  CSS gradients and ruled lines so the app has no external image dependencies. If photography is
  added later it must follow one look: soft overcast daylight, matte paper/slate textures, palette
  limited to warm grey, chalkboard green and desk-bell amber.

---

## 9. Accessibility & Content Rules

- Body text contrast ≥ 4.5:1 on paper; on the stage, secondary text is `rgba(255,255,255,.5–.6)`.
- Every interactive control is a real `<button>`, `<a>` or `<label>`; answer tiles wrap native radios.
- Keyboard: `1-4` / `A-D` select, `Enter` advances, forms submit normally (no JS-only paths).
- Confirmation dialogs guard destructive actions (submit test, delete class/test/question).
- Copy tone: short, operational, British spelling ("centre", "marking"). Never expose correctness
  mid-test. Use "class code" (not "room code") in student-facing copy.

---

## 10. Theme Variants

`theme-demos.html` at the repo root previews six alternative token sets on the same two screens
(Chalkboard & Bell — current; Ink & Brick; Maroon & Brass; Teal & Saffron; Graphite & Flame;
Olive & Honey). Switching themes only requires replacing the `:root` variables in §2; layouts,
typography and components are theme-agnostic.
