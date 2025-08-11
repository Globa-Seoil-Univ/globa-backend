import base64
import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend


class AESUtil:
    def __init__(self, secret_key: str):
        self.secret_key = secret_key
        self.algorithm = algorithms.AES
        self.key_length = 16
        self.iv_length = 16

    def _prepare_key(self) -> bytes:
        key_bytes = self.secret_key.encode('utf-8')
        if len(key_bytes) < self.key_length:
            # 부족한 부분을 0으로 패딩
            key_bytes += b'\x00' * (self.key_length - len(key_bytes))
        else:
            key_bytes = key_bytes[:self.key_length]
        return key_bytes

    def encrypt(self, value: int) -> str:
        if value is None:
            raise ValueError("Invalid AES encryption value")

        try:
            key_bytes = self._prepare_key()
            iv = os.urandom(self.iv_length)
            plaintext = str(value).encode('utf-8')

            padder = padding.PKCS7(128).padder()
            padded_data = padder.update(plaintext)
            padded_data += padder.finalize()

            cipher = Cipher(
                algorithms.AES(key_bytes),
                modes.CBC(iv),
                backend=default_backend()
            )
            encryptor = cipher.encryptor()
            encrypted = encryptor.update(padded_data) + encryptor.finalize()

            encrypted_with_iv = iv + encrypted

            return base64.b64encode(encrypted_with_iv).decode('utf-8')

        except Exception as e:
            raise RuntimeError(f"Failed AES encryption: {str(e)}")

    def decrypt(self, encrypted_value: str) -> int:
        """Base64 암호화 문자열을 복호화하여 Long 값으로 반환"""
        if not encrypted_value or not encrypted_value.strip():
            raise ValueError("Invalid AES decryption value")

        try:
            key_bytes = self._prepare_key()

            encrypted_with_iv = base64.b64decode(encrypted_value)

            iv = encrypted_with_iv[:self.iv_length]
            encrypted = encrypted_with_iv[self.iv_length:]

            cipher = Cipher(
                algorithms.AES(key_bytes),
                modes.CBC(iv),
                backend=default_backend()
            )
            decryptor = cipher.decryptor()
            padded_plaintext = decryptor.update(encrypted) + decryptor.finalize()

            unpadder = padding.PKCS7(128).unpadder()
            plaintext = unpadder.update(padded_plaintext)
            plaintext += unpadder.finalize()

            user_id_str = plaintext.decode('utf-8')
            return int(user_id_str)

        except Exception as e:
            raise RuntimeError(f"Failed AES decryption: {str(e)}")


