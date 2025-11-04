import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import MarketTrends from "../components/MarketTrends.jsx";

export default function Landing() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const API = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
    const [busy, setBusy] = useState(false);

    async function handleLogin(e) {
        e.preventDefault();
        if (busy) return;
        setBusy(true);
        try {
            const fd = new FormData(e.currentTarget);
            const email = String(fd.get("email") || "").trim();
            const password = String(fd.get("password") || "");
            if (!email || !password) return alert("Enter both email and password");

            const resp = await fetch(`${API}/api/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            const data = await resp.json().catch(() => ({}));
            if (!resp.ok) throw new Error(data.message || "Login failed");

            login({ ...data, provider: "local" });
            navigate("/jobs");
        } catch (err) {
            alert(err.message);
        } finally {
            setBusy(false);
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        if (busy) return;
        setBusy(true);
        try {
            const fd = new FormData(e.currentTarget);
            const name = String(fd.get("name") || "").trim();
            const email = String(fd.get("email") || "").trim();
            const password = String(fd.get("password") || "");
            if (!name || !email || !password) return alert("Fill all fields");

            const resp = await fetch(`${API}/api/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ name, email, password }),
            });

            const data = await resp.json().catch(() => ({}));
            if (!resp.ok) throw new Error(data.message || "Registration failed");

            login({ ...data, provider: "local" });
            navigate("/jobs");
        } catch (err) {
            alert(err.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className="landing container">
            <div className="landing__grid">
                {/* Left: hero + trends */}
                <section className="landing__left">
                    <div className="hero">
                        <h1 className="hero-title">
                            <span className="aurora-text">IntelliHire</span>
                        </h1>

                        <p className="hero-sub">
                            Discover roles, track trends, and apply smarter.
                        </p>
                        <div className="hero-tags">
                            <span className="tag">Real-time trends</span>
                            <span className="tag">Salary insights</span>
                            <span className="tag">Smart search</span>
                        </div>
                    </div>

                    <div className="mt">
                        <MarketTrends />
                    </div>
                </section>

                {/* Right: auth */}
                <aside className="auth-card glass-card">
                    <h2 className="card-title">Sign in</h2>

                    {/* Email/password login */}
                    <form className="auth-form" onSubmit={handleLogin} autoComplete="on">
                        <label className="field">
                            <span>Email</span>
                            <input
                                type="email"
                                name="email"
                                placeholder="you@concordia.ca"
                                required
                                autoComplete="email"
                            />
                        </label>
                        <label className="field">
                            <span>Password</span>
                            <input
                                type="password"
                                name="password"
                                placeholder="••••••••"
                                required
                                autoComplete="current-password"
                            />
                        </label>
                        <button className="btn btn-primary w-full" type="submit" disabled={busy}>
                            {busy ? "Signing in..." : "Sign in"}
                        </button>
                    </form>

                    <div className="divider"><span>or</span></div>

                    {/* Register */}
                    <h3 className="section-title">Create an account</h3>
                    <form className="auth-form" onSubmit={handleRegister} autoComplete="on">
                        <label className="field">
                            <span>Full name</span>
                            <input name="name" placeholder="e.g., Narendra Mishra" required />
                        </label>
                        <label className="field">
                            <span>Email</span>
                            <input type="email" name="email" placeholder="name@name.com" required />
                        </label>
                        <label className="field">
                            <span>Password</span>
                            <input
                                type="password"
                                name="password"
                                placeholder="••••••••"
                                required
                                autoComplete="new-password"
                            />
                        </label>
                        <button className="btn btn-ghost w-full" type="submit" disabled={busy}>
                            {busy ? "Registering..." : "Register"}
                        </button>
                    </form>
                </aside>
            </div>
        </div>
    );
}
