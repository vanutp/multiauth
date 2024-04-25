package dev.vanutp.multiauth;

import java.security.*;
import java.util.ArrayList;

public class MultiSignature extends Signature {
    private ArrayList<Signature> signatures;


    protected MultiSignature(String algorithm) {
        super(algorithm);
    }

    public MultiSignature(String algorithm, ArrayList<PublicKey> publicKeys) {
        super(algorithm);
        try {
            signatures = new ArrayList<>();
            for (var key : publicKeys) {
                var sign = Signature.getInstance(algorithm);
                sign.initVerify(key);
                signatures.add(sign);
            }
            state = VERIFY;
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AssertionError("Failed to create signature", e);
        }
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        for (var signature : signatures) {
            signature.update(b);
        }
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        for (var signature : signatures) {
            try {
                if (signature.verify(sigBytes)) {
                    return true;
                }
            } catch (final SignatureException ignored) {}
        }
        return false;
    }

    @Override
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new UnsupportedOperationException();
    }
}
