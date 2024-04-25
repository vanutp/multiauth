package dev.vanutp.multiauth.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import dev.vanutp.multiauth.MultiSignature;
import org.spongepowered.asm.mixin.*;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@Mixin(YggdrasilServicesKeyInfo.class)
public class ServicesKeyInfoMixin {
    @Shadow
    @Final
    private PublicKey publicKey;

    @Unique
    private final byte[] elybyKeyBytes = Base64.getDecoder().decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxgFJRb0e9fRyVG5+JlCgh0hccRIcgO5yxEVkMJajAI12Ev/Pc7lpTt6OtKTEcUNfjYgBnEhIKbdLD0Z+B5BxSg9DQmozgzZcesScpASQb4Kt6P8itowdbgbUm4v+6x1QUKJjjmhHq93m9OIEbxQLCq+SrEMZpDrXRgd9DhNPjZv/95ximP8otvh7+bmEl8jwINgfJx0PAeJFYlceQcshiYh+LHtaIwzbTTqkDibDm7QiEc+/qGab3mABtVTpqw/refwFoR0M8+xkWF+1/D8kH0WFa+rBhdjLyLG+2hdOpKXoH/fMH0tQMPHU78J17JVKWwIWCwEWXp8HiWSbIt3acmBYtyW3tqarFFMMECx2wmJP6FVOvYVThZxq9qc9/f3yeTGz3g7zU1YljHSVRP16iEbEnHQBKxmrj2cdZgosJej4YppV7f3iZ8o8PF6UY51LSqvaCteXuWeYSJJESGAsUoV7ihJfWL8DymHamywB2Cahx7EiDGS3/iBcQUmpk4TTg2FrZPuKGItn1QfIRieOknnj9CPKiWdfOtJBr3i1FXLEfExgcJhQ00Y6B08QVvgiCzUF3t+VAG3Ef2YINYyGAXcW0TIgMalwwgGzdhQRhItODXptWigy0DNTUAgKQT9PS8N09yPBGxIq64T9A3/zFqC/k2bMLWUSVtIlilIItn0CAwEAAQ==");

    /**
     * @author vanutp
     * @reason because.
     */
    @Overwrite(remap = false)
    public Signature signature() throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keys = new ArrayList<PublicKey>();
        keys.add(this.publicKey);
        var spec = new X509EncodedKeySpec(elybyKeyBytes);
        var keyFactory = KeyFactory.getInstance("RSA");
        var elybyKey = keyFactory.generatePublic(spec);
        keys.add(elybyKey);
        return new MultiSignature("SHA1withRSA", keys);
    }
}
