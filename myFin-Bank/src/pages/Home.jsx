import { Link } from "react-router-dom";
import "../styles/Home.css";

function Home() {
  return (
    <div className="home-container">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <h1>Welcome to MyFin Bank</h1>
          <p className="hero-subtitle">Your trusted financial partner for secure, seamless banking</p>
          
          <div className="hero-buttons">
            <Link to="/login" className="btn btn-primary btn-lg">
              🔐 Customer Login
            </Link>
            <Link to="/admin" className="btn btn-secondary btn-lg">
              👨‍💼 Admin Login
            </Link>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className="about-section">
        <h2>About MyFin Bank</h2>
        <p className="about-intro">
          MyFin Bank is a modern, digital-first financial institution dedicated to providing 
          innovative banking solutions for individuals and businesses.
        </p>

        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">🔒</div>
            <h3>Secure Banking</h3>
            <p>Industry-leading security with end-to-end encryption for all your transactions</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">⚡</div>
            <h3>Fast & Easy</h3>
            <p>Complete your banking tasks in seconds with our intuitive mobile and web platform</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📈</div>
            <h3>Smart Investments</h3>
            <p>Grow your wealth with personalized investment recommendations and tools</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">💰</div>
            <h3>Loans & Credit</h3>
            <p>Quick loan approvals with competitive rates for your financial needs</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📱</div>
            <h3>24/7 Access</h3>
            <p>Manage your account anytime, anywhere through our digital platforms</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">🤝</div>
            <h3>Customer Support</h3>
            <p>Dedicated support team ready to help you with any banking needs</p>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="services-section">
        <h2>Our Services</h2>
        <div className="services-list">
          <div className="service-item">
            <h4>💳 Account Management</h4>
            <p>Open accounts, manage deposits, and track your balance in real-time</p>
          </div>
          <div className="service-item">
            <h4>📊 Transactions</h4>
            <p>Send money, receive payments, and view complete transaction history</p>
          </div>
          <div className="service-item">
            <h4>📈 Investments</h4>
            <p>Invest in mutual funds, stocks, and other financial instruments</p>
          </div>
          <div className="service-item">
            <h4>💰 Loans</h4>
            <p>Apply for personal loans with quick approval and flexible terms</p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <h2>Ready to Start Banking with Us?</h2>
        <p>Join thousands of satisfied customers experiencing the future of banking</p>
        <div className="cta-buttons">
          <Link to="/login" className="btn btn-primary btn-lg">
            Login as Customer
          </Link>
          <Link to="/register" className="btn btn-outline btn-lg">
            Open New Account
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="home-footer">
        <p>&copy; 2024 MyFin Bank. All rights reserved. | Privacy Policy | Terms of Service</p>
      </footer>
    </div>
  );
}

export default Home;
