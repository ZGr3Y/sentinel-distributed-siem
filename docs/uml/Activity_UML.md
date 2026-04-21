# Sentinel SIEM - Activity Diagram

This diagram reflects the implemented event flow across Agent and Core,
including asynchronous analytics and manual ACK/NACK handling.

```mermaid
flowchart TD
    Start((Start)) --> AgentMode{Agent mode}

    %% Replay mode path
    AgentMode -- replay --> ReadLog[Read NASA log line]
    ReadLog --> ParseLine[Parse line with regex]
    ParseLine --> IsValid{Valid line?}
    IsValid -- No --> ReadLog
    IsValid -- Yes --> CalcDelta[Compute time delta]
    CalcDelta --> SleepReplay{Delta > 0?}
    SleepReplay -- Yes --> WaitReplay[Thread.sleep(delta/speedup)]
    SleepReplay -- No --> SetReplayId
    WaitReplay --> SetReplayId[Set eventId UUID]
    SetReplayId --> ShiftNow[Shift timestamp to now]
    ShiftNow --> PublishEvent

    %% Generate mode path
    AgentMode -- generate --> BuildSynthetic[Generate synthetic event]
    BuildSynthetic --> PublishEvent[Publish EventDTO to RabbitMQ]

    %% Core processing
    PublishEvent --> ConsumeEvent[Core consumes event]
    ConsumeEvent --> MapEntity[Map DTO to RawEvent]
    MapEntity --> ClassifySeverity[Classify severity]
    ClassifySeverity --> SaveRawEvent[Persist raw event]

    SaveRawEvent --> DuplicateCheck{Unique constraint violation?}
    DuplicateCheck -- Yes --> AckDuplicate[ACK duplicate message]
    AckDuplicate --> End((End))

    DuplicateCheck -- No --> RunAsync[Run analytics asynchronously]
    RunAsync --> EvaluateRules[Evaluate DOS / Brute Force / Pattern rules]
    EvaluateRules --> NeedAlert{Alert condition met?}
    NeedAlert -- Yes --> SaveAlert[Persist alert]
    NeedAlert -- No --> AckSuccess
    SaveAlert --> AckSuccess[ACK message]
    AckSuccess --> End

    %% Error paths
    MapEntity -. Exception .-> NackError[NACK with requeue]
    RunAsync -. Analytics exception .-> NackError
    NackError --> End
```
