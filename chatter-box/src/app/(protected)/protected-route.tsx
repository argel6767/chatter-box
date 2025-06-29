'use client';

import {useEffect, useState, ReactNode, useMemo, createContext, useContext} from 'react';
import { useRouter } from 'next/navigation';
import { checkCookie } from '@/api/auths';
import { isFailedResponse } from '@/lib/utils';

interface ProtectedContextType {
    isAuthorized:boolean,
    setIsAuthorized: React.Dispatch<React.SetStateAction<boolean>>,
    isAuthLoading:boolean,
    setIsAuthLoading: React.Dispatch<React.SetStateAction<boolean>>,
}

const ProtectedContext = createContext<ProtectedContextType | undefined>(undefined);

interface ProtectedRouteProps {
  children: ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const [isAuthorized, setIsAuthorized] = useState<boolean>(false);
  const [isAuthLoading, setIsAuthLoading] = useState<boolean>(true);
  const router = useRouter();

  const contextValues = useMemo(() => ({
      isAuthorized, setIsAuthorized, isAuthLoading, setIsAuthLoading
  }), [isAuthorized, isAuthLoading])

  useEffect(() => {
    // Check for token immediately on component mount
    const checkCookieStatus = async () => {
        const cookieStatus = await checkCookie();
        if (!isFailedResponse(cookieStatus)) {
            setIsAuthorized(true);
            setIsAuthLoading(false);
        }
        else {
            setIsAuthorized(false);
            setIsAuthLoading(false);
            router.replace('/auth');
        }
    }

    checkCookieStatus()
  }, [router]);

  // display this when checking user's authentication status
  if (!isAuthorized || isAuthLoading) {
    return (
        <main className='min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900'>
        </main>
    );
  }

  // Only render children when authorized
  return <ProtectedContext.Provider value={contextValues}>{children}</ProtectedContext.Provider>;
}

export const useProtectedContext = (): ProtectedContextType => {
    const context = useContext(ProtectedContext);
    if (!context) {
        throw new Error("useProtectedContext must be used within ProtectedContext")
    }
    return context;
}