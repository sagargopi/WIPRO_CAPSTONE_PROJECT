import { useState, useEffect } from "react";
import axios from "axios";
import "../styles/AdminChat.css";

/**
 * Admin Chat Component
 * Allows admins to view customer messages and send replies
 */
function AdminChat() {
  const [conversations, setConversations] = useState([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [unreadCount, setUnreadCount] = useState(0);

  const ownerId = sessionStorage.getItem("ownerId") || 1;

  // Fetch conversations for admin on mount
  useEffect(() => {
    fetchConversations();
  }, []);

  const fetchConversations = async () => {
    setLoading(true);
    try {
      const adminToken = sessionStorage.getItem("adminToken");
      const response = await axios.get(
        `http://localhost:8083/api/admin/messages/conversations/admin/${ownerId}`,
        {
          headers: { Authorization: `Bearer ${adminToken}` }
        }
      );
      const data = response.data || [];
      setConversations(data);
      
      // Calculate total unread count for the header badge
      const totalUnread = data.reduce((acc, conv) => acc + (conv.unreadCount || 0), 0);
      setUnreadCount(totalUnread);
      setError("");
    } catch (err) {
      console.error(err);
      setError("Failed to load conversations");
      setConversations([]);
    }
    setLoading(false);
  };

  // Select conversation and fetch messages
  const handleSelectCustomer = async (userId) => {
    setSelectedCustomerId(userId);
    setLoading(true);
    try {
      const adminToken = sessionStorage.getItem("adminToken");
      const response = await axios.get(
        "http://localhost:8083/api/admin/messages/conversation",
        {
          params: { userId : userId, ownerId },
          headers: { Authorization: `Bearer ${adminToken}` }
        }
      );
      setMessages(response.data || []);
      setError("");
    } catch (err) {
      console.error(err);
      setError("Failed to load messages");
      setMessages([]);
    }
    setLoading(false);
  };

  // Send reply
  const handleSendReply = async () => {
    if (!newMessage.trim() || !selectedCustomerId) return;
    try {
      const adminToken = sessionStorage.getItem("adminToken");
      
      // Posting to 8082 (User Service) ensures the customer sees it immediately
      await axios.post("http://localhost:8082/api/messages/send-admin", null, {
        params: {
          userId: selectedCustomerId,
          ownerId : ownerId,
          messageContent: newMessage,
          subject: "Admin Reply"
        },
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      
      setSuccess("Reply sent successfully");
      setNewMessage("");
      // Refresh messages for the current view
      await handleSelectCustomer(selectedCustomerId);
      // Refresh sidebar to update last message
      fetchConversations();
      
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      console.error(err);
      setError("Failed to send reply");
    }
  };

  // ... [Existing imports and state logic remain the same] ...

    const renderMessage = (msg, index) => {
      const messageText = msg.messageText || msg.messageContent || msg.message;
      const timestamp = msg.createdAt || msg.timestamp || msg.time;
      const isSentByAdmin = msg.messageType === "ADMIN_TO_CUSTOMER";

      return (
        <div
          key={index}
          className={`msg-group ${isSentByAdmin ? "sent" : "received"}`}
        >
          <div className="msg-bubble">
            {!isSentByAdmin && msg.subject && (
              <span className="msg-subject-tag">{msg.subject}</span>
            )}
            <div className="msg-text">{messageText}</div>
          </div>
          <div className="msg-meta">
            <span className="msg-time">
              {timestamp ? new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ""}
            </span>
          </div>
        </div>
      );
    };

    return (
      <div className="admin-chat-page-wrapper">
        {error && <div className="error-banner">{error}</div>}
        {success && <div className="success-banner">{success}</div>}
        <div className="admin-chat-container">
          
          {/* SIDEBAR: CONVERSATIONS */}
          <aside className="chat-sidebar">
            <div className="sidebar-header">
              <h3>Conversations ({conversations.length})</h3>
              {unreadCount > 0 && <span className="total-unread-badge">{unreadCount}</span>}
            </div>
            <div className="conversation-items">
              {conversations.map((conv) => (
                <div
                  key={conv.customerId}
                  className={`conv-item ${selectedCustomerId === conv.customerId ? "active" : ""}`}
                  onClick={() => handleSelectCustomer(conv.customerId)}
                >
                  <div className="conv-avatar">👤</div>
                  <div className="conv-details">
                    <div className="conv-top">
                      <span className="conv-name">{conv.customerName || `User #${conv.customerId}`}</span>
                      {conv.unreadCount > 0 && <span className="unread-dot-indicator"></span>}
                    </div>
                    <p className="conv-preview">{conv.lastMessage}</p>
                  </div>
                </div>
              ))}
            </div>
          </aside>

          {/* MAIN CHAT AREA */}
          <main className="chat-window">
            {selectedCustomerId ? (
              <>
                <div className="chat-window-header">
                  <div className="user-info">
                    <span className="user-name">
                      {conversations.find(c => c.customerId === selectedCustomerId)?.customerName || 'Customer'}
                    </span>
                    <span className="online-status">● Support Active</span>
                  </div>
                </div>

                <div className="chat-messages">
                  {loading && <p className="empty-notice">Loading...</p>}
                  {messages.length === 0 ? (
                    <p className="empty-notice">No messages yet. Send a reply to start helping.</p>
                  ) : (
                    messages.map((msg, index) => renderMessage(msg, index))
                  )}
                </div>

                <div className="chat-input-area">
                  <textarea
                    className="admin-message-input"
                    placeholder="Type your reply here..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" && !e.shiftKey) {
                        e.preventDefault();
                        handleSendReply();
                      }
                    }}
                  />
                  <button className="btn-send-admin" onClick={handleSendReply}>
                    Send
                  </button>
                </div>
              </>
            ) : (
              <div className="no-selection-state">
                <div className="no-selection-icon">💬</div>
                <p>Select a customer to view their inquiry</p>
              </div>
            )}
          </main>
        </div>
      </div>
    );
  }

export default AdminChat;
