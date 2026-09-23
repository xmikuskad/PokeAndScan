# Agent document router

Use this router for Android or product-code work, product questions, and changes to project documentation. The product documents describe the **intended MVP**; they do not prove a feature exists in the scaffold. Inspect the current code and the task's acceptance criteria before implementing.

Read the smallest relevant set of documents below. Open the linked section, not the whole file, unless the task spans the document.

## Authority

- [Business specification](business-specification.md) owns product scope, user rules, and MVP acceptance.
- [Design specification](design-specification.md) owns screen behavior, copy, accessibility, and UI tokens; [Brand direction](brand-direction.md) owns name, identity, typography, and voice.
- [Technical design](technical-design.md) owns implementation contracts, architecture, persistence, processing, exports, and verification criteria.
- [Domain glossary](../CONTEXT.md) defines domain terms. [ADRs](adr/) explain durable architectural choices.
- The issue's acceptance criteria describe the requested change. Code shows current behavior, not necessarily intended behavior.

If authoritative current documents conflict, compare the related issue and implementation for context. If that does not settle intent, do not invent a resolution: ask the owner about the specific blocking behavior before implementing it.

The [external UI prompt](ui-design-prompt.md) and [imported v0.4 handoff](uiux-reference/v0.4/PokeAndScan_UIUX_Design_Handoff_v0.4.md) are historical. Use them only for visual background. They never override current product documents. The [reference guide](uiux-reference/README.md) explains how to read the imported boards.

## Route by task

| Task | Read |
|---|---|
| Android code change | Inspect the affected implementation, nearby tests, and relevant configuration first. Read Technical design: [platform and technology choices](technical-design.md#platform-and-technology-choices) and [logical architecture](technical-design.md#logical-architecture); then follow the relevant behavior row below. Follow conventions visible in nearby code and repository configuration. |
| Product scope, roadmap, user rules, or release readiness | Business specification: [MVP scope](business-specification.md#mvp-scope), [business rules](business-specification.md#business-rules), [completion gate](business-specification.md#mvp-completion-gate). For feature acceptance, also read its issue. |
| Screen, interaction, copy, or accessibility change | Design specification: the relevant section under [New scan](design-specification.md#new-scan-flow), [Review](design-specification.md#review), [Snapshots](design-specification.md#snapshots-and-library), [Export](design-specification.md#export), or [Visual and accessibility direction](design-specification.md#visual-and-accessibility-direction). Read [Brand direction](brand-direction.md) only when the change touches product identity, voice, or branding. |
| Capture source, screen recognition, parser, or deduplication | Business specification: [business rules](business-specification.md#business-rules) and [appraisal traversal](business-specification.md#confirmed-appraisal-traversal-protocol); Technical design: [supported profile](technical-design.md#supported-mvp-profile), [capture abstraction](technical-design.md#capture-abstraction), [appraisal pipeline](technical-design.md#appraisal-pipeline), and [temporal identity](technical-design.md#temporal-identity-and-deduplication); read [capture ADR](adr/0001-dual-capture-offline-snapshot-processing.md). |
| Record status, review, evidence, or manual correction | [Domain glossary](../CONTEXT.md#evidence-and-review); Business specification: [business rules](business-specification.md#business-rules); Design specification: [Review](design-specification.md#review); Technical design: [field confidence and evidence](technical-design.md#field-confidence-and-evidence). |
| Persistence, interruption, background processing, or deletion | Business specification: [business rules](business-specification.md#business-rules); Technical design: [persistence and storage](technical-design.md#persistence-and-storage), [live capture](technical-design.md#live-capture-requirements), or [MP4 background execution](technical-design.md#mp4-background-execution), as relevant; read [lifecycle ADR](adr/0002-orthogonal-scan-lifecycle-and-quality.md) for lifecycle changes. |
| CSV/JSON schema or export flow | Business specification: [business rules](business-specification.md#business-rules); Technical design: [export contract](technical-design.md#export-contract); Design specification: [Export](design-specification.md#export) for user experience. |
| Visual assets, illustration, or logo work | Start with [Brand direction](brand-direction.md) and the relevant design section. For reference only, read the [UI/UX guide](uiux-reference/README.md), then the specific board or handoff section needed. |
| GitHub issue work, PRDs, or triage | [Issue tracker guide](agents/issue-tracker.md); for triage labels also read [triage labels](agents/triage-labels.md). |
| Changing a domain term or architectural decision | Read [domain documentation guide](agents/domain.md), the relevant glossary entry, and any related ADR. |

## Working rules

1. For code changes, inspect the current implementation, nearby tests, and relevant configuration before editing. The scaffold may not match the intended MVP.
2. Use the issue's acceptance criteria when present. If no issue exists, derive observable acceptance criteria from the relevant current specification before coding.
3. Preserve the product trust rules: local/offline processing, user-controlled capture, retained evidence for review, and explicit Unknown values instead of guessed data.
4. Verify the affected behavior with focused checks appropriate to the change. Report what changed, what was checked, and any unmet acceptance criterion.
5. Treat MVP completion as a separate gate. A feature or scaffold change is not proof that the MVP completion gate has passed.

## Project Markdown inventory

- Current requirements: [business specification](business-specification.md), [design specification](design-specification.md), [technical design](technical-design.md), and [brand direction](brand-direction.md).
- Domain and decisions: [CONTEXT.md](../CONTEXT.md) and [ADRs](adr/).
- Agent workflows: [issue tracker](agents/issue-tracker.md), [triage labels](agents/triage-labels.md), and [domain documentation](agents/domain.md).
- Historical design exploration: [external UI prompt](ui-design-prompt.md) and [UI/UX reference pack](uiux-reference/README.md) with its imported handoff.
