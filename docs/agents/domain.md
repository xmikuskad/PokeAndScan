# Domain documentation

This is a single-context repository. Root [CONTEXT.md](../../CONTEXT.md) defines domain terms. [docs/adr/](../adr/) records architectural decisions and their reasons.

When a task touches a domain concept, read its definition in `CONTEXT.md` and use that term in code, tests, issues, and explanations. Read only the ADRs related to the behavior being changed. If a proposed change conflicts with an ADR, call out the conflict before changing the design.

When a domain term is genuinely missing or an architectural trade-off is decided, use the `domain-modeling` skill to update the glossary or record an ADR. Keep implementation steps in the relevant specification, not in the glossary.
