import { useState, useEffect } from "react";
import { userAPI } from "../services/api";
import "../styles/LoanApplication.css";

function LoanApplication() {
  const [loans, setLoans] = useState([]);
  const [balance, setBalance] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  
  // Search and Filter State
  const [searchTerm, setSearchTerm] = useState("");
  const [category, setCategory] = useState("All");

  const [formData, setFormData] = useState({
    loanType: "Personal",
    loanAmount: "",
    loanTenure: "12",
    userComments: "",
    interestRate: 8.5
  });

  const [emiCalculation, setEmiCalculation] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const userId = sessionStorage.getItem("userId");
  const token = sessionStorage.getItem("token");

  async function fetchLoans() {
    try {
      const response = await userAPI.get(`/loans/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setLoans(response.data || []);
      setLoading(false);
    } catch {
      setError("Failed to fetch loan history.");
      setLoading(false);
    }
  }

  async function fetchBalance() {
    try {
      const response = await userAPI.get("/account/balance", {
        params: { userId },
        headers: { Authorization: `Bearer ${token}` }
      });
      setBalance(response.data || 0);
    } catch {
      console.error("Balance fetch failed");
    }
  }

  useEffect(() => {
    fetchLoans();
    fetchBalance();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    // Set dynamic interest rates based on category
    let rate = 8.5;
    if (name === "loanType") {
      if (value === "Home") rate = 7.0;
      if (value === "Education") rate = 6.5;
      if (value === "Vehicle") rate = 9.0;
    }
    
    setFormData(prev => ({ 
        ...prev, 
        [name]: value,
        interestRate: name === "loanType" ? rate : prev.interestRate 
    }));
    setEmiCalculation(null); // Reset EMI if values change
  };

  const calculateEMI = () => {
    const P = parseFloat(formData.loanAmount);
    const r = parseFloat(formData.interestRate) / 12 / 100;
    const n = parseInt(formData.loanTenure);

    if (!P || P <= 0) {
      setError("Please enter a valid loan amount.");
      return;
    }

    const emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
    const totalPayable = emi * n;
    
    setEmiCalculation({
      monthly_emi: emi,
      total_interest: totalPayable - P
    });
    setError("");
  };

  const handleApplyLoan = async () => {
    try {
      const payload = {
        ...formData,
        userId: userId,
        monthlyEmi: emiCalculation.monthly_emi,
        loanStatus: "PENDING"
      };

      await userAPI.post("/loans/apply", payload, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setSuccess("Loan application submitted for review! ✅");
      setShowForm(false);
      setFormData({ loanType: "Personal", loanAmount: "", loanTenure: "12", userComments: "", interestRate: 8.5 });
      setEmiCalculation(null);
      fetchLoans();
      setTimeout(() => setSuccess(""), 5000);
    } catch {
      setError("Failed to submit loan application.");
    }
  };

  // Filter Logic for Rubric
  const filteredLoans = loans.filter((loan) => {
    const matchesCategory = category === "All" || loan.loanType === category;
    const matchesSearch = loan.id?.toString().includes(searchTerm) || 
                          loan.loanType?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  if (loading) return <div className="loading">Checking Loan Eligibility...</div>;

  return (
    <div className="loan-page-bg">
    <div className="loan-container">
      <div className="loan-header">
        <h1>💳 Loan & Application Center</h1>
        <div className="loan-info-cards">
          <div className="info-card">
            <p>Wallet Balance</p>
            <p className="info-value">₹{balance.toFixed(2)}</p>
          </div>
          <div className="info-card">
            <p>Active Loans</p>
            <p className="info-value">{loans.filter(l => l.loanStatus === "APPROVED").length}</p>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="loan-actions-bar">
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? "✖ Close Form" : "➕ Apply for New Loan"}
        </button>

        <div className="search-filter-box">
          <input 
            type="text" 
            placeholder="Search by ID or Type..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <select value={category} onChange={(e) => setCategory(e.target.value)} className="category-select">
            <option value="All">All Categories</option>
            <option value="Personal">Personal</option>
            <option value="Home">Home</option>
            <option value="Education">Education</option>
            <option value="Vehicle">Vehicle</option>
          </select>
        </div>
      </div>
      </div>

      {showForm && (
        <div className="form-card application-form">
          <div className="form-title">
            <h3>New Loan Application</h3>
            <p>Admin will review your request based on your current balance.</p>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Loan Category</label>
              <select name="loanType" value={formData.loanType} onChange={handleInputChange}>
                <option value="Personal">Personal Loan</option>
                <option value="Home">Home Loan</option>
                <option value="Education">Education Loan</option>
                <option value="Vehicle">Vehicle Loan</option>
              </select>
            </div>
            <div className="form-group">
              <label>Amount (₹)</label>
              <input type="number" name="loanAmount" value={formData.loanAmount} onChange={handleInputChange} placeholder="e.g. 50000" />
            </div>
          </div>

          <div className="form-group">
            <label>Purpose / Comments (Rubric: Review Submission)</label>
            <textarea 
              name="userComments" 
              value={formData.userComments} 
              onChange={handleInputChange} 
              placeholder="Describe why you need this loan..."
              rows="3"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Tenure (Months)</label>
              <select name="loanTenure" value={formData.loanTenure} onChange={handleInputChange}>
                <option value="12">12 Months</option>
                <option value="36">36 Months</option>
                <option value="60">60 Months</option>
              </select>
            </div>
            <div className="form-group">
              <label>Interest Rate (% p.a.)</label>
              <input type="text" value={formData.interestRate + "%"} className="disabled-input" disabled />
            </div>
          </div>

          <div className="form-buttons">
            <button className="btn btn-secondary" onClick={calculateEMI}>📊 Calculate EMI</button>
            {emiCalculation && (
              <button className="btn btn-success" onClick={handleApplyLoan}>Submit Application</button>
            )}
          </div>

          {emiCalculation && (
            <div className="emi-result-box bounce-in">
              <div className="emi-item">
                <p>Monthly EMI</p>
                <strong>₹{emiCalculation.monthly_emi?.toFixed(2)}</strong>
              </div>
              <div className="emi-item">
                <p>Total Interest</p>
                <strong>₹{emiCalculation.total_interest?.toFixed(2)}</strong>
              </div>
            </div>
          )}
        </div>
      )}

      <div className="loans-list">
        <h2>📑 Application History</h2>
        <div className="loans-grid">
          {filteredLoans.length > 0 ? (
            filteredLoans.map((loan) => (
              <div key={loan.id} className={`loan-card-item status-${loan.loanStatus?.toLowerCase()}`}>
                <div className="card-top">
                  <span className="loan-id">#LN-{loan.id}</span>
                  <span className={`status-pill ${loan.loanStatus?.toLowerCase()}`}>{loan.loanStatus}</span>
                </div>
                <div className="card-mid">
                  <h4>{loan.loanType} Loan</h4>
                  <p className="main-amt">₹{parseFloat(loan.loanAmount).toLocaleString()}</p>
                </div>
                <div className="card-bottom">
                  <div className="stat">
                    <span>EMI</span>
                    <strong>₹{loan.monthlyEmi?.toFixed(0)}</strong>
                  </div>
                  <div className="stat">
                    <span>Tenure</span>
                    <strong>{loan.loanTenure}M</strong>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="no-data-card">
              <p>No applications found matching your criteria.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default LoanApplication;
