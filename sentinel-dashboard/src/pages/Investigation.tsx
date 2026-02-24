import { useState } from 'react';
import { api } from '../services/api';
import type { BatchQueryResponse, Alert } from '../types/api.types';
import { Search, AlertCircle, ShieldCheck } from 'lucide-react';

export const Investigation = () => {
    const [ipList, setIpList] = useState<string>('');
    const [result, setResult] = useState<BatchQueryResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleAnalyze = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const ips = ipList.split('\n').map(ip => ip.trim()).filter(ip => ip !== '');

        if (ips.length === 0) {
            setError("Please enter at least one IP address.");
            setLoading(false);
            return;
        }

        try {
            // Backend expects field "ipsToInvestigate"
            const response = await api.post<BatchQueryResponse>('/api/investigation/batch', {
                ipsToInvestigate: ips
            });
            setResult(response.data);
        } catch (err) {
            setError("Failed to communicate with the Sentinel API.");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-white tracking-wide">BATCH INVESTIGATION</h2>
                <p className="text-gray-400 text-sm">Query alerts history for specific IP addresses in a single batch request.</p>
            </div>

            <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-lg">
                <form onSubmit={handleAnalyze} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-400 mb-2">
                            Target IP Addresses (one per line)
                        </label>
                        <textarea
                            className="w-full h-40 bg-dark-bg border border-dark-border rounded-lg p-4 text-primary-400 font-mono text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all resize-none"
                            placeholder={"192.168.1.100\n10.0.0.51\n172.16.0.1"}
                            value={ipList}
                            onChange={(e) => setIpList(e.target.value)}
                        />
                    </div>

                    {error && (
                        <div className="text-red-400 text-sm flex items-center space-x-2 bg-red-500/10 p-3 rounded-lg border border-red-500/20">
                            <AlertCircle className="w-4 h-4 shrink-0" />
                            <span>{error}</span>
                        </div>
                    )}

                    <div className="flex justify-end">
                        <button
                            type="submit"
                            disabled={loading}
                            className="bg-primary-600 hover:bg-primary-500 text-white px-6 py-2 rounded-lg font-medium flex items-center space-x-2 transition-colors disabled:opacity-50"
                        >
                            {loading ? (
                                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                            ) : (
                                <>
                                    <Search className="w-4 h-4" />
                                    <span>Execute Query</span>
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>

            {result && (
                <div className="space-y-4">
                    <h3 className="text-lg font-semibold text-white border-b border-dark-border pb-2">
                        Results — {result.totalIpsQueried} IP(s) queried
                    </h3>

                    {Object.entries(result.ipAlertsMap).map(([ip, alerts]) => (
                        <IpResultCard key={ip} ip={ip} alerts={alerts} />
                    ))}
                </div>
            )}
        </div>
    );
};

const IpResultCard = ({ ip, alerts }: { ip: string; alerts: Alert[] }) => {
    const hasAlerts = alerts.length > 0;

    return (
        <div className={`bg-dark-card border rounded-xl overflow-hidden transition-all ${hasAlerts ? 'border-red-500/30' : 'border-dark-border'}`}>
            <div className="p-4 flex items-center justify-between bg-dark-bg/50">
                <div className="flex items-center space-x-3">
                    {hasAlerts ? (
                        <AlertCircle className="w-5 h-5 text-red-500" />
                    ) : (
                        <ShieldCheck className="w-5 h-5 text-green-500" />
                    )}
                    <span className="font-mono font-bold text-white text-lg">{ip}</span>
                </div>
                <div className={`px-3 py-1 text-xs font-bold rounded-full uppercase tracking-wider ${hasAlerts ? 'bg-red-500/20 text-red-400' : 'bg-green-500/20 text-green-400'}`}>
                    {hasAlerts ? `${alerts.length} Alert(s)` : 'Clean'}
                </div>
            </div>

            {hasAlerts && (
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-gray-300">
                        <thead className="bg-dark-bg/50 text-gray-400 uppercase text-xs">
                            <tr>
                                <th className="px-4 py-2 font-medium">Type</th>
                                <th className="px-4 py-2 font-medium">Description</th>
                                <th className="px-4 py-2 font-medium">Time</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-dark-border">
                            {alerts.map((alert) => (
                                <tr key={alert.id} className="hover:bg-dark-bg/30">
                                    <td className="px-4 py-2 whitespace-nowrap">
                                        <span className={`px-2 py-0.5 text-[10px] uppercase tracking-wider font-bold rounded-full ${alert.type === 'DOS' ? 'bg-orange-500/10 text-orange-400 border border-orange-500/20' :
                                                alert.type === 'BRUTE_FORCE' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                                                    'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                            }`}>
                                            {alert.type}
                                        </span>
                                    </td>
                                    <td className="px-4 py-2 text-gray-400">{alert.description}</td>
                                    <td className="px-4 py-2 text-xs text-gray-500 whitespace-nowrap">
                                        {new Date(alert.createdAt).toLocaleString()}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};
