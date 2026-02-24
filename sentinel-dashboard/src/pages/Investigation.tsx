import { useState } from 'react';
import { api } from '../services/api';
import type { IpRiskReport } from '../types/investigation.types';
import { Search, AlertCircle, ShieldCheck } from 'lucide-react';

export const Investigation = () => {
    const [ipList, setIpList] = useState<string>('');
    const [reports, setReports] = useState<IpRiskReport[] | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleAnalyze = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const ips = ipList.split('\n').map(ip => ip.trim()).filter(ip => ip !== '');

        if (ips.length === 0) {
            setError("Please isolate at least one IPv4/IPv6 address.");
            setLoading(false);
            return;
        }

        try {
            const response = await api.post<IpRiskReport[]>('/api/investigation/batch', {
                ipAddresses: ips
            });
            setReports(response.data);
        } catch (err) {
            setError("Failed to communicate with Core Analysis Engine.");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-white tracking-wide">BATCH INVESTIGATION</h2>
                <p className="text-gray-400 text-sm">Query the datalake for historical telemetry and threat intelligence on specific nodes.</p>
            </div>

            <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-lg">
                <form onSubmit={handleAnalyze} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-400 mb-2">
                            Target IP Addresses (One per line)
                        </label>
                        <textarea
                            className="w-full h-40 bg-dark-bg border border-dark-border rounded-lg p-4 text-primary-400 font-mono text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all resize-none"
                            placeholder="192.168.1.100&#10;10.0.0.51"
                            value={ipList}
                            onChange={(e) => setIpList(e.target.value)}
                        />
                    </div>

                    {error && (
                        <div className="text-red-400 text-sm flex items-center space-x-2 bg-red-500/10 p-3 rounded-lg border border-red-500/20">
                            <AlertCircle className="w-4 h-4" />
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

            {reports && (
                <div className="space-y-4">
                    <h3 className="text-lg font-semibold text-white border-b border-dark-border pb-2">Analysis Results</h3>
                    {reports.map((report, idx) => {
                        const isHighRisk = report.threatIntelligence.maliciousReputation || report.recentAlertsCount > 4;

                        return (
                            <div key={idx} className={`bg-dark-card border rounded-xl overflow-hidden transition-all ${isHighRisk ? 'border-red-500/30' : 'border-dark-border'}`}>
                                <div className="p-4 flex items-center justify-between bg-dark-bg/50">
                                    <div className="flex items-center space-x-3">
                                        {isHighRisk ? (
                                            <AlertCircle className="w-5 h-5 text-red-500" />
                                        ) : (
                                            <ShieldCheck className="w-5 h-5 text-green-500" />
                                        )}
                                        <span className="font-mono font-bold text-white text-lg">{report.ipAddress}</span>
                                    </div>
                                    <div className={`px-3 py-1 text-xs font-bold rounded-full uppercase tracking-wider ${isHighRisk ? 'bg-red-500/20 text-red-400 text-shadow-glow' : 'bg-green-500/20 text-green-400 text-shadow-glow'}`}>
                                        {isHighRisk ? 'Critical Risk' : 'Low Risk'}
                                    </div>
                                </div>

                                <div className="p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div className="bg-dark-bg p-3 rounded-lg border border-dark-border">
                                        <p className="text-gray-500 text-xs uppercase mb-1">Total Requests</p>
                                        <p className="text-xl font-bold text-gray-200">{report.totalRequests.toLocaleString()}</p>
                                    </div>
                                    <div className="bg-dark-bg p-3 rounded-lg border border-dark-border">
                                        <p className="text-gray-500 text-xs uppercase mb-1">Recent Alerts</p>
                                        <p className={`text-xl font-bold ${report.recentAlertsCount > 0 ? 'text-yellow-500' : 'text-gray-200'}`}>
                                            {report.recentAlertsCount.toLocaleString()}
                                        </p>
                                    </div>
                                    <div className="bg-dark-bg p-3 rounded-lg border border-dark-border">
                                        <p className="text-gray-500 text-xs uppercase mb-1">Intelligence Flags</p>
                                        <div className="flex flex-col space-y-1 mt-1 text-sm">
                                            <span className={report.threatIntelligence.knownBots ? 'text-red-400' : 'text-gray-400'}>
                                                {report.threatIntelligence.knownBots ? '⚠ Known Botnet' : '✓ Clean IP'}
                                            </span>
                                            <span className={report.threatIntelligence.maliciousReputation ? 'text-red-400' : 'text-gray-400'}>
                                                {report.threatIntelligence.maliciousReputation ? '⚠ Malicious Rep.' : '✓ No Flags'}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};
