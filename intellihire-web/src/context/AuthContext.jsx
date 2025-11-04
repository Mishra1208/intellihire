import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { loadSaved, saveSaved, addSaved, removeSaved, clearSaved } from "../lib/savedStore";

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);      // { id, name, email } or null
    const [saved, setSaved] = useState([]);      // current user's saved jobs
    const API = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

    // 1) Try to restore the session on first mount
    useEffect(() => {
        let cancelled = false;
        (async () => {
            try {
                const resp = await fetch(`${API}/api/auth/me`, { credentials: "include" });
                if (!resp.ok) return;
                const u = await resp.json();
                if (!cancelled) setUser(u);
            } catch {
                /* ignore */
            }
        })();
        return () => { cancelled = true; };
    }, [API]);

    // 2) Whenever the user changes, load that user's saved list
    useEffect(() => {
        setSaved(loadSaved(user?.id));  // uses "anon" bucket if user is null
    }, [user?.id]);

    // 3) Auth helpers
    const login = (u) => setUser(u);
    const logout = () => {
        // If you want to wipe this user's bucket on logout, uncomment:
        // clearSaved(user?.id);
        setUser(null);
        setSaved(loadSaved(undefined)); // go back to "anon" bucket
    };

    // 4) Saved-jobs API scoped to the current user
    const savedApi = useMemo(() => ({
        get list() {
            return saved;
        },
        add(job) {
            const next = addSaved(user?.id, job);
            setSaved(next);
        },
        remove(jobId) {
            const next = removeSaved(user?.id, jobId);
            setSaved(next);
        },
        clear() {
            clearSaved(user?.id);
            setSaved([]);
        },
        replace(list) {
            saveSaved(user?.id, list);
            setSaved(list ?? []);
        }
    }), [saved, user?.id]);

    return (
        <AuthCtx.Provider value={{ user, login, logout, saved: savedApi }}>
            {children}
        </AuthCtx.Provider>
    );
}

export function useAuth() {
    return useContext(AuthCtx);
}
