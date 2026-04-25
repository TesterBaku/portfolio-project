---
name: "design-architecture"
description: "Design system architecture, write ADRs, and document design decisions. Use when: designing services and boundaries, writing architecture decision records, defining API contracts, or evolving data models."
user-invocable: true
---

# Architecture Design Skill

Design and document your system architecture using proven patterns and templates.

## When to Use This Skill

- **New service design**: You're designing a new microservice or major component
- **Architecture decision**: You're making a significant technical choice (database, framework, pattern)
- **API design**: You're defining a new API contract
- **Data model evolution**: You're changing the database schema
- **System refactoring**: You're rearchitecting part of the system

## What This Skill Provides

### ADR Template
An Architecture Decision Record (ADR) captures a significant architectural decision, its context, and the reasoning behind it.

Location: `.github/skills/architecture-design/assets/adr-template.md`

### Architecture Diagram Template
A text-based architecture diagram to visualize system boundaries and interactions.

Location: `.github/skills/architecture-design/assets/diagram-template.md`

### Design Checklist
A checklist to ensure your architecture is well-designed and documented.

Location: `.github/skills/architecture-design/assets/design-checklist.md`

## Workflow

### 1. Clarify the Problem
Start by understanding the requirements and constraints:
- What problem are we solving?
- What are the constraints (performance, scalability, cost)?
- Who are the users and what are their needs?
- What are the current pain points?

### 2. Explore Options
Propose 2-3 architectural approaches with tradeoffs:
- Monolith vs. microservices
- Synchronous vs. asynchronous
- Relational vs. NoSQL database
- Own vs. managed services

### 3. Propose a Design
Use the architecture diagram template to visualize the proposed design.

### 4. Document the Decision
Write an ADR explaining the choice using the template.

### 5. Design Checkpoints
Walk through the design checklist to ensure completeness.

## Example ADR

```
# ADR-001: Use Microservices Architecture for Logistics Platform

## Status
Accepted

## Context
The logistics platform needs to:
- Scale independently for different services (orders, shipments, notifications)
- Allow teams to deploy independently
- Use different languages and technologies for optimal fit (Python for AI, Java for core, C# for messaging)

## Decision
We will use a microservices architecture with:
- Synchronous REST APIs for request/response
- Asynchronous messaging (RabbitMQ/SNS) for events
- Docker containers for packaging
- Docker Compose for local dev, Kubernetes for production

## Consequences
### Positive
- Independent scaling and deployment
- Technology flexibility
- Clear team boundaries

### Negative
- Operational complexity
- Network latency between services
- Distributed debugging and testing
- Data consistency challenges

## Alternatives Considered
1. Monolith: Simpler initially, but harder to scale and limits team velocity
2. Serverless: Lower ops overhead, but less control and higher costs at scale
```

## Architecture Diagram Example

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                              │
│                    (Authentication, Routing)                    │
└──────────┬──────────────────────────────────────────────────────┘
           │
    ┌──────┴───────────────┬──────────────────┐
    │                      │                  │
┌───▼────────┐   ┌─────────▼──────┐   ┌──────▼──────────┐
│ Orders     │   │ Shipments      │   │ AI Assistant   │
│ Service    │   │ Service        │   │ (Python)       │
│ (Java)     │   │ (Java)         │   │                │
└───┬────────┘   └────────┬───────┘   └────────┬───────┘
    │                     │                    │
    └──────────┬──────────┴────────────────────┘
               │
        ┌──────▼──────────────┐
        │ PostgreSQL (RDS)    │
        │ - Orders            │
        │ - Shipments         │
        │ - Events            │
        └─────────────────────┘
```

## Tips for Good Architecture

1. **Draw it first**: Visualize before implementing
2. **Name services by capability, not technology**: "OrderService" not "JavaService"
3. **Define boundaries clearly**: Who owns what data?
4. **Plan for change**: Design with evolution in mind
5. **Document assumptions**: What are you assuming about scale, traffic, etc.?

## Related Resources

- ADR template: `.github/skills/architecture-design/assets/adr-template.md`
- Diagram template: `.github/skills/architecture-design/assets/diagram-template.md`
- Design checklist: `.github/skills/architecture-design/assets/design-checklist.md`
- Project standards: `.github/copilot-instructions.md`

---

Need help with a specific design decision? Use `@Architect` agent to discuss tradeoffs and options.
