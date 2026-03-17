import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { adminAPI } from "../services/api";
import "../styles/Auth.css";

function AdminLogin() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  // Yup validation schema
  const validationSchema = Yup.object().shape({
    username: Yup.string()
      .required("Username is required"),
    password: Yup.string()
      .required("Password is required")
  });

  const initialValues = {
    username: "",
    password: ""
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    setServerError("");

    try {
      const response = await adminAPI.post("/owner/login", {
        username: values.username,
        password: values.password
      });

      if (response.data && response.data.token) {
        sessionStorage.setItem("adminToken", response.data.token);
        sessionStorage.setItem("ownerId", response.data.ownerId);
        sessionStorage.setItem("admin", JSON.stringify(response.data));
        alert("Admin Login Successful");
        navigate("/admin/dashboard");
      }
    } catch (err) {
      setServerError(err.response?.data?.message || "Admin login failed. Please try again.");
      console.error("Admin login error:", err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Admin Login</h2>
        {serverError && <div className="error-message">{serverError}</div>}
        
        <Formik
          initialValues={initialValues}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
        >
          {({ isSubmitting, touched, errors }) => (
            <Form>
              <div className="form-group">
                <label>Username</label>
                <Field
                  type="text"
                  name="username"
                  placeholder="Enter your admin username"
                  className={touched.username && errors.username ? "input-error" : ""}
                />
                <ErrorMessage name="username">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <div className="form-group">
                <label>Password</label>
                <div style={{ position: "relative" }}>
                  <Field
                    type={showPassword ? "text" : "password"}
                    name="password"
                    placeholder="Enter your password"
                    className={touched.password && errors.password ? "input-error" : ""}
                    style={{ paddingRight: "44px" }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((v) => !v)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                    style={{
                      position: "absolute",
                      right: "10px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      background: "transparent",
                      border: "none",
                      cursor: "pointer",
                      padding: 0,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: "#555"
                    }}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path
                        d="M2 12C4.8 6.5 8.7 4 12 4s7.2 2.5 10 8c-2.8 5.5-6.7 8-10 8s-7.2-2.5-10-8Z"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinejoin="round"
                      />
                      <path
                        d="M12 15.5A3.5 3.5 0 1 0 12 8.5a3.5 3.5 0 0 0 0 7Z"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinejoin="round"
                      />
                      {showPassword && (
                        <path
                          d="M4 4l16 16"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                        />
                      )}
                    </svg>
                  </button>
                </div>
                <ErrorMessage name="password">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <button type="submit" disabled={isSubmitting} className="btn-submit">
                {isSubmitting ? "Logging in..." : "Login as Admin"}
              </button>
            </Form>
          )}
        </Formik>

        <p className="auth-footer">
          New admin? <a href="/admin/register">Register here</a>
        </p>
      </div>
    </div>
  )
}

export default AdminLogin;
