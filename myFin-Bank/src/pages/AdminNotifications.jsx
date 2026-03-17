import { useState, useEffect } from "react";
import axios from "axios";
import "../styles/Notifications.css"; // Reuse your existing styles

function AdminNotifications() {
  const [notifications, setNotifications] = useState([]);

  async function fetchAdminNotifications() {
    const adminToken = sessionStorage.getItem("adminToken");
    const userToken = sessionStorage.getItem("token");
    const activeToken = adminToken || userToken;
    const ownerId = sessionStorage.getItem("ownerId") || 1;

    if (!activeToken) {
      return;
    }

    try {
      const res = await axios.get(`http://localhost:8082/api/notifications/owner/${ownerId}`, {
        headers: { 
          Authorization: `Bearer ${activeToken}`,
          Accept: "application/json",
          "Content-Type": "application/json"
        },
        withCredentials: true
      });
      
      setNotifications(res.data);
    } catch (err) {
      if (err.response?.status === 401) {
        console.error("Unauthorized: Owner Service rejected the token.");
      } else {
        console.error("Error fetching admin notifications:", err);
      }
    }
  }

  useEffect(() => {
    fetchAdminNotifications();
  }, []);

  return (
    <div className="notifications-container">
      <h1>🚩 Admin Alerts & Notifications</h1>
      <div className="notifications-list">
        {notifications.length > 0 ? (
          notifications.map((n) => (
            <div key={n.id} className={`notification-card ${n.notificationType}`}>
              <div className="notif-header">
                <span className="type-badge">{n.notificationType.replace("_", " ")}</span>
                <span className="date">{new Date(n.createdAt).toLocaleString()}</span>
              </div>
              <p className="message">{n.notificationContent}</p>
            </div>
          ))
        ) : (
          <p>No new alerts at this time.</p>
        )}
      </div>
    </div>
  );
}

export default AdminNotifications;
