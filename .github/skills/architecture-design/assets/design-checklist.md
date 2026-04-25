# Architecture Design Checklist

Use this checklist when designing or reviewing system architecture.

## Problem Understanding
- [ ] Problem statement is clear and written down
- [ ] Success criteria are defined (latency, throughput, availability)
- [ ] Constraints are documented (cost, team size, deadline)
- [ ] Stakeholders have been consulted
- [ ] Current pain points are explicitly identified

## Solution Design
- [ ] Architecture diagram is documented (text or visual)
- [ ] Service boundaries are clear (what owns what data?)
- [ ] Communication patterns are defined (sync, async, event-driven)
- [ ] Data flow is documented
- [ ] Single points of failure are identified
- [ ] Scaling strategy is explained

## Scalability and Performance
- [ ] Expected traffic/load is documented
- [ ] Scaling strategy is defined (horizontal, vertical, hybrid)
- [ ] Bottlenecks have been identified
- [ ] Caching strategy is addressed (if needed)
- [ ] Database query performance is considered
- [ ] Network latency impact is considered

## Reliability and Fault Tolerance
- [ ] Redundancy strategy is defined
- [ ] Failure modes are considered (service down, database down, network partition)
- [ ] Graceful degradation is designed
- [ ] Monitoring and alerting strategy is defined
- [ ] Disaster recovery plan exists
- [ ] Health checks and circuit breakers are planned

## Data Management
- [ ] Data schema is designed or referenced
- [ ] Data consistency model is defined (strong vs eventual consistency)
- [ ] Backup and recovery strategy exists
- [ ] Data retention policy is documented
- [ ] Privacy and security requirements are addressed
- [ ] Database migration strategy exists (if needed)

## Security
- [ ] Authentication strategy is defined
- [ ] Authorization strategy is defined
- [ ] Data encryption is planned (in transit and at rest)
- [ ] API authentication mechanism is chosen (JWT, OAuth, mTLS)
- [ ] Secrets management strategy exists
- [ ] CORS, CSRF, and other web security concerns are addressed

## Operational Concerns
- [ ] Deployment strategy is defined
- [ ] Rollback strategy is documented
- [ ] Logging and monitoring are planned
- [ ] Metrics to track are identified
- [ ] Alerting thresholds are defined
- [ ] On-call runbooks will be created

## Testing Strategy
- [ ] Unit test approach is defined
- [ ] Integration test approach is defined
- [ ] Load/stress test approach is defined
- [ ] Chaos testing is considered (optional for MVP)
- [ ] Test environment setup is planned

## Development and Team
- [ ] Team structure aligns with architecture (Conway's Law)
- [ ] Development environment setup is documented
- [ ] Onboarding process is defined
- [ ] Code review process is established
- [ ] Branching strategy is defined

## Documentation
- [ ] Architecture Decision Record (ADR) is written
- [ ] Diagram is included (ASCII, Mermaid, or image)
- [ ] API contracts are documented (OpenAPI/Swagger)
- [ ] Data model is documented
- [ ] Deployment instructions are documented
- [ ] Runbooks for common operations exist

## Cost and Resource Estimation
- [ ] Infrastructure costs are estimated
- [ ] Development effort is estimated
- [ ] Timeline is realistic
- [ ] Team capacity is planned
- [ ] Contingency buffer is included

## Alternatives and Tradeoffs
- [ ] At least 2 alternatives were evaluated
- [ ] Tradeoffs are documented (complexity vs performance vs cost)
- [ ] Rejected options are explained and filed away
- [ ] Reversibility is considered (can we change our mind later?)

## Sign-off and Communication
- [ ] Stakeholders have reviewed and approved
- [ ] Team understands the rationale
- [ ] Risks are clearly communicated
- [ ] Open questions are tracked
- [ ] Architecture review meeting scheduled (if needed)

---

## After Approval

- [ ] Architecture diagram is shared with team
- [ ] ADR is published in the repository
- [ ] Implementation tickets are created
- [ ] Team begins implementation
- [ ] Regular checkpoints on progress
- [ ] Post-mortem if architecture changes needed

---

## Red Flags

Watch for these warning signs:

🚩 **Unclear requirements**: "We'll figure it out as we go"
🚩 **No tradeoff analysis**: "This is obviously the best choice"
🚩 **Technology-first**: "Let's use Kubernetes because it's cool"
🚩 **Gold-plating**: Designing for 10x scale before proving value at 1x
🚩 **No failure plan**: "This will never fail"
🚩 **Team misalignment**: "The architecture team decided..."
🚩 **No rollback plan**: "We'll never need to revert"

If you see any of these, pause and reassess before proceeding.
