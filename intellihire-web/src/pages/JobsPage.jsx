import React, { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import "../App.css";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export default function JobsPage() {
    const { saved: savedApi } = useAuth();       // ← use local saved store
    const saved = savedApi.list;

    const [page, setPage] = useState(1);
    const [q, setQ] = useState("Java Developer");
    const [location, setLocation] = useState("Montreal");
    const [remote, setRemote] = useState(true);
    const [rank, setRank] = useState(true);
    const [limit, setLimit] = useState(10);
    const [skills, setSkills] = useState("java,spring,aws");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [jobs, setJobs] = useState([]);

    const [showSaved, setShowSaved] = useState(false);
    const [savingId, setSavingId] = useState("");

    const queryString = useMemo(() => {
        const p = new URLSearchParams({
            q,
            location,
            remote: String(remote),
            rank: String(rank),
            limit: String(limit),
            page: String(page),
            skills: skills || "",
        });
        return p.toString();
    }, [q, location, remote, rank, limit, page, skills]);

    async function runSearch() {
        setLoading(true);
        setError("");
        try {
            const res = await fetch(`${API_BASE}/api/jobs?${queryString}`);
            if (!res.ok) {
                const msg =
                    res.status === 403 ? "Forbidden (403): Check your RapidAPI key/host in backend."
                        : res.status === 429 ? "Rate limited (429): Slow down or upgrade your RapidAPI plan."
                            : `HTTP ${res.status}`;
                throw new Error(msg);
            }
            const data = await res.json();
            setJobs(Array.isArray(data) ? data : []);
        } catch (e) {
            setError(e.message || "Search failed");
            setJobs([]);
        } finally {
            setLoading(false);
        }
    }

    function saveJobLocal(job) {
        setSavingId(job.applyUrl ?? job.title);
        // minimal projection we keep locally (id is generated in savedStore)
        savedApi.add({
            title: job.title,
            company: job.company,
            location: job.location,
            remote: !!job.remote,
            salaryMin: job.salaryMin ?? null,
            salaryMax: job.salaryMax ?? null,
            currency: job.currency ?? null,
            postedAt: job.postedAt ?? null,
            description: job.description ?? "",
            applyUrl: job.applyUrl ?? null,
            source: job.source ?? "web",
            createdAt: Date.now()
        });
        setSavingId("");
    }

    const deleteSavedLocal = (id) => savedApi.remove(id);
    const clearAllSavedLocal = () => {
        if (!window.confirm("Delete ALL saved jobs? This cannot be undone.")) return;
        savedApi.clear();
    };

    useEffect(() => { runSearch(); }, [queryString]);        // eslint-disable-line
    useEffect(() => { if (page !== 1) runSearch(); }, [page]);
    useEffect(() => { setPage(1); }, [q, location, skills, remote, rank, limit]);

    return (
        <div className="container">
            <header className="header">
                <h2>Job Search</h2>
                <p className="sub">Search curated jobs via your Spring Boot API.</p>
            </header>

            {/* Controls */}
            <div className="toolbar">
                <div className="field">
                    <label className="label" htmlFor="role">Role</label>
                    <input id="role" className="input" value={q}
                           onChange={(e) => setQ(e.target.value)} placeholder="e.g., Java Developer" />
                </div>

                <div className="field">
                    <label className="label" htmlFor="location">Location</label>
                    <input id="location" className="input" value={location}
                           onChange={(e) => setLocation(e.target.value)} placeholder="e.g., Montreal" />
                </div>

                <div className="field">
                    <label className="label" htmlFor="skills">Skills (for ranking)</label>
                    <input id="skills" className="input" value={skills}
                           onChange={(e) => setSkills(e.target.value)} placeholder="java,spring,aws" />
                </div>

                <label className="check">
                    <input type="checkbox" checked={remote}
                           onChange={(e) => setRemote(e.target.checked)} /> Remote
                </label>

                <label className="check">
                    <input type="checkbox" checked={rank}
                           onChange={(e) => setRank(e.target.checked)} /> Rank
                </label>

                <select className="select" value={limit} onChange={(e) => setLimit(Number(e.target.value))}>
                    {[5, 10, 15, 20].map(n => <option key={n} value={n}>{n}</option>)}
                </select>

                <button onClick={() => setShowSaved(s => !s)} className="btn btn-secondary">
                    {showSaved ? "← Back to Search" : "Saved Jobs"}
                </button>
            </div>

            {/* Search view */}
            {!showSaved && (
                <>
                    <div className="topbar">
                        <button
                            type="button"
                            className="btn btn-primary"
                            onClick={() => { if (page !== 1) setPage(1); else runSearch(); }}
                            disabled={loading}
                        >
                            {loading ? "Searching..." : "Search"}
                        </button>

                        <small className="badge">Backend: {API_BASE}</small>

                        <div className="spacer">
                            <button className="btn btn-primary"
                                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                                    disabled={loading || page === 1}>
                                ◀ Prev
                            </button>
                            <div className="chip">Page {page}</div>
                            <button className="btn btn-primary"
                                    onClick={() => setPage((p) => p + 1)}
                                    disabled={loading}>
                                Next ▶
                            </button>
                        </div>
                    </div>

                    {error && <div className="error">{error}</div>}

                    <div className="stack">
                        {jobs.length === 0 && !loading && !error &&
                            <div className="card">No jobs found. Try changing your query.</div>}

                        {jobs.map((j, i) => (
                            <article key={i} className="card">
                                <div className="card__row">
                                    <h3 className="card__title">{j.title || "Untitled role"}</h3>
                                    <span className="card__muted">{j.location || "—"}</span>
                                </div>
                                <div className="card__muted" style={{ marginTop: 4 }}>
                                    <strong>{j.company || "Unknown company"}</strong>{j.remote ? " • Remote" : ""}
                                </div>
                                {j.description && <p>{truncate(j.description, 420)}</p>}
                                <div className="actions">
                                    {j.applyUrl &&
                                        <a href={j.applyUrl} target="_blank" rel="noreferrer" className="btn btn-primary">Apply</a>}
                                    <button className="btn btn-success"
                                            onClick={() => saveJobLocal(j)}
                                            disabled={!!savingId && savingId === (j.applyUrl ?? j.title)}>
                                        {savingId === (j.applyUrl ?? j.title) ? "Saving..." : "Save"}
                                    </button>
                                    <span className="badge">
                    {j.postedAt ? new Date(j.postedAt).toLocaleDateString() : ""}
                  </span>
                                </div>
                            </article>
                        ))}
                    </div>
                </>
            )}

            {/* Saved view */}
            {showSaved && (
                <>
                    <div className="savedbar">
                        {/* CSV export is a backend route; hide if you’re purely local */}
                        {/* <a className="btn btn-primary" href={`${API_BASE}/api/saved/export.csv`}>⬇ Download CSV</a> */}
                        <button className="btn btn-danger" onClick={clearAllSavedLocal}>🗑 Delete All</button>
                        <small className="badge">{saved.length} saved {saved.length === 1 ? "job" : "jobs"}</small>
                    </div>

                    <div className="stack">
                        {saved.length === 0 && <div className="card">No saved jobs yet.</div>}
                        {saved.map(job => (
                            <article key={job.id} className="card">
                                <div className="card__row">
                                    <h3 className="card__title">{job.title}</h3>
                                    <span className="card__muted">{job.location || "—"}</span>
                                </div>
                                <div className="card__muted" style={{ marginTop: 4 }}>
                                    <strong>{job.company || "Unknown company"}</strong>{job.remote ? " • Remote" : ""}
                                </div>
                                {job.description && <p>{truncate(job.description, 420)}</p>}
                                <div className="actions">
                                    {job.applyUrl &&
                                        <a className="btn btn-primary" href={job.applyUrl} target="_blank" rel="noreferrer">Apply</a>}
                                    <button className="btn btn-danger" onClick={() => deleteSavedLocal(job.id)}>Delete</button>
                                    <span className="badge">saved {job.createdAt ? new Date(job.createdAt).toLocaleString() : ""}</span>
                                </div>
                            </article>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}

function truncate(s, n) { return !s ? "" : (s.length > n ? s.slice(0, n) + "…" : s); }
