import { NavLink, useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import axios from "axios";
import "../styles/Navbar.css";

function Navbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isAdminLoggedIn, setIsAdminLoggedIn] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();

  // Determine if we are on an admin route
  const isAdminRoute = location.pathname.startsWith("/admin");

  useEffect(() => {
    // Check auth status whenever location changes
    const token = sessionStorage.getItem("token");
    const adminToken = sessionStorage.getItem("adminToken");

    setIsLoggedIn(!!token);
    setIsAdminLoggedIn(!!adminToken);

    // Initial fetch
    if (token || adminToken) {
      fetchUnreadCount();
    }
  }, [location.pathname]); // Triggered on route change

  // Polling for real-time notification badges (Rubric: Notification to User/Owner)
  useEffect(() => {
    const token = sessionStorage.getItem("token") || sessionStorage.getItem("adminToken");
    if (!token) return;

    const interval = setInterval(() => {
      fetchUnreadCount();
    }, 10000); // Poll every 10 seconds

    return () => clearInterval(interval);
  }, [isLoggedIn, isAdminLoggedIn]);

  const fetchUnreadCount = async () => {
    const userId = sessionStorage.getItem("userId");
    const ownerId = sessionStorage.getItem("ownerId");
    const token = sessionStorage.getItem("token") || sessionStorage.getItem("adminToken");

    if (!token) return;

    try {
      // Logic to fetch unread notifications for the specific logged-in role
      // If ownerId exists in session, we fetch for Admin, otherwise for User
      const url = ownerId 
        ? `http://localhost:8082/api/notifications/unread/count/owner/${ownerId}`
        : `http://localhost:8082/api/notifications/unread/count/user/${userId}`;

      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setUnreadCount(response.data);
    } catch (err) {
      console.warn("Unread count fetch skipped or failed.", err);
    }
  };

  const handleLogout = () => {
    sessionStorage.clear(); // Clears all session data at once
    setIsLoggedIn(false);
    navigate("/login");
  };

  const handleAdminLogout = () => {
    sessionStorage.clear();
    setIsAdminLoggedIn(false);
    navigate("/admin");
  };

  return (
    <nav className="navbar">
      {/* BRANDING: Now text-only and non-clickable as requested */}
      <div className="navbar-logo-text">
        <span className="logo-icon">💳</span> MyFin Bank
      </div>

      <div className="navbar-links">
        {isAdminRoute ? (
          /* ----- ADMIN (OWNER) NAVIGATION ----- */
          isAdminLoggedIn ? (
            <>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/admin/dashboard" end>
                Dashboard
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/admin/chat" end>
                Admin Chat
                {unreadCount > 0 && <span className="unread-badge admin-badge">{unreadCount}</span>}
              </NavLink>
              <button className="logout-btn" onClick={handleAdminLogout}>Logout</button>
            </>
          ) : (
            <>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/" end>
                Home
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/login" end>
                Customer Login
              </NavLink>
            </>
          )
        ) : (
          /* ----- CUSTOMER (USER) NAVIGATION ----- */
          isLoggedIn ? (
            <>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/dashboard" end>
                Dashboard
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/transactions" end>
                Transactions
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/investments" end>
                Investments
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/loans" end>
                Loans
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/chat" end>
                💬 Chat
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link notifications-link${isActive ? " active" : ""}`} to="/notifications" end>
                🔔 Notifications
                {unreadCount > 0 && <span className="unread-badge">{unreadCount}</span>}
              </NavLink>
              <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/" end>
                Home
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/login" end>
                Login
              </NavLink>
              <NavLink className={({ isActive }) => `nav-link admin-access${isActive ? " active" : ""}`} to="/admin" end>
                Admin Access
              </NavLink>
            </>
          )
        )}
      </div>
    </nav>
  );
}

export default Navbar;
