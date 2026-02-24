import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User, KeyRound } from 'lucide-react';

export const Login = () => {
    const { login, isAuthenticated, isLoading, error } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await login(username, password);
    };

    return (
        <div className="min-h-screen bg-dark-bg flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-dark-card rounded-2xl border border-dark-border p-8 shadow-2xl relative overflow-hidden">

                {/* Decorative Grid Background */}
                <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoMjU1LDI1NSwyNTUsMC4wNSkiLz48L3N2Zz4=')] opacity-20" />

                <div className="relative z-10 flex flex-col items-center">
                    <div className="w-20 h-20 bg-primary-600/20 rounded-full flex items-center justify-center mb-6 shadow-[0_0_30px_rgba(59,130,246,0.2)]">
                        <Shield className="w-10 h-10 text-primary-500" />
                    </div>

                    <h1 className="text-3xl font-bold text-white mb-2 tracking-wide">SENTINEL</h1>
                    <p className="text-gray-400 mb-6 text-center">Distributed Security Information & Event Management</p>

                    <form onSubmit={handleSubmit} className="w-full space-y-4">
                        <div className="relative">
                            <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500" />
                            <input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Username"
                                className="w-full bg-dark-bg border border-dark-border rounded-lg py-3 pl-11 pr-4 text-white placeholder-gray-500 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                                autoComplete="username"
                                required
                            />
                        </div>

                        <div className="relative">
                            <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500" />
                            <input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Password"
                                className="w-full bg-dark-bg border border-dark-border rounded-lg py-3 pl-11 pr-4 text-white placeholder-gray-500 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                                autoComplete="current-password"
                                required
                            />
                        </div>

                        {error && (
                            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3 text-red-400 text-sm text-center">
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full relative group bg-primary-600 hover:bg-primary-500 text-white font-semibold py-3 px-4 rounded-lg transition-all duration-200 overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <div className="absolute inset-0 w-full h-full bg-white/20 -translate-x-full group-hover:animate-shimmer" />
                            <div className="flex items-center justify-center space-x-2 relative z-10">
                                {isLoading ? (
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                                ) : (
                                    <>
                                        <Lock className="w-5 h-5" />
                                        <span>Authenticate Securely</span>
                                    </>
                                )}
                            </div>
                        </button>
                    </form>

                    <p className="mt-6 text-xs text-center text-gray-500">
                        Default credentials: admin/admin or analyst/analyst<br />
                        Access requires valid JWT Authorization.
                    </p>
                </div>
            </div>
        </div>
    );
};
