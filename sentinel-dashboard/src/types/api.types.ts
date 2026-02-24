// Matches the enriched DashboardSummaryDTO.java
export interface DashboardSummary {
    systemHealth: Record<string, string>;
    totalEvents: number;
    totalAlerts: number;
    dosAttacks: number;
    bruteForceAttacks: number;
    latestAlerts: AlertDTO[];
}

export interface AlertDTO {
    id: string;
    type: string;
    sourceIp: string;
    description: string;
    createdAt: string;
}

// Matches Alert.java entity
export interface Alert {
    id: string;
    type: string;
    sourceIp: string;
    description: string;
    createdAt: string;
}

// Matches BatchQueryResponse.java
export interface BatchQueryResponse {
    ipAlertsMap: Record<string, Alert[]>;
    totalIpsQueried: number;
}

// Matches DraftState.java entity
export interface DraftState {
    id: string;
    userId: string;
    draftPayload: string;
    updatedAt: string;
}
