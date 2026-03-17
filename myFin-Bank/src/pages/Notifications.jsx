import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userAPI } from "../services/api";
import "../styles/Notifications.css";

/**
 * Notifications Component
 * Displays all notifications for the customer
 * Features: View notifications, filter by type, mark as read
 */
function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [filteredNotifications, setFilteredNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filterType, setFilterType] = useState("ALL");
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();

  const userId = sessionStorage.getItem("userId");
  const token = sessionStorage.getItem("token");

  async function fetchNotifications() {
    try {
      const response = await userAPI.get(`/api/notifications/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setNotifications(response.data || []);
      setLoading(false);
      setError("");
    } catch (err) {
      setError("Failed to load notifications");
      setLoading(false);
      console.error(err);
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await userAPI.get(
        `/api/notifications/unread/count/user/${userId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setUnreadCount(response.data || 0);
    } catch (err) {
      console.error("Failed to fetch unread count:", err);
    }
  }

  function filterNotifications() {
    if (filterType === "ALL") {
      setFilteredNotifications(notifications);
    } else if (filterType === "UNREAD") {
      setFilteredNotifications(
        notifications.filter((n) => !n.isRead)
      );
    } else {
      setFilteredNotifications(
        notifications.filter((n) => n.notificationType === filterType)
      );
    }
  }

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }
    fetchNotifications();
    fetchUnreadCount();

    // Poll for new notifications every 10 seconds
    const interval = setInterval(() => {
      fetchNotifications();
      fetchUnreadCount();
    }, 10000);

    return () => clearInterval(interval);
  }, [navigate, token]);

  useEffect(() => {
    filterNotifications();
  }, [notifications, filterType]);

  const handleMarkAsRead = async (notificationId) => {
    try {
      await userAPI.put(`/api/notifications/${notificationId}/read`, null, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchNotifications();
      fetchUnreadCount();
    } catch (err) {
      console.error("Failed to mark notification as read:", err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await userAPI.put(`/api/notifications/mark-all-read/user/${userId}`, null, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchNotifications();
      fetchUnreadCount();
    } catch (err) {
      console.error("Failed to mark all as read:", err);
    }
  };

  const handleDeleteNotification = async (notificationId) => {
    try {
      await userAPI.delete(`/api/notifications/${notificationId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchNotifications();
      fetchUnreadCount();
    } catch (err) {
      console.error("Failed to delete notification:", err);
    }
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case "BALANCE_ALERT":
        return "⚠️";
      case "LOAN_STATUS":
        return "📋";
      case "TRANSACTION":
        return "💳";
      case "SYSTEM":
        return "💬";
      default:
        return "🔔";
    }
  };

  if (loading) {
    return <div className="loading">Loading notifications...</div>;
  }

  return (
    <div className="notifications-container">
      <div className="notifications-header">
        <h1>🔔 Notifications</h1>
        {unreadCount > 0 && (
          <div className="unread-badge">
            <span>{unreadCount} Unread</span>
          </div>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="notifications-controls">
        <div className="filter-buttons">
          <button
            className={`filter-btn ${filterType === "ALL" ? "active" : ""}`}
            onClick={() => setFilterType("ALL")}
          >
            All
          </button>
          <button
            className={`filter-btn ${filterType === "UNREAD" ? "active" : ""}`}
            onClick={() => setFilterType("UNREAD")}
          >
            Unread ({unreadCount})
          </button>
          <button
            className={`filter-btn ${filterType === "BALANCE_ALERT" ? "active" : ""}`}
            onClick={() => setFilterType("BALANCE_ALERT")}
          >
            ⚠️ Balance Alerts
          </button>
          <button
            className={`filter-btn ${filterType === "LOAN_STATUS" ? "active" : ""}`}
            onClick={() => setFilterType("LOAN_STATUS")}
          >
            📋 Loans
          </button>
          <button
            className={`filter-btn ${filterType === "TRANSACTION" ? "active" : ""}`}
            onClick={() => setFilterType("TRANSACTION")}
          >
            💳 Transactions
          </button>
        </div>
        {unreadCount > 0 && (
          <button className="btn btn-secondary" onClick={handleMarkAllAsRead}>
            Mark All as Read
          </button>
        )}
      </div>

      <div className="notifications-list">
        {filteredNotifications.length > 0 ? (
          filteredNotifications.map((notification) => (
            <div
              key={notification.id}
              className={`notification-item ${
                notification.isRead ? "read" : "unread"
              }`}
              onClick={() =>
                !notification.isRead && handleMarkAsRead(notification.id)
              }
            >
              <div className="notification-icon">
                {getNotificationIcon(notification.notificationType)}
              </div>
              <div className="notification-content">
                <div className="notification-type">
                  {notification.notificationType.replace(/_/g, " ")}
                </div>
                <div className="notification-message">
                  {notification.notificationContent}
                </div>
                <div className="notification-timestamp">
                  {new Date(notification.createdAt).toLocaleDateString("en-US", {
                    month: "short",
                    day: "numeric",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </div>
              </div>
              <div className="notification-actions">
                {!notification.isRead && (
                  <button
                    className="action-btn read-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleMarkAsRead(notification.id);
                    }}
                    title="Mark as read"
                  >
                    ✓
                  </button>
                )}
                <button
                  className="action-btn delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteNotification(notification.id);
                  }}
                  title="Delete"
                >
                  ✕
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="no-notifications">
            <p>No notifications to display</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Notifications;
