import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { userAPI } from "../services/api";
import "../styles/Chat.css";

/**
 * Chat Component
 * Allows customers to communicate with bank support (admins)
 */
function Chat() {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [subject, setSubject] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [unreadCount, setUnreadCount] = useState(0);
  
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  const userId = sessionStorage.getItem("userId");
  const token = sessionStorage.getItem("token");
  const ownerId = 1;

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  async function fetchMessages(isBackground = false) {
    try {
      const response = await userAPI.get("/api/messages/conversation", {
        params: { userId: userId, ownerId: ownerId },
        headers: { Authorization: `Bearer ${token}` },
      });

      const sortedMessages = (response.data || []).reverse();
      
      setMessages(sortedMessages);
      if (!isBackground) setLoading(false);
      setError("");
    } catch {
      if (!isBackground) {
        setError("Failed to load messages");
        setLoading(false);
      }
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await userAPI.get(`/api/messages/unread/count/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setUnreadCount(response.data || 0);
    } catch (err) {
      console.error(err);
    }
  }

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    fetchMessages();
    fetchUnreadCount();

    const interval = setInterval(() => {
      fetchMessages(true);
      fetchUnreadCount();
    }, 5000);

    return () => clearInterval(interval);
  }, [navigate, token]);

  const handleSendMessage = async () => {
    if (!newMessage.trim()) {
      setError("Please enter a message");
      return;
    }

    try {
      setError("");
      await userAPI.post("/api/messages/send", null, {
        params: {
          userId: userId, 
          ownerId: ownerId,
          messageContent: newMessage,
          subject: subject || "General Inquiry",
        },
        headers: { Authorization: `Bearer ${token}` },
      });

      setSuccess("Message sent! ✅");
      setNewMessage("");
      setSubject("");
      fetchMessages(true);
      fetchUnreadCount();
      setTimeout(() => setSuccess(""), 3000);
    } catch {
      setError("Failed to send message");
    }
  };

  const handleMarkAsRead = async (messageId) => {
    try {
      await userAPI.put(`/api/messages/${messageId}/read`, null, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchUnreadCount();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="loading">Initializing Support Channel...</div>;

  return (
    <div className="chat-page-wrapper">
      <div className="chat-container">
        <div className="chat-header">
          <div className="header-info">
            <h1>💬 Support Chat</h1>
            <p className="status-indicator">● Online</p>
          </div>
          {unreadCount > 0 && (
            <div className="unread-badge">
              {unreadCount} New Replies
            </div>
          )}
        </div>

        {error && <div className="chat-alert alert-error">{error}</div>}
        {success && <div className="chat-alert alert-success">{success}</div>}

        <div className="chat-messages">
          {messages.length > 0 ? (
            messages.map((msg) => {
              const isCustomer = msg.messageType === "CUSTOMER_TO_ADMIN";
              return (
                <div
                  key={msg.id}
                  className={`msg-group ${isCustomer ? "sent" : "received"}`}
                  onClick={() => !isCustomer && !msg.isRead && handleMarkAsRead(msg.id)}
                >
                  <div className="msg-bubble">
                    {!isCustomer && msg.subject && (
                      <span className="msg-subject-tag">{msg.subject}</span>
                    )}
                    <div className="msg-text">{msg.messageContent}</div>
                  </div>
                  <div className="msg-meta">
                    <span className="msg-time">
                      {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    {!isCustomer && !msg.isRead && <span className="unread-dot">●</span>}
                  </div>
                </div>
              );
            })
          ) : (
            <div className="empty-chat">
              <div className="empty-icon">✉️</div>
              <p>How can we help you today?</p>
              <span>Start a conversation with our support team.</span>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <div className="chat-input-area">
          <div className="input-row">
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="Subject (Inquiry, Loan, Account...)"
              className="subject-input"
            />
          </div>
          <div className="input-row">
            <textarea
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type your message here..."
              className="message-input"
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage();
                }
              }}
            />
            <button className="btn-send-chat" onClick={handleSendMessage}>
              Send
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Chat;
