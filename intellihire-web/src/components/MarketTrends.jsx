import React, { useEffect, useState } from "react";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, BarChart, Bar
} from "recharts";

// TODO: wire to your backend later
async function fetchTrends() {
    return {
        weekly: [
            { week: "Wk-1", postings: 210 },
            { week: "Wk-2", postings: 235 },
            { week: "Wk-3", postings: 260 },
            { week: "Wk-4", postings: 310 },
            { week: "Now", postings: 345 },
        ],
        topRoles: [
            { role: "AI/ML Engineer", growth: 42 },
            { role: "Data Engineer", growth: 31 },
            { role: "Cloud DevOps", growth: 28 },
            { role: "Backend (Java)", growth: 22 },
            { role: "Security Analyst", growth: 19 },
        ],
        snapshot: { yoy: 18, remoteShare: 37, medianSalary: 104000 },
    };
}

function StatCard({ label, value, note }) {
    return (
        <div className="stat-card">
            <div className="stat-label">{label}</div>
            <div className="stat-value">{value}</div>
            <div className="stat-note">{note}</div>
        </div>
    );
}

export default function MarketTrends() {
    const [data, setData] = useState(null);
    useEffect(() => { fetchTrends().then(setData); }, []);

    if (!data) return <div className="card">Loading market trends…</div>;

    return (
        <div className="trends">
            <div className="stats-grid">
                <StatCard label="YoY Postings" value={`+${data.snapshot.yoy}%`} note="vs last year" />
                <StatCard label="Remote Share" value={`${data.snapshot.remoteShare}%`} note="of all roles" />
                <StatCard label="Median Salary" value={`$${(data.snapshot.medianSalary/1000).toFixed(0)}k`} note="CAD" />
            </div>

            <div className="chart-card">
                <h3>Weekly Job Postings</h3>
                <div className="chart-wrap">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={data.weekly} margin={{ top: 10, right: 10, bottom: 0, left: -20 }}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="week" />
                            <YAxis />
                            <Tooltip />
                            <Line type="monotone" dataKey="postings" strokeWidth={3} dot={false} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

            <div className="chart-card">
                <h3>Fastest Growing Roles (last 30–60 days)</h3>
                <div className="chart-wrap tall">
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data.topRoles} margin={{ top: 10, right: 10, bottom: 0, left: -20 }}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="role" />
                            <YAxis />
                            <Tooltip />
                            <Bar dataKey="growth" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
