export interface ThreatIntelligence {
    knownBots: boolean;
    maliciousReputation: boolean;
}

export interface IpRiskReport {
    ipAddress: string;
    totalRequests: number;
    recentAlertsCount: number;
    threatIntelligence: ThreatIntelligence;
}
