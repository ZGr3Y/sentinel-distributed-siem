export interface Alert {
    id: string;
    type: string;
    sourceIp: string;
    description: string;
    createdAt: string;
}

export interface SystemHealth {
    database: string;
    broker: string;
}

export interface Metrics {
    totalEventsLast10Min: number;
    eventsPerSecond: number;
}

export interface DashboardSummary {
    systemHealth: SystemHealth;
    metrics: Metrics;
    latestAlerts: Alert[];
}
