import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext.jsx";
import { Header } from "./components/Header.jsx";
import Landing from "./pages/Landing.jsx";
import JobsPage from "./pages/JobsPage.jsx";

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Header />
                <Routes>
                    <Route path="/" element={<Landing />} />
                    <Route path="/jobs" element={<JobsPage />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
