package com.example.affine.chatapp2;

public class Response<V> {
    private String status;
    private V data;
    private RespError error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public V getData() {
        return data;
    }

    public void setData(V data) {
        this.data = data;
    }

    public RespError getError() {
        return error;
    }

    public void setError(RespError error) {
        this.error = error;
    }

    public class RespError{
        private int status;
        private String reason;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

}

