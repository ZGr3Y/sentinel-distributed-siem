import { createContext } from 'react';

export interface AuthContextType {
    token: string | null;
    userId: string | null;
    role: string | null;
    login: (username: string, password: string) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
