import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userAPI } from "../services/api";
import { generateUserFriendlyTransactionId } from "../utils/transactionIdGenerator";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import "../styles/Transactions.css";

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [balance, setBalance] = useState(0);
  
  // Filtering & Search State
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const [showDepositForm, setShowDepositForm] = useState(false);
  const [showWithdrawForm, setShowWithdrawForm] = useState(false);
  const [showTransferForm, setShowTransferForm] = useState(false);
  
  const [formData, setFormData] = useState({ 
    amount: "", 
    recipientId: "", 
    recipientAccountNumber: "" 
  });
  
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [transferLoading, setTransferLoading] = useState(false);
  const navigate = useNavigate();

  const userId = sessionStorage.getItem("userId");
  const accountNumber = sessionStorage.getItem("accountNumber");
  const token = sessionStorage.getItem("token");

  async function fetchTransactions() {
    try {
      const response = await userAPI.get(`/transactions/history`, {
        params: { accountNumber: accountNumber },
        headers: { Authorization: `Bearer ${token}` },
      });

      setTransactions(response.data || []);
      setError("");
    } catch {
      setError("Failed to load transactions");
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

  useEffect(() => {
    if (!token || !accountNumber) {
      navigate("/login");
      return;
    }
    fetchTransactions();
    fetchBalance();
  }, [navigate, token, accountNumber]);

  // --- FILTER LOGIC ---
  const filteredTransactions = transactions.filter((tx) => {
    const txType = tx.type ? tx.type.toUpperCase().trim() : "";
    const category = selectedCategory.toUpperCase().trim();

    const matchesCategory = selectedCategory === "All" || txType === category;
    const matchesSearch = 
      tx.id?.toString().toLowerCase().includes(searchTerm.toLowerCase()) ||
      txType.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesCategory && matchesSearch;
  });

  const handleDownloadStatement = async () => {
    const user = JSON.parse(sessionStorage.getItem("user"));
    
    try {
      await userAPI.post("/api/notifications/send", {
        recipientId: 1, 
        message: `Activity Alert: Customer ${user?.username} (${accountNumber}) has downloaded their bank statement.`,
        type: "DOWNLOAD_TRACKING"
      }, { headers: { Authorization: `Bearer ${token}` }});
    } catch (err) {
      console.warn("Tracking failed, but continuing download...", err);
    }

    const doc = new jsPDF();
    doc.setFontSize(20);
    doc.setTextColor(102, 126, 234);
    doc.text("MyFin Bank Statement", 14, 20);
    
    doc.setFontSize(11);
    doc.setTextColor(100);
    doc.text(`Customer Name: ${user?.username || "N/A"}`, 14, 42);
    doc.text(`Account Number: ${accountNumber || "N/A"}`, 14, 48);
    doc.text(`Current Balance: INR ${balance.toFixed(2)}`, 14, 54);
    doc.text(`Generated: ${new Date().toLocaleString()}`, 14, 60);

    const tableColumn = ["Date", "Transaction ID", "Type", "Amount", "Status"];
    const tableRows = filteredTransactions.map(tx => [
      tx.date ? new Date(tx.date).toLocaleDateString() : "N/A",
      `TXN-${tx.id}`,
      tx.type,
      `INR ${tx.amount.toFixed(2)}`,
      tx.status || "Success"
    ]);

    autoTable(doc, {
      head: [tableColumn],
      body: tableRows,
      startY: 70,
      headStyles: { fillColor: [102, 126, 234] }
    });

    doc.save(`Statement_${accountNumber}.pdf`);
    setSuccess("Statement downloaded! Admin has been notified. ✅");
    setTimeout(() => setSuccess(""), 4000);
  };

  const handleDeposit = async () => {
    if (!formData.amount || formData.amount <= 0) {
      setError("Please enter a valid amount");
      return;
    }
    try {
      const txnId = generateUserFriendlyTransactionId(userId);
      await userAPI.post("/account/deposit", null, {
        params: { userId, amount: formData.amount, transactionId: txnId },
        headers: { Authorization: `Bearer ${token}` },
      });
      setSuccess("Deposit successful! ✅");
      setFormData({ amount: "", recipientId: "", recipientAccountNumber: "" });
      setShowDepositForm(false);
      fetchTransactions();
      fetchBalance();
      setTimeout(() => setSuccess(""), 5000);
    } catch {
      setError("Deposit failed");
    }
  };

  const handleWithdraw = async () => {
    if (!formData.amount || formData.amount > balance) {
      setError("Invalid amount or insufficient balance");
      return;
    }
    try {
      const txnId = generateUserFriendlyTransactionId(userId);
      await userAPI.post("/account/withdraw", null, {
        params: { userId, amount: formData.amount, transactionId: txnId },
        headers: { Authorization: `Bearer ${token}` },
      });
      setSuccess("Withdrawal successful! ✅");
      setShowWithdrawForm(false);
      fetchBalance();
      fetchTransactions();
    } catch {
      setError("Withdrawal failed");
    }
  };

  const handleTransfer = async () => {
    if (!formData.amount || !formData.recipientId || !formData.recipientAccountNumber) {
        setError("Please fill all transfer details.");
        return;
    }

    setTransferLoading(true);
    try {
        const txnId = generateUserFriendlyTransactionId(userId);
        
        // 1. Recipient Lookup
        const lookup = await userAPI.get("/auth/lookup", {
            params: { username: formData.recipientId.trim() },
            headers: { Authorization: `Bearer ${token}` },
        });

        if (lookup.data?.found === false) {
            const inputAcc = formData.recipientAccountNumber.trim();
            throw new Error(`User not found with username "${formData.recipientId}" and account number "${inputAcc}".`);
        }

        const lookedUpUser = lookup.data?.user ?? lookup.data;

        // 2. SAFE CHECK: Ensure lookup returned a user
        if (!lookedUpUser || !lookedUpUser.accountNumber) {
            const inputAcc = formData.recipientAccountNumber.trim();
            throw new Error(`User not found with username "${formData.recipientId}" and account number "${inputAcc}".`);
        }

        // 3. Compare account numbers safely
        const inputAcc = formData.recipientAccountNumber.trim();
        const dbAcc = lookedUpUser.accountNumber.trim();

        if (inputAcc !== dbAcc) {
            throw new Error(`User not found with username "${formData.recipientId}" and account number "${inputAcc}".`);
        }

        // 4. Perform Transfer
        await userAPI.post("/account/transfer", null, {
            params: { 
                fromUserId: userId, 
                toUserId: lookedUpUser.id, 
                amount: formData.amount, 
                transactionId: txnId 
            },
            headers: { Authorization: `Bearer ${token}` },
        });

        setSuccess(`Successfully sent ₹${formData.amount} to ${formData.recipientId} ✅`);
        setShowTransferForm(false);
        fetchTransactions();
        fetchBalance();
    } catch (err) {
        // Handle the "JWT expired" case specifically
        if (err.response?.status === 401) {
            setError("Your session has expired. Please log out and log in again.");
        } else {
            setError(err.response?.data?.message || err.message || "Transfer failed");
        }
    } finally {
        setTransferLoading(false);
    }
};

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (loading) return <div className="loading">Syncing Bank Records...</div>;

  return (
    <div className="transactions-page-wrapper">
      <div className="transactions-container">
        <header className="transactions-header">
          <h1>💳 Transaction Management</h1>
          <div className="balance-display-box">
            <div className="balance-card">
              <p>Current Account Balance</p>
              <span className="balance-amount">₹{balance.toFixed(2)}</span>
            </div>
          </div>
        </header>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="action-button-grid">
          <button className="btn-action-main" onClick={() => { setShowDepositForm(!showDepositForm); setShowWithdrawForm(false); setShowTransferForm(false); }}>💰 Deposit</button>
          <button className="btn-action-main" onClick={() => { setShowWithdrawForm(!showWithdrawForm); setShowDepositForm(false); setShowTransferForm(false); }}>📤 Withdraw</button>
          <button className="btn-action-main" onClick={() => { setShowTransferForm(!showTransferForm); setShowDepositForm(false); setShowWithdrawForm(false); }}>🔄 Transfer</button>
        </div>

        {/* Dynamic Transaction Forms */}
        {(showDepositForm || showWithdrawForm || showTransferForm) && (
          <div className="transaction-form-card">
            <h3>{showDepositForm ? "Deposit Funds" : showWithdrawForm ? "Withdraw Funds" : "Transfer Funds"}</h3>
            <div className="form-content">
              {showTransferForm && (
                <div className="form-row">
                  <div className="form-input-group">
                    <label>Recipient Username</label>
                    <input type="text" name="recipientId" value={formData.recipientId} onChange={handleInputChange} placeholder="Username" />
                  </div>
                  <div className="form-input-group">
                    <label>Account Number</label>
                    <input type="text" name="recipientAccountNumber" value={formData.recipientAccountNumber} onChange={handleInputChange} placeholder="Acc Num" />
                  </div>
                </div>
              )}
              <div className="form-input-group">
                <label>Amount (₹)</label>
                <input type="number" name="amount" value={formData.amount} onChange={handleInputChange} placeholder="0.00" />
              </div>
              <div className="form-button-group">
                <button className="btn-confirm" onClick={showDepositForm ? handleDeposit : showWithdrawForm ? handleWithdraw : handleTransfer} disabled={transferLoading}>
                  {transferLoading ? "Processing..." : "Confirm Transaction"}
                </button>
                <button className="btn-cancel-link" onClick={() => { setShowDepositForm(false); setShowWithdrawForm(false); setShowTransferForm(false); }}>Cancel</button>
              </div>
            </div>
          </div>
        )}

        <section className="history-section">
          <div className="history-header">
            <h2>📋 Transaction History</h2>
            
            {/* --- NEW: FILTER & SEARCH UI --- */}
            <div className="filter-controls">
              <input 
                type="text" 
                placeholder="Search ID or Type..." 
                className="search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              
              <select 
                className="category-select"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <option value="All">All Categories</option>
                <option value="DEPOSIT">Deposits</option>
                <option value="WITHDRAW">Withdrawals</option>
                <option value="TRANSFER_CREDIT">Transfers</option>
              </select>

              <button className="btn-download-pro" onClick={handleDownloadStatement}>
                📄 Download Statement
              </button>
            </div>
          </div>
          
          <div className="table-responsive-box">
            <table className="pro-table">
              <thead>
                <tr>
                  <th>ID</th><th>Type</th><th>Amount</th><th>Status</th><th>Date</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.length > 0 ? filteredTransactions.map((tx) => (
                  <tr key={tx.id}>
                   <td className="tx-id-cell">
                        TXN-{userId}-{tx.id} {/* FIX: Added {userId} here */}
                    </td>
                    <td><span className={`type-tag ${tx.type?.toLowerCase()}`}>{tx.type}</span></td>
                    <td className="amt-cell">₹{tx.amount.toFixed(2)}</td>
                    <td><span className="status-success">{tx.status || "Completed"}</span></td>
                    <td className="date-cell">{new Date(tx.date).toLocaleDateString()}</td>
                  </tr>
                )) : <tr><td colSpan="5" className="empty-row">No records match your criteria.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}

export default Transactions;
