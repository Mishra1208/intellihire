import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export function Header() {
    const { user, logout } = useAuth();

    return (
        <header className="site-header">
            <div className="site-header__wrap">
                <Link to="/" className="brand">IntelliHire</Link>

                {user ? (
                    <div className="userbar">
                        <span className="hello">Hi, <strong>{user.name}</strong></span>
                        <button onClick={logout} className="btn btn-outline">Logout</button>
                        <Link to="/jobs" className="btn btn-primary">Jobs</Link>
                    </div>
                ) : (
                    <Link to="/" className="btn btn-primary">Sign in</Link>
                )}
            </div>
        </header>
    );
}
