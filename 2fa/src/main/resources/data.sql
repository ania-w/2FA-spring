
INSERT INTO users (username, password, totp_secret, two_factor_enabled)
VALUES ('user', '$2a$10$0eR/4DEBxJJEw5YLwbYEvOtaKk6ziVWJICIYvW5ewa5h4uz4gTjg6','JSCKPSQY7KHKTND7',true);

INSERT INTO users (username, email, password, totp_secret, device_id,rsa_private_key)
VALUES ('aa','wojciak.anna99@gmail.com','$2a$10$Ce6gZTLy8EX7eT501/Fy3.s1UZFQiUCLLtaLbTza0yWgl6ZTFgnJ.',
'BA2DXXHNQCY22XNL', '24dc5340-912d-4b1b-8070-806fa997a36d',
'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEriqEtMS/y3xGLfN3nnVflUx4Ug+V6SdPXLKxAIHEcS4dRQn+VXgavkp5SHcRHTnLcdBILpVY1ptpijNG+zsjHmafnhM47NSVKFK+7b2AUsZwQXYoZuMa8Xk+hN7iijEksj9k5xwkTcWHxtlpTfuif1OFeX+7mAsNKTmoVNMJpwIDAQAB')
