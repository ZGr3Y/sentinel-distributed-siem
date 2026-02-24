import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Save, DownloadCloud, AlertCircle } from 'lucide-react';

interface DraftState {
    id?: string;
    userId?: string;
    draftPayload: string;
    updatedAt?: string;
}

export const SessionDrafts = () => {
    const [draftPayload, setDraftPayload] = useState<string>('{\n  "query": "SELECT * FROM raw_events LIMIT 10",\n  "notes": "Investigating recent DOS spike..."\n}');
    const [lastSaved, setLastSaved] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Attempt to load existing draft on mount
        const loadDraft = async () => {
            try {
                const response = await api.get<DraftState | { message: string }>('/api/draft');
                if ('draftPayload' in response.data) {
                    setDraftPayload(response.data.draftPayload);
                    setLastSaved(new Date(response.data.updatedAt!).toLocaleString());
                }
            } catch (err) {
                console.error("No draft found or failed to load");
            }
        };
        loadDraft();
    }, []);

    const handleSave = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post<DraftState>('/api/draft', {
                payload: draftPayload
            });
            setLastSaved(new Date(response.data.updatedAt!).toLocaleString());
        } catch (err) {
            setError("Failed to persist draft to PostgreSQL via Sentinel Core.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-white tracking-wide">SESSION DRAFTS</h2>
                <p className="text-gray-400 text-sm">Persist working JSON payloads or investigation notes directly to the backend database using your JWT identity.</p>
            </div>

            <div className="bg-dark-card border border-dark-border rounded-xl shadow-lg flex flex-col h-[600px]">
                {/* Toolbar */}
                <div className="p-4 border-b border-dark-border flex items-center justify-between bg-dark-bg/50">
                    <div className="flex items-center space-x-2 text-sm text-gray-400">
                        <DownloadCloud className="w-4 h-4 text-primary-500" />
                        <span>State synchronized to Postgres</span>
                    </div>

                    {lastSaved && (
                        <div className="text-xs text-green-400 font-mono">
                            Last Saved: {lastSaved}
                        </div>
                    )}
                </div>

                {/* Editor Area */}
                <div className="flex-1 p-4 bg-dark-bg">
                    <textarea
                        className="w-full h-full bg-transparent text-gray-300 font-mono text-sm resize-none focus:outline-none"
                        value={draftPayload}
                        onChange={(e) => setDraftPayload(e.target.value)}
                        spellCheck={false}
                    />
                </div>

                {/* Footer actions */}
                <div className="p-4 border-t border-dark-border bg-dark-bg/50 flex items-center justify-between">
                    <div className="flex-1">
                        {error && (
                            <div className="text-red-400 text-xs flex items-center space-x-1">
                                <AlertCircle className="w-4 h-4" />
                                <span>{error}</span>
                            </div>
                        )}
                    </div>

                    <button
                        onClick={handleSave}
                        disabled={loading}
                        className="bg-primary-600 hover:bg-primary-500 text-white px-6 py-2 rounded-lg font-medium flex items-center space-x-2 transition-colors disabled:opacity-50"
                    >
                        {loading ? (
                            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                        ) : (
                            <>
                                <Save className="w-4 h-4" />
                                <span>Persist State</span>
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};
