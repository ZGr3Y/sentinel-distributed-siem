import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { api } from '../services/api';

interface AuthContextType {
    token: string | null;
    userId: string | null;
    login: () => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('sentinel_jwt'));
    const [userId, setUserId] = useState<string | null>(localStorage.getItem('sentinel_userId'));
    const [isLoading, setIsLoading] = useState(false);

    // If token changes natively (or across tabs), sync it.
    useEffect(() => {
        if (token) {
            localStorage.setItem('sentinel_jwt', token);
        } else {
            localStorage.removeItem('sentinel_jwt');
        }
    }, [token]);

    useEffect(() => {
        if (userId) {
            localStorage.setItem('sentinel_userId', userId);
        } else {
            localStorage.removeItem('sentinel_userId');
        }
    }, [userId]);

    const login = async () => {
        setIsLoading(true);
        try {
            const response = await api.post('/auth/login');
            const data = response.data;
            setToken(data.token);
            setUserId(data.userId);
        } catch (error) {
            console.error("Login failed", error);
        } finally {
            setIsLoading(false);
        }
    };

    const logout = () => {
        setToken(null);
        setUserId(null);
    };

    return (
        <AuthContext.Provider value={{
            token,
            userId,
            login,
            logout,
            isAuthenticated: !!token,
            isLoading
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
