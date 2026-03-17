import axios from "axios";

export const userAPI = axios.create({
  baseURL: "http://localhost:8082"
});

export const adminAPI = axios.create({
  baseURL: "http://localhost:8083"
});