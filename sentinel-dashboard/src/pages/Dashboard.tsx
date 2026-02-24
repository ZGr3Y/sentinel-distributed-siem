import { useEffect, useState } from 'react';
import { api } from '../services/api';
import type { DashboardSummary } from '../types/api.types';
import { Activity, Database, Server, AlertTriangle } from 'lucide-react';

export const Dashboard = () => {
    const [data, setData] = useState<DashboardSummary | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await api.get<DashboardSummary>('/api/dashboard/summary');
                setData(response.data);
            } catch (error) {
                console.error('Failed to fetch dashboard data', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
        // Poll every 5 seconds for real-time vibe
        const intervalId = setInterval(fetchData, 5000);
        return () => clearInterval(intervalId);
    }, []);

    if (loading && !data) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="w-8 h-8 border-4 border-primary-500/30 border-t-primary-500 rounded-full animate-spin" />
            </div>
        );
    }

    if (!data) return <div className="text-red-400">Failed to load Dashboard Metrics.</div>;

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-white tracking-wide">SYSTEM OVERVIEW</h2>
                <p className="text-gray-400 text-sm">Real-time statistics and active security alerts.</p>
            </div>

            {/* Top Metrics Row */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                {/* DB Health */}
                <div className="bg-dark-card border border-dark-border p-4 rounded-xl shadow-lg flex items-center justify-between group hover:border-primary-500/50 transition-colors">
                    <div>
                        <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider mb-1">PostgreSQL DB</p>
                        <div className="flex items-center space-x-2">
                            <span className="text-xl font-bold text-white">{data.systemHealth.database}</span>
                            <span className={`w-3 h-3 rounded-full shadow-[0_0_10px] ${data.systemHealth.database === 'UP' ? 'bg-green-500 shadow-green-500/50' : 'bg-red-500 shadow-red-500/50'}`} />
                        </div>
                    </div>
                    <Database className="w-8 h-8 text-gray-600 group-hover:text-primary-400 transition-colors" />
                </div>

                {/* Broker Health */}
                <div className="bg-dark-card border border-dark-border p-4 rounded-xl shadow-lg flex items-center justify-between group hover:border-primary-500/50 transition-colors">
                    <div>
                        <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider mb-1">RabbitMQ Broker</p>
                        <div className="flex items-center space-x-2">
                            <span className="text-xl font-bold text-white">{data.systemHealth.broker}</span>
                            <span className={`w-3 h-3 rounded-full shadow-[0_0_10px] ${data.systemHealth.broker === 'UP' ? 'bg-green-500 shadow-green-500/50' : 'bg-red-500 shadow-red-500/50'}`} />
                        </div>
                    </div>
                    <Server className="w-8 h-8 text-gray-600 group-hover:text-primary-400 transition-colors" />
                </div>

                {/* Total Events */}
                <div className="bg-dark-card border border-dark-border p-4 rounded-xl shadow-lg flex items-center justify-between group hover:border-primary-500/50 transition-colors">
                    <div>
                        <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider mb-1">Events (10m)</p>
                        <span className="text-2xl font-bold text-white">{data.metrics.totalEventsLast10Min.toLocaleString()}</span>
                    </div>
                    <Activity className="w-8 h-8 text-gray-600 group-hover:text-primary-400 transition-colors" />
                </div>

                {/* Rate */}
                <div className="bg-dark-card border border-dark-border p-4 rounded-xl shadow-lg flex items-center justify-between group hover:border-primary-500/50 transition-colors">
                    <div>
                        <p className="text-gray-500 text-xs font-semibold uppercase tracking-wider mb-1">Ingestion Rate</p>
                        <span className="text-2xl font-bold text-white">{data.metrics.eventsPerSecond.toFixed(1)} /s</span>
                    </div>
                    <Activity className="w-8 h-8 text-gray-600 group-hover:text-primary-400 transition-colors" />
                </div>
            </div>

            {/* Alerts Table */}
            <div className="bg-dark-card border border-dark-border rounded-xl shadow-lg overflow-hidden">
                <div className="p-4 border-b border-dark-border flex items-center space-x-2">
                    <AlertTriangle className="w-5 h-5 text-yellow-500" />
                    <h3 className="text-lg font-semibold text-white">Active Threat Feed</h3>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-gray-300">
                        <thead className="bg-dark-bg/50 text-gray-400 uppercase text-xs">
                            <tr>
                                <th className="px-6 py-3 font-medium tracking-wider">Time</th>
                                <th className="px-6 py-3 font-medium tracking-wider">Type</th>
                                <th className="px-6 py-3 font-medium tracking-wider">Source IP</th>
                                <th className="px-6 py-3 font-medium tracking-wider">Details</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-dark-border">
                            {data.latestAlerts.map((alert) => (
                                <tr key={alert.id} className="hover:bg-dark-bg/30 transition-colors">
                                    <td className="px-6 py-4 whitespace-nowrap text-xs">
                                        {new Date(alert.createdAt).toLocaleTimeString()}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 text-[10px] uppercase tracking-wider font-bold rounded-full ${alert.type === 'DOS_ATTACK' ? 'bg-orange-500/10 text-orange-400 border border-orange-500/20' :
                                            alert.type === 'BRUTE_FORCE' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                                                'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                            }`}>
                                            {alert.type}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap font-mono text-primary-400">
                                        {alert.sourceIp}
                                    </td>
                                    <td className="px-6 py-4">
                                        {alert.description}
                                    </td>
                                </tr>
                            ))}

                            {data.latestAlerts.length === 0 && (
                                <tr>
                                    <td colSpan={4} className="px-6 py-8 text-center text-gray-500">
                                        No active threats detected in the current window.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};
