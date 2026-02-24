import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Activity, FileText, LogOut } from 'lucide-react';

export const MainLayout = () => {
    const { isAuthenticated, logout, userId } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return (
        <div className="min-h-screen bg-dark-bg text-gray-100 flex flex-col md:flex-row">
            {/* Sidebar */}
            <aside className="w-full md:w-64 bg-dark-card border-r border-dark-border flex flex-col">
                <div className="p-6 border-b border-dark-border flex items-center space-x-3">
                    <Shield className="w-8 h-8 text-primary-500" />
                    <h1 className="text-xl font-bold tracking-wider text-white">SENTINEL</h1>
                </div>

                <nav className="flex-1 p-4 space-y-2">
                    <a href="/" className="flex items-center space-x-3 w-full p-3 rounded-lg bg-primary-600/10 text-primary-500 hover:bg-primary-600/20 transition-colors">
                        <Activity className="w-5 h-5" />
                        <span className="font-medium">Dashboard</span>
                    </a>
                    <a href="/investigation" className="flex items-center space-x-3 w-full p-3 rounded-lg text-gray-400 hover:bg-dark-border hover:text-white transition-colors">
                        <Shield className="w-5 h-5" />
                        <span className="font-medium">Investigation</span>
                    </a>
                    <a href="/drafts" className="flex items-center space-x-3 w-full p-3 rounded-lg text-gray-400 hover:bg-dark-border hover:text-white transition-colors">
                        <FileText className="w-5 h-5" />
                        <span className="font-medium">Session Drafts</span>
                    </a>
                </nav>

                <div className="p-4 border-t border-dark-border">
                    <div className="text-xs text-gray-500 mb-4 truncate" title={userId || ''}>
                        User: {userId}
                    </div>
                    <button
                        onClick={logout}
                        className="flex items-center space-x-2 text-gray-400 hover:text-red-400 transition-colors w-full"
                    >
                        <LogOut className="w-5 h-5" />
                        <span>Sign Out</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 p-8 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    );
};
