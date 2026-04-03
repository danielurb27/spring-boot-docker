-- Verificar el hash exacto byte a byte
SELECT username, 
       length(password_hash) as len,
       password_hash
FROM users 
WHERE username = 'admin';
