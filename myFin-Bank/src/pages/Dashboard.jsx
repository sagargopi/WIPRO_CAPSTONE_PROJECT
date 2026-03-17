import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userAPI } from "../services/api";
import "../styles/Dashboard.css";

function Dashboard() {

  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [balance, setBalance] = useState(0);
  const navigate = useNavigate();

  const userId = sessionStorage.getItem("userId");
  const accountNumber = sessionStorage.getItem("accountNumber");
  const token = sessionStorage.getItem("token");

  async function fetchBalance() {
    try {
      const response = await userAPI.get("/account/balance", {
        params: { userId: userId },
        headers: { Authorization: `Bearer ${token}` },
      });
      setBalance(response.data || 0);
    } catch (err) {
      console.error("Failed to load balance", err);
    }
  }

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    const userData = sessionStorage.getItem("user");
    if (userData) {
      setUser(JSON.parse(userData));
    }
    
    fetchBalance();
    setLoading(false);
  }, [navigate, token]);

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Welcome to MyFin Bank Dashboard</h1>
      </div>

      <div className="dashboard-content">
        <div className="welcome-card">
          <h2>Hello, {user?.username || "Customer"}!</h2>
          <p>Email: {user?.email}</p>
          <p>Account Number: {accountNumber || "N/A"}</p>
          <p>User ID: {user?.userId}</p>
          <div className="balance-section">
            <p className="balance-label">Current Balance</p>
            <p className="balance-amount">₹{balance.toFixed(2)}</p>
          </div>
        </div>

        <div className="features-grid">
          <div className="feature-card">
            <h3>💰 Transactions</h3>
            <p>Send money, withdraw, and view transaction history</p>
            <button 
              className="btn-feature"
              onClick={() => navigate("/transactions")}
            >
              Go to Transactions
            </button>
          </div>

          <div className="feature-card">
            <h3>📈 Investments</h3>
            <p>Explore fixed deposits, recurring deposits, and loan investments</p>
            <button 
              className="btn-feature"
              onClick={() => navigate("/investments")}
            >
              Go to Investments
            </button>
          </div>

          <div className="feature-card">
            <h3>🏦 Loans</h3>
            <p>Apply for loans, calculate EMI, and track your applications</p>
            <button 
              className="btn-feature"
              onClick={() => navigate("/loans")}
            >
              Go to Loans
            </button>
          </div>

          {/* Support Chat Card - Now Styled to Match */}
          <div className="feature-card">
            <h3>💬 Support Chat</h3>
            <p>Connect with bank support for queries</p>
            <button 
              className="btn-feature"
              onClick={() => navigate("/chat")}
            >
              Go to Chat
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard;
