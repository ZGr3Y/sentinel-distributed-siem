import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode, FC } from 'react';
import { api } from '../services/api';

interface AuthContextType {
    token: string | null;
    userId: string | null;
    role: string | null;
    login: (username: string, password: string) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: FC<{ children: ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('sentinel_jwt'));
    const [userId, setUserId] = useState<string | null>(localStorage.getItem('sentinel_userId'));
    const [role, setRole] = useState<string | null>(localStorage.getItem('sentinel_role'));
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (token) localStorage.setItem('sentinel_jwt', token);
        else localStorage.removeItem('sentinel_jwt');
    }, [token]);

    useEffect(() => {
        if (userId) localStorage.setItem('sentinel_userId', userId);
        else localStorage.removeItem('sentinel_userId');
    }, [userId]);

    useEffect(() => {
        if (role) localStorage.setItem('sentinel_role', role);
        else localStorage.removeItem('sentinel_role');
    }, [role]);

    const login = async (username: string, password: string) => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await api.post('/auth/login', { username, password });
            const data = response.data;
            setToken(data.token);
            setUserId(data.userId);
            setRole(data.role);
        } catch (err: unknown) {
            const axiosErr = err as { response?: { data?: { error?: string } } };
            setError(axiosErr.response?.data?.error || 'Authentication failed.');
        } finally {
            setIsLoading(false);
        }
    };

    const logout = () => {
        setToken(null);
        setUserId(null);
        setRole(null);
        setError(null);
    };

    return (
        <AuthContext.Provider value={{
            token, userId, role, login, logout,
            isAuthenticated: !!token,
            isLoading, error
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
