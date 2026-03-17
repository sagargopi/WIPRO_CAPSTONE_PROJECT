import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userAPI } from "../services/api";
import "../styles/Investments.css";

function Investments() {
  const [investments, setInvestments] = useState([]);
  const [balance, setBalance] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [summary, setSummary] = useState(null);
  const [formData, setFormData] = useState({
    investmentType: "FIXED_DEPOSIT",
    amount: "",
    tenureMonths: 12,
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const userId = sessionStorage.getItem("userId");
  const accountId = sessionStorage.getItem("accountId");
  const token = sessionStorage.getItem("token");

  const investmentTypes = [
    { code: "FIXED_DEPOSIT", name: "Fixed Deposit", rate: 6.5 },
    { code: "RECURRING_DEPOSIT", name: "Recurring Deposit", rate: 5.5 },
    { code: "LOAN_INVESTMENT", name: "Loan Investment", rate: 8.0 },
  ];

  async function fetchInvestments() {
    try {
      const response = await userAPI.get(`/investments/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setInvestments(response.data || []);
      setError("");
    } catch {
      setError("Failed to load investments");
    }
  }

  async function fetchBalance() {
    try {
      const response = await userAPI.get("/account/balance", {
        params: { userId: userId },
        headers: { Authorization: `Bearer ${token}` },
      });
      setBalance(response.data || 0);
      setLoading(false);
    } catch {
      setError("Failed to load balance");
      setLoading(false);
    }
  }

  async function fetchSummary() {
    try {
      const response = await userAPI.get(`/investments/user/${userId}/summary`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setSummary(response.data || {});
    } catch (err) {
      console.error(err);
    }
  }

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }
    fetchInvestments();
    fetchBalance();
    fetchSummary();
  }, [navigate, token]);

  const calculateMaturityAmount = () => {
    const amount = parseFloat(formData.amount) || 0;
    const selectedType = investmentTypes.find((t) => t.code === formData.investmentType);
    const rate = selectedType?.rate || 0;
    const years = formData.tenureMonths / 12;
    return amount + (amount * rate * years) / 100;
  };

  const handleCreateInvestment = async () => {
    if (!formData.amount || formData.amount <= 0 || formData.amount > balance) {
      setError(formData.amount > balance ? "Insufficient balance" : "Enter a valid amount");
      return;
    }
    try {
      await userAPI.post("/investments/create", null, {
        params: { userId, accountId, investmentType: formData.investmentType, amount: formData.amount, tenureMonths: formData.tenureMonths },
        headers: { Authorization: `Bearer ${token}` },
      });
      setSuccess("Investment created successfully! 💰");
      setFormData({ investmentType: "FIXED_DEPOSIT", amount: "", tenureMonths: 12 });
      setShowForm(false);
      fetchInvestments();
      fetchBalance();
      fetchSummary();
      setTimeout(() => setSuccess(""), 4000);
    } catch {
      setError("Failed to create investment");
    }
  };

  const handleMaturityInvestment = async (id) => {
    try {
      await userAPI.post(`/investments/${id}/maturity`, null, {
        params: { userId },
        headers: { Authorization: `Bearer ${token}` },
      });
      setSuccess("Investment matured successfully! ✅");
      fetchInvestments();
      fetchBalance();
      fetchSummary();
      setTimeout(() => setSuccess(""), 4000);
    } catch {
      setError("Failed to mature investment");
    }
  };

  if (loading) return <div className="loading">Updating Portfolio...</div>;

  return (
    <div className="investments-page-wrapper">
      <div className="investments-container">
        
        <header className="investments-header">
          <h1>📈 Investment Management</h1>
          <div className="investment-stats">
            <div className="stat-card balance">
              <p>Available Balance</p>
              <span className="stat-value">₹{balance.toFixed(2)}</span>
            </div>
            <div className="stat-card invested">
              <p>Total Invested</p>
              <span className="stat-value">₹{summary?.total_invested?.toFixed(2) || "0.00"}</span>
            </div>
            <div className="stat-card returns">
              <p>Expected Returns</p>
              <span className="stat-value">₹{summary?.total_expected_returns?.toFixed(2) || "0.00"}</span>
            </div>
          </div>
        </header>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="investment-actions">
          <button className="btn-primary-action" onClick={() => setShowForm(!showForm)}>
            {showForm ? "✖ Cancel Request" : "➕ Create New Investment"}
          </button>
        </div>

        {showForm && (
          <div className="investment-form-card">
            <h3>Configure Your Investment</h3>
            <div className="form-grid">
              <div className="form-group">
                <label>Investment Type</label>
                <select name="investmentType" value={formData.investmentType} onChange={(e) => setFormData({...formData, investmentType: e.target.value})}>
                  {investmentTypes.map(t => <option key={t.code} value={t.code}>{t.name} ({t.rate}%)</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Amount (₹)</label>
                <input type="number" name="amount" value={formData.amount} onChange={(e) => setFormData({...formData, amount: e.target.value})} placeholder="e.g. 5000" />
              </div>
              <div className="form-group">
                <label>Tenure (Months)</label>
                <select name="tenureMonths" value={formData.tenureMonths} onChange={(e) => setFormData({...formData, tenureMonths: e.target.value})}>
                    <option value="6">6 Months</option>
                    <option value="12">12 Months (1 Year)</option>
                    <option value="24">24 Months (2 Years)</option>
                    <option value="36">36 Months (3 Years)</option>
                    <option value="60">60 Months (5 Years)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Interest Rate (% p.a.)</label>
                <input type="text" value={investmentTypes.find(t => t.code === formData.investmentType)?.rate + "%"} disabled className="disabled-input" />
              </div>
            </div>

            {formData.amount > 0 && (
              <div className="preview-box">
                <div className="preview-item">
                    <p>Maturity Value</p>
                    <strong>₹{calculateMaturityAmount().toFixed(2)}</strong>
                </div>
                <div className="preview-item">
                    <p>Net Profit</p>
                    <strong>₹{(calculateMaturityAmount() - parseFloat(formData.amount)).toFixed(2)}</strong>
                </div>
              </div>
            )}

            <button className="btn-success-solid" onClick={handleCreateInvestment}>Confirm & Start Investment</button>
          </div>
        )}

        <section className="investments-history">
          <h2>🏦 Your Active Portfolio</h2>
          {investments.length > 0 ? (
            <div className="investments-grid">
              {investments.map((inv) => (
                <div key={inv.id} className={`inv-card status-${inv.investmentStatus.toLowerCase()}`}>
                  <div className="inv-card-header">
                    <span className="inv-type-label">{inv.investmentType.replace('_', ' ')}</span>
                    <span className={`status-pill ${inv.investmentStatus.toLowerCase()}`}>{inv.investmentStatus}</span>
                  </div>
                  <div className="inv-card-body">
                    <p className="inv-amount">₹{inv.amount.toLocaleString()}</p>
                    <div className="inv-meta">
                      <span>Rate: {inv.interestRate}%</span>
                      <span>{inv.tenureMonths} Mo</span>
                    </div>
                  </div>
                  {inv.investmentStatus === "ACTIVE" && (
                    <button className="btn-mature" onClick={() => handleMaturityInvestment(inv.id)}>Mature Now</button>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state-card">
               No active investments found. Ready to grow your savings?
            </div>
          )}
        </section>

        <section className="investment-educational">
          <h2>ℹ️ Market Opportunities</h2>
          <div className="educational-grid">
            {investmentTypes.map(t => (
              <div key={t.code} className="edu-card">
                <h4>{t.name}</h4>
                <p className="rate-text">Yield: {t.rate}% p.a.</p>
                <p className="desc-text">
                  {t.code === "FIXED_DEPOSIT" && "Secure, guaranteed returns with zero risk to principal."}
                  {t.code === "RECURRING_DEPOSIT" && "Build savings systematically with monthly contributions."}
                  {t.code === "LOAN_INVESTMENT" && "High-yield opportunity by participating in bank lending."}
                </p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}

export default Investments;
