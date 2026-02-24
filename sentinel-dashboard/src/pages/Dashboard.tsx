import { useEffect, useState } from 'react';
import { api } from '../services/api';
import type { DashboardSummary } from '../types/api.types';
import { Database, AlertTriangle, ShieldAlert, Skull, Bug, CheckCircle, XCircle } from 'lucide-react';

export const Dashboard = () => {
    const [data, setData] = useState<DashboardSummary | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await api.get<DashboardSummary>('/api/dashboard/summary');
                setData(response.data);
                setError(null);
            } catch (err) {
                console.error('Failed to fetch dashboard data', err);
                setError('Failed to connect to Sentinel API. Is the backend running on port 8083?');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
        const intervalId = setInterval(fetchData, 5000);
        return () => clearInterval(intervalId);
    }, []);

    if (loading && !data) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="w-8 h-8 border-4 border-primary-500/30 border-t-primary-500 rounded-full animate-spin" />
            </div>
        );
    }

    if (error && !data) {
        return (
            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-6 text-red-400">
                <p className="font-bold mb-1">Connection Error</p>
                <p className="text-sm">{error}</p>
            </div>
        );
    }

    if (!data) return null;

    const cards = [
        { label: 'Total Events', value: data.totalEvents.toLocaleString(), icon: Database, color: 'text-blue-400' },
        { label: 'Total Alerts', value: data.totalAlerts.toLocaleString(), icon: AlertTriangle, color: 'text-yellow-400' },
        { label: 'DOS Attacks', value: data.dosAttacks.toLocaleString(), icon: ShieldAlert, color: 'text-orange-400' },
        { label: 'Brute Force', value: data.bruteForceAttacks.toLocaleString(), icon: Skull, color: 'text-red-400' },
    ];

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-white tracking-wide">SYSTEM OVERVIEW</h2>
                <p className="text-gray-400 text-sm">Real-time statistics from the Sentinel Distributed SIEM. Auto-refreshes every 5 seconds.</p>
            </div>

            {/* System Health */}
            {data.systemHealth && (
                <div className="flex items-center space-x-4">
                    {Object.entries(data.systemHealth).map(([key, status]) => (
                        <div key={key} className="flex items-center space-x-2 bg-dark-card border border-dark-border rounded-lg px-4 py-2">
                            {status === 'UP' ? (
                                <CheckCircle className="w-4 h-4 text-green-500" />
                            ) : (
                                <XCircle className="w-4 h-4 text-red-500" />
                            )}
                            <span className="text-sm text-gray-300 capitalize">{key}</span>
                            <span className={`text-xs font-bold uppercase ${status === 'UP' ? 'text-green-400' : 'text-red-400'}`}>{status}</span>
                        </div>
                    ))}
                </div>
            )}

            {/* Metrics Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {cards.map((card) => (
                    <div
                        key={card.label}
                        className="bg-dark-card border border-dark-border p-5 rounded-xl shadow-lg flex items-center justify-between group hover:border-primary-500/50 transition-colors"
                    >
                        <div>
                            <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider mb-1">{card.label}</p>
                            <span className="text-3xl font-bold text-white">{card.value}</span>
                        </div>
                        <card.icon className={`w-10 h-10 ${card.color} opacity-40 group-hover:opacity-80 transition-opacity`} />
                    </div>
                ))}
            </div>

            {/* Threat Distribution */}
            <div className="bg-dark-card border border-dark-border rounded-xl shadow-lg p-6">
                <div className="flex items-center space-x-2 mb-4">
                    <Bug className="w-5 h-5 text-primary-500" />
                    <h3 className="text-lg font-semibold text-white">Threat Distribution</h3>
                </div>
                <div className="space-y-3">
                    {data.totalAlerts > 0 ? (
                        <>
                            <ThreatBar label="DOS Attacks" count={data.dosAttacks} total={data.totalAlerts} color="bg-orange-500" />
                            <ThreatBar label="Brute Force" count={data.bruteForceAttacks} total={data.totalAlerts} color="bg-red-500" />
                            <ThreatBar label="Other / Pattern Match" count={data.totalAlerts - data.dosAttacks - data.bruteForceAttacks} total={data.totalAlerts} color="bg-purple-500" />
                        </>
                    ) : (
                        <p className="text-gray-500 text-sm">No threats detected yet.</p>
                    )}
                </div>
            </div>

            {/* Latest Alerts */}
            {data.latestAlerts && data.latestAlerts.length > 0 && (
                <div className="bg-dark-card border border-dark-border rounded-xl shadow-lg overflow-hidden">
                    <div className="p-4 border-b border-dark-border">
                        <h3 className="text-lg font-semibold text-white">Latest Alerts</h3>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-gray-300">
                            <thead className="bg-dark-bg/50 text-gray-400 uppercase text-xs">
                                <tr>
                                    <th className="px-4 py-2 font-medium">Type</th>
                                    <th className="px-4 py-2 font-medium">Source IP</th>
                                    <th className="px-4 py-2 font-medium">Description</th>
                                    <th className="px-4 py-2 font-medium">Time</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-dark-border">
                                {data.latestAlerts.map((alert) => (
                                    <tr key={alert.id} className="hover:bg-dark-bg/30">
                                        <td className="px-4 py-2 whitespace-nowrap">
                                            <span className={`px-2 py-0.5 text-[10px] uppercase tracking-wider font-bold rounded-full ${alert.type === 'DOS' ? 'bg-orange-500/10 text-orange-400 border border-orange-500/20' :
                                                    alert.type === 'BRUTE_FORCE' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                                                        'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                                }`}>
                                                {alert.type}
                                            </span>
                                        </td>
                                        <td className="px-4 py-2 font-mono text-sm">{alert.sourceIp}</td>
                                        <td className="px-4 py-2 text-gray-400 max-w-xs truncate">{alert.description}</td>
                                        <td className="px-4 py-2 text-xs text-gray-500 whitespace-nowrap">
                                            {alert.createdAt ? new Date(alert.createdAt).toLocaleString() : '-'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

const ThreatBar = ({ label, count, total, color }: { label: string; count: number; total: number; color: string }) => {
    const pct = total > 0 ? Math.round((count / total) * 100) : 0;
    return (
        <div>
            <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-300">{label}</span>
                <span className="text-gray-400 font-mono">{count.toLocaleString()} ({pct}%)</span>
            </div>
            <div className="w-full bg-dark-border rounded-full h-2">
                <div className={`${color} h-2 rounded-full transition-all duration-500`} style={{ width: `${pct}%` }} />
            </div>
        </div>
    );
};
