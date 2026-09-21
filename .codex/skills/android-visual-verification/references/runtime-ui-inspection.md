# Runtime UI inspection contract

Runtime metadata supplements the clean screenshot; it never replaces visual
inspection. Use it when a screenshot raises a suspicion or when objective
geometry/style evidence would make a finding actionable.

Prefer a small app-owned debug helper or integration-test utility over a raw
widget/render tree dump. Inspect only visible design-relevant elements, using
stable keys strategically (for example `Key('profile.title')`). If the app has
no utility, do not invent a production architecture during a visual check;
inspect source and record runtime data as unavailable, or add a task-scoped
debug-only probe when the task explicitly permits code changes.

## Snapshot shape

Use JSON, Markdown, or text, but expose these fields where available:

```text
SCREEN: <route/state>
VIEWPORT: <width> x <height> @ <dpr>

ELEMENT <stable-id or type>
  text / semantic_label: <value>
  bounds: x=<x> y=<y> w=<w> h=<h> right=<right> bottom=<bottom>
  visible: <true|false>
  typography: family=<family> size=<px> weight=<weight> lineHeight=<px>
               letterSpacing=<px> color=<color> align=<alignment>
               maxLines=<value> overflow=<value>
  surface: background=<color> radius=<value> border=<value> opacity=<value>

RELATIONSHIPS
  <element-a> -> <element-b>: vertical gap=<px>, left delta=<px>
```

Prioritize rendered bounds and resolved typography. Add icon size, constraints,
padding, border, or background only for components involved in a suspicion.
Derived relationships such as vertical gaps and shared left edges are more
useful than a complete tree dump. Treat unusual token values as investigation
signals, not automatic failures.

## Evidence rule

Combine three kinds of evidence:

1. perceptual: what looks wrong in the clean screenshot;
2. objective: what the rendered snapshot measures;
3. implementation: which source/theme/token likely produced it.

For example, “the section feels disconnected” becomes actionable as “the
subtitle-to-card gap is 46 px while nearby section gaps are 16–24 px; the
snapshot confirms the bounds, and source shows a 32 px section padding plus a
14 px child gap.”

An annotated screenshot may overlay IDs, bounds, guides, and measured gaps.
Always retain and inspect the clean screenshot separately; annotations are
diagnostic evidence only.
