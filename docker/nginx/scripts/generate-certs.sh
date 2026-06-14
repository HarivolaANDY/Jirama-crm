#!/bin/bash
# ══════════════════════════════════════════════════════════
# JIRAMA CRM — Self-Signed SSL Certificate Generator
# ══════════════════════════════════════════════════════════
# Usage: bash docker/nginx/scripts/generate-certs.sh
#
# This generates self-signed certificates for local development.
# For production, use Let's Encrypt or a trusted CA.
# ══════════════════════════════════════════════════════════

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SSL_DIR="$SCRIPT_DIR/ssl"
DAYS_VALID=3650  # 10 years for dev certs

mkdir -p "$SSL_DIR"

echo "=== Generating self-signed SSL certificates ==="

# Generate CA key and cert
openssl genrsa -out "$SSL_DIR/ca-key.pem" 4096 2>/dev/null
openssl req -new -x509 -days "$DAYS_VALID" -key "$SSL_DIR/ca-key.pem" \
    -out "$SSL_DIR/ca.pem" -subj "/C=MG/ST=Analamanga/L=Antananarivo/O=JIRAMA/OU=IT/CN=JIRAMA CA" 2>/dev/null

# Generate server key and CSR
openssl genrsa -out "$SSL_DIR/key.pem" 2048 2>/dev/null
openssl req -new -key "$SSL_DIR/key.pem" \
    -out "$SSL_DIR/csr.pem" \
    -subj "/C=MG/ST=Analamanga/L=Antananarivo/O=JIRAMA/OU=IT/CN=jirama.local" 2>/dev/null

# Create config for SAN cert
cat > "$SSL_DIR/openssl.cnf" << EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
C = MG
ST = Analamanga
L = Antananarivo
O = JIRAMA
OU = IT
CN = jirama.local

[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = jirama.local
DNS.3 = *.jirama.mg
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Sign the certificate
openssl x509 -req -days "$DAYS_VALID" -in "$SSL_DIR/csr.pem" \
    -CA "$SSL_DIR/ca.pem" -CAkey "$SSL_DIR/ca-key.pem" \
    -CAcreateserial -out "$SSL_DIR/cert.pem" \
    -extfile "$SSL_DIR/openssl.cnf" -extensions v3_req 2>/dev/null

# Set permissions
chmod 600 "$SSL_DIR/key.pem"
chmod 644 "$SSL_DIR/cert.pem" "$SSL_DIR/ca.pem"

echo ""
echo "=== Certificates generated in $SSL_DIR ==="
echo "  cert.pem  — Server certificate"
echo "  key.pem   — Server private key"
echo "  ca.pem    — CA certificate (import to browser trust store for dev)"
echo ""
echo "For Nginx: ensure the files are mounted at:"
echo "  /etc/nginx/ssl/cert.pem"
echo "  /etc/nginx/ssl/key.pem"
echo "  /etc/nginx/ssl/ca.pem"
