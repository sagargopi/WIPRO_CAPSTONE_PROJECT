import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { userAPI } from "../services/api";
import "../styles/Auth.css";

function Register() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");

  // Yup validation schema
  const validationSchema = Yup.object().shape({
    username: Yup.string()
      .required("Username is required")
      .min(3, "Username must be at least 3 characters"),
    email: Yup.string()
      .email("Invalid email format")
      .required("Email is required"),
    password: Yup.string()
      .min(6, "Password must be at least 6 characters")
      .required("Password is required"),
    confirmPassword: Yup.string()
      .oneOf([Yup.ref("password"), null], "Passwords must match")
      .required("Please confirm your password")
  });

  const initialValues = {
    username: "",
    email: "",
    password: "",
    confirmPassword: ""
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    setServerError("");
    console.log("📝 Registration attempt with email:", values.email);

    try {
      const response = await userAPI.post("/auth/register", {
        username: values.username,
        email: values.email,
        password: values.password,
        role: "CUSTOMER"
      });

      if (response.status === 201) {
        console.log("✅ Registration successful!");
        alert("User Registered Successfully");
        navigate("/login");
      }
    } catch (err) {
      setServerError(err.response?.data?.message || "Registration failed. Please try again.");
      console.error("Registration error:", err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Customer Registration</h2>
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
                  placeholder="Choose a username"
                  className={touched.username && errors.username ? "input-error" : ""}
                />
                <ErrorMessage name="username">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <div className="form-group">
                <label>Email</label>
                <Field
                  type="email"
                  name="email"
                  placeholder="Enter your email"
                  className={touched.email && errors.email ? "input-error" : ""}
                />
                <ErrorMessage name="email">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <div className="form-group">
                <label>Password</label>
                <Field
                  type="password"
                  name="password"
                  placeholder="Enter your password (min 6 characters)"
                  className={touched.password && errors.password ? "input-error" : ""}
                />
                <ErrorMessage name="password">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <div className="form-group">
                <label>Confirm Password</label>
                <Field
                  type="password"
                  name="confirmPassword"
                  placeholder="Re-enter your password"
                  className={touched.confirmPassword && errors.confirmPassword ? "input-error" : ""}
                />
                <ErrorMessage name="confirmPassword">
                  {msg => <div className="field-error">{msg}</div>}
                </ErrorMessage>
              </div>

              <button type="submit" disabled={isSubmitting} className="btn-submit">
                {isSubmitting ? "Registering..." : "Register"}
              </button>
            </Form>
          )}
        </Formik>

        <p className="auth-footer">
          Already have an account? <a href="/login">Login here</a>
        </p>
      </div>
    </div>
  )
}

export default Register;
